package com.xwr.smarttermin.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.IncidentalBean;
import com.xwr.smarttermin.bean.RecipientBean;
import com.xwr.smarttermin.comm.FragmentParms;
import com.xwr.smarttermin.comm.Session;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class SettleInfoFrag extends BaseFragment {
  private CountDownTimer timer;
  @BindView(R.id.tv_name)
  TextView mTvName;
  @BindView(R.id.tv_total_money)
  TextView mTvTotalMoney;
  @BindView(R.id.tv_over_money)
  TextView mTvOverMoney;
  @BindView(R.id.tv_medicare_money)
  TextView mTvMedicareMoney;
  @BindView(R.id.tv_cash_money)
  TextView mTvCashMoney;
  Unbinder unbinder;
  RecipientBean mRecipientBean = null;
 /* @SuppressLint("HandlerLeak")
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      initInfoData();
    }
  };*/

  @Override
  protected void initData() {
    super.initData();
    if (Session.mSocketResult != null) {
      mRecipientBean = Session.mSocketResult.getRecipientData();
      initInfoData();
    }
  }

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_settle_info;
  }

  @Override
  protected void initView() {
    timer = new CountDownTimer(30 * 1000, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        // TODO Auto-generated method stub
        //        mTvTimer.setText(millisUntilFinished / 1000 + "s");
      }

      @Override
      public void onFinish() {
        FragmentParms.sChangeFragment.change(0);
      }
    }.start();
  }

  private void initInfoData() {
    if (mRecipientBean != null) {
      IncidentalBean incidentalBean = mRecipientBean.getIncidentalData();
      mTvName.setText(incidentalBean.getName());
      mTvMedicareMoney.setText(incidentalBean.getMedicareMoney());
      mTvTotalMoney.setText(incidentalBean.getTotalMoney());
      mTvCashMoney.setText(incidentalBean.getCashMoney());
      mTvOverMoney.setText(incidentalBean.getOverMoney());
    }
  }

  //  @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  //  public void onEvent(RecipientBean data) {
  //    if (null != data) {
  //      mRecipientBean = data;
  //      mHandler.sendEmptyMessage(0);
  //    }
  //  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    unbinder = ButterKnife.bind(this, rootView);
    //    EventBus.getDefault().register(this);
    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    timer.cancel();
    //    EventBus.getDefault().unregister(this);
  }
}
