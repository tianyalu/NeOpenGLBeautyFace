package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;


import com.sty.ne.opengl.beautyface.util.TextureHelper;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glTexImage2D;

public class BaseFrameFilter extends BaseFilter{
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;
    public BaseFrameFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        super(context, vertexSourceId, fragmentSourceId);
    }

    /**
     * 需要旋转的话继续在子类中实现
     */
    @Override
    protected void changeTextureData() {
//        float[] TEXTURE = {
//                0.0f, 0.0f,
//                1.0f, 0.0f,
//                0.0f, 1.0f,
//                1.0f, 1.0f
//        };
//        mTextureBuffer.clear();
//        mTextureBuffer.put(TEXTURE);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        if(mFrameBuffers != null) {
            releaseFBO();
        }
        //创建FBO（虚拟的，看不见的离屏的一个屏幕）
        //int n: FBO的个数
        //int[] framebuffers: 用来保存FBO id 的数组
        //int offset: 从数组的第几个id来开始保存
        mFrameBuffers = new int[1];
        glGenFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);

        //创建属于FBO的纹理（需要配置）
        mFrameBufferTextures = new int[1];
        TextureHelper.genTextures(mFrameBufferTextures);

        //让FBO与 上面生成的FBO纹理发生关系
        //绑定
        glBindTexture(GL_TEXTURE_2D, mFrameBufferTextures[0]);
        //目标 2d纹理 + 等级 + 格式 + 宽 + 高 + 边界 + 格式 + 数据类型（byte） + 像素数据
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, null);
        glBindFramebuffer(GL_FRAMEBUFFER, mFrameBuffers[0]);
        glFramebufferTexture2D(
                //帧缓冲类型
                GL_FRAMEBUFFER,
                //附着点
                // GL_COLOR_ATTACHMENT0 - 颜色缓冲
                // GL_DEPTH_ATTACHMENT - 深度缓冲
                // GL_STENCIL_ATTACHMENT - 模板缓冲
                GL_COLOR_ATTACHMENT0,
                //希望附着的纹理类型
                GL_TEXTURE_2D,
                //附着的纹理id
                mFrameBufferTextures[0],
                //等级一般0
                0);

        //解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void releaseFBO() {
        if(null != mFrameBufferTextures) {
            glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if(null != mFrameBuffers) {
            glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    @Override
    public void release() {
        super.release();
        releaseFBO();
    }
}
