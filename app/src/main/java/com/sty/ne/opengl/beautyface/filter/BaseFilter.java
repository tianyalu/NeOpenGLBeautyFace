package com.sty.ne.opengl.beautyface.filter;

import android.content.Context;


import com.sty.ne.opengl.beautyface.util.BufferHelper;
import com.sty.ne.opengl.beautyface.util.ShaderHelper;
import com.sty.ne.opengl.beautyface.util.TextResourceReader;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class BaseFilter {
    protected int mVertexSourceId;
    protected int mFragmentSourceId;
    protected int vPosition;
    protected int vCoord;
    protected int vMatrix;
    protected int vTexture;

    protected FloatBuffer mVertexBuffer; //顶点坐标
    protected FloatBuffer mTextureBuffer; //纹理坐标

    protected int mProgramId;
    protected int mHeight;
    protected int mWidth;

    public BaseFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        mVertexSourceId = vertexSourceId;
        mFragmentSourceId = fragmentSourceId;

        //参考：https://www.jianshu.com/p/c4dda6884655  opengl世界坐标系.png
        float[] VERTEX = {
                -1.0f, -1.0f, //左下
                1.0f, -1.0f, //右下
                -1.0f, 1.0f, //左上
                1.0f, 1.0f   //右上
        };
        mVertexBuffer = BufferHelper.getFloatBuffer(VERTEX);

        //参考：https://www.jianshu.com/p/c4dda6884655  Android屏幕坐标系.png
        //这里维持图像未旋转的状态，需要旋转的话在子类中实现
        float[] TEXTURE = {
                0.0f, 1.0f, //左下
                1.0f, 1.0f, //右下
                0.0f, 0.0f, //左上
                1.0f, 0.0f  //右上

                //因为是反的，所以要逆时针旋转180度，并且左右镜像。
//                0.0f, 0.0f, //左下
//                1.0f, 0.0f, //右下
//                0.0f, 1.0f, //左上
//                1.0f, 1.0f  //右上
        };
        mTextureBuffer = BufferHelper.getFloatBuffer(TEXTURE);

        init(context);
        changeTextureData();
    }

    /**
     * 修改纹理坐标 textureData（有需求时可以重写该方法）
     */
    protected void changeTextureData() {

    }

    private void init(Context context) {
        //获取顶点着色器代码字符串
        String vertexSource = TextResourceReader.readTextFileFromResource(context,
                mVertexSourceId);  //顶点着色器源码
        //获取片元着色器代码字符串
        String fragmentSource = TextResourceReader.readTextFileFromResource(context,
                mFragmentSourceId);  //片元着色器源码
        //编译并获取顶点着色器id
        int vertexShaderId = ShaderHelper.compileVertexShader(vertexSource);
        //编译并获取片元着色器id
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentSource);
        //将顶点着色器和片元着色器链接到程序
        mProgramId = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId);

        //通过变量索引给变量赋值
        //获取着色器代码中 Attribute 变量的索引值
        vPosition = glGetAttribLocation(mProgramId, "vPosition");
        vCoord = glGetAttribLocation(mProgramId, "vCoord");
        //获取着色器代码中Uniform 变量的索引值
        vMatrix = glGetUniformLocation(mProgramId, "vMatrix");
        vTexture = glGetUniformLocation(mProgramId, "vTexture");
    }

    public void onReady(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int onDrawFrame(int textureId) {
        glViewport(0, 0, mWidth, mHeight); //设置视窗大小
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
        return textureId;
    }

    public void release() {
        glDeleteProgram(mProgramId);
    }
}
