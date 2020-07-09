package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;
import android.opengl.GLES11Ext;

import com.sty.ne.opengl.beautyface.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * 不需要渲染到屏幕（而是写入到FBO缓冲中）
 */
public class CameraFilter extends BaseFrameFilter {

    private float[] matrix;

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vetex, R.raw.camera_fragment);
    }

    /**
     *
     * @param textureId 摄像头的纹理id
     * @return
     */
    @Override
    public int onDrawFrame(int textureId) {
        glViewport(0, 0, mWidth, mHeight); //设置视窗大小
        //绑定 FBO （否则会渲染到屏幕）
        glBindFramebuffer(GL_FRAMEBUFFER, mFrameBuffers[0]);

        glUseProgram(mProgramId);
        //画画
        //顶点坐标赋值
        mVertexBuffer.position(0);
        //传值
        glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, mVertexBuffer);
        //激活
        glEnableVertexAttribArray(vPosition);

        //纹理坐标
        mTextureBuffer.position(0);
        glVertexAttribPointer(vCoord, 2, GL_FLOAT, false, 0, mTextureBuffer);
        glEnableVertexAttribArray(vCoord);

        //变换矩阵
        glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        //vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE0);
        //绑定纹理
//        glBindTexture(GL_TEXTURE_2D, textureId);
        //因为这一层是摄像头后的第一层，所以需要使用扩展的 GL_TEXTURE_EXTERNAL_OES
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        glUniform1i(vTexture, 0);

        //通知OpenGL绘制
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        //glBindTexture(GL_TEXTURE_2D, 0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //return textureId; //注意这里,不能反回摄像头的纹理id，而是返回与 FBO 绑定了的纹理id
        return mFrameBufferTextures[0];
    }

    public void setMatrix(float[] mtx) {
        this.matrix = mtx;
    }

    @Override
    protected void changeTextureData() {

    }
}
