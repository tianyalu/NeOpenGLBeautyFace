//相机预览着色器，不能直接使用 Sampler2D, 而是 samplerExternalOES
#extension GL_OES_EGL_image_external : require
//中等精度
precision mediump float;
//从顶点着色器传过来的
varying vec2 aCoord;
//采样器
uniform samplerExternalOES vTexture;

void main() {
    gl_FragColor = texture2D(vTexture, aCoord);

    //简单的滤镜，如果黑白相机效果（305911公式）
//    vec4 rgba = texture2D(vTexture, aCoord);
//    float gray = rgba.r * 0.30 + rgba.g * 0.59 + rgba.b * 0.11;
//    gl_FragColor = vec4(gray, gray, gray, 1.0);
}