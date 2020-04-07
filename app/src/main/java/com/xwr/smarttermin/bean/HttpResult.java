package com.xwr.smarttermin.bean;

import java.io.Serializable;

/**
 * Create by xwr on 2020/4/7
 * Describe:
 */
public class HttpResult implements Serializable {
  boolean success;
  String msg;
  FileDetails obj;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public FileDetails getObj() {
    return obj;
  }

  public void setObj(FileDetails obj) {
    this.obj = obj;
  }
}
