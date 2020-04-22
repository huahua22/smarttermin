package com.xwr.smarttermin.arcface.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


public class Util {
	private static Handler mHandler = new Handler(Looper.getMainLooper());

	public static void runOnUIThread(Runnable r) {
		if (isRunOnUIThread()) {
			// 已经是主线程, 直接运行
			r.run();
		} else {
			// 如果是子线程, 借助handler让其运行在主线程
			mHandler.post(r);
		}
	}

	// 获取图片
	public static Drawable getDrawable(Context context, int id) {
		return ContextCompat.getDrawable(context, id);
	}

	// 获取颜色
	public static int getColor(Context context, int id) {
		return ContextCompat.getColor(context, id);
	}

	// /////////////////dip和px转换//////////////////////////
	public static int dip2px(Context context, float dip) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dip * density + 0.5f);
	}

	public static float px2dip(Context context, int px) {
		float density = context.getResources().getDisplayMetrics().density;
		return px / density;
	}

	// /////////////////获取屏幕高度和宽度//////////////////////////
	public static int getHeightPixels(Context context, double d) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int heightPixels = metrics.heightPixels;
		return (int) (heightPixels * d);
	}

	public static int getWidthPixels(Context context, double i) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int widthPixels = metrics.widthPixels;
		return (int) (widthPixels * i);
	}

	// /////////////////判断是否运行在主线程//////////////////////////
	public static boolean isRunOnUIThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	public static void showToast(final Context mContext, final String s) {
		runOnUIThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static int getDimen(Context context,int id){
		return (int) context.getResources().getDimension(id);
	}

	public static void getAndroiodScreenProperty(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;         // 屏幕宽度（像素）
		int height = dm.heightPixels;       // 屏幕高度（像素）
		float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
		// 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
		int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
		int screenHeight = (int) (height / density);// 屏幕高度(dp)


		Log.d("h_bl", "屏幕宽度（像素）：" + width);
		Log.d("h_bl", "屏幕高度（像素）：" + height);
		Log.d("h_bl", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
		Log.d("h_bl", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
		Log.d("h_bl", "屏幕宽度（dp）：" + screenWidth);
		Log.d("h_bl", "屏幕高度（dp）：" + screenHeight);
	}
}
