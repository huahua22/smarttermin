package com.xwr.smarttermin.bean;

import java.io.Serializable;

/**
 * Create by xwr on 2020/4/1
 * Describe:
 */
public class CardBean implements Serializable {
  String name;//姓名
  String cardNum;//卡号

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
}
