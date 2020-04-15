package com.xwr.smarttermin.bean;

/**
 * Create by xwr on 2020/4/1
 * Describe:
 */
public class CardBean {
  String name;//姓名
  String cardNum;//卡号
  String socialCardNum;//社保卡号

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCardNum() {
    return cardNum;
  }

  public void setCardNum(String cardNum) {
    this.cardNum = cardNum;
  }

  public String getSocialCardNum() {
    return socialCardNum;
  }

  public void setSocialCardNum(String socialCardNum) {
    this.socialCardNum = socialCardNum;
  }
}
