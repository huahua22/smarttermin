package com.xwr.smarttermin.arcface.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import zz.yy.ucamir.cam.Camera;


public class CameraSurfaceView extends SurfaceView {

  private Handler mHandler=new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {

    }
  };
  private Camera mCamera;

  public CameraSurfaceView(Context context) {
    super(context);
  }

  public CameraSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        break;
      case MotionEvent.ACTION_UP:
        break;
    }
    return super.onTouchEvent(event);
  }

  public void addCamera(Camera camera) {
    this.mCamera=camera;
  }
}
