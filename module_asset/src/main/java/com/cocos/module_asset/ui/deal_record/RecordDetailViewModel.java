package com.cocos.module_asset.ui.deal_record;

import android.app.Application;
import android.content.ClipData;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

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
public class RecordDetailViewModel extends BaseViewModel {

    public RecordDetailViewModel(@NonNull Application application) {
        super(application);
        String netType = SPUtils.getString(Utils.getContext(), SPKeyGlobal.NET_TYPE, "");
        symbolType.set(TextUtils.equals(netType, "0") ? Utils.getString(R.string.module_asset_coin_type_test) : "");
    }


    public DealDetailModel dealDetailModel;

    //封装一个界面发生改变的观察者
    public UIChangeObservable uc = new UIChangeObservable();


    public class UIChangeObservable {
        public ObservableBoolean checkMemoObservable = new ObservableBoolean(false);
    }

    public ObservableField<String> dealType = new ObservableField<>("");
    public ObservableField<String> dealAmount = new ObservableField<>("");
    public ObservableField<String> senderAccount = new ObservableField<>("");
    public ObservableField<String> receiablesAccount = new ObservableField<>("");
    public ObservableField<String> dealMemo = new ObservableField<>(Utils.getString(R.string.unlock_memo_tips));
    public ObservableField<String> minerFee = new ObservableField<>("");
    public ObservableField<String> dealHash = new ObservableField<>("");
    public ObservableField<String> squareHeight = new ObservableField<>("");
    public ObservableField<String> dealTime = new ObservableField<>("");
    public ObservableInt dealMemoVisibility = new ObservableInt(View.VISIBLE);
    public ObservableField<String> symbolType = new ObservableField<>("");
    //返回按钮的点击事件
    public BindingCommand backOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            finish();
        }
    });

    //收款复制按钮的点击事件
    public BindingCommand onReceiableCopyClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            ClipData mClipData = ClipData.newPlainText("Label", receiablesAccount.get());
            ClipboardManagerInstance.getClipboardManager().setPrimaryClip(mClipData);
            ToastUtils.showShort(R.string.copy_success);
        }
    });

    //转账复制按钮的点击事件
    public BindingCommand onTransferCopyClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            ClipData mClipData = ClipData.newPlainText("Label", senderAccount.get());
            ClipboardManagerInstance.getClipboardManager().setPrimaryClip(mClipData);
            ToastUtils.showShort(R.string.copy_success);
        }
    });

    //转账复制按钮的点击事件
    public BindingCommand checkMemoClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.checkMemoObservable.set(!uc.checkMemoObservable.get());
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
        dealHash.set(dealDetailModel.tx_id);
        dealAmount.set(dealDetailModel.amount);
        senderAccount.set(dealDetailModel.from);
        receiablesAccount.set(dealDetailModel.to);
        minerFee.set(dealDetailModel.fee + dealDetailModel.feeSymbol);
        squareHeight.set(dealDetailModel.block_header);
        dealTime.set(dealDetailModel.time);
        dealMemoVisibility.set(null != dealDetailModel.memo ? View.VISIBLE : View.GONE);
        if (null != dealDetailModel.memo) {
            Double is_encrpt = (Double) dealDetailModel.memo.get(0);
            if (is_encrpt == 0) {
                String memo_str = (String) dealDetailModel.memo.get(1);
                dealMemo.set(memo_str);
            }
        }
    }
}
