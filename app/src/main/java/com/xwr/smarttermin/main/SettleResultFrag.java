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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class SettleResultFrag extends BaseFragment {
  @BindView(R.id.iv_settle)
  ImageView mIvSettle;
  @BindView(R.id.tv_settle_result)
  TextView mTvSettleResult;
  Unbinder unbinder;


  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case 0:
          mIvSettle.setImageResource(R.mipmap.settle_fail);
          mTvSettleResult.setText("结算失败");
          break;
        case 1:
          mIvSettle.setImageResource(R.mipmap.settle_success);
          mTvSettleResult.setText("结算成功");
          break;
      }
    }
  };

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_settle_result;
  }

  @Override
  protected void initView() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    unbinder = ButterKnife.bind(this, rootView);
    EventBus.getDefault().register(this);
    return rootView;
  }

  @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
  public void onMessageEvent(RecipientBean data) {
    if ("055".equals(data.getRecipientNo())) {
      mHandler.sendEmptyMessage(1);
    } else {
      mHandler.sendEmptyMessage(0);
    }

  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    EventBus.getDefault().unregister(this);
  }
}