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

import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.util.FileUtil;
import com.xwr.smarttermin.view.CircleCameraPreview;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class FaceCollectFrag extends BaseFragment {
  @BindView(R.id.sv_face)
  CircleCameraPreview mSvFace;
  Unbinder unbinder;
  @BindView(R.id.btn_take_photo)
  Button mBtnTakePhoto;
  String path = FileUtil.getSDPath() + "/face_img.jpg";

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_face_collect;
  }

  @Override
  protected void initView() {
    mSvFace.setIPictureListener(new CircleCameraPreview.IPictureListener() {
      @Override
      public void onPictureData(byte[] data) {
        System.out.print(data);

      }
    });


    //    sfh = mSvFace.getHolder();
    //    sfh.addCallback(new SurfaceHolder.Callback() {
    //      @Override
    //      public void surfaceCreated(SurfaceHolder holder) {
    //        try {
    //          camera = Camera.open(4);
    //          camera.setPreviewDisplay(holder);
    //          camera.startPreview();
    //        } catch (Exception e) {
    //          e.printStackTrace();
    //        }
    //      }
    //
    //      @Override
    //      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    //      }
    //
    //      @Override
    //      public void surfaceDestroyed(SurfaceHolder holder) {
    //        camera.stopPreview();
    //        camera.release();
    //        camera = null;
    //      }
    //    });
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
    checkPermission();
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
}
