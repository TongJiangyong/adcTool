//
// Created by yong on 2018/10/22.
//
//
// Created by yong on 2018/10/22.
//
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include <algorithm>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <malloc.h>
#include <math.h>
#include "libyuvJni.h"
#include "libyuvWrap.h"
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL
Java_agora_Libyuv_LibyuvJava_testConnectToJni(JNIEnv *env, jclass type) {

    __android_log_print(ANDROID_LOG_INFO, "LibyuvCpp", "testConnect");

}

static int (*rgbaToI420Func[])(const uint8 *,int,uint8 *,int,uint8 *,int ,uint8 *,int,int,int)={
        libyuv::ABGRToI420,libyuv::RGBAToI420,libyuv::ARGBToI420,libyuv::BGRAToI420,
        libyuv::RGB24ToI420,libyuv::RGB565ToI420
};

static LibyuvWrap *libyuvWrap=new LibyuvWrap;

JNIEXPORT void JNICALL
Java_agora_Libyuv_LibyuvJava_FMTtoYUV420Planer(JNIEnv *env, jobject instance, jbyteArray pData_,
                                            jint nLen, jint nWidth, jint nHeight, jint pixFmt,
                                            jint nDegree, jbyteArray pOu_) {
    jbyte *pData = env->GetByteArrayElements(pData_, NULL);
    jbyte *pOu = env->GetByteArrayElements(pOu_, NULL);
    __android_log_print(ANDROID_LOG_INFO, "LibyuvCpp", "%d,%d,%d",nLen,nWidth,nHeight);
    libyuvWrap->FMTtoYUV420Planer((uint8* )pData,nLen,nWidth,nHeight,pixFmt,nDegree,(uint8* )pOu);
    env->ReleaseByteArrayElements(pData_, pData, 0);
    env->ReleaseByteArrayElements(pOu_, pOu, 0);
}


JNIEXPORT jint JNICALL
Java_agora_Libyuv_LibyuvJava_RGBAoYUV420Planer(JNIEnv * env,jclass clazz,jint type,jbyteArray rgba,jbyteArray yuv,jint width,jint height){
    uint8 cType=(uint8) (type & 0x0F);
    int rgba_stride= ((type & 0xF0) >> 4)*width;
    int y_stride=width;
    int u_stride=width>>1;
    int v_stride=u_stride;
    return libyuvWrap->rgbaToI420(env,clazz,rgba,rgba_stride,yuv,y_stride,u_stride,v_stride,width,height,rgbaToI420Func[cType]);
}




#ifdef __cplusplus
}
#endif
