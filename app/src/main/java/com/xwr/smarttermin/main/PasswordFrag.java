package com.xwr.smarttermin.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.Gson;
import com.xwr.smarttermin.R;
import com.xwr.smarttermin.base.BaseFragment;
import com.xwr.smarttermin.comm.Session;
import com.xwr.smarttermin.view.CustomNumKeyView;
import com.zhangke.websocket.WebSocketHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Create by xwr on 2020/4/3
 * Describe:
 */
public class PasswordFrag extends BaseFragment implements CustomNumKeyView.CallBack {

  @BindView(R.id.kv_pwd)
  CustomNumKeyView mKvPwd;
  Unbinder unbinder;
  @BindView(R.id.et_psd)
  EditText mEtPsd;

  @Override
  public int getContentLayoutId() {
    return R.layout.frag_password;
  }

  @Override
  protected void initView() {
    mKvPwd.setOnCallBack(this);
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

  @Override
  public void clickNum(String num) {
    mEtPsd.append(num);
  }

  @Override
  public void deleteNum() {
    int last = mEtPsd.getText().length();
    if (last > 0) {
      //删除最后一位
      mEtPsd.getText().delete(last - 1, last);
    }
  }

  @Override
  public void finishNum() {
    Session.mSocketResult.getRecipientData().setResult(mEtPsd.getText().toString());
    String mrecipient = Session.mSocketResult.getRecipientData().getSender();
    Session.mSocketResult.getRecipientData().setSender(Session.mSocketResult.getRecipientData().getRecipient());
    Session.mSocketResult.getRecipientData().setRecipient(mrecipient);
    Session.mSocketResult.getRecipientData().setSuccess(true);
    WebSocketHandler.getDefault().send(new Gson().toJson(Session.mSocketResult));
    mEtPsd.setText("");
  }
}
