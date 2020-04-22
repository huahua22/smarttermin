package com.xwr.smarttermin.arcface.common;

import android.content.Context;

import java.io.File;

public class Const {
  public static final String APP_ID = "yex8coMQjQcbMx81jGraUQYU17bVhstrJgVb34kCcCe";
  public static final String SDK_KEY = "6zbJLKGvwoZFRXWR3ZawuHkFx8SqrkpSwT5xXRQRNTUk";
  public static final String DEV_PATH = "/dev/video1";
  public static String ROOT_PATH;
  /**
   * 存放注册图的目录
   */
  public static final String SAVE_IMG_DIR = "register" + File.separator + "images";
  /**
   * 存放特征的目录
   */
  private static final String SAVE_FEATURE_DIR = "register" + File.separator + "features";

  public static File getFeatureDir(Context context) {
    if (ROOT_PATH == null) {
      ROOT_PATH = context.getFilesDir().getAbsolutePath();
    }
    //特征存储的文件夹
    File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
    if (!featureDir.exists()) {
      featureDir.mkdirs();
    }
    return featureDir;
  }

  public static File getImgDir(Context context) {
    if (ROOT_PATH == null) {
      ROOT_PATH = context.getFilesDir().getAbsolutePath();
    }
    //图片存储的文件夹
    File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
    if (!imgDir.exists()) {
      imgDir.mkdirs();
    }
    return imgDir;
  }
}
