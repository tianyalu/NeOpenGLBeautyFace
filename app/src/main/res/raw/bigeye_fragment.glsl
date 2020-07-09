//大眼特效的算法处理（针对纹理数据进行处理：局部放大算法）

//中等精度
precision mediump float;
//从顶点着色器传过来的
varying vec2 aCoord;
//采样器
uniform sampler2D vTexture;

//左眼
uniform vec2 left_eye;
//右眼
uniform vec2 right_eye;

//参考：resources/warping-thesis.pdf  page47 4.4.2的公式  或 show/big_eyes_scale_formula.png
//rmax: 局部放大最大作用半径
//return: fsr--> 放大后的半径
float fs(float r, float rmax) {
    float a = 0.4; //放大系数

//    return (1.0 - pow(r / rmax - 1.0, 2.0) * a) * r;
    return (1.0 - pow(r / rmax - 1.0, 2.0) * a);
}

//参考：show/big_eyes.scale_theory.png
//oldCoord: 旧的采样点坐标
//eye: 眼睛坐标
//rmax: 局部放大最大作用半径
//在部分机型上，变量名和函数名同名的话会导致编译报错！
vec2 calcNewCoord(vec2 oldCoord, vec2 eye, float rmax) {
    vec2 newCoord = oldCoord;
    float r = distance(oldCoord, eye);
    float fsr = fs(r, rmax);

    if(r > 0.0f && r < rmax) {
        //(新点 - 眼睛) / (老点 - 眼睛) = 新距离 / 老距离
        //(newCoord - eye) / (oldCoord - eye) = fsr / r
//        newCoord = (fsr / r) * (oldCoord - eye) + eye;  //vec可以直接相减求距离

        newCoord = fsr * (oldCoord - eye) + eye;  //化简写法//vec可以直接相减求距离
    }
    return newCoord;
}

void main() {
    //两眼距离的一半
    float rmax = distance(left_eye, right_eye) / 2.0;
    vec2 newCoord = calcNewCoord(aCoord, left_eye, rmax); //左眼放大位置的采样点
    newCoord = calcNewCoord(newCoord, right_eye, rmax); //右眼放大位置的采样点

    gl_FragColor = texture2D(vTexture, newCoord);
}