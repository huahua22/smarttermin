package com.xwr.smarttermin.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by xwr on 2020/4/14
 * Describe:
 */
public class TypeUtil {
  public static byte[] hexString2Bytes(String hex) {

    if ((hex == null) || (hex.equals(""))) {
      return null;
    } else if (hex.length() % 2 != 0) {
      return null;
    } else {
      hex = hex.toUpperCase();
      int len = hex.length() / 2;
      byte[] b = new byte[len];
      char[] hc = hex.toCharArray();
      for (int i = 0; i < len; i++) {
        int p = 2 * i;
        b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
      }
      return b;
    }

  }

  /*
   * 字符转换为字节
   */
  private static byte charToByte(char c) {
    return (byte) "0123456789ABCDEF".indexOf(c);
  }

  public String utfToString(byte[] data) {

    String str = null;

    try {
      str = new String(data, "GBK");
    } catch (UnsupportedEncodingException e) {
    }
    return str;

  }

  public static boolean isMessyCode(String strName) {
    try {
      Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
      Matcher m = p.matcher(strName);
      String after = m.replaceAll("");
      String temp = after.replaceAll("\\p{P}", "");
      char[] ch = temp.trim().toCharArray();

      int length = (ch != null) ? ch.length : 0;
      for (int i = 0; i < length; i++) {
        char c = ch[i];
        if (!Character.isLetterOrDigit(c)) {
          String str = "" + ch[i];
          if (!str.matches("[\u4e00-\u9fa5]+")) {
            return true;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }
}
