package com.xwr.smarttermin.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dev.scan.HYScan;
import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.comm.Session;
import com.zhangke.websocket.WebSocketHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cd.hex.Hex;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class ScanPayBarcodeFrag extends BaseFragment {
  @BindView(R.id.btnScan)
  Button mBtnScan;
  Unbinder unbinder;
  private HYScan mScan;
  private int mFd;

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_scan_paybarcode;
  }

  @Override
  protected void initView() {

  }

  @Override
  protected void initData() {
    //    openScan();
  }

  private void openScan() {
    mScan = new HYScan();
    mFd = mScan.open_device("dev/ttysWK3", 9600);
    int ret = mScan.init_mode(mFd, (byte) 0x01);
    Log.d("xwr", "HYScan fd:" + ret);
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void onResume() {
    super.onResume();
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
    super.onDestroyView();
    unbinder.unbind();
  }

  @OnClick(R.id.btnScan)
  public void onViewClicked() {
    openScan();
    int ret;
    boolean flag = true;
    byte[] bytes = new byte[1024];
    ret = mScan.begin_scan(mFd, 4000, bytes, 1024);
    while (flag) {
      if (ret > 0) {
        byte[] bytes1 = new byte[ret];
        System.arraycopy(bytes, 0, bytes1, 0, ret);
        System.out.println("scan " + Hex.byteArray2Hex(bytes, 0, ret));
        System.out.println("scan " + new String(bytes1));
        flag = false;
//        new String(bytes1)
        //        Session.mSocketResult.getRecipientData().setResult();
        String mrecipient = Session.mSocketResult.getRecipientData().getSender();
        Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
        Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
        Session.mSocketResult.getRecipientData().setSuccess(true);
        WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
      }
    }
  }
}
