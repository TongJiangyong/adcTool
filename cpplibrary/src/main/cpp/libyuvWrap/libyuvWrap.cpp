//
// Created by yong on 2018/10/22.
//
#include <jni.h>
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

#include "libyuvWrap.h"


using namespace libyuv;

#ifdef __cplusplus
extern "C" {
#endif

LibyuvWrap::LibyuvWrap() {
    __android_log_print(ANDROID_LOG_INFO, "libyuvWrap", "LibyuvWrap init");
}
LibyuvWrap::~LibyuvWrap() {
    __android_log_print(ANDROID_LOG_INFO, "libyuvWrap", "LibyuvWrap uninit");
}

void LibyuvWrap::FMTtoYUV420Planer(const uint8* src_frame,
                              int nLen,
                              int nWidth,
                              int nHeight,
                              uint32 pixFmt,
                              int nDegree,
                              uint8* des_frame){
    int nYStride = nWidth;
    int nUVStride = nWidth / 2;
    RotationMode rotationMode = kRotate0;
    uint32 format = FOURCC_NV21;
    __android_log_print(ANDROID_LOG_INFO, "libyuvWrap", "%d,%d,%d",nLen,nWidth,nHeight);
    switch (nDegree)
    {
        case 0:
            rotationMode = kRotate0;
            break;
        case 90:
            nYStride = nHeight;
            nUVStride = nHeight/2;
            rotationMode = kRotate90;
            break;
        case 270:
            nYStride = nHeight;
            nUVStride = nHeight/2;
            rotationMode = kRotate270;
            break;
        case 180:
            rotationMode = kRotate180;
            break;
        default:
            break;
    }

    switch (pixFmt)
    {
        case 0:
            format = FOURCC_NV21;
            break;
        case 1:
            format = FOURCC_YV12;
            break;
        default:
            break;
    }

    //I420 ： 亮度（行×列） ＋ U（行×列/4) + V（行×列/4）
    uint8 *pY = (uint8 *) des_frame;
    uint8 *pU = pY + nWidth*nHeight;
    uint8 *pV = pU + nWidth*nHeight/4;

    int nRet = ConvertToI420((uint8 *) src_frame, (int)nLen,
                             pY, nYStride,
                             pU, nUVStride,
                             pV, nUVStride,
                             0, 0,
                             (int)nWidth, nHeight,
                             (int)nWidth, nHeight,
                             rotationMode, format);

}




#ifdef __cplusplus
}
#endif
