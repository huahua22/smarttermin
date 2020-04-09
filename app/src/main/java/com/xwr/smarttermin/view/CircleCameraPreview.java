package com.xwr.smarttermin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.xwr.smarttermin.util.BitmapUtil;
import com.xwr.smarttermin.util.FileUtil;
import com.xwr.smarttermin.util.MultipartEntityUtil;

import java.io.File;
import java.io.IOException;

import zz.yy.ucamir.cam.Camera;

/**
 * Create by xwr on 2020/4/3
 * Describe:相机圆形预览
 */
public class CircleCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private static final String TAG = "CircleCameraPreview";


  /**
   * 相机对象
   */
  private Camera camera;

  /**
   * 半径
   */
  private int radius;

  /**
   * 中心点坐标
   */
  private Point centerPoint;

  /**
   * 剪切路径
   */
  private Path clipPath;

  /**
   * 是否在预览
   */
  private boolean isPreviewing;

  /**
   * 是否已经设置过窗口尺寸
   */
  private boolean isSizeFitted = false;

  private IPictureListener mIPictureListener;

  public CircleCameraPreview(Context context) {
    super(context);
    init();
  }

  public CircleCameraPreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CircleCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  /**
   * 初始化
   */
  private void init() {
    this.setFocusable(true);
    this.setFocusableInTouchMode(true);
    getHolder().addCallback(this);
    clipPath = new Path();
    centerPoint = new Point();
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    // 坐标转换为实际像素
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    // 计算出圆形的中心点
    centerPoint.x = widthSize >> 1;
    centerPoint.y = heightSize >> 1;
    // 计算出最短的边的一半作为半径
    radius = (centerPoint.x > centerPoint.y) ? centerPoint.y : centerPoint.x;
    Log.i(TAG, "onMeasure: " + centerPoint.toString());
    clipPath.reset();
    clipPath.addCircle(centerPoint.x, centerPoint.y, radius, Path.Direction.CCW);
    setMeasuredDimension(widthSize, heightSize);
  }


  /**
   * 绘制
   *
   * @param canvas 画布
   */
  @Override
  public void draw(Canvas canvas) {
    //裁剪画布，并设置其填充方式
    if (Build.VERSION.SDK_INT >= 26) {
      canvas.clipPath(clipPath);
    } else {
      canvas.clipPath(clipPath, Region.Op.REPLACE);
    }
    super.draw(canvas);
  }


  /**
   * 根据相机旋转动态修改view的尺寸
   * <p>
   * 以抵消失真的现象
   *
   * @param rotate 旋转角度
   */
  private void changeViewSize(int rotate) {
    if (isSizeFitted) {
      // 如果屏幕尺寸已经重设过那么，则认为不需要再设置
      return;
    }
    isSizeFitted = true;

    DisplayMetrics metrics = new DisplayMetrics();
    ((WindowManager) getContext()
      .getSystemService(Context.WINDOW_SERVICE))
      .getDefaultDisplay().getMetrics(metrics);

    ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
    if (rotate == 0) {
      layoutParams.height = layoutParams.width * 3 / 4;
    } else {
      layoutParams.width = layoutParams.width * 3 / 4;
    }
    this.setLayoutParams(layoutParams);
  }


  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    try {
      camera = Camera.open(4);
      camera.setDisplayOrientation(180);
      camera.setPreviewDisplay(holder);
      camera.startPreview();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }


  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    camera.stopPreview();
    camera.release();
    camera = null;
  }

  public void takePhoto() {
    camera.takePicture(null, null, new Camera.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] bytes, Camera camera) {
        final File file = BitmapUtil.getFileFromBytes(bytes, FileUtil.getSDPath() + "/pic.jpg");
        try {
          new Thread() {
            @Override
            public void run() {
              String str = null;
              try {
                str = MultipartEntityUtil.post("http://miyeehealth.com:8080/intelligent/uploadFile", file);
              } catch (IOException e) {
                e.printStackTrace();
              }
              mIPictureListener.onPictureData(str);
              System.out.println("str111111--->>>" + str);

            }
          }.start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  /**
   * 计算最大公约数
   *
   * @param a
   * @param b
   * @return 最大公约数
   */
  private int gcd(int a, int b) {
    if (b == 0)
      return a;
    return gcd(b, a % b);
  }

  public interface IPictureListener {
    public void onPictureData(String data);
  }

  public void setIPictureListener(IPictureListener listener) {
    mIPictureListener = listener;
  }


}