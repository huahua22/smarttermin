package com.xwr.smarttermin.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.RecipientBean;
import com.xwr.smarttermin.comm.FragmentParms;
import com.xwr.smarttermin.comm.Session;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/13
 * Describe:
 */
public class OwnExpenseFrag extends BaseFragment {
  @BindView(R.id.tv_medicare_pay)
  TextView mTvMedicarePay;
  @BindView(R.id.tv_cash_money)
  TextView mTvCashMoney;
  @BindView(R.id.iv_face_pay)
  ImageView mIvFacePay;
  @BindView(R.id.iv_code_pay)
  ImageView mIvCodePay;
  Unbinder unbinder;
  RecipientBean mRecipientBean = null;
  @SuppressLint("HandlerLeak")
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

    }
  };

  @Override
  protected void initData() {
    super.initData();
    initInfoData();
  }

  private void initInfoData() {
    if (Session.mSocketResult.getRecipientData() != null) {
      mRecipientBean = Session.mSocketResult.getRecipientData();
      mTvMedicarePay.setText(mRecipientBean.getIncidentalData().getMedicareMoney());
      mTvCashMoney.setText(mRecipientBean.getIncidentalData().getCashMoney());
    }

  }

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_own_expense;
  }

  @Override
  protected void initView() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    //    EventBus.getDefault().register(this);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

 /* @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  public void onMessageEvent(RecipientBean data) {
    if (null != data) {
      mRecipientBean = data;
      mHandler.sendEmptyMessage(0);
    }*/
  //  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    //    EventBus.getDefault().unregister(this);
  }

  @OnClick({R.id.iv_face_pay, R.id.iv_code_pay})
  public void onViewClicked(View view) {
    switch (view.getId()) {
      case R.id.iv_face_pay:
        Session.mSocketResult.getRecipientData().setRecipientNo("032");
        FragmentParms.sChangeFragment.change(4);
        break;
      case R.id.iv_code_pay:
        FragmentParms.sChangeFragment.change(3);
        break;
    }
  }
}
