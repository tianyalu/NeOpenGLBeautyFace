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
