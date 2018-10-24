//
// Created by yong on 2018/10/24.
//

#ifndef ADCTOOL_CALLBACKPROCESS_H
#define ADCTOOL_CALLBACKPROCESS_H
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
#include <stddef.h>
#include <assert.h>
#include <pthread.h>

typedef struct CBData{
    jobject m_objInstance;
    JNIEnv* m_pEnv;
    jobject m_objCallBack;
} CBData;

class callBackProcess {

public:
    callBackProcess(CBData cbData,JavaVM* jvm)
            :g_cbData(cbData),attached_(false),jvm_(jvm){
        jint ret_val = jvm->GetEnv(reinterpret_cast<void**>(&(cbData.m_pEnv)),
                                   JNI_VERSION_1_6);
        if (ret_val == JNI_EDETACHED) {
            // Attach the thread to the Java VM.
            ret_val = jvm_->AttachCurrentThread(&(cbData.m_pEnv), nullptr);
            attached_ = ret_val >= 0;
            assert(attached_);
        }
    }

    ~callBackProcess(){
        if (attached_ && (jvm_->DetachCurrentThread() < 0)) {
            assert(false);
        }
    }

    int processJNICB()
    {
        jclass jclsProcess = g_cbData.m_pEnv->GetObjectClass(g_cbData.m_objInstance);
        if (jclsProcess == NULL)
        {
            printf("jclsProcess = NULL\n");
            return -1;
        }
        jmethodID jmidProcess = g_cbData.m_pEnv->GetMethodID(jclsProcess,"dataCallbackToJava","([B)V");
        if (jmidProcess == NULL)
        {
            printf("jmidProcess = NULL\n");
            return -2;
        }
        uint byteLength = 13;
        jbyte cpp_raw_data[byteLength];
        //create new array
        memset(&cpp_raw_data,0x02,byteLength);
        jbyteArray jarrRV =g_cbData.m_pEnv->NewByteArray(byteLength);
        g_cbData.m_pEnv->SetByteArrayRegion(jarrRV, 0,byteLength,cpp_raw_data);
        g_cbData.m_pEnv->CallVoidMethod(g_cbData.m_objInstance,jmidProcess,jarrRV);
        return 0;
    }

public:
    bool attached_;
    JavaVM* jvm_;
    CBData g_cbData;

};


#endif //ADCTOOL_CALLBACKPROCESS_H
