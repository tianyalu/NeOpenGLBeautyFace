package com.sty.ne.opengl.beautyface.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


import com.sty.ne.opengl.beautyface.util.CameraHelper;

import androidx.annotation.NonNull;

/**
 * 人脸与关键点的定位追踪api类
 */
public class FaceTrack {
    private static final String TAG = FaceTrack.class.getSimpleName();

    static {
        System.loadLibrary("native-lib");
    }

    private CameraHelper mCameraHelper;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private long self;
    //结果
    private Face mFace;

    /**
     *
     * @param face_model 人脸检测的模型文件路径
     * @param seeta 中科院人脸关键点检测的模型文件路径
     * @param cameraHelper
     */
    public FaceTrack(String face_model, String seeta, CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
        self = native_create(face_model, seeta);

        mHandlerThread = new HandlerThread("FaceTrack");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //子线程耗时再久也不会对其他地方（如：opengl绘制线程）产生影响
                synchronized (FaceTrack.this) {
//                    Log.d("sty", "native_detector------>");
                    //定位线程中检测
                    mFace = native_detector(self, (byte[]) msg.obj, mCameraHelper.getmCameraID(),
                            mCameraHelper.getmWidth(), mCameraHelper.getmHeight());
                    if(mFace != null) {
                        Log.d(TAG, mFace.toString());
                    }
                }
            }
        };
    }

    public void startTrack() {
        native_start(self);
    }

    public void stopTrack() {
        synchronized (this) {
            mHandlerThread.quitSafely();
            mHandler.removeCallbacksAndMessages(null);
            native_stop(self);
            self = 0;
        }
    }

    public void detector(byte[] data) {
        //把积压的11号文件移除掉
        mHandler.removeMessages(11);
        //加入新的11号任务
        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public Face getFace() {
        return mFace;
    }

    private native long native_create(String face_model, String seeta);

    private native void native_start(long self);

    private native void native_stop(long self);

    private native Face native_detector(long self, byte[] data, int cameraId, int width, int height);
}
