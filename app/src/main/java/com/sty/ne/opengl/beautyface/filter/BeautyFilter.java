package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;

import com.sty.ne.opengl.beautyface.R;

import static android.opengl.GLES20.*;

public class BeautyFilter extends BaseFrameFilter {
    private int width;
    private int height;

    public BeautyFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.beauty_fragment);
        width = glGetUniformLocation(mProgramId, "width");
        height = glGetUniformLocation(mProgramId, "height");
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

        glUniform1i(width, mWidth);
        glUniform1i(height, mHeight);

        //vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE0);
        //绑定纹理
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(vTexture, 0);

        //通知OpenGL绘制
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //return textureId; //注意这里,不能反回摄像头的纹理id，而是返回与 FBO 绑定了的纹理id
        return mFrameBufferTextures[0];
    }
}
