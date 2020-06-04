package com.cocos.module_asset.ui.contract_detail;

import android.app.Application;
import android.content.ClipData;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cocos.library_base.base.BaseViewModel;
import com.cocos.library_base.binding.command.BindingAction;
import com.cocos.library_base.binding.command.BindingCommand;
import com.cocos.library_base.entity.WebViewModel;
import com.cocos.library_base.global.IntentKeyGlobal;
import com.cocos.library_base.global.SPKeyGlobal;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.SPUtils;
import com.cocos.library_base.utils.ToastUtils;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.singleton.ClipboardManagerInstance;
import com.cocos.module_asset.R;
import com.cocos.module_asset.entity.DealDetailModel;

/**
 * @author ningkang.guo
 * @Date 2019/2/20
 */
public class ContractRecordDetailViewModel extends BaseViewModel {

    public ContractRecordDetailViewModel(@NonNull Application application) {
        super(application);
        String netType = SPUtils.getString(Utils.getContext(), SPKeyGlobal.NET_TYPE, "");
        symbolType.set(TextUtils.equals(netType, "0") ? Utils.getString(R.string.module_asset_coin_type_test) : "");
    }

    private DealDetailModel dealDetailModel;
    public ObservableField<String> dealType = new ObservableField<>("");
    public ObservableField<String> symbolType = new ObservableField<>("");
    public ObservableField<String> contractAuthor = new ObservableField<>("");
    public ObservableField<String> contractName = new ObservableField<>("");
    public ObservableField<String> dealHash = new ObservableField<>("");
    public ObservableField<String> contractAction = new ObservableField<>(Utils.getString(R.string.unlock_memo_tips));
    public ObservableField<String> contractData = new ObservableField<>("");
    public ObservableField<String> minerFee = new ObservableField<>("");
    public ObservableField<String> squareHeight = new ObservableField<>("");
    public ObservableField<String> dealTime = new ObservableField<>("");

    //返回按钮的点击事件
    public BindingCommand backOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            finish();
        }
    });

    //收款复制按钮的点击事件
    public BindingCommand onAuthorCopyClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            ClipData mClipData = ClipData.newPlainText("Label", contractAuthor.get());
            ClipboardManagerInstance.getClipboardManager().setPrimaryClip(mClipData);
            ToastUtils.showShort(R.string.copy_success);
        }
    });

    //区块按钮的点击事件
    public BindingCommand onSquareHeightClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            WebViewModel webViewModel = new WebViewModel();
            webViewModel.setUrl(Utils.getString(R.string.module_asset_block_head_address) + dealDetailModel.block_header);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKeyGlobal.WEB_MODEL, webViewModel);
            ARouter.getInstance().build(RouterActivityPath.ACTIVITY_JS_WEB).with(bundle).navigation();
        }
    });

    public void setDealDetailData(DealDetailModel dealDetailModel) {
        this.dealDetailModel = dealDetailModel;
        dealType.set(dealDetailModel.deal_type);
        contractAuthor.set(dealDetailModel.caller);
        contractName.set(dealDetailModel.contract_name);
        contractAction.set(dealDetailModel.function_name);
        contractData.set(dealDetailModel.params);
        minerFee.set(dealDetailModel.fee + dealDetailModel.feeSymbol);
        squareHeight.set(dealDetailModel.block_header);
        dealTime.set(dealDetailModel.time);
        dealHash.set(dealDetailModel.tx_id);
    }
}
