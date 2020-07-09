# 人脸贴纸与美颜磨皮

[TOC]

## 一、前言

本文基于 [大眼滤镜](https://github.com/tianyalu/NeOpenGLBigEyes)  

本文通过`OpenGL`实现了人脸贴纸与美颜效果，效果如下图所示：  

![image](https://github.com/tianyalu/NeOpenGLBeautyFace/raw/master/show/show.gif)  

## 二、人脸贴纸实现

### 2.1 总体实现思路

人脸贴纸的总体实现思路很简单：即需要检测出人脸框，然后通过`OpenGL`的图层混合模式在合适的位置画上贴纸图层即可。  

### 2.2 代码实现贴图绘制

#### 2.2.1 将贴图转成`Bitmap`

```java
public StickFilter(Context context) {
  super(context, R.raw.base_vertex, R.raw.base_fragment);
  mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.erduo_000);
}
```

#### 2.2.2 绘制前置图层

```java
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
```

#### 2.2.3 画贴图

```java
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
```

## 三、美颜磨皮实现

### 3.1 概念

#### 3.1.1 高反差保留算法

高反差保留算法就是保留原图中反差比较大的地方，比如，一副人脸图像中，反差比较大的地方就是五官了，在进行人脸美化的时候我们通常会对原图进行平滑处理，然而处理完之后丢失了图片的细节信息，因此在另一边我们通常会通过高反差保留或者其它高通滤波器保留图片的细节信息，然后将平滑之后的图片和高频图像进行光线性混合，可以得到更好的效果。

具体到高反差保留算法，先使用高斯滤波器（高斯滤波器对边缘的平滑作用更加明显）对图像进行平滑，使用原图减去高斯平滑之后的图就得到强化边缘值。

> 通过调节高斯模糊的半径可以控制得到的边缘的强度。

**高斯反差保留 = 原图 - 高斯模糊图 **  

**锐化的图像 = 原图 + 高斯反差保留图**  

详情可参考：[【OPENCV】高反差保留算法](https://www.jianshu.com/p/bb702124d2ad)

#### 3.1.2 图层混合算法

图层混合算法有如下几种模式：  

* 滤色模式

* 叠加模式

* 柔光模式

* 强光模式

详情可参考：[PS图层混合算法之三（滤色， 叠加， 柔光， 强光）](https://blog.csdn.net/matrix_space/article/details/22426633)

### 3.2 代码实现步骤

总体思路边上通过高斯反差保留等算法对`FBO`图层进行处理，最后获得美颜磨皮效果。  

#### 3.2.1着色器文件`beauty_fragment.glsl`

```glsl
precision lowp float;
uniform sampler2D vTexture;
varying vec2 aCoord;
//纹理宽高
uniform int width;
uniform int height;
vec2 blurCoordinates[20];

void main() {
    //1.高斯模糊
    vec2 singleStepOffset = vec2(1.0 / float(width), 1.0 / float(height));
    //采集20个点
    blurCoordinates[0] = aCoord.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] = aCoord.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = aCoord.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = aCoord.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = aCoord.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = aCoord.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = aCoord.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = aCoord.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = aCoord.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = aCoord.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = aCoord.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = aCoord.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = aCoord.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = aCoord.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = aCoord.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = aCoord.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = aCoord.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = aCoord.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = aCoord.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = aCoord.xy + singleStepOffset * vec2(4.0, 4.0);

    //当前采样点的像素值
    vec4 currentColor = texture2D(vTexture, aCoord);
    vec3 rgb = currentColor.rgb;

    //求和
    for (int i = 0; i < 20; i++) {
        rgb += texture2D(vTexture, blurCoordinates[i].xy).rgb;
    }

    vec4 blur = vec4(rgb / 21.0, currentColor.a);

//    gl_FragColor = blur; //高斯特效图

    //2. 高反差(原图-高斯)
    vec4 hightPassColor = currentColor - blur;
    //强度系数
    //clamp 家居函数（取三个参数中间值）
    hightPassColor.r = clamp(2.0 * hightPassColor.r * hightPassColor.r * 24.0, 0.0, 1.0);
    hightPassColor.g = clamp(2.0 * hightPassColor.g * hightPassColor.g * 24.0, 0.0, 1.0);
    hightPassColor.b = clamp(2.0 * hightPassColor.b * hightPassColor.b * 24.0, 0.0, 1.0);

    vec4 highPassBlur = vec4(hightPassColor.rgb, 1.0);

//    gl_FragColor = blur; //高斯特效图

    //3. 磨皮
    //取蓝色分量
    float blue = min(currentColor.b, blur.b);

    float value = clamp((blue - 0.2) * 5.0, 0.0, 1.0);

    //取rgb最大值
    float maxChannelColor = max(max(highPassBlur.r, highPassBlur.g), highPassBlur.b);

    float factor = 1.0; //磨皮强度

    float currentFactor = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * factor;

    //线性混合
    vec3 r = mix(currentColor.rgb, blur.rgb, currentFactor);

    gl_FragColor = vec4(r, 1.0);
}

```

#### 3.2.2 美颜过滤器`BeautyFilter`

```java
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
```

### 3.3 参考资料

* 开源美颜相机工程：[MagicCamera](https://github.com/wuhaoyu1990/MagicCamera)  

* 美白着色器代码：[[AGLFramework](https://github.com/smzhldr/AGLFramework)] 项目中的 [light_f.glsl](https://github.com/smzhldr/AGLFramework/blob/master/aglframework/src/main/res/raw/light_f.glsl)