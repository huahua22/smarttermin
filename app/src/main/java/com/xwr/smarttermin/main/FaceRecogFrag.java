package com.xwr.smarttermin.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.arcface.ArcFacePresenter;
import com.xwr.smarttermin.arcface.CameraView;
import com.xwr.smarttermin.arcface.FaceRectView;
import com.xwr.smarttermin.arcface.FaceServer;
import com.xwr.smarttermin.arcface.IFaceResultListener;
import com.xwr.smarttermin.arcface.common.ThreadManager;
import com.xwr.smarttermin.arcface.utils.Util;
import com.xwr.smarttermin.arcface.view.SurfaceCallback;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.comm.Session;
import com.xwr.smarttermin.util.UiUtil;
import com.xwr.smarttermin.view.CircleCameraPreview;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import zz.yy.ucamir.cam.CamException;
import zz.yy.ucamir.cam.Camera;
import zz.yy.ucamir.cam.CameraInfo;

/**
 * Create by xwr on 2020/4/16
 * Describe:
 */
public class FaceRecogFrag extends BaseFragment implements CameraView, IFaceResultListener {
  @BindView(R.id.face_rect_view)
  FaceRectView mFaceRectView;
  @BindView(R.id.fl_content)
  FrameLayout mFlContent;
  @BindView(R.id.btn_face_register)
  Button mBtnFaceRegister;
  @BindView(R.id.cc_camera)
  CircleCameraPreview mCcCamera;
  @BindView(R.id.btn_clear_face)
  Button mBtnClearFace;
  Unbinder unbinder;
  int faceFlag = 0;
  private int index = 0;
  private int sindex = 0;
  //权限信息
  private static final String[] NEEDED_PERMISSIONS = new String[]{
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
  @BindView(R.id.tv_face_title)
  TextView mTvFaceTitle;


  private int camId = CameraInfo.CAMERA_CAM_FOCUS;
  private Camera mCamera;
  private SurfaceHolder mSurfaceHolder;
  private ArcFacePresenter mArcFacePresenter;
  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case 0:
          mTvFaceTitle.setVisibility(View.VISIBLE);
          mBtnFaceRegister.setVisibility(View.VISIBLE);
          break;
        case 1:
          mTvFaceTitle.setVisibility(View.GONE);
          mBtnFaceRegister.setVisibility(View.GONE);
          break;
        case 2:
          //          Session.mSocketResult.getRecipientData().setResult(true);
          String mrecipient = Session.mSocketResult.getRecipientData().getSender();
          Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
          Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
          Session.mSocketResult.getRecipientData().setSuccess(true);
          WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
          break;
        case 3:
          //          Session.mSocketResult.getRecipientData().setResult(false);
          String mr = Session.mSocketResult.getRecipientData().getSender();
          Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
          Session.mSocketResult.getRecipientData().setRecipient(mr);
          Session.mSocketResult.getRecipientData().setSuccess(false);
          WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
          break;
        case 4:
          UiUtil.showToast(getContext(), "用户已注册");
          break;
      }
    }
  };

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_face_recog;
  }

  @Override
  protected void initView() {

  }

  @Override
  protected void initData() {
    mArcFacePresenter = new ArcFacePresenter(getContext(), this);
    initFace();
    initCamera();
    mArcFacePresenter.setOnFaceResultListener(this);

  }

  private void initCamera() {
    addSurfaceView();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: inflate a fragment view
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    unbinder = ButterKnife.bind(this, rootView);
    EventBus.getDefault().register(this);
    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }


  private void initFace() {
    if (!checkPermissions(NEEDED_PERMISSIONS)) {
      ActivityCompat.requestPermissions(getActivity(), NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
      return;
    }
    mArcFacePresenter.initEngine();
  }

  private boolean checkPermissions(String[] neededPermissions) {
    if (neededPermissions == null || neededPermissions.length == 0) {
      return true;
    }
    boolean allGranted = true;
    for (String neededPermission : neededPermissions) {
      allGranted &= ContextCompat.checkSelfPermission(getContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
    }
    return allGranted;
  }


  @Override
  public void addDebugInfo(String info) {
    Log.i("xwr--->>debug info", info);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == ACTION_REQUEST_PERMISSIONS) {
      boolean isAllGranted = true;
      for (int grantResult : grantResults) {
        isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
      }
      if (isAllGranted) {
        mArcFacePresenter.initEngine();
      } else {
        Util.showToast(getContext(), "缺少权限");
        Log.i("xwr", "缺少权限");
      }
    }
  }

  private void addSurfaceView() {
    mCcCamera.getHolder().addCallback(new SurfaceCallback() {

      @Override
      public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        try {
          if (mCamera != null) {
            if (mCamera.isPaused())
              mCamera.startPreview();
          } else {
            mCamera = Camera.open(4);
            mCamera.setDisplayOrientation(180);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
          }
        } catch (CamException e) {
          e.printStackTrace();
          Log.i("xwr", e.getMessage());
        }
        mArcFacePresenter.startFaceRecog(mCamera, camId, mFaceRectView, mBtnFaceRegister, mSurfaceHolder);
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        ThreadManager.getInstance().clear();
        if (mCamera != null) {
          mCamera.setPreviewCallback(null);
          if (mFaceRectView != null) {
            mFaceRectView.stopFace();
          }
          mCamera.release();
        }
        mCamera = null;
      }
    });
  }

  @Override
  public void onResult(int result) {
    if (faceFlag == 1) {
      if (result == 1) {
        index = 0;
        sindex++;
        if (sindex == 10) {
          mHandler.sendEmptyMessage(2);
        }
      } else {
        sindex = 0;
        index++;
        if (index == 20) {
          mHandler.sendEmptyMessage(3);
          index = 0;
        }
      }
    } else {
      if (result == 1) {
        mHandler.sendEmptyMessage(4);
      }
    }
  }


  @Override
  public void onDestroy() {
    if (mArcFacePresenter != null) {
      mArcFacePresenter.onDestroy();
    }
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }

  @OnClick({R.id.btn_face_register, R.id.btn_clear_face})
  public void onViewClicked(View view) {
    switch (view.getId()) {
      case R.id.btn_face_register:
        mArcFacePresenter.onClickRegisterFace();
        break;
      case R.id.btn_clear_face:
        FaceServer.getInstance().clearAllFaces(getContext());
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  public void onEvent(String data) {
    System.out.println("--->>>read result event：" + data);
    if ("031".equals(data)) {
      mHandler.sendEmptyMessage(0);
      faceFlag = 0;
    }
    if ("032".equals(data)) {
      mHandler.sendEmptyMessage(1);
      faceFlag = 1;
      index = 0;
    }
  }

}
