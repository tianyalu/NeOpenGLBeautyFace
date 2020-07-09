package com.sty.ne.opengl.beautyface.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.sty.ne.opengl.beautyface.MyGLRender;


public class MyGLSurfaceView extends GLSurfaceView {
    private Speed mSpeed = Speed.NORMAL;

    public enum Speed {
        EXTRA_SLOW, SLOW, NORMAL, FAST, EXTRA_FAST
    }

    private MyGLRender mRender;

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGL();
    }

    public void initGL() {
        setEGLContextClientVersion(2); //设置egl版本
        mRender = new MyGLRender(this);
        setRenderer(mRender); //设置自定义渲染器
        setRenderMode(RENDERMODE_WHEN_DIRTY); //设置按需渲染模式
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mRender.surfaceDestroyed();
    }

    public void setSpeedMode(Speed speed) {
        mSpeed = speed;
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        float speed = 1.0f;
        switch (mSpeed) {
            case EXTRA_SLOW:
                speed = 0.3f;
                break;
            case SLOW:
                speed = 0.5f;
                break;
            case NORMAL:
                speed = 1.0f;
                break;
            case FAST:
                speed = 1.5f;
                break;
            case EXTRA_FAST:
                speed = 3.0f;
                break;
            default:
                break;
        }
        mRender.startRecording(speed);
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        mRender.stopRecording();
    }

    /**
     * 开启大眼特效
     *
     * @param isChecked
     */
    public void enableBigEyes(boolean isChecked) {
        mRender.enableBigEye(isChecked);
    }

    public void enableStick(boolean isChecked) {
        mRender.enableStick(isChecked);
    }

    public void enableBeauty(boolean isChecked) {
        mRender.enableBeauty(isChecked);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //do nothing
                break;
            case MotionEvent.ACTION_UP:
                switchCamera();
                break;
            default:
                break;
        }
        return true;
    }

    public void switchCamera() {
        mRender.switchCamera();
    }
}
