package com.xwr.smarttermin.bean;

/**
 * Create by xwr on 2020/4/1
 * Describe:
 */
public class IncidentalBean  {
  String name;//姓名
  String totalMoney;//结算总额
  String medicareMoney;//个账总额
  String cashMoney;//自费总额
  String overMoney;//统筹总额

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTotalMoney() {
    return totalMoney;
  }

  public void setTotalMoney(String totalMoney) {
    this.totalMoney = totalMoney;
  }

  public String getMedicareMoney() {
    return medicareMoney;
  }

  public void setMedicareMoney(String medicareMoney) {
    this.medicareMoney = medicareMoney;
  }

  public String getCashMoney() {
    return cashMoney;
  }

  public void setCashMoney(String cashMoney) {
    this.cashMoney = cashMoney;
  }

  public String getOverMoney() {
    return overMoney;
  }

  public void setOverMoney(String overMoney) {
    this.overMoney = overMoney;
  }
}
