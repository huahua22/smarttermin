package com.xwr.smarttermin.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wrfid.dev.IDCardInfo;
import com.wrfid.dev.USBMsg;
import com.wrfid.dev.WRFIDApi;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.CardBean;
import com.xwr.smarttermin.comm.Session;
import com.zhangke.websocket.WebSocketHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/3
 * Describe:
 */
public class IdCardFrag extends BaseFragment {
  @BindView(R.id.tvOutput)
  TextView mTvOutput;
  @BindView(R.id.btnleft01)
  Button mBtnleft01;
  Unbinder unbinder;
  WRFIDApi api;
  boolean isInit = false;
  @SuppressLint("HandlerLeak")
  Handler MyHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case USBMsg.USB_DeviceConnect:// 设备连接
          break;
        case USBMsg.USB_DeviceOffline:// 设备断开
          break;
        case USBMsg.ReadIdCardSusse:
          break;
        case USBMsg.ReadIdCardFail:
          break;
        default:
          break;
      }
    }
  };

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_idcard;
  }

  @Override
  protected void initView() {
    mTvOutput.setMovementMethod(ScrollingMovementMethod.getInstance());
  }

  @Override
  protected void initData() {
    super.initData();
    api = new WRFIDApi(MyHandler);
    isInit = api.WRFID_Init(getActivity());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onDestroyView() {
    api.UnInit();
    super.onDestroyView();
    unbinder.unbind();
  }

  @OnClick(R.id.btnleft01)
  public void onViewClicked() {
    if (isInit) {
      int ret;
      ret = api.WRFID_Authenticate();// 卡认证
      if (ret != 0) {
        mTvOutput.setText("认卡失败");
        api.WRFID_Authenticate();
      }
      IDCardInfo ic = new IDCardInfo();
      ret = api.WRFID_Read_Content(ic);// 读卡
      if (ret == -2) {
        mTvOutput.setText("读卡异常");
        return;
      }
      if (ret != 0) {// 读卡失败
        mTvOutput.setText("读卡失败");
        api.WRFID_Read_Content(ic);
        //        return;
      }
      mTvOutput.setText(ic.getPeopleName() + ic.getIDCard());
      if (ic.getPeopleName() != null) {
        CardBean cardBean = new CardBean();
        cardBean.setName("张玲");
        cardBean.setCardNum("362502199703045662");
        Session.mSocketResult.getRecipientData().setResult(cardBean);
        String mrecipient = Session.mSocketResult.getRecipientData().getSender();
        Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
        Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
        Session.mSocketResult.getRecipientData().setSuccess(true);
        WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
      }
    } else {
      mTvOutput.setText("初始失败");
    }
  }


}
