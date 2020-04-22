package com.xwr.smarttermin.arcface.view;

import android.view.SurfaceHolder;

/*
 * created by ：cjw
 * date ：2018/5/9 0009
 */
public abstract class SurfaceCallback implements SurfaceHolder.Callback {
	@Override
	public abstract void surfaceCreated(SurfaceHolder surfaceHolder);

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

	}
}
