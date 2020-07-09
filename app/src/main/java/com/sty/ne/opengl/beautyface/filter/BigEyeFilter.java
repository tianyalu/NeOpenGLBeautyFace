package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;


import com.sty.ne.opengl.beautyface.R;
import com.sty.ne.opengl.beautyface.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * 大眼滤镜
 */
public class BigEyeFilter extends BaseFrameFilter {
    private int left_eye;
    private int right_eye;

    private FloatBuffer left;
    private FloatBuffer right;
    private Face mFace;

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.bigeye_fragment);

        left_eye = glGetUniformLocation(mProgramId, "left_eye");
        right_eye = glGetUniformLocation(mProgramId, "right_eye");

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    /**
     * 图像旋转镜像操作
     */
    @Override
    protected void changeTextureData() {
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        mTextureBuffer.clear();
        mTextureBuffer.put(TEXTURE);
    }

    @Override
    public int onDrawFrame(int textureId) {
        if(mFace == null) {
            return textureId;
        }

        glViewport(0, 0, mWidth, mHeight); //设置视窗大小
        //绑定FBO（否则会渲染到屏幕）
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
        //指定索引处的顶点属性数组的位置和数据格式，方便渲染时来使用
        //index: 索引值
        //size: 每个顶点属性的分量（组件数），必须是1、2、3或4（比如顶点vec2(x,y), vec3(x,y,z), 颜色vec4(r,g,b,a））
        //type: 数据类型
        //normalized: 是否需要归一化处理
        //stride: 步长（0：数据是紧密排列的）
        //Buffer: 缓冲区，告诉OpenGL到哪里去拿数据
        glVertexAttribPointer(vCoord, 2, GL_FLOAT, false, 0, mTextureBuffer);
        glEnableVertexAttribArray(vCoord);

        float[] landmarks = mFace.landmarks;
        //左眼
        //眼睛在人脸图像中的位置坐标与在整幅图像中的坐标的换算 ？？？
        float x = landmarks[2] / mFace.imgWidth;
        float y = landmarks[3] / mFace.imgHeight;
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        glUniform2fv(left_eye, 1, left);

        //右眼
        //眼睛在人脸图像中的位置坐标与在整幅图像中的坐标的换算 ？？？
        x = landmarks[4] / mFace.imgWidth;
        y = landmarks[5] / mFace.imgHeight;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        glUniform2fv(right_eye, 1, right);

        //变换矩阵
        //glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);

        //vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE0);
        //绑定纹理
        glBindTexture(GL_TEXTURE_2D, textureId);
        //glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        glUniform1i(vTexture, 0);

        //通知OpenGL绘制
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[0];
    }

    public void setFace(Face face) {
        mFace = face;
    }
}
