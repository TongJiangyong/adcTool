//
// Created by yong on 2018/10/22.
//

#ifndef ADCTOOL_LIBYUVWRAP_H
#define ADCTOOL_LIBYUVWRAP_H
#include "libyuv.h"
class LibyuvWrap
{
public:
    void FMTtoYUV420Planer(const uint8* src_frame,int nLen,int nWidth,int nHeight,uint32 pixFmt,int nDegree,uint8* des_frame);

public:
    LibyuvWrap(void);
    ~LibyuvWrap(void);
};


#endif //ADCTOOL_LIBYUVWRAP_H
