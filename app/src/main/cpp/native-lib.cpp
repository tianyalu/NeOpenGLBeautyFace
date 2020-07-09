#include <jni.h>
#include <string>
#include <opencv2/imgproc/types_c.h>
#include "FaceTrack.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_sty_ne_opengl_beautyface_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sty_ne_opengl_beautyface_face_FaceTrack_native_1create(JNIEnv *env, jobject thiz,
                                                                jstring face_model_, jstring seeta_) {
    const char *model = env->GetStringUTFChars(face_model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTrack *faceTrack = new FaceTrack(model, seeta);

    env->ReleaseStringUTFChars(face_model_, model);
    env->ReleaseStringUTFChars(seeta_, seeta);

    return reinterpret_cast<jlong>(faceTrack);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sty_ne_opengl_beautyface_face_FaceTrack_native_1start(JNIEnv *env, jobject thiz,
                                                               jlong self) {
    if(self == 0) {
        return;
    }
    FaceTrack* faceTrack = reinterpret_cast<FaceTrack *>(self);
    faceTrack->startTracking();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sty_ne_opengl_beautyface_face_FaceTrack_native_1stop(JNIEnv *env, jobject thiz,
                                                              jlong self) {
    if(self == 0) {
        return;
    }
    FaceTrack* faceTrack = reinterpret_cast<FaceTrack *>(self);
    faceTrack->stopTracking();
    delete faceTrack;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_sty_ne_opengl_beautyface_face_FaceTrack_native_1detector(JNIEnv *env, jobject thiz,
                                                                  jlong self, jbyteArray data_,
                                                                  jint camera_id, jint width,
                                                                  jint height) {
    if(self == 0) {
        return NULL;
    }
    jbyte* data = env->GetByteArrayElements(data_, 0);
    FaceTrack* faceTrack = reinterpret_cast<FaceTrack *>(self);
    //摄像头数据data转成 OpenCV的 Mat
    Mat src(height + height / 2, width, CV_8UC1, data);

    imwrite("/storage/emulated/0/sty/big_eyes/camera.jpg", src); //摄像头原始图像
    cvtColor(src, src, CV_YUV2RGBA_NV21);
    if(camera_id == 1) { //前置
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE); //逆时针90度
        flip(src, src, 1); //y轴翻转
    }else { //后摄
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }
    //灰度化
    cvtColor(src, src, COLOR_RGBA2GRAY);

    //均衡化处理
    equalizeHist(src, src);

    vector<Rect2f> rects;
    faceTrack->detector(src, rects);

    env->ReleaseByteArrayElements(data_, data, 0);

    int imgWidth = src.cols;
    int imgHeight = src.rows;
    int ret = rects.size();
    if(ret) {
        jclass clazz = env->FindClass("com/sty/ne/opengl/beautyface/face/Face");
        jmethodID construct = env->GetMethodID(clazz, "<init>", "(IIII[F)V");
        //int width, int height, int imgWidth, int imgHeight, float[] landmark
        int size = ret * 2;
        jfloatArray floatArray = env->NewFloatArray(size);
        for (int i = 0, j = 0; i < size; ++j) {
            float f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(floatArray, i, 2, f);
            i += 2;
        }
        Rect2f faceRect = rects[0];
        int faceWidth = faceRect.width;
        int faceHeight = faceRect.height;

        jobject face = env->NewObject(clazz, construct, faceWidth, faceHeight, imgWidth, imgHeight,
                                      floatArray);
        //画人脸矩形
//        rectangle(src, faceRect, Scalar(255, 255, 255));
        rectangle(src, faceRect, Scalar(255, 0, 0));
        for (int i = 1; i < ret; ++i) {
            circle(src, Point2f(rects[i].x, rects[i].y), 5, Scalar(0, 255, 0));
        }

        imwrite("/storage/emulated/0/sty/big_eyes/face.jpg", src); //画了人脸的图像
        return face;
    }
    src.release();
    return NULL;
}