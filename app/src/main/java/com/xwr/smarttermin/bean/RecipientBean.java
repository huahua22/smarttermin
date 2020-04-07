package com.xwr.smarttermin.bean;

import java.io.Serializable;

/**
 * Create by xwr on 2020/3/31
 * Describe:
 */
public class RecipientBean implements Serializable {
  String recipient;//接受者编号
  String sender;//发送者编号
  String recipientNo;//指令编号
  IncidentalBean incidentalData;
  boolean success;
  String msg;
  CardBean result;

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

  public CardBean getResult() {
    return result;
  }

  public void setResult(CardBean result) {
    this.result = result;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getRecipientNo() {
    return recipientNo;
  }

  public void setRecipientNo(String recipientNo) {
    this.recipientNo = recipientNo;
  }

  public IncidentalBean getIncidentalData() {
    return incidentalData;
  }

  public void setIncidentalData(IncidentalBean incidentalData) {
    this.incidentalData = incidentalData;
  }
}
