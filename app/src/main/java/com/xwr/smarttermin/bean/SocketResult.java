package com.xwr.smarttermin.bean;

/**
 * Create by xwr on 2020/3/31
 * Describe:
 */
public class SocketResult{
  int id;
  String msgType;
  long timestamp;
  RecipientBean recipientData;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public RecipientBean getRecipientData() {
    return recipientData;
  }

  public void setRecipientData(RecipientBean recipientData) {
    this.recipientData = recipientData;
  }
}
