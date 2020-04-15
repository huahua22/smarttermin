package com.xwr.smarttermin.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.wrfid.dev.IDCardInfo;
import com.wrfid.dev.USBMsg;
import com.wrfid.dev.WRFIDApi;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.CardBean;
import com.xwr.smarttermin.comm.Session;
import com.xwr.smarttermin.util.UiUtil;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class IndexFrag extends BaseFragment {
  private Handler mainHandler;
  private boolean mRunning = false;
  WRFIDApi api = null;
  boolean isInit = false;
  @SuppressLint("HandlerLeak")
  Handler MyHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case USBMsg.USB_DeviceConnect:// 设备连接
          //          UiUtil.showToast(getContext(), "设备连接");
          break;
        case USBMsg.USB_DeviceOffline:// 设备断开
          UiUtil.showToast(getContext(), "设备断开");
          break;
        case USBMsg.ReadIdCardSusse:
          break;
        case USBMsg.ReadIdCardFail:
          break;
        case 6:
          api = new WRFIDApi(MyHandler);
          boolean nedInit = true;
          while (nedInit) {
            isInit = api.WRFID_Init(getActivity());
            if (isInit) {
              mRunning = true;
              initReadData();
              nedInit = false;
            } else {
              UiUtil.showToast(getContext(), "设备初始化失败");
            }
          }
          break;
        default:
          break;
      }
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    EventBus.getDefault().register(this);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_index;
  }

  @Override
  protected void initView() {

  }

  @Override
  protected void initData() {
    super.initData();
  }

  @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  public void onMessageEvent(String data) {
    System.out.println("--->>>read result event：" + data);
    if ("011".equals(data)) {
      MyHandler.sendEmptyMessage(6);
    }
  }

  private void initReadData() {
    HandlerThread scanThread = new HandlerThread("readIcThread");
    scanThread.start();
    mainHandler = new Handler(scanThread.getLooper());
    mainHandler.post(mBackgroundRunnable);//将线程post到handler中
  }

  //实现扫描窗耗时操作
  Runnable mBackgroundRunnable = new Runnable() {
    @Override
    public void run() {
      while (mRunning) {
        System.out.println("--->>>start");
        int ret;
        ret = api.WRFID_Authenticate();// 卡认证
        if (ret != 0) {
          System.out.println("--->>>authenticate fail");
          continue;
        }
        IDCardInfo ic = new IDCardInfo();
        ret = api.WRFID_Read_Content(ic);// 读卡
        if (ret == -2) {
          System.out.println("--->>>read content error");
          continue;
        }
        if (ret != 0) {// 读卡失败
          System.out.println("--->>>read content fail");
          continue;
        }
        System.out.println(ic.getPeopleName() + ic.getIDCard());
        if (ic != null) {
          CardBean cardBean = new CardBean();
          cardBean.setName(ic.getPeopleName());
          cardBean.setCardNum(ic.getIDCard());
          Session.mSocketResult.getRecipientData().setResult(cardBean);
          String mrecipient = Session.mSocketResult.getRecipientData().getSender();
          Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
          Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
          Session.mSocketResult.getRecipientData().setSuccess(true);
          WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
          mRunning = false;
        }
      }
    }
  };

  @Override
  public void onResume() {
    super.onResume();
    System.out.println("--->>>resume");
    mRunning = false;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mRunning = false;
    if (api != null) {
      api.UnInit();
    }
    if (mainHandler != null) {
      mainHandler.removeCallbacks(mBackgroundRunnable);
    }
    EventBus.getDefault().unregister(this);
  }
}
