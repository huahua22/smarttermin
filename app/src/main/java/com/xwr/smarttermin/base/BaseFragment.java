package com.xwr.smarttermin.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

  private View mView;
  private Unbinder unbinder;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
    savedInstanceState) {
    mView = inflater.inflate(getContentLayoutId(), null);
    unbinder = ButterKnife.bind(this, mView);
    initView();
    return mView;
  }

  public abstract int getContentLayoutId();

  protected abstract void initView();

  protected void initData() {

  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser) {// 当fragment处于可见状态，当fragment结合viewpagers使用的时候
      if (mView != null) {
        System.out.println("--->>>visible");
//        initData();
      }
    }
  }


  private boolean isFirstCreate = false;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (getUserVisibleHint() && !isFirstCreate) {
      System.out.println("---->>>create");
      isFirstCreate = true;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    initData();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

}
