package com.xwr.smarttermin.arcface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.util.ImageUtils;
import com.xwr.smarttermin.arcface.common.ThreadManager;
import com.xwr.smarttermin.arcface.model.CompareResult;
import com.xwr.smarttermin.arcface.model.DrawInfo;
import com.xwr.smarttermin.arcface.model.FacePreviewInfo;
import com.xwr.smarttermin.arcface.model.LivenessType;
import com.xwr.smarttermin.arcface.model.RecognizeColor;
import com.xwr.smarttermin.arcface.model.RequestFeatureStatus;
import com.xwr.smarttermin.arcface.model.RequestLivenessStatus;
import com.xwr.smarttermin.arcface.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zz.yy.ucamir.cam.Camera;
import zz.yy.ucamir.cam.CameraInfo;
import zz.yy.ucamir.cam.Size;

public class ArcFacePresenter {
  private final Context mContext;
  private final CameraView mCameraView;
  private DrawHelper mDrawHelper;
  private boolean isFaceRecoging = false;
  /**
   * 注册人脸状态码，准备注册
   */
  private static final int REGISTER_STATUS_READY = 0;
  /**
   * 注册人脸状态码，注册中
   */
  private static final int REGISTER_STATUS_PROCESSING = 1;
  /**
   * 注册人脸状态码，注册结束（无论成功失败）
   */
  private static final int REGISTER_STATUS_DONE = 2;

  private int registerStatus = REGISTER_STATUS_DONE;

  /**
   * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
   */
  private FaceEngine ftEngine;
  /**
   * 用于特征提取的引擎
   */
  private FaceEngine frEngine;
  /**
   * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
   */
  private FaceEngine flEngine;

  private int ftInitCode = -1;
  private int frInitCode = -1;
  private int flInitCode = -1;
  String name = null;
  private static final int MAX_DETECT_NUM = 10;
  private static final String TAG = "ArcFace";
  private FaceHelper faceHelper;
  private Size previewSize;
  private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();
  private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
  private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

  private List<CompareResult> compareResultList = new ArrayList<>();
  ;
  /**
   * 失败重试间隔时间（ms）
   */
  private static final long FAIL_RETRY_INTERVAL = 1000;
  /**
   * 出错重试最大次数
   */
  private static final int MAX_RETRY_TIME = 3;
  /**
   * 当FR成功，活体未成功时，FR等待活体的时间
   */
  private static final int WAIT_LIVENESS_INTERVAL = 100;
  private static final float SIMILAR_THRESHOLD = 0.8F;
  private boolean livenessDetect = false;
  private IFaceResultListener mIFaceResultListener = null;

  public ArcFacePresenter(Context context, CameraView cameraView) {
    this.mContext = context;
    this.mCameraView = cameraView;

    //本地人脸库初始化
    FaceServer.getInstance().init(mContext);
  }

  public void initEngine() {
    ftEngine = new FaceEngine();
    ftInitCode = ftEngine.init(mContext, DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY,
      16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

    frEngine = new FaceEngine();
    frInitCode = frEngine.init(mContext, DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY,
      16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

    flEngine = new FaceEngine();
    flInitCode = flEngine.init(mContext, DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY,
      16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);


    VersionInfo versionInfo = new VersionInfo();
    ftEngine.getVersion(versionInfo);
    Log.i(TAG, "initEngine:  init: " + ftInitCode + "  version:" + versionInfo);

    if (ftInitCode != ErrorInfo.MOK) {
      mCameraView.addDebugInfo("initEngine: " + ftInitCode);
    }
    if (frInitCode != ErrorInfo.MOK) {
      mCameraView.addDebugInfo("initEngine: " + frInitCode);
    }
    if (flInitCode != ErrorInfo.MOK) {
      mCameraView.addDebugInfo("initEngine: " + flInitCode);
    }

  }

  private final FaceListener faceListener = new FaceListener() {
    @Override
    public void onFail(Exception e) {
      Log.e(TAG, "onFail: " + e.getMessage());
    }

    //请求FR的回调
    @Override
    public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
      //FR成功
      if (faceFeature != null) {
        Integer liveness = livenessMap.get(requestId);
        if (!livenessDetect) {
          searchFace(faceFeature, requestId);
        }
        //活体检测通过，搜索特征
        else if (liveness != null && liveness == LivenessInfo.ALIVE) {
          searchFace(faceFeature, requestId);
        }
        //活体检测未出结果，或者非活体，延迟执行该函数
        else {
          if (requestFeatureStatusMap.containsKey(requestId)) {
            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
              .subscribe(new Observer<Long>() {
                Disposable disposable;

                @Override
                public void onSubscribe(Disposable d) {
                  disposable = d;
                  getFeatureDelayedDisposables.add(disposable);
                }

                @Override
                public void onNext(Long aLong) {
                  onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                  getFeatureDelayedDisposables.remove(disposable);
                }
              });
          }
        }

      }
      //特征提取失败
      else {
        if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
          extractErrorRetryMap.put(requestId, 0);

          String msg;
          // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
          if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
            //人脸置信度低
          }
          faceHelper.setName(requestId, "UNKNOWN");
          // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
          requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
          retryRecognizeDelayed(requestId);
        } else {
          requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
        }
      }
    }

