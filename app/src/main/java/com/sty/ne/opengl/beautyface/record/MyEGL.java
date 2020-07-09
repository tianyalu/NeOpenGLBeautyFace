package com.sty.ne.opengl.beautyface.record;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;


import com.sty.ne.opengl.beautyface.filter.ScreenFilter;

import static android.opengl.EGL14.EGL_ALPHA_SIZE;
import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_DEFAULT_DISPLAY;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_NO_CONTEXT;
import static android.opengl.EGL14.EGL_NO_SURFACE;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;
import static android.opengl.EGL14.eglChooseConfig;
import static android.opengl.EGL14.eglCreateContext;
import static android.opengl.EGL14.eglCreateWindowSurface;
import static android.opengl.EGL14.eglDestroyContext;
import static android.opengl.EGL14.eglDestroySurface;
import static android.opengl.EGL14.eglGetDisplay;
import static android.opengl.EGL14.eglInitialize;
import static android.opengl.EGL14.eglMakeCurrent;
import static android.opengl.EGL14.eglReleaseThread;
import static android.opengl.EGL14.eglSwapBuffers;
import static android.opengl.EGL14.eglTerminate;

//自定义EGL环境
public class MyEGL {

    private EGLDisplay mEglDisplay;
    private EGLConfig mEGLConfig;
    private EGLContext mEGLContext;
    private final EGLSurface mEGLSurface;
    private final ScreenFilter mScreenFilter;

    public MyEGL(EGLContext share_context, Surface mediaCodecSurface, Context context, int width,
                 int height) {
        //1. 创建 egl 环境
        createEGL(share_context);
        //2. 创建窗口（画布），绘制线程中的图像
//        EGLConfig config,
//        Object win,
//        int[] attrib_list,
//        int offset
        int[] attrib_list = {
                EGL_NONE //一定要有结束符
        };
        mEGLSurface = eglCreateWindowSurface(mEglDisplay, mEGLConfig, mediaCodecSurface, attrib_list,
                0);
        //3. 让 mEglDisplay 和 mEGLSurface 绑定起来
//        EGLDisplay dpy,
//        EGLSurface draw,
//        EGLSurface read,
//        EGLContext ctx
        eglMakeCurrent(
                mEglDisplay,
                mEGLSurface,
                mEGLSurface,
                mEGLContext
        );

        //4. 画画，渲染
        mScreenFilter = new ScreenFilter(context);
        mScreenFilter.onReady(width, height);
    }

    public void draw(int textureId, long timestamp) {
        //渲染
        mScreenFilter.onDrawFrame(textureId);
        //刷新时间戳
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEGLSurface, timestamp);
        //交换缓冲区数据
        eglSwapBuffers(mEglDisplay, mEGLSurface);

    }

    private void createEGL(EGLContext share_context) {
        //1.1 获取显示设备，使用默认设备（手机屏）
        mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        //1.2 初始化设备
        int[] version = new int[2];
        eglInitialize(mEglDisplay, version, 0, version, 1);
        //1.3 选择配置
//        EGLDisplay dpy,
//        int[] attrib_list,
//        int attrib_listOffset,
//        EGLConfig[] configs,
//        int configsOffset,
//        int config_size,
//        int[] num_config,
//        int num_configOffset
        //属性列表
        int[] attrib_list = {
                //像素格式 rgba
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                //渲染API类型
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                //告诉 EGL 以android 兼容的方式创建 Surface
                EGLExt.EGL_RECORDABLE_ANDROID, 1,
                EGL_NONE //一定要有结束符

        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        eglChooseConfig(
                mEglDisplay,
                attrib_list,
                0,
                configs,
                0,
                configs.length,
                num_config,
                0
                );
        mEGLConfig = configs[0];

        //1.4 创建上下文
//        EGLDisplay dpy,
//        EGLConfig config,
//        EGLContext share_context, //共享上下文，绘制线程 GLThread 中 EGL 上下文，达到资源共享
//        int[] attrib_list,
//        int offset
        int[] ctx_attrib_list = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE //一定要有结束符
        };
        mEGLContext = eglCreateContext(
                mEglDisplay,
                mEGLConfig,
                share_context,
                ctx_attrib_list,
                0
        );
    }

    //释放资源
    public void release() {
        eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(mEglDisplay, mEGLSurface);
        eglDestroyContext(mEglDisplay, mEGLContext);
        eglReleaseThread();
        eglTerminate(mEglDisplay);
    }
}
