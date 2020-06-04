package com.cocos.module_asset.nh_order_manager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.bcx_sdk.bcx_error.NetworkStatusException;
import com.cocos.bcx_sdk.bcx_wallet.chain.asset_object;
import com.cocos.library_base.base.BaseViewModel;
import com.cocos.library_base.entity.NhAssetOrderEntity;
import com.cocos.library_base.utils.TimeUtil;
import com.cocos.library_base.utils.ToastUtils;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;
import com.cocos.library_base.utils.singleton.MainHandler;
import com.cocos.module_asset.BR;
import com.cocos.module_asset.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ningkang.guo
 * @Date 2019/7/15
 */
public class AllNhOrderViewModel extends BaseViewModel {


    String pattern = "yyyy-MM-dd'T'HH:mm:ss";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);

    public AllNhOrderViewModel(@NonNull Application application) {
        super(application);
    }

    public ObservableInt emptyViewVisible = new ObservableInt(View.GONE);

    public ObservableInt recyclerViewVisible = new ObservableInt(View.VISIBLE);

    public ObservableList<AllNhOrderItemViewModel> observableList = new ObservableArrayList<>();

    public ItemBinding<AllNhOrderItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.module_asset_item_all_nh_order);

    public final BindingRecyclerViewAdapter<AllNhOrderItemViewModel> adapter = new BindingRecyclerViewAdapter<>();

    /**
     * 加载全网NH订单
     *
     * @param page
     * @param pageSize
     * @param ptrFrameLayout
     */
    public void requestAssetsListData(final int page, int pageSize, final PtrFrameLayout ptrFrameLayout) {

        CocosBcxApiWrapper.getBcxInstance().list_nh_asset_order(page, pageSize, new IBcxCallBack() {
            @Override
            public void onReceiveValue(String s) {
                Log.i("list_nh_asset_order", s);
                final NhAssetOrderEntity nhOrderEntity = GsonSingleInstance.getGsonInstance().fromJson(s, NhAssetOrderEntity.class);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (page <= 1) {
                            observableList.clear();
                        }

                        if (null == nhOrderEntity.getData()) {
                            emptyViewVisible.set(View.VISIBLE);
                            recyclerViewVisible.set(View.GONE);
                            if (null != ptrFrameLayout) {
                                ptrFrameLayout.refreshComplete();
                            }
                            return;
                        }

                        if (nhOrderEntity.getData().size() <= 0 && page > 1) {
                            if (null != ptrFrameLayout) {
                                ptrFrameLayout.refreshComplete();
                            }
                            return;
                        }
                        if (!nhOrderEntity.isSuccess() || nhOrderEntity.getData() == null || nhOrderEntity.getData().size() <= 0) {
                            emptyViewVisible.set(View.VISIBLE);
                            recyclerViewVisible.set(View.GONE);
                            if (null != ptrFrameLayout) {
                                ptrFrameLayout.refreshComplete();
                            }
                            return;
                        }
                        List<NhAssetOrderEntity.NhOrderBean> nhOrderBeans = nhOrderEntity.getData();
                        for (NhAssetOrderEntity.NhOrderBean nhOrderBean : nhOrderBeans) {
                            try {
                                asset_object asset_object = CocosBcxApiWrapper.getBcxInstance().get_asset_object(nhOrderBean.price.asset_id);
                                if (null != asset_object) {
                                    NumberFormat nf = NumberFormat.getInstance();
                                    nf.setMaximumFractionDigits(5);
                                    nf.setGroupingUsed(false);
                                    nhOrderBean.priceWithSymbol = nf.format(new BigDecimal(nhOrderBean.price.amount).divide(BigDecimal.valueOf(Math.pow(10, asset_object.precision))).setScale(5, RoundingMode.HALF_UP).add(BigDecimal.ZERO)) + " " + asset_object.symbol;
                                    Date dateObject = null;
                                    dateObject = sDateFormat.parse(nhOrderBean.expiration);
                                    nhOrderBean.expirationTime = TimeUtil.formDate(dateObject);
                                    AllNhOrderItemViewModel itemViewModel = new AllNhOrderItemViewModel(AllNhOrderViewModel.this, nhOrderBean);
                                    observableList.add(itemViewModel);
                                    emptyViewVisible.set(View.GONE);
                                    recyclerViewVisible.set(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                ToastUtils.showShort(com.cocos.library_base.R.string.net_work_failed);
                            }
                        }
                        if (null != ptrFrameLayout) {
                            ptrFrameLayout.refreshComplete();
                        }
                    }
                });
            }
        });

    }
}
