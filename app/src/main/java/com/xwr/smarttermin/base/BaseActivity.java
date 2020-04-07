package com.xwr.smarttermin.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import butterknife.ButterKnife;

/**
 * Create by xwr on 2020/3/31
 * Describe:
 */
public abstract class BaseActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(getContentLayoutId());
    ButterKnife.bind(this);
    initView();
  }

  public abstract int getContentLayoutId();

  protected abstract void initView();
}
