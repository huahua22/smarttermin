package com.xwr.smarttermin.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Create by xwr on 2020/4/7
 * Describe:
 */
public class MultipartEntityUtil {
  public static String post(String actionUrl, File file) throws IOException {
    String result = null;
    String BOUNDARY = java.util.UUID.randomUUID().toString();
    String PREFIX = "--", LINEND = "\r\n";
    String MULTIPART_FROM_DATA = "multipart/form-data";
    String CHARSET = "UTF-8";

    URL uri = new URL(actionUrl);
    HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
    conn.setReadTimeout(5 * 1000); // 缓存的最长时间
    conn.setDoInput(true);// 允许输入
    conn.setDoOutput(true);// 允许输出
    conn.setUseCaches(false); // 不允许使用缓存
    conn.setRequestMethod("POST");
    conn.setRequestProperty("connection", "keep-alive");
    conn.setRequestProperty("Charsert", "UTF-8");
    conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

    DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
    // 发送文件数据
    if (file != null)

    {
      StringBuilder sb1 = new StringBuilder();
      sb1.append(PREFIX);
      sb1.append(BOUNDARY);
      sb1.append(LINEND);
      sb1.append("Content-Disposition: form-data;name=\"file\";filename=\"" + FileUtil.getSDPath() + "/pic.jpg" + "\"" + LINEND);
      sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
      sb1.append(LINEND);
      outStream.write(sb1.toString().getBytes());
      InputStream is = new FileInputStream(file);
      byte[] buffer = new byte[1024];
      int len = 0;
      while ((len = is.read(buffer)) != -1) {
        outStream.write(buffer, 0, len);
      }

      is.close();
      outStream.write(LINEND.getBytes());
    }
   
    // 请求结束标志
    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
    outStream.write(end_data);
    outStream.flush();
    // 得到响应码
    int res = conn.getResponseCode();
    System.out.println("response----->>" + conn.getResponseMessage());
    InputStream in = conn.getInputStream();
    if (res == 200) {
      result = convertStreamToString(in);
    }
    outStream.close();
    conn.disconnect();
    return result;

  }

  public static String convertStreamToString(InputStream is) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    is.close();
    System.out.println("sb-->>" + sb.toString());
    return sb.toString();
  }


}
