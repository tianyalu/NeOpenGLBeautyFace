//顶点坐标（确定画的形状）
attribute vec4 vPosition;
//纹理坐标（这里用vec4是为了下面和变换矩阵做运算，运算结束之后实际只取了两维，这也是BaseFilter中108行size为2的原因）
attribute vec4 vCoord;
//变化矩阵
uniform mat4 vMatrix;
//传给片元着色器
varying vec2 aCoord;

void main() {
    gl_Position = vPosition; // gl_Position 内置变量
    aCoord = (vMatrix * vCoord).xy; //矩阵运算（位置不能反！） 这里取两维
}