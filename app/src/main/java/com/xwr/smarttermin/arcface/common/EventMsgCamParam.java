package com.xwr.smarttermin.arcface.common;

public class EventMsgCamParam {
  private int id;
  private int value;

  public EventMsgCamParam(int id, int value) {
    this.id = id;
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
