//
// Created by yong on 2018/10/22.
//
#include <jni.h>
#ifndef ADCTOOL_LIBYUVJni_H
#define ADCTOOL_LIBYUVJni_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_agora_LibyuvJava_testConnectToJni(JNIEnv *env, jclass type);

JNIEXPORT void JNICALL
Java_agora_LibyuvJava_FMTtoYUV420Planer(JNIEnv *env, jobject instance, jbyteArray pData_,
jint nLen, jint nWidth, jint nHeight, jint pixFmt,
jint nDegree, jbyteArray pOu_);

#ifdef __cplusplus
}
#endif

#endif
