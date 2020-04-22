package com.xwr.smarttermin.arcface.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


public class CameraParamView extends FrameLayout {

  private TextView mTv_param_name;
  private TextView mTv_param_value;
  private View mView;

  public CameraParamView(Context context) {
    this(context,null);
  }

  public CameraParamView(Context context, AttributeSet attrs) {
    this(context, attrs,0);
  }

  public CameraParamView(Context context,  AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }


  public void setTv_param_name(String name){
    mTv_param_name.setText(name);
  }

  public void setTv_param_value(String value){
    mTv_param_value.setText(value);
  }

}
