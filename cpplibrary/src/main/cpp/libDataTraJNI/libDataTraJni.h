//
// Created by yong on 2018/10/22.
//
#include <jni.h>
#ifndef ADCTOOL_LIB_DATA_TRA_JNI_h
#define ADCTOOL_LIB_DATA_TRA_JNI_h

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_agora_DataTransmission_LibDataTransmission_javaReturnData(
    JNIEnv *env,
    jobject instance,
    jbyteArray pData_,
    jint nLen);

JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_javaShareData(
        JNIEnv *env,
        jobject instance,
        jbyteArray pInData_,
        jbyteArray pOutData_,
        jint nLen);


JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_setByteBUffer(
        JNIEnv *env,
        jclass type,
        jobject byteBuffer,
        jint nLen);

JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_setDataCallback(
        JNIEnv *env,
        jobject instance,
        jobject dataCallback);
#ifdef __cplusplus
}
#endif

#endif
