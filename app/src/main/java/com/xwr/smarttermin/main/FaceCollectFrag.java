package com.xwr.smarttermin.main;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.bean.HttpResult;
import com.xwr.smarttermin.comm.Session;
import com.xwr.smarttermin.util.UiUtil;
import com.xwr.smarttermin.view.CircleCameraPreview;
import com.zhangke.websocket.WebSocketHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class FaceCollectFrag extends BaseFragment implements CircleCameraPreview.IPictureListener {
  @BindView(R.id.sv_face)
  CircleCameraPreview mSvFace;
  Unbinder unbinder;
  @BindView(R.id.btn_take_photo)
  Button mBtnTakePhoto;

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_face_collect;
  }

  @Override
  protected void initView() {
  }

  @Override
  protected void initData() {
    super.initData();
    checkPermission();
    mSvFace.setIPictureListener(this);
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

  @OnClick(R.id.btn_take_photo)
  public void onViewClicked() {
    mSvFace.takePhoto();
  }

  public void checkPermission() {
    boolean isGranted = true;
    if (android.os.Build.VERSION.SDK_INT >= 23) {
      if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //如果没有写sd卡权限
        isGranted = false;
      }
      if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        isGranted = false;
      }
      Log.i("cbs", "isGranted == " + isGranted);
      if (!isGranted) {
        ((Activity) getContext()).requestPermissions(
          new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
            .ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE},
          102);
      }
    }

  }

  @Override
  public void onPictureData(String data) {
    HttpResult msg = new Gson().fromJson(data, HttpResult.class);
    if (msg.isSuccess()) {
      Session.mSocketResult.getRecipientData().setResult(msg.getObj().getFilePath());
      String mrecipient = Session.mSocketResult.getRecipientData().getSender();
      Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
      Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
      Session.mSocketResult.getRecipientData().setSuccess(true);
      WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
    } else {
      UiUtil.showToast(getContext(), "图片上传失败");
    }
  }
}
