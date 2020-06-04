package com.cocos.library_base.widget;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.library_base.R;
import com.cocos.library_base.base.BaseDialogFragment;
import com.cocos.library_base.entity.AccountNamesEntity;
import com.cocos.library_base.entity.BaseResult;
import com.cocos.library_base.entity.FoundListModel;
import com.cocos.library_base.entity.WebViewModel;
import com.cocos.library_base.global.IntentKeyGlobal;
import com.cocos.library_base.global.SPKeyGlobal;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.AccountHelperUtils;
import com.cocos.library_base.utils.SPUtils;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ningkang.guo
 * @Date 2019/3/5
 */
public class DialogFragment extends BaseDialogFragment {

    @Override
    protected int getPopUpLayoutId() {
        return R.layout.fragment_dialog_layout;
    }

    @Override
    protected void initView(View mainView) {
        try {
            Bundle bundle = getArguments();
            FoundListModel foundListModel = (FoundListModel) bundle.getSerializable(IntentKeyGlobal.WEB_MODEL);
            String content = bundle.getString(IntentKeyGlobal.DIALOG_CONTENT);
            int type = bundle.getInt(IntentKeyGlobal.DIALOG_TYPE);
            mainView.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (type == 2 && null != foundListModel) {
                        ArrayList<String> urls = SPUtils.getUrlList(SPKeyGlobal.FOUND_DIALOG_SHOWED_MARK);
                        WebViewModel webViewModel = new WebViewModel();
                        webViewModel.setTitle(foundListModel.getListTitle());
                        webViewModel.setUrl(foundListModel.getLinkUrl());
                        webViewModel.setIconUrl(foundListModel.getImageUrl());
                        webViewModel.setDesc(foundListModel.getListDesc());
                        urls.add(foundListModel.getLinkUrl());
                        SPUtils.setDataList(SPKeyGlobal.FOUND_DIALOG_SHOWED_MARK, urls);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(IntentKeyGlobal.WEB_MODEL, webViewModel);
                        ARouter.getInstance().build(RouterActivityPath.ACTIVITY_JS_WEB).with(bundle).navigation();
                    } else if (type == 1) {
                        String name = bundle.getString(IntentKeyGlobal.ACCOUNT_NAME);
                        CocosBcxApiWrapper.getBcxInstance().delete_account_by_name(name, s -> {
                            BaseResult logoutModel = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
                            if (!logoutModel.isSuccess()) {
                                return;
                            }
                            CocosBcxApiWrapper.getBcxInstance().queryAccountNamesByChainId(new IBcxCallBack() {
                                @Override
                                public void onReceiveValue(String s) {
                                    AccountNamesEntity accountNamesEntity = GsonSingleInstance.getGsonInstance().fromJson(s, AccountNamesEntity.class);
                                    if (accountNamesEntity.isSuccess()) {
                                        List<String> accountNames = Arrays.asList(accountNamesEntity.data.split(","));
                                        AccountHelperUtils.setCurrentAccountName(accountNames.get(0));
                                    } else {
                                        AccountHelperUtils.setCurrentAccountName("");
                                    }
                                    ARouter.getInstance().build(RouterActivityPath.ACTIVITY_MAIN_PATH).navigation();
                                }
                            });
                        });
                    }
                }
            });
            mainView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
            TextView tvContent = mainView.findViewById(R.id.tv_content);
            tvContent.setText(content);
        } catch (Exception e) {

        }
    }

    @Override
    protected boolean needForbidBackPress() {
        return true;
    }

    @Override
    protected boolean isCancelableTouchOutSide() {
        return false;
    }

}
