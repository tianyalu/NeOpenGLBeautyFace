package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.sty.ne.opengl.beautyface.R;
import com.sty.ne.opengl.beautyface.face.Face;
import com.sty.ne.opengl.beautyface.util.TextureHelper;

import static android.opengl.GLES20.*;

public class StickFilter extends BaseFrameFilter {

    private final Bitmap mBitmap; //贴图
    private int[] mTextureID;
    private Face mFace;

    public StickFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_fragment);

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.erduo_000);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        //把 bitmap 变成纹理
        mTextureID = new int[1];
        TextureHelper.genTextures(mTextureID);

        glBindTexture(GL_TEXTURE_2D, mTextureID[0]);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, mBitmap, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
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

        //前面（上一层）的纹理绘制完之后再绘制贴图耳朵
        drawStick();

        return mFrameBufferTextures[0];
    }

    /**
     * 画贴图（耳朵）
     */
    private void drawStick() {
        //图层混合
        //开启OpenGL混合模式，让贴图和原纹理进行混合操作（融合）
        glEnable(GL_BLEND);
        //int sfactor: 原图层因子
        //int dfactor: 目标图因子
        //GL_ONE: 全部绘制
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA); //混合模式和因子可自行搭配看效果

        //画贴图
        //人脸框起始位置
        float x = mFace.landmarks[0];
        float y = mFace.landmarks[1];

        //重新设置坐标(人脸-->屏幕）
        // ? / mWidth = x / mFace.imgWidth
        x = x / mFace.imgWidth * mWidth;
        y = y / mFace.imgHeight * mHeight;

        //设置贴图视窗大小（画板多大） //TODO 需要调
        // ? / mWidth = mFace.width / mFace.imgWidth
        int viewWidth = (int) ((float)mFace.width / mFace.imgWidth * mWidth); //注意int和float
        int viewHeight = mBitmap.getHeight();

        glViewport((int)x, (int)y - mBitmap.getHeight() / 2, viewWidth, viewHeight); //TODO 需要调

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

        //顶点坐标赋值
        mTextureBuffer.position(0);
        //传值
        glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, mVertexBuffer);
        //激活
        glEnableVertexAttribArray(vPosition);
        //纹理坐标
        mTextureBuffer.position(0);

        //激活图层
        glActiveTexture(GL_TEXTURE0);
        //绑定纹理
        glBindTexture(GL_TEXTURE_2D, mTextureID[0]);
        //glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        glUniform1i(vTexture, 0);

        //通知OpenGL绘制
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        //解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //关闭混合模式
        glDisable(GL_BLEND);
    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }

    public void setFace(Face face) {
        mFace = face;
    }
}
