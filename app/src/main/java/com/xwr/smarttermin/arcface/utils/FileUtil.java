package com.xwr.smarttermin.arcface.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

public class FileUtil {
	public static String getPath(Context context, Uri uri) {
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor == null) return null;
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String s = cursor.getString(column_index);
		cursor.close();
		return s;
	}

	public static boolean mkDir(String dir) {
		File file = new File(dir);
		return file.exists() || file.mkdir();
	}

	public static void deleteDirWithFile(String path) {
		File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) return;
		for (File file : dir.listFiles()) {
			if (file.isFile()) file.delete(); // 删除所有文件
			else if (file.isDirectory()) deleteDirWithFile(path); // 递规的方式删除文件夹
		}
	}

	public static void deleteDirWithFile(File dir) {
		if (!dir.exists() || !dir.isDirectory()) return;
		for (File file : dir.listFiles()) {
			if (file.isFile()) file.delete(); // 删除所有文件
			else if (file.isDirectory()) deleteDirWithFile(file); // 递规的方式删除文件夹
		}
	}
}
