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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.HexUtil;
import utils.UsbUtil;

/**
 * Create by xwr on 2020/4/3
 * Describe:
 */
public class IccFrag extends BaseFragment {
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
    long ret, ret2;
    byte slot = 0x01;
    switch (view.getId()) {
      case R.id.readerInit:
        if (USBDevice.mDeviceConnection != null) {
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
        byte[] atr = new byte[64];
        ret = UsbApi.ICC_Reader_PowerOn(slot, atr);
        mResult.append("\npower on=" + ret);
        if (ret > 0) {
          String data = null;
          data = HexUtil.bytesToHexString(atr, (int) ret);
          mResult.append(";data=" + data);
        } else {
          String data = new String("上电失败");
          mResult.append("\n" + data + " ret:" + ret);
        }
        break;
      case R.id.readWrite:
        byte[] apdu = new byte[64];
        byte[] apdu2 = new byte[64];
        byte APU1[] = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0f, 0x73, 0x78, 0x31, 0x2e, 0x73, 0x68, 0x2e, (byte) 0xc9, (byte) 0xe7, (byte) 0xbb, (byte) 0xe1, (byte) 0xb1, (byte) 0xa3, (byte) 0xd5,
          (byte) 0xcf};
        ret = UsbApi.ICC_Reader_Application(slot, 20, APU1, apdu);
        mResult.append("\napu1=" +  HexUtil.bytesToHexString(apdu, (int) ret));
        byte APU2[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x05};
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU2, apdu);
        mResult.append("\napu2=" +  HexUtil.bytesToHexString(apdu, (int) ret));
        byte APU3[] = {0x00, (byte) 0xb2, 0x07, 0x04, 0x0b};
        ret2 = UsbApi.ICC_Reader_Application(slot, 5, APU3, apdu2);
        mResult.append("\napu3=" + HexUtil.bytesToHexString(apdu2, (int) ret));
        //        byte[] cmd = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};

        //        ret = UsbApi.ICC_Reader_Application(slot, 5, cmd, apdu);
        mResult.append("\napplication=" + ret + " " + ret2);
        if (ret > 0) {
          String data = null;
          String data2 = null;
          data = HexUtil.bytesToHexString(apdu, (int) ret);
          data2 = HexUtil.bytesToHexString(apdu2, (int) ret2);
          mResult.append(";data=" + data + "data2:" + data2);
        } else {
          String data = new String("取随机数失败");
          mResult.append("\n" + data);
        }
        break;
      case R.id.powerOff:
        ret = UsbApi.ICC_Reader_PowerOff(slot);
        mResult.append("\npowerOff=" + ret);
        mResult.append("\n------------------------");
        break;
    }
  }

  @Override
  protected void initData() {
    super.initData();
    try {
      UsbUtil.getInstance(getContext()).initUsbData();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
