package com.xwr.smarttermin.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.f4mds.usbreader.usbapi.USBDevice;
import com.f4mds.usbreader.usbapi.UsbApi;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.util.TypeUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.HexUtil;
import utils.UsbUtil;

import static com.xwr.smarttermin.util.UiUtil.println;

/**
 * Create by xwr on 2020/4/17
 * Describe:
 */
public class CardFrag extends BaseFragment {
  byte slot = 0x01;
  @BindView(R.id.readerInit)
  Button mReaderInit;
  @BindView(R.id.powerOn)
  Button mPowerOn;
  @BindView(R.id.readWrite)
  Button mReadWrite;
  @BindView(R.id.powerOff)
  Button mPowerOff;
  @BindView(R.id.result)
  TextView mResult;
  Unbinder unbinder;
  byte APU01[] = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0f, 0x73, 0x78, 0x31, 0x2e, 0x73, 0x68, 0x2e, (byte) 0xc9, (byte) 0xe7, (byte) 0xbb, (byte) 0xe1, (byte) 0xb1, (byte) 0xa3, (byte) 0xd5, (byte)
    0xcf};//卡片初始化1
  byte APU02[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x05};//卡片初始化2
  String str = "0020000003000000";

  byte APU2[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x06};//卡片验证
  byte APU3[] = {0x00, (byte) 0xb2, 0x01, 0x04, 0x14};//获取身份证号
  byte APU4[] = {0x00, (byte) 0xb2, 0x02, 0x04, 0x20};//姓名

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_icc;
  }

  @Override
  protected void initView() {

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

  @OnClick({R.id.readerInit, R.id.powerOn, R.id.readWrite, R.id.powerOff})
  public void onViewClicked(View view) {
    long ret;
    switch (view.getId()) {
      case R.id.readerInit:
        if (USBDevice.mDeviceConnection != null) {
          //初始化
          ret = UsbApi.Reader_Init(USBDevice.mDeviceConnection, USBDevice.usbEpIn, USBDevice.usbEpOut);
          mResult.append("\nread init=" + ret);
        } else {
          try {
            UsbUtil.getInstance(getContext()).initUsbData();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        break;
      case R.id.powerOn:
        //上电
        byte[] atr = new byte[64];
        ret = UsbApi.ICC_Reader_PowerOn(slot, atr);
        mResult.append("\npower on=" + ret);
        if (ret > 0) {
          String data = null;
          data = HexUtil.bytesToHexString(atr, (int) ret);
          mResult.append("   data:" + data);
        } else {
          mResult.append("\n上电失败");
        }
        break;
      case R.id.readWrite:
        byte[] apdu1 = new byte[64];
        byte[] apdu2 = new byte[64];
        byte[] apdu3 = new byte[64];
        byte[] apdu4 = new byte[64];
        ret = UsbApi.ICC_Reader_Application(slot, 20, APU01, apdu1);
        println("init1 card=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU02, apdu1);
       println("init2 card=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        byte APU1[] = TypeUtil.hexString2Bytes(str);
        ret = UsbApi.ICC_Reader_Application(slot, APU1.length, APU1, apdu1);//卡片锁定

        mResult.append("\napu1=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU2, apdu1);//卡片初始化2
        mResult.append("\nret=" + ret + ";apu2=" + HexUtil.bytesToHexString(apdu1, (int) ret));

        ret = UsbApi.ICC_Reader_Application(slot, 5, APU3, apdu2);//身份证号
        mResult.append("\n" + "apu2=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 5, APU4, apdu4);//姓名
        System.out.println("--->>>read:" + HexUtil.bytesToHexString(apdu4, (int) ret));
        byte[] data2 = new byte[6];
        System.arraycopy(apdu4, 2, data2, 0, 6);
        if (ret > 0) {
          byte[] data1 = new byte[9];
          System.arraycopy(apdu2, 2, data1, 0, 9);
          System.out.println("--->>>" + HexUtil.bytesToHexString(data1, (int) 9));
          System.out.println("--->>>" + new String(data1));
        } else {
          System.out.println("--->>>read fail");
        }
        break;
      case R.id.powerOff:
        ret = UsbApi.ICC_Reader_PowerOff(slot);
        mResult.append("\nret:" + ret);
        break;
    }
  }
}