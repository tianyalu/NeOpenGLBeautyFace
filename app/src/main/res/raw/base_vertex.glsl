//顶点坐标（确定画的形状）
attribute vec4 vPosition;
//纹理坐标
attribute vec2 vCoord;

//传给片元着色器
varying vec2 aCoord;

void main() {
    gl_Position = vPosition; // gl_Position 内置变量
    aCoord = vCoord; //矩阵运算（位置不能反！） 这里取两维
}