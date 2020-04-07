package com.xwr.smarttermin.bean;

import java.io.Serializable;

/**
 * Create by xwr on 2020/4/7
 * Describe:
 */
public class FileDetails implements Serializable {
  String fileName;
  String filePath;
  String fileSize;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }
}
