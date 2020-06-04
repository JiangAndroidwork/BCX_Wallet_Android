package com.cocos.module_asset.ui.deal_record;

import android.app.Application;
import android.content.ClipData;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.bcx_sdk.bcx_log.LogUtils;
import com.cocos.bcx_sdk.bcx_wallet.chain.global_config_object;
import com.cocos.library_base.base.BaseViewModel;
import com.cocos.library_base.binding.command.BindingAction;
import com.cocos.library_base.binding.command.BindingCommand;
import com.cocos.library_base.entity.AllAssetBalanceModel;
import com.cocos.library_base.entity.AssetsModel;
import com.cocos.library_base.global.IntentKeyGlobal;
import com.cocos.library_base.global.SPKeyGlobal;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.AccountHelperUtils;
import com.cocos.library_base.utils.CurrencyUtils;
import com.cocos.library_base.utils.SPUtils;
import com.cocos.library_base.utils.ToastUtils;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.singleton.ClipboardManagerInstance;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;
import com.cocos.library_base.utils.singleton.MainHandler;
import com.cocos.module_asset.BR;
import com.cocos.module_asset.R;
import com.cocos.module_asset.entity.DealRecordModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ningkang.guo
 * @Date 2019/2/20
 */
public class DealRecordViewModel extends BaseViewModel {

    public DealRecordViewModel(@NonNull Application application) {
        super(application);
        String netType = SPUtils.getString(Utils.getContext(), SPKeyGlobal.NET_TYPE, "");
        symbolType.set(TextUtils.equals(netType, "0") ? Utils.getString(R.string.module_asset_coin_type_test) : "");
    }

    NumberFormat nf = NumberFormat.getInstance();

    private AssetsModel.AssetModel assetModel;

    public ObservableInt emptyViewVisible = new ObservableInt(View.GONE);

    public ObservableInt recyclerViewVisible = new ObservableInt(View.VISIBLE);

    public ObservableField<String> symbolType = new ObservableField<>("");

    //资产名称
    public ObservableField<String> tokenSymbol = new ObservableField<>("COCOS");

    //总资产
    public ObservableField<String> totalAsset = new ObservableField<>();

    public ObservableField<String> totalAssetValue = new ObservableField<>();

    public ObservableField<String> accountName = new ObservableField<>("");

    //返回按钮的点击事件
    public BindingCommand backOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            finish();
        }
    });

    //复制帐户名按钮的点击事件
    public BindingCommand copyOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (TextUtils.isEmpty(accountName.get())) {
                return;
            }
            ClipData mClipData = ClipData.newPlainText("Label", accountName.get());
            ClipboardManagerInstance.getClipboardManager().setPrimaryClip(mClipData);
            ToastUtils.showShort(R.string.copy_success);
        }
    });

    //转账按钮的点击事件
    public BindingCommand transferClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKeyGlobal.ASSET_MODEL, assetModel);
            ARouter.getInstance().build(RouterActivityPath.ACTIVITY_TRANSFER).with(bundle).navigation();
        }
    });

    //收款按钮的点击事件
    public BindingCommand receivablesOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKeyGlobal.ASSET_MODEL, assetModel);
            ARouter.getInstance().build(RouterActivityPath.ACTIVITY_RECEIVABLES).with(bundle).navigation();
        }
    });

    public void setAssetModel(AssetsModel.AssetModel assetModel) {
        this.assetModel = assetModel;
        try {
            tokenSymbol.set(assetModel.symbol);
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(5);
            totalAsset.set(nf.format(assetModel.amount.setScale(5, RoundingMode.HALF_UP).add(BigDecimal.ZERO)));
            totalAssetValue.set(CurrencyUtils.getSingleCurrencyType() + (TextUtils.equals(assetModel.symbol, "COCOS") ? SPUtils.getString(Utils.getContext(), SPKeyGlobal.TOTAL_ASSET_VALUE, "0.00") : "0.00"));
            accountName.set(String.valueOf(AccountHelperUtils.getCurrentAccountName()));
        } catch (Exception e) {
        }
    }

    public ObservableList<DealRecordItemViewModel> observableList = new ObservableArrayList<>();

    public ItemBinding<DealRecordItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.module_asset_deal_record_item);

    public final BindingRecyclerViewAdapter<DealRecordItemViewModel> adapter = new BindingRecyclerViewAdapter<>();

    public void requestDealRecordList() {
        try {
            showDialog();
            CocosBcxApiWrapper.getBcxInstance().get_account_history(accountName.get(), 100, new IBcxCallBack() {
                @Override
                public void onReceiveValue(final String value) {
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("get_account_history", value);
                            DealRecordModel dealRecordModel = global_config_object.getInstance().getGsonBuilder().create().fromJson(value, DealRecordModel.class);
                            observableList.clear();
                            if (!dealRecordModel.isSuccess() || dealRecordModel.data.size() <= 0) {
                                return;
                            }
                            for (DealRecordModel.DealRecordItemModel recordItemModel : dealRecordModel.getData()) {
                                double option = (double) recordItemModel.op.get(0);
                                if (option == 0 || option == 35 || option == 42) {
                                    DealRecordItemViewModel itemViewModel = new DealRecordItemViewModel(DealRecordViewModel.this, recordItemModel);
                                    observableList.add(itemViewModel);
                                }
                            }
                            dismissDialog();
                        }
                    });
                }
            });
        } catch (Exception e) {
        }
    }


    public void requestAssetsListData() {
        try {
            final String accountId = AccountHelperUtils.getCurrentAccountId();
            if (TextUtils.isEmpty(accountId)) {
                return;
            }
            CocosBcxApiWrapper.getBcxInstance().get_all_account_balances(accountId, new IBcxCallBack() {
                @Override
                public void onReceiveValue(final String s) {
                    LogUtils.d("get_account_balances", s);
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            AllAssetBalanceModel balanceEntity = GsonSingleInstance.getGsonInstance().fromJson(s, AllAssetBalanceModel.class);
                            if (!balanceEntity.isSuccess() || balanceEntity.getData().size() <= 0) {
                                return;
                            }
                            final List<AllAssetBalanceModel.DataBean> dataBeans = balanceEntity.getData();
                            for (int i = 0; i < dataBeans.size(); i++) {
                                final AllAssetBalanceModel.DataBean dataBean = dataBeans.get(i);
                                if (TextUtils.equals(dataBean.getAsset_id(), assetModel.id)) {
                                    nf.setGroupingUsed(false);
                                    nf.setMaximumFractionDigits(5);
                                    totalAsset.set(nf.format(dataBean.getAmount().setScale(5, RoundingMode.HALF_UP).add(BigDecimal.ZERO)));
                                    return;
                                }
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
        }
    }
}


