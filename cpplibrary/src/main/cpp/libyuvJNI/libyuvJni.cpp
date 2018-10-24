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



#ifdef __cplusplus
}
#endif
