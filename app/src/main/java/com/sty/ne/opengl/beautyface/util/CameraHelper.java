package com.sty.ne.opengl.beautyface.util;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Iterator;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private static final String TAG = CameraHelper.class.getSimpleName();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 480;
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mCameraID;
    private int mWidth;
    private int mHeight;
    private byte[] cameraBuffer;
    private byte[] cameraBuffer_;

    private Activity mActivity;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private Camera.PreviewCallback mPreviewCallback;
    private OnChangedSizeListener mOnChangedSizeListener;
    private int mRotation;
    private SurfaceTexture mSurfaceTexture;

    public CameraHelper(Activity activity) {
        this.mCameraID = CAMERA_ID;
        this.mWidth = WIDTH;
        this.mHeight = HEIGHT;
        this.mActivity = activity;
    }

    public CameraHelper(int mCameraID, int mWidth, int mHeight, Activity mActivity) {
        this.mCameraID = mCameraID;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mActivity = mActivity;
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;
        this.mSurfaceHolder.addCallback(this);
    }

    //虽然执行了setPreviewOrientation(parameters)，屏幕中的图像正常了 --> 这里不做旋转，由底层代码来旋转
    //但此时回调中data中的数据仍然是没有旋转过的
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        Log.i(TAG, "onPreviewFrame");
//        switch (mRotation) {
//            case Surface.ROTATION_0:
//                rotation90(data);
//                break;
//            case Surface.ROTATION_90: //横屏，左边是头部（home键在右边）
//                break;
//            case Surface.ROTATION_270: //横屏，头部在右边
//                break;
//            default:
//                break;
//        }
        if (mPreviewCallback != null) {
//            mPreviewCallback.onPreviewFrame(cameraBuffer_, camera);
            mPreviewCallback.onPreviewFrame(data, camera);
        }
        camera.addCallbackBuffer(cameraBuffer);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated");
    }

    /**
     * 屏幕状态改变时回调
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged");

        //释放摄像头
        stopPreview();
        //开启摄像头
        startPreview(mSurfaceTexture);
    }

    /**
     * 销毁时回调
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed");
        stopPreview();
    }

    public void rePreview() {
        stopPreview();
        startPreview(mSurfaceTexture);
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        try {
            //获取Camera对象
            mCamera = Camera.open(mCameraID);
            //配置Camera的属性
            Camera.Parameters parameters = mCamera.getParameters();
            //自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //设置预览数据格式为nv21
            parameters.setPictureFormat(ImageFormat.NV21);
            //这里是摄像头预览尺寸（宽、高）
            setPreviewSize(parameters);
            //设置摄像头图像传感器的角度、方向
            setPreviewOrientation(parameters);
            mCamera.setParameters(parameters);
            //可以看出无论是哪种排列方式，YUV420的数据量都为: wh+w/2h/2+w/2h/2 即为wh*3/2
            //参考：[NV21与I420](https://www.jianshu.com/p/9ad01d4f824c)
            cameraBuffer = new byte[mWidth * mHeight * 3 / 2];
            cameraBuffer_ = new byte[mWidth * mHeight * 3 / 2];
            //数据缓冲区
            mCamera.addCallbackBuffer(cameraBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);  //onPreviewFrame()
            //设置预览画面
            //mCamera.setPreviewDisplay(mSurfaceHolder);
            //离屏渲染
            mCamera.setPreviewTexture(surfaceTexture);
            //开启预览
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        //获取摄像头的宽、高
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        Log.i(TAG, "Camera支持： " + size.width + " x " + size.height);
        //选择一个与设置的差距最小的支持分辨率
        int m = Math.abs(size.width * size.height - mWidth * mHeight);
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        //遍历
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
//            Log.i(TAG, "遍历支持： " + next.width + " x " + next.height);
            int n = Math.abs(next.height * next.width - mWidth * mHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }
        mWidth = size.width;
        mHeight = size.height;
        parameters.setPreviewSize(mWidth, mHeight);
        Log.i(TAG, "预览分辨率： " + mWidth + " x " + mHeight);
    }

    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        mRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mHeight, mWidth);
                }
                break;
            case Surface.ROTATION_90: //横屏，左边是头部（home键在右边）
                degrees = 90;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mWidth, mHeight);
                }
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mHeight, mWidth);
                }
                break;
            case Surface.ROTATION_270: //横屏，右边边是头部
                degrees = 270;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mWidth, mHeight);
                }
                break;
            default:
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置角度，参考源码注释
        mCamera.setDisplayOrientation(result);
    }

    public void stopPreview() {
        if(mCamera != null) {
            //预览数据回调接口
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if(mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview(mSurfaceTexture);
    }

    public int getmCameraID() {
        return mCameraID;
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    //参考：[NV21与I420](https://www.jianshu.com/p/9ad01d4f824c)
    //这里仅仅是旋转或旋转+镜像操作，并没有将NV21转为I420
    private void rotation90(byte[] data) {
        int index = 0;
        int ySize = mWidth * mHeight;
        // u 和 v
        int uvHeight = mHeight / 2;
        //后置摄像头顺时针旋转90度
        if(mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //将y的数据转换后放入新的byte数组
            for (int i = 0; i < mWidth; i++) {
                for (int j = mHeight - 1; j >= 0; j--) {
                    cameraBuffer_[index++] = data[mWidth * j + i];
                }
            }

            //每次处理两个数组
            for (int i = 0; i < mWidth; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    //v
                    cameraBuffer_[index++] = data[ySize + mWidth * j + i];
                    //u
                    cameraBuffer_[index++] = data[ySize + mWidth * j + i + 1];
                }
            }
        }else {
            //逆时针旋转90度
//            for (int i = 0; i < mWidth; i++) {
//                for (int j = 0; j < mHeight; j++) {
//                    cameraBuffer_[index++] = data[mWidth * j + mWidth - 1 - i];
//                }
//            }
//            //  u v
//            for (int i = 0; i < mWidth; i += 2) {
//                for (int j = 0; j < uvHeight; j++) {
//                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i - 1];
//                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i];
//                }
//            }

            //旋转并镜像
            for (int i = 0; i < mWidth; i++) {
                for (int j = mHeight - 1; j >= 0; j--) {
                    cameraBuffer_[index++] = data[mWidth * j + mWidth -1 -i];
                }
            }
            // u v
            for (int i = 0; i < mWidth; i += 2) {
                for (int j = uvHeight -1; j >= 0; j--) {
                    //v
                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth -1 -i -1];
                    //u
                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth -1 -i];
                }
            }
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.mPreviewCallback = previewCallback;
    }

    public void setOnChangedSizeListener(OnChangedSizeListener listener) {
        this.mOnChangedSizeListener = listener;
    }

    public interface OnChangedSizeListener {
        void onChanged(int w, int h);
    }
}
