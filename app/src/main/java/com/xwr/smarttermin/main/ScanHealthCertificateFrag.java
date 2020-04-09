package com.xwr.smarttermin.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.scan.HYScan;
import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.comm.Session;
import com.zhangke.websocket.WebSocketHandler;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cd.hex.Hex;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class ScanHealthCertificateFrag extends BaseFragment {
  Unbinder unbinder;
  private HYScan mScan;
  private int mFd;
  private Handler mHandler;
  private boolean mRunning = false;

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_scan_health_certificate;
  }

  @Override
  protected void initView() {

  }
  private void initScan() {
    HandlerThread scanThread = new HandlerThread("scanHealthThread");
    scanThread.start();
    mHandler = new Handler(scanThread.getLooper());
    mHandler.post(mBackgroundRunnable);//将线程post到handler中
  }


  //实现扫描窗耗时操作
  Runnable mBackgroundRunnable = new Runnable() {
    @Override
    public void run() {
      System.out.println("--->>>open scan:" + Thread.currentThread().getName());
      mScan = new HYScan();
      mFd = mScan.open_device("dev/ttysWK3", 9600);
      int fd = mScan.init_mode(mFd, (byte) 0x01);
      Log.d("xwr", "HYScan fd:" + fd);
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      byte[] bytes = new byte[1024];
      while (mRunning) {
        int ret = mScan.begin_scan(mFd, 4000, bytes, 1024);
        if (ret > 0) {
          byte[] bytes1 = new byte[ret];
          System.arraycopy(bytes, 0, bytes1, 0, ret);
          System.out.println("scan " + Hex.byteArray2Hex(bytes, 0, ret));
          String code = new String(bytes1);
          System.out.println("scan " + code);
          mRunning = false;
          Session.mSocketResult.getRecipientData().setResult(code);
          System.out.println("xwr--->>code:" + Session.mSocketResult.getRecipientData().getResult());
          String mrecipient = Session.mSocketResult.getRecipientData().getSender();
          Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
          Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
          Session.mSocketResult.getRecipientData().setSuccess(true);
          WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
        }
      }
      mScan.close_device(fd);
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    mRunning = false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mHandler.removeCallbacks(mBackgroundRunnable);
  }

  @Override
  public void onResume() {
    super.onResume();
    mRunning = true;
    initScan();
  }
}
