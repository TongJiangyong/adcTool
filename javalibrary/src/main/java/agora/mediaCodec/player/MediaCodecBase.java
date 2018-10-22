package agora.mediaCodec.player;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;


import agora.mediaCodec.player.IvideoCallBack.IvideoSizeCallBack;
import agora.mediaCodec.player.IvideoCallBack.IvideoStatusCallBack;

import static android.content.ContentValues.TAG;

/**
 * Created by hasee on 2017/11/2.
 */

public abstract class MediaCodecBase {
    public IvideoSizeCallBack mVideoSizeCallback=null;
    public IvideoStatusCallBack mVideoStatusCallBack=null;
    public MediaCodec mVideoMediaCodec=null;
    public MediaCodec mAduioMediaCodec=null;
    public MediaExtractor mVideoExtractor=null;
    public MediaExtractor mAudioExtractor=null;
    public Surface playerSurface;
    public Handler handler;
    public static final long TIMEOUT_US = 10000;
    public abstract  void startPlay();
    public abstract void stopPlay();
    public abstract void seekTo(long timeMs);
    public abstract void release();
    public abstract void setIsPausing(Boolean isPausing);



    //获取指定类型媒体文件所在轨道
    protected int getMediaTrackIndex(MediaExtractor videoExtractor, String MEDIA_TYPE) {
        int trackIndex = -1;
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            //获取视频所在轨道
            MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(MEDIA_TYPE)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }


    //将缓冲区传递至解码器
    protected boolean putBufferToCoder(MediaExtractor extractor, MediaCodec decoder, ByteBuffer[] inputBuffers) {
        boolean isMediaEOS = false;
        int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isMediaEOS = true;
                Log.v(TAG, "media eos");
            } else {
                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                extractor.advance();
            }
        }
        return isMediaEOS;
    }





    /**
     * 设置callback相关的处理
     * **/
    public void setStatusCallback(IvideoStatusCallBack mVideoStatusCallBack){
        this.mVideoStatusCallBack =mVideoStatusCallBack;
    }
    /**
     * 设置状态相关的处理
     * **/
    public void setSizeCallback(IvideoSizeCallBack mVideoSizeCallback){
        this.mVideoSizeCallback =mVideoSizeCallback;
    }

    /**
     * MeidaCodec的工具类
     * 获取支持的图像类型
     * **/
    public void showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
        System.out.print("supported color format: ");
        for (int c : caps.colorFormats) {
            System.out.print(c + "\t");
        }
        System.out.println();
    }

}
