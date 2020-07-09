//相机预览着色器，不能直接使用 Sampler2D, 而是 samplerExternalOES
//#extension GL_OES_EGL_image_external : require
//中等精度
precision mediump float;
//从顶点着色器传过来的
varying vec2 aCoord;
//采样器
//uniform samplerExternalOES vTexture;
uniform sampler2D vTexture;

void main() {
    gl_FragColor = texture2D(vTexture, aCoord);
}