package com.cocos.module_asset.nh_order_manager;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.library_base.BR;
import com.cocos.library_base.base.BaseActivity;
import com.cocos.library_base.base.BaseVerifyPasswordDialog;
import com.cocos.library_base.bus.event.EventBusCarrier;
import com.cocos.library_base.entity.NhAssetOrderEntity;
import com.cocos.library_base.entity.OperateResultModel;
import com.cocos.library_base.global.EventTypeGlobal;
import com.cocos.library_base.global.IntentKeyGlobal;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.AccountHelperUtils;
import com.cocos.library_base.utils.ToastUtils;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;
import com.cocos.module_asset.R;
import com.cocos.module_asset.databinding.ActivityNhOrderDetailBinding;
import com.cocos.module_asset.databinding.DialogBuyOrderConfirmBinding;
import com.cocos.module_asset.databinding.DialogCancelOrderConfirmBinding;

/**
 * @author ningkang.guo
 * @Date 2019/7/16
 */
@Route(path = RouterActivityPath.ACTIVITY_NH_ORDER_DETAIL)
public class NhOrderDetailActivity extends BaseActivity<ActivityNhOrderDetailBinding, NhOrderDetailViewModel> {

    NhAssetOrderEntity.NhOrderBean nhOrderBean;
    private BottomSheetDialog dialog;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_nh_order_detail;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        try {
            nhOrderBean = (NhAssetOrderEntity.NhOrderBean) getIntent().getExtras().getSerializable(IntentKeyGlobal.NH_ORDER_MODEL);
        } catch (Exception e) {
        }
    }

    @Override
    public void initData() {
        viewModel.requestOrderDetailData(nhOrderBean);
    }


    @Override
    public void onHandleEvent(EventBusCarrier busCarrier) {
        if (null != busCarrier) {
            if (TextUtils.equals(EventTypeGlobal.SHOW_CANCEL_ORDER_PASSWORD_VERIFY_DIALOG, busCarrier.getEventType())) {
                dialog.dismiss();
                NhAssetOrderEntity.NhOrderBean nhOrderBean = (NhAssetOrderEntity.NhOrderBean) busCarrier.getObject();
                showCancelOrderPasswordVerifyDialog(nhOrderBean);
            } else if (TextUtils.equals(EventTypeGlobal.SHOW_BUY_ORDER_PASSWORD_VERIFY_DIALOG, busCarrier.getEventType())) {
                dialog.dismiss();
                NhAssetOrderEntity.NhOrderBean nhOrderBean = (NhAssetOrderEntity.NhOrderBean) busCarrier.getObject();
                showBuyOrderPasswordVerifyDialog(nhOrderBean);
            } else if (TextUtils.equals(EventTypeGlobal.DIALOG_DISMISS_TYPE, busCarrier.getEventType())) {
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        }
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.cancelBtnObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                dialog = new BottomSheetDialog(NhOrderDetailActivity.this);
                DialogCancelOrderConfirmBinding binding = DataBindingUtil.inflate(LayoutInflater.from(Utils.getContext()), R.layout.dialog_cancel_order_confirm, null, false);
                dialog.setContentView(binding.getRoot());
                // 设置dialog 完全显示
                View parent = (View) binding.getRoot().getParent();
                BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
                binding.getRoot().measure(0, 0);
                behavior.setPeekHeight(binding.getRoot().getMeasuredHeight());
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                parent.setLayoutParams(params);
                dialog.setCanceledOnTouchOutside(false);
                final CancelOrderConfirmViewModel cancelOrderConfirmViewModel = new CancelOrderConfirmViewModel(getApplication());
                binding.setViewModel(cancelOrderConfirmViewModel);
                cancelOrderConfirmViewModel.setCancelOrderModel(nhOrderBean);
                dialog.show();

            }
        });

        viewModel.uc.buyBtnObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (TextUtils.equals(AccountHelperUtils.getCurrentAccountName(), nhOrderBean.sellerName)) {
                    ToastUtils.showShort(R.string.module_asset_can_not_buy_owner_order);
                    return;
                }
                dialog = new BottomSheetDialog(NhOrderDetailActivity.this);
                DialogBuyOrderConfirmBinding binding = DataBindingUtil.inflate(LayoutInflater.from(Utils.getContext()), R.layout.dialog_buy_order_confirm, null, false);
                dialog.setContentView(binding.getRoot());
                // 设置dialog 完全显示
                View parent = (View) binding.getRoot().getParent();
                BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
                binding.getRoot().measure(0, 0);
                behavior.setPeekHeight(binding.getRoot().getMeasuredHeight());
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                parent.setLayoutParams(params);
                dialog.setCanceledOnTouchOutside(false);
                final BuyOrderConfirmViewModel buyOrderConfirmViewModel = new BuyOrderConfirmViewModel(getApplication());
                binding.setViewModel(buyOrderConfirmViewModel);
                buyOrderConfirmViewModel.setBuyOrderModel(nhOrderBean);
                dialog.show();
            }
        });

    }


    /**
     * 弹出取消订单密码验证弹窗
     *
     * @param nhOrderBean
     */
    private void showCancelOrderPasswordVerifyDialog(
            final NhAssetOrderEntity.NhOrderBean nhOrderBean) {
        final BaseVerifyPasswordDialog passwordVerifyDialog = new BaseVerifyPasswordDialog();
        passwordVerifyDialog.show(getSupportFragmentManager(), "passwordVerifyDialog");
        passwordVerifyDialog.setPasswordListener(new BaseVerifyPasswordDialog.IPasswordListener() {
            @Override
            public void onFinish(String password) {
                CocosBcxApiWrapper.getBcxInstance().cancel_nh_asset_order(nhOrderBean.seller, password, nhOrderBean.id, new IBcxCallBack() {
                    @Override
                    public void onReceiveValue(String s) {
                        final OperateResultModel operateResultModel = GsonSingleInstance.getGsonInstance().fromJson(s, OperateResultModel.class);
                        if (operateResultModel.code == 161) {
                            ToastUtils.showShort(R.string.module_asset_order_not_exist);
                            return;
                        }
                        if (operateResultModel.code == 105) {
                            ToastUtils.showShort(R.string.module_asset_wrong_password);
                            return;
                        }
                        if (!TextUtils.isEmpty(operateResultModel.message)
                                && (operateResultModel.message.contains("insufficient_balance")
                                || operateResultModel.message.contains("Insufficient Balance"))) {
                            ToastUtils.showShort(R.string.insufficient_balance);
                            return;
                        }
                        if (!operateResultModel.isSuccess()) {
                            ToastUtils.showShort(R.string.net_work_failed);
                            return;
                        }
                        ToastUtils.showShort(R.string.module_asset_order_cancel_success);
                        finish();
                    }
                });
            }

            @Override
            public void cancel() {

            }
        });
    }

    private void showBuyOrderPasswordVerifyDialog(final NhAssetOrderEntity.NhOrderBean nhOrderBean) {
        final BaseVerifyPasswordDialog passwordVerifyDialog = new BaseVerifyPasswordDialog();
        passwordVerifyDialog.show(getSupportFragmentManager(), "passwordVerifyDialog");
        passwordVerifyDialog.setPasswordListener(new BaseVerifyPasswordDialog.IPasswordListener() {
            @Override
            public void onFinish(String password) {
                CocosBcxApiWrapper.getBcxInstance().buy_nh_asset(AccountHelperUtils.getCurrentAccountName(), password, nhOrderBean.id, new IBcxCallBack() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.i("buy_nh_asset", s);
                        final OperateResultModel operateResultModel = GsonSingleInstance.getGsonInstance().fromJson(s, OperateResultModel.class);

                        if (operateResultModel.code == 161) {
                            ToastUtils.showShort(R.string.module_asset_order_not_exist);
                            return;
                        }

                        if (operateResultModel.code == 105) {
                            ToastUtils.showShort(R.string.module_asset_wrong_password);
                            return;
                        }

                        if (!TextUtils.isEmpty(operateResultModel.message)
                                && (operateResultModel.message.contains("insufficient_balance")
                                || operateResultModel.message.contains("Insufficient Balance"))) {
                            ToastUtils.showShort(R.string.insufficient_balance);
                            return;
                        }

                        if (!operateResultModel.isSuccess()) {
                            ToastUtils.showShort(R.string.net_work_failed);
                            return;
                        }
                        finish();
                        ToastUtils.showShort(R.string.module_asset_order_buy_success);
                    }
                });
            }

            @Override
            public void cancel() {

            }
        });
    }
}
