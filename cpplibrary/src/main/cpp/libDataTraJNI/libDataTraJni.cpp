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
#include "libDataTraJni.h"
#include "callBackProcess.h"
#ifdef __cplusplus
extern "C" {
#endif

static JavaVM *gJVM = nullptr;
JNIEXPORT jbyteArray JNICALL
Java_agora_DataTransmission_LibDataTransmission_javaReturnData(JNIEnv *env,
                                                               jobject instance,
                                                               jbyteArray pData_,
                                                               jint nLen) {
    __android_log_print(ANDROID_LOG_INFO, "LibDataTraJni", "javaReturnData on");
    jbyte *pData = env->GetByteArrayElements(pData_, NULL);
    jbyte gs_raw_data[nLen];
    //create new array
    memset(&gs_raw_data,0x01,nLen);
    __android_log_print(ANDROID_LOG_INFO, "TJY", "javaReturnData %02x,%02x,%02x,%02x",pData[0],pData[1],pData[2],pData[3]);
    jbyteArray jarrRV =env->NewByteArray(nLen);
    env->SetByteArrayRegion(jarrRV, 0,nLen,gs_raw_data);
    env->ReleaseByteArrayElements(pData_, pData, 0);
    return jarrRV;
}

JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_javaShareData(
        JNIEnv *env,
        jobject instance,
        jbyteArray pInData_,
        jbyteArray pOutData_,
        jint nLen){
    jbyte *pData = env->GetByteArrayElements(pInData_, NULL);
    jbyte *pOu = env->GetByteArrayElements(pOutData_, NULL);
    __android_log_print(ANDROID_LOG_INFO, "TJY", "LibDataTransmission");
    memcpy(pOu,pData,nLen);
    env->ReleaseByteArrayElements(pInData_, pData, 0);
    env->ReleaseByteArrayElements(pOutData_, pOu, 0);

}

JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_setByteBUffer(
        JNIEnv *env,
        jclass type,
        jobject byteBuffer,
        jint nLen){
    void *_javaDirectPlayBufferCapture = nullptr;
    _javaDirectPlayBufferCapture = env->GetDirectBufferAddress(byteBuffer);
    memset(_javaDirectPlayBufferCapture,0x05,nLen);
}



CBData g_cbData;
JNIEXPORT void JNICALL
Java_agora_DataTransmission_LibDataTransmission_setDataCallback(
        JNIEnv *env,
        jobject dataCallback,
        jobject instance) {
    env->GetJavaVM(&gJVM);
    // set callback
    g_cbData.m_pEnv = env;
    g_cbData.m_objCallBack = env->NewGlobalRef(dataCallback);
    g_cbData.m_objInstance = env->NewGlobalRef(instance);
    // use callback
    callBackProcess callBack(g_cbData,gJVM);
    callBack.processJNICB();
}






#ifdef __cplusplus
}
#endif
