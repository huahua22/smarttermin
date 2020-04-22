package com.xwr.smarttermin.main;

import android.os.Handler;
import android.os.HandlerThread;

import com.f4mds.usbreader.usbapi.USBDevice;
import com.f4mds.usbreader.usbapi.UsbApi;
import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.CardBean;
import com.xwr.smarttermin.comm.Session;
import com.zhangke.websocket.WebSocketHandler;

import java.io.UnsupportedEncodingException;

import utils.HexUtil;
import utils.UsbUtil;

/**
 * Create by xwr on 2020/4/14
 * Describe:银联卡扫描
 */
public class UnionPaycardsFrag extends BaseFragment {
  byte slot = 0x01;
  private Handler mHandler;
  private boolean mRunning;
  byte APU1[] = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0f, 0x73, 0x78, 0x31, 0x2e, 0x73, 0x68, 0x2e, (byte) 0xc9, (byte) 0xe7, (byte) 0xbb, (byte) 0xe1, (byte) 0xb1, (byte) 0xa3, (byte) 0xd5, (byte)
    0xcf};//卡片初始化1
  byte APU2[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x05};//卡片初始化2
  byte APU3[] = {0x00, (byte) 0xb2, 0x07, 0x04, 0x0b};//获取社保卡号
  byte APU4[] = {0x00, (byte) 0xb2, 0x02, 0x04, 0x20};//姓名
  byte APU5[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x06};

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_unionpay_cards;
  }

  @Override
  protected void initView() {

  }

  private void sendData(String num, String name) {
    CardBean cardBean = new CardBean();
    cardBean.setCardNum(num);
    cardBean.setName("张玲");
    Session.mSocketResult.getRecipientData().setResult(cardBean);
    String mrecipient = Session.mSocketResult.getRecipientData().getSender();
    Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
    Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
    Session.mSocketResult.getRecipientData().setSuccess(true);
    WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
    mRunning = false;
  }

  @Override
  protected void initData() {
    super.initData();
    if (USBDevice.mDeviceConnection == null) {
      try {
        UsbUtil.getInstance(getContext()).initUsbData();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    //    initReadData();

  }

  private void initReadData() {
    HandlerThread scanThread = new HandlerThread("readIccThread");
    scanThread.start();
    mRunning = true;
    mHandler = new Handler(scanThread.getLooper());
    mHandler.post(mBackgroundRunnable);//将线程post到handler中
  }

  //实现扫描窗耗时操作
  Runnable mBackgroundRunnable = new Runnable() {
    long ret, ret2;
    String name;

    @Override
    public void run() {
      while (mRunning) {
        if (USBDevice.mDeviceConnection != null) {
          //初始化
          ret = UsbApi.Reader_Init(USBDevice.mDeviceConnection, USBDevice.usbEpIn, USBDevice.usbEpOut);
          System.out.println("--->>>read init=" + ret);
        } else {
          try {
            UsbUtil.getInstance(getContext()).initUsbData();
            continue;
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        //上电
        byte[] atr = new byte[64];
        ret = UsbApi.ICC_Reader_PowerOn(slot, atr);
        System.out.println("\npower on=" + ret);
        if (ret > 0) {
          String data = null;
          data = HexUtil.bytesToHexString(atr, (int) ret);
          System.out.println("--->>>power on:" + data);
        } else {
          System.out.println("--->>>上电失败");
          continue;
        }
        byte[] apdu1 = new byte[64];
        byte[] apdu2 = new byte[64];
        byte[] apdu3 = new byte[64];
        byte[] apdu4 = new byte[64];
        ret = UsbApi.ICC_Reader_Application(slot, 20, APU1, apdu1);
        System.out.println("\napu1=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU2, apdu1);
        System.out.println("\napu2=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret2 = UsbApi.ICC_Reader_Application(slot, 5, APU3, apdu2);
        ret2 = UsbApi.ICC_Reader_Application(slot, 7, APU5, apdu3);
        System.out.println("--->>>read:" + HexUtil.bytesToHexString(apdu3, (int) ret2));
        ret2 = UsbApi.ICC_Reader_Application(slot, 5, APU4, apdu4);
        System.out.println("--->>>read:" + HexUtil.bytesToHexString(apdu4, (int) ret2));
        byte[] data2 = new byte[6];
        System.arraycopy(apdu4, 2, data2, 0, 6);
        try {
          name = new String(data2, "GBK");
          System.out.println("--->>>read:" + name);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        if (ret > 0) {
          byte[] data1 = new byte[9];
          System.arraycopy(apdu2, 2, data1, 0, 9);
          System.out.println("--->>>" + HexUtil.bytesToHexString(data1, (int) 9));
          System.out.println("--->>>" + new String(data1));
          sendData(new String(data1), name);
        } else {
          System.out.println("--->>>read fail");
          continue;
        }
      }
    }
  };

  @Override
  public void onDestroy() {
    super.onDestroy();
    mRunning = false;
    if (mHandler != null) {
      mHandler.removeCallbacks(mBackgroundRunnable);
    }
    UsbApi.ICC_Reader_PowerOff(slot);

  }

}