    @Override
    public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
      if (livenessInfo != null) {
        int liveness = livenessInfo.getLiveness();
        livenessMap.put(requestId, liveness);
        // 非活体，重试
        if (liveness == LivenessInfo.NOT_ALIVE) {
          faceHelper.setName(requestId, "NOT_ALIVE");
          // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
          retryLivenessDetectDelayed(requestId);
        }
      } else {
        if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
          livenessErrorRetryMap.put(requestId, 0);
          String msg;
          // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
          if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
            //人脸置信度低
          }
          faceHelper.setName(requestId, "UNKNOWN");
          retryLivenessDetectDelayed(requestId);
        } else {
          livenessMap.put(requestId, LivenessInfo.UNKNOWN);
        }
      }
    }


  };

  public void startFaceRecog(Camera mCamera, int camId, FaceRectView mFaceRectView, Button btRegister, SurfaceHolder mSurfaceHolder) {
    System.out.println("--->>>start face recog");
    if (mCamera != null && mCamera.isWorking()) {
      if (isFaceRecoging) {
        ThreadManager.getInstance().clear();
        mCamera.setPreviewCallback(null);
        mFaceRectView.stopFace();
        isFaceRecoging = false;
        btRegister.setVisibility(View.INVISIBLE);
      } else {
        mFaceRectView.setVisibility(View.VISIBLE);
        btRegister.setVisibility(View.VISIBLE);

        Size lastPreviewSize = previewSize;
        Camera.Parameters parameters = mCamera.getParameters();
        previewSize = parameters.getPictureSize();
        mDrawHelper = new DrawHelper(previewSize.width, previewSize.height, mSurfaceHolder.getSurfaceFrame().width(), mSurfaceHolder.getSurfaceFrame().height(), 0, 1, true, false, false);

        if (faceHelper == null || lastPreviewSize == null || lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
          Integer trackedFaceCount = null;
          // 记录切换时的人脸序号
          if (faceHelper != null) {
            trackedFaceCount = faceHelper.getTrackedFaceCount();
            faceHelper.release();
          }
          faceHelper = new FaceHelper.Builder()
            .ftEngine(ftEngine)
            .frEngine(frEngine)
            .flEngine(flEngine)
            .frQueueSize(MAX_DETECT_NUM)
            .flQueueSize(MAX_DETECT_NUM)
            .previewSize(previewSize)
            .faceListener(faceListener)
            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(mContext) : trackedFaceCount)
            .build();
        }


        CameraInfo caminfo = new CameraInfo();
        mCamera.setPreviewCallback((bytes, camera) -> {
          BitmapFactory.Options opts1 = new BitmapFactory.Options();
          opts1.inPreferredConfig = Bitmap.Config.ARGB_8888;
          Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts1);
          int width = bitmap.getWidth();
          int height = bitmap.getHeight();

          Camera.getCameraInfo(camId, caminfo);
          Matrix matrix = new Matrix();
          matrix.setRotate(caminfo.orientation);
          bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
          byte[] byteData = ImageUtils.bitmapToBgr24(bitmap);
          //          byte[] byteData = bitmapToNv21(bitmap, width, height);
          bitmap.recycle();

          int format = FaceEngine.CP_PAF_BGR24;
          List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(byteData, format);
          if (facePreviewInfoList != null && mFaceRectView != null && mDrawHelper != null) {
            //开始画
            drawPreviewInfo(facePreviewInfoList, mFaceRectView);
          }
          registerFace(byteData, facePreviewInfoList, format);//注册人脸
          clearLeftFace(facePreviewInfoList);//删除已经离开的人脸

          if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
              Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
              /**
               * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
               */
              if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                if (liveness == null || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                  livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                  faceHelper.requestFaceLiveness(byteData, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, format, facePreviewInfoList.get(i)
                    .getTrackId(), LivenessType.RGB);
                }
              }
              /**
               * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
               * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
               */
              if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                faceHelper.requestFaceFeature(byteData, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, format, facePreviewInfoList.get(i).getTrackId());
              }
            }
          }
        });
        isFaceRecoging = true;
      }
    }
  }

  private void registerFace(final byte[] data, final List<FacePreviewInfo> facePreviewInfoList, int format) {
    if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
      registerStatus = REGISTER_STATUS_PROCESSING;
      Observable.create(new ObservableOnSubscribe<Boolean>() {
        @Override
        public void subscribe(ObservableEmitter<Boolean> emitter) {
          boolean success = false;
          if (format == FaceEngine.CP_PAF_NV21)
            success = FaceServer.getInstance()
              .registerNv21(mContext, data.clone(), previewSize.width, previewSize.height, facePreviewInfoList.get(0).getFaceInfo(), "已注册用户 " + faceHelper.getTrackedFaceCount());
          else
            success = FaceServer.getInstance().registerBgr24(mContext, data.clone(), previewSize.width, previewSize.height, "已注册用户 " + faceHelper.getTrackedFaceCount());
          emitter.onNext(success);
        }
      })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Boolean>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onNext(Boolean success) {
            String result = success ? "注册成功!" : "注册失败!";
            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            registerStatus = REGISTER_STATUS_DONE;
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(mContext, "注册失败!", Toast.LENGTH_SHORT).show();
            registerStatus = REGISTER_STATUS_DONE;
          }

          @Override
          public void onComplete() {

          }
        });
    }
  }

  private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList, FaceRectView mFaceRectView) {
    List<DrawInfo> drawInfoList = new ArrayList<>();
    for (int i = 0; i < facePreviewInfoList.size(); i++) {
      String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
      Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
      Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

      // 根据识别结果和活体结果设置颜色
      int color = RecognizeColor.COLOR_UNKNOWN;
      if (recognizeStatus != null) {
        if (recognizeStatus == RequestFeatureStatus.FAILED) {
          color = RecognizeColor.COLOR_UNKNOWN;
          mIFaceResultListener.onResult(0);
        }
        if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
          color = RecognizeColor.COLOR_SUCCESS;
          mIFaceResultListener.onResult(1);
        }
      }
      if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
        color = RecognizeColor.COLOR_UNKNOWN;
        mIFaceResultListener.onResult(0);
      }

      drawInfoList.add(new DrawInfo(mDrawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
        GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
        name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
    }
    mDrawHelper.draw(mFaceRectView, drawInfoList);
  }

  private void searchFace(final FaceFeature frFace, final Integer requestId) {
    Observable.create(new ObservableOnSubscribe<CompareResult>() {
      @Override
      public void subscribe(ObservableEmitter<CompareResult> emitter) {
        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
        emitter.onNext(compareResult);

      }
    })
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Observer<CompareResult>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(CompareResult compareResult) {
          if (compareResult == null || compareResult.getUserName() == null) {
            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
            faceHelper.setName(requestId, "访客 " + requestId);
            System.out.println("---->>>访客" + requestId);
            return;
          }

          if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
            boolean isAdded = false;
            if (compareResultList == null) {
              requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
              faceHelper.setName(requestId, "访客 " + requestId);
              return;
            }
            for (CompareResult compareResult1 : compareResultList) {
              if (compareResult1.getTrackId() == requestId) {
                isAdded = true;
                break;
              }
            }
            if (!isAdded) {
              //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
              if (compareResultList.size() >= MAX_DETECT_NUM) {
                compareResultList.remove(0);
              }
              //添加显示人员时，保存其trackId
              compareResult.setTrackId(requestId);
              compareResultList.add(compareResult);
            }
            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
            faceHelper.setName(requestId, compareResult.getUserName());

          } else {
            System.out.println("---->>>访客" + requestId);
            faceHelper.setName(requestId, "未注册");
            retryRecognizeDelayed(requestId);
          }
        }

        @Override
        public void onError(Throwable e) {
          if (faceHelper != null) {
            System.out.println("---->>>访客" + requestId);
            faceHelper.setName(requestId, "未注册");
          }

          retryRecognizeDelayed(requestId);
        }

        @Override
        public void onComplete() {

        }
      });
  }

  /**
   * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
   *
   * @param requestId 人脸ID
   */
  private void retryLivenessDetectDelayed(final Integer requestId) {
    Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
      .subscribe(new Observer<Long>() {
        Disposable disposable;

        @Override
        public void onSubscribe(Disposable d) {
          disposable = d;
          delayFaceTaskCompositeDisposable.add(disposable);
        }

        @Override
        public void onNext(Long aLong) {

        }

        @Override
        public void onError(Throwable e) {
          e.printStackTrace();
        }

        @Override
        public void onComplete() {
          // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
          if (livenessDetect) {
            faceHelper.setName(requestId, "访客 " + requestId);
          }

          livenessMap.put(requestId, LivenessInfo.UNKNOWN);
          delayFaceTaskCompositeDisposable.remove(disposable);
        }
      });
  }

  /**
   * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
   *
   * @param requestId 人脸ID
   */
  private void retryRecognizeDelayed(final Integer requestId) {
    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
    Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
      .subscribe(new Observer<Long>() {
        Disposable disposable;

        @Override
        public void onSubscribe(Disposable d) {
          disposable = d;
          delayFaceTaskCompositeDisposable.add(disposable);
        }

        @Override
        public void onNext(Long aLong) {

        }

        @Override
        public void onError(Throwable e) {
          e.printStackTrace();
        }

        @Override
        public void onComplete() {
          // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
          if (faceHelper != null)
            faceHelper.setName(requestId, "访客 " + requestId);
          requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
          delayFaceTaskCompositeDisposable.remove(disposable);
        }
      });
  }

  /**
   * 将map中key对应的value增1回传
   *
   * @param countMap map
   * @param key      key
   * @return 增1后的value
   */
  public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
    if (countMap == null) {
      return 0;
    }
    Integer value = countMap.get(key);
    if (value == null) {
      value = 0;
    }
    countMap.put(key, ++value);
    return value;
  }

  /**
   * 删除已经离开的人脸
   *
   * @param facePreviewInfoList 人脸和trackId列表
   */
  private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
    if (compareResultList != null) {
      for (int i = compareResultList.size() - 1; i >= 0; i--) {
        if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
          compareResultList.remove(i);
        }
      }
    }
    if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
      requestFeatureStatusMap.clear();
      livenessMap.clear();
      livenessErrorRetryMap.clear();
      extractErrorRetryMap.clear();
      if (getFeatureDelayedDisposables != null) {
        getFeatureDelayedDisposables.clear();
      }
      return;
    }
    Enumeration<Integer> keys = requestFeatureStatusMap.keys();
    while (keys.hasMoreElements()) {
      int key = keys.nextElement();
      boolean contained = false;
      for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
        if (facePreviewInfo.getTrackId() == key) {
          contained = true;
          break;
        }
      }
      if (!contained) {
        requestFeatureStatusMap.remove(key);
        livenessMap.remove(key);
        livenessErrorRetryMap.remove(key);
        extractErrorRetryMap.remove(key);
      }
    }


  }

  /**
   * Bitmap转化为ARGB数据，再转化为NV21数据
   *
   * @param src    传入的Bitmap，格式为{@link Bitmap.Config#ARGB_8888}
   * @param width  NV21图像的宽度
   * @param height NV21图像的高度
   * @return nv21数据
   */
  public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
    if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
      int[] argb = new int[width * height];
      src.getPixels(argb, 0, width, 0, 0, width, height);
      return argbToNv21(argb, width, height);
    } else {
      return null;
    }
  }

  /**
   * ARGB数据转化为NV21数据
   *
   * @param argb   argb数据
   * @param width  宽度
   * @param height 高度
   * @return nv21数据
   */
  private static byte[] argbToNv21(int[] argb, int width, int height) {
    int frameSize = width * height;
    int yIndex = 0;
    int uvIndex = frameSize;
    int index = 0;
    byte[] nv21 = new byte[width * height * 3 / 2];
    for (int j = 0; j < height; ++j) {
      for (int i = 0; i < width; ++i) {
        int R = (argb[index] & 0xFF0000) >> 16;
        int G = (argb[index] & 0x00FF00) >> 8;
        int B = argb[index] & 0x0000FF;
        int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
        int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
        int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
        nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
        if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
          nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
          nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
        }

        ++index;
      }
    }
    return nv21;
  }

  public void onDestroy() {
    unInitEngine();
    if (faceHelper != null) {
      ConfigUtil.setTrackedFaceCount(mContext, faceHelper.getTrackedFaceCount());
      faceHelper.release();
      faceHelper = null;
    }
    if (getFeatureDelayedDisposables != null) {
      getFeatureDelayedDisposables.clear();
    }
    if (delayFaceTaskCompositeDisposable != null) {
      delayFaceTaskCompositeDisposable.clear();
    }

    FaceServer.getInstance().unInit();
  }

  /**
   * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
   */
  private void unInitEngine() {
    if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
      synchronized (ftEngine) {
        int ftUnInitCode = ftEngine.unInit();
        Log.i(TAG, "unInitEngine: " + ftUnInitCode);
      }
    }
    if (frInitCode == ErrorInfo.MOK && frEngine != null) {
      synchronized (frEngine) {
        int frUnInitCode = frEngine.unInit();
        Log.i(TAG, "unInitEngine: " + frUnInitCode);
      }
    }
    if (flInitCode == ErrorInfo.MOK && flEngine != null) {
      synchronized (flEngine) {
        int flUnInitCode = flEngine.unInit();
        Log.i(TAG, "unInitEngine: " + flUnInitCode);
      }
    }
  }

  public void onClickRegisterFace() {
    if (registerStatus == REGISTER_STATUS_DONE) {
      registerStatus = REGISTER_STATUS_READY;
    }
  }


  public void setOnFaceResultListener(IFaceResultListener listener) {
    mIFaceResultListener = listener;
  }

}
