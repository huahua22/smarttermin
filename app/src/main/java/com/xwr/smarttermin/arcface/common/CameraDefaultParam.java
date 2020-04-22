package com.xwr.smarttermin.arcface.common;

public class CameraDefaultParam {
  /**
   * 摇臂单目
   * @return
   */
  public static Param getCam(){
    Param param = new Param();
    param.brightness=0;
    param.contrast=15;
    param.saturation=32;
    param.hue=0;
    param.gamma=130;
    param.gain=1000;
    param.definition=30;
    param.negativeContrast=1000;
    param.whiteBalance=1;
    param.exposure=1;
    return param;
  }

  /**
   * 对焦
   * @return
   */
  public static Param getFocusCam(){
    Param param = new Param();
    param.brightness=0;
    param.contrast=32;
    param.saturation=32;
    param.hue=0;
    param.gamma=0;
    param.gain=184;
    param.definition=16;
    param.negativeContrast=0;
    param.whiteBalance=1;
    param.exposure=1;
    return param;
  }


  public static class Param{
    public int brightness;
    public int contrast;
    public int definition;
    public int gain;
    public int gamma;
    public int hue;
    public int negativeContrast;
    public int saturation;
    public int whiteBalance;
    public int exposure;
    public int focus;
  }
}
