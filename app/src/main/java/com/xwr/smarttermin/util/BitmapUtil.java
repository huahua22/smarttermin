package com.xwr.smarttermin.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Create by xwr on 2020/2/25
 * Describe:图片处理工具类
 */
public class BitmapUtil {

  /**
   * byte数组转为bitmap
   *
   * @param bytes
   * @param opts
   * @return
   */
  public static Bitmap getPicFromBytes(byte[] bytes,
    BitmapFactory.Options opts) {
    if (bytes != null)
      if (opts != null)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
          opts);
      else
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    return null;
  }

  /**
   * @param b          图片byte数组
   * @param outputFile 保存文件路径
   * @return 文件
   */
  public static File getFileFromBytes(byte[] b, String outputFile) {
    BufferedOutputStream stream = null;
    File file = null;
    try {
      file = new File(outputFile);
      FileOutputStream fstream = new FileOutputStream(file);
      stream = new BufferedOutputStream(fstream);
      stream.write(b);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
    return file;
  }

  /**
   * bitmap水平镜面翻转
   *
   * @param bmp
   * @return bitmap
   */
  public static Bitmap convertBmp(Bitmap bmp) {
    int w = bmp.getWidth();
    int h = bmp.getHeight();
    Matrix matrix = new Matrix();
    matrix.postScale(-1, 1); // 镜像水平翻转
    Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
    return convertBmp;
  }

  /**
   * bitmap 转为byte[]
   *
   * @param bm
   * @return
   */
  public static byte[] BitmapToBytes(Bitmap bm) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    return baos.toByteArray();
  }
}
