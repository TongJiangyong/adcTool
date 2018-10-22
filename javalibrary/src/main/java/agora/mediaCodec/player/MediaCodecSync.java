package agora.mediaCodec.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Created by hasee on 2017/10/31.
 */

public class MediaCodecSync extends MediaCodecBase {

    private String videoFilePath;

    private static final String TAG = "MediaCodecSync";
    private static final long TIMEOUT_US = 10000;
    private static final long SLEEP_US = 10;
    private static final long outOfAudioTimeThreshold = 500; //ms
    private static final long outOfVideoTimeThreshold = 30; //ms

    //控制用的video thread
    private VideoThread videoThread;
    private AudioThread audioThread;
    //控制用的audio thread
    //private AudioThread audioThread;
    public MediaCodecSync(String videoFilePath, Surface surface, Handler handler){
        this.videoFilePath = videoFilePath;
        this.playerSurface = surface;
        this.handler = handler;
    }


    //开始播放
    public void startPlay(){
        Log.i(TAG,"startPlay");
        //初始化音频相关
        audioThread = new AudioThread(true);
        audioThread.initAudioExtractor();
        audioThread.initAudioCodec();
        //初始化视频相关
        videoThread = new VideoThread(true);
        videoThread.initVideoExtractor();
        videoThread.initVideoCodec();
        //启动线程
        new Thread(audioThread).start();
        new Thread(videoThread).start();
    }

    //停止播放
    public void stopPlay(){
        //停止视频
        videoThread.setVideoPlayStatus(false);
        videoThread.cleanVideoMediaCodec();
        videoThread =null;
        //停止音频
        audioThread.setAudioPlayStatus(false);
        videoThread.cleanVideoMediaCodec();
        audioThread =null;
    }

    public void seekTo(long timeMs) {
        videoThread.seekTo(timeMs);
        audioThread.seekTo(timeMs);
    }

    public void release() {
        videoThread.cleanVideoMediaCodec();
        audioThread.cleanAudioMediaCodec();
    }

    //设置能够暂停播放
    //设置能够暂停播放
    public void setIsPausing(Boolean isPausing){
        if(isPausing){
            videoThread.pausePlay();
            audioThread.pausePlay();
        }else{
            videoThread.resumePlay();
            audioThread.resumePlay();
        }
    }


    //MeidaCodec的video线程.....
    private class VideoThread implements Runnable {
        private boolean isPausing = false;
        private boolean isPlaying = false;
        private long startVideoMs;
        //最后正常的时间
        private long lastVideoPresentationTimeMs = 0;
        //seek后正常的时间
        private long seekVideoNormalTimeUs = 0;
        //时间差
        private long diffVideoTimeUs;
        private long pauseVideoDuringMs=0;
        private long pauseVideoStartMs=0;
        private long pauseVideoEndMs=0;
        private boolean videoIsSeeking=false;
        public VideoThread (boolean isPlay){
            isPlaying = isPlay;
        }
        public void setVideoPlayStatus(boolean isPlay){
            isPlaying = isPlay;
        }
        public void initVideoExtractor(){
            //初始化extractor的部分
            mVideoExtractor= new MediaExtractor();
            try {
                mVideoExtractor.setDataSource(videoFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void initVideoCodec(){
            //初始化video的部分....
            int videoTrackIndex;
            //获取视频所在轨道
            videoTrackIndex = getMediaTrackIndex(mVideoExtractor, "video/");
            if (videoTrackIndex >= 0) {
                MediaFormat mediaFormat = mVideoExtractor.getTrackFormat(videoTrackIndex);
                int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                //视频长度:秒
                //获取视频的长度
                //Log.d(TAG,"TJYtime :"+mediaFormat.getLong(MediaFormat.KEY_DURATION));
                float time = mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000;
                //Log.d(TAG,"TJYtime :"+time);
                //返回高宽的变化
                mVideoSizeCallback.videoSize(width,height);
                //返回时间的变化
                mVideoStatusCallBack.videoLong((float)time);

                mVideoExtractor.selectTrack(videoTrackIndex);
                try {
                    mVideoMediaCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
                    mVideoMediaCodec.configure(mediaFormat, playerSurface, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mVideoMediaCodec == null) {
                Log.v(TAG, "MediaCodec null");
                return;
            }
        }

        //开始视频的线程......
        @Override
        public void run() {

            //video的decoder start.....
            mVideoMediaCodec.start();

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = mVideoMediaCodec.getInputBuffers();
//            ByteBuffer[] outputBuffers = videoCodec.getOutputBuffers();
            boolean isVideoEOS = false;

            startVideoMs = System.currentTimeMillis();
            while (!Thread.interrupted()&&isPlaying) {
                //Log.i(TAG,"just a test_3");
                if (!isPlaying) {
                    continue;
                }
                //这种方法暂停后，不容易恢复播放.....这里处理一下....
                if(isPausing){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                //将资源传递到解码器
                if (!isVideoEOS) {
                    isVideoEOS = putBufferToCoder(mVideoExtractor, mVideoMediaCodec, inputBuffers);
                }
                int outputBufferIndex = mVideoMediaCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.v(TAG, "format changed");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG, "解码当前帧超时");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        //outputBuffers = videoCodec.getOutputBuffers();
                        Log.v(TAG, "output buffers changed");
                        break;
                    default:
                        //直接渲染到Surface时使用不到outputBuffer
                        //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        //Log.i(TAG,"just a test_2");
                        if(sleepVideoRender(mVideoMediaCodec,videoBufferInfo,outputBufferIndex)){
                            lastVideoPresentationTimeMs = videoBufferInfo.presentationTimeUs/1000;
                            mVideoMediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                        }
                        //Log.i(TAG,"just a test_1");
                        break;
                }

                if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.v(TAG, "buffer stream end");
                    break;
                }
            }//end while
            mVideoMediaCodec.stop();
            mVideoMediaCodec.release();
            mVideoExtractor.release();
        }
        //TODO
        //想了很久，这里还是处理成在音视频线程中分别处理的好.....
        //进行a/v sync
        private boolean sleepVideoRender(MediaCodec mediaCodec, MediaCodec.BufferInfo bufferInfo, int outputBufferIndex) {
            //当时间比较多的时候，就开始等.....
            //只对视频做丢帧处理，不对音频做处理
            sendPTSToMain(bufferInfo.presentationTimeUs / 1000);

            //如果时间差播放时间30ms，开始丢帧
            if(!videoIsSeeking&&((bufferInfo.presentationTimeUs / 1000) - seekVideoNormalTimeUs + outOfVideoTimeThreshold < System.currentTimeMillis() - startVideoMs-pauseVideoDuringMs))
            {
                Log.v(TAG, "video packet too late drop it ... ");
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                return false;
            }
            //限定seek的容错时间
            if (videoIsSeeking && Math.abs(bufferInfo.presentationTimeUs / 1000 - seekVideoNormalTimeUs) < 100)
            {
                //Log.v(TAG, "tjy video seek结束....");
                videoThread.seekOver();
            }
            //如果帧的时间大于实际播放时间，则开始休眠
            Log.i(TAG,"tjy System.currentTimeMillis() - startVideoMs-pauseVideoDuringMs is:"+String.valueOf(System.currentTimeMillis() - startVideoMs-pauseVideoDuringMs)+" pauseVideoDuringMs: "+pauseVideoDuringMs);
            Log.i(TAG,String.valueOf(bufferInfo.presentationTimeUs /1000+","+" tjy seekVideoNormalTimeUs is:"+seekVideoNormalTimeUs));
            while (!videoIsSeeking&&((bufferInfo.presentationTimeUs/ 1000 - seekVideoNormalTimeUs > System.currentTimeMillis() - startVideoMs-pauseVideoDuringMs))) {
                try {
                    Log.v(TAG, "try to sleep");
                    Thread.sleep(SLEEP_US);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    return false;
                }
            }
            return true;
        }

        //用于hander 送回messager
        public void sendPTSToMain(float pts){
            Message message = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putFloat("currentPTS", pts);
            message.setData(b);
            handler.sendMessage(message);  //或使用 handler.sendMessage(message);
        }

        public void seekTo(long timeMs) {
            Log.d(TAG, "SeekTo Requested to : " + timeMs);
            Log.i(TAG, "SampleTime Before SeekTo : " + mVideoExtractor.getSampleTime());
            videoIsSeeking = true;
            startVideoMs = System.currentTimeMillis();
            mVideoExtractor.seekTo(timeMs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            seekVideoNormalTimeUs = mVideoExtractor.getSampleTime() / 1000;
            pauseVideoDuringMs = 0;
            Log.i(TAG, "SampleTime after SeekTo : " + mVideoExtractor.getSampleTime());
            diffVideoTimeUs = (seekVideoNormalTimeUs - lastVideoPresentationTimeMs / 1000);
            Log.d(TAG, "seekTo with diff : " + diffVideoTimeUs);
        }

        public void cleanVideoMediaCodec(){
            mVideoMediaCodec=null;
            mVideoExtractor=null;
        }


        public void seekOver() {
            videoIsSeeking = false;
        }
        public void pausePlay() {
            //首先通过pause，不让extractor喂资料
            isPausing = true;
            pauseVideoStartMs = System.currentTimeMillis();
        }

        public void resumePlay() {
            //resume后要重新计算时间
            isPausing = false;
            pauseVideoEndMs = System.currentTimeMillis();
            pauseVideoDuringMs += pauseVideoEndMs-pauseVideoStartMs;
        }
    }

    //设置音频的播放线程
    private class AudioThread extends Thread {
        private boolean isPausing = false;
        private boolean isPlaying = false;
        private int audioInputBufferSize;
        private AudioTrack audioTrack;
        private long startAudioMs;
        private boolean audioIsSeeking=false;
        private long pauseAudioDuringMs=0;
        private long pauseAudioStartMs=0;
        private long pauseAudioEndMs=0;
        //最后正常的时间
        private long lastAudioPresentationTimeUs = 0;
        //seek后正常的时间
        private long seekAudioNormalTimeUs;
        //时间差
        private long diffAudioTimeUs;
        public AudioThread (boolean isPlay){
            isPlaying = isPlay;
        }
        public void setAudioPlayStatus(boolean isPlay){
            isPlaying = isPlay;
        }
        public void initAudioExtractor(){
            //初始化extractor的部分
            mAudioExtractor= new MediaExtractor();
            try {
                mAudioExtractor.setDataSource(videoFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void initAudioCodec(){
            //初始化audio的部分....
            for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mAudioExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    mAudioExtractor.selectTrack(i);
                    //获取audioChannel相关
                    int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    Log.i(TAG,"MediaFormat.KEY_CHANNEL_COUNT:"+audioChannels);
                    int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    Log.i(TAG,"MediaFormat.KEY_SAMPLE_RATE:"+audioSampleRate);
                    int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                            (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                            AudioFormat.ENCODING_PCM_16BIT);
                    Log.i(TAG,"MediaFormat.KEY_MAX_INPUT_SIZE:"+minBufferSize);
                    //mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024);
                    //int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    //Log.i(TAG,"MediaFormat.KEY_MAX_INPUT_SIZE:"+maxInputSize);
                    //audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
                    audioInputBufferSize = minBufferSize;
                    int frameSizeInBytes = audioChannels * 2;
                    audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;
                    //配置audio相关的track，写入audioTrack即开始播放音视频
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            audioSampleRate,
                            (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                            AudioFormat.ENCODING_PCM_16BIT,
                            audioInputBufferSize,
                            AudioTrack.MODE_STREAM);
                    audioTrack.play();
                    Log.i(TAG, "set audio play");
                    //
                    try {
                        mAduioMediaCodec = MediaCodec.createDecoderByType(mime);
                        mAduioMediaCodec.configure(mediaFormat, null, null, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            if (mAduioMediaCodec == null) {
                Log.v(TAG, "audio decoder null");
                return;
            }
        }
        @Override
        public void run() {

            mAduioMediaCodec.start();
            //
            final ByteBuffer[] buffers = mAduioMediaCodec.getOutputBuffers();
            int sz = buffers[0].capacity();
            if (sz <= 0)
                sz = audioInputBufferSize;
            byte[] mAudioOutTempBuf = new byte[sz];

            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = mAduioMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mAduioMediaCodec.getOutputBuffers();
            boolean isAudioEOS = false;
            startAudioMs = System.currentTimeMillis();

            while (!Thread.interrupted()&&isPlaying) {
                if (!isPlaying) {
                    continue;
                }
                if(isPausing){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (!isAudioEOS) {
                    isAudioEOS = putBufferToCoder(mAudioExtractor, mAduioMediaCodec, inputBuffers);
                }
                //
                int outputBufferIndex = mAduioMediaCodec.dequeueOutputBuffer(audioBufferInfo, TIMEOUT_US);
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.v(TAG, "format changed");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG, "audio 超时");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        outputBuffers = mAduioMediaCodec.getOutputBuffers();
                        Log.v(TAG, "audio output buffers changed");
                        break;
                    default:
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        if(sleepAudioRender(mAduioMediaCodec,audioBufferInfo,outputBufferIndex)){
                            if (audioBufferInfo.size > 0) {
                                if (mAudioOutTempBuf.length < audioBufferInfo.size) {
                                    mAudioOutTempBuf = new byte[audioBufferInfo.size];
                                }
                                outputBuffer.position(0);
                                outputBuffer.get(mAudioOutTempBuf, 0, audioBufferInfo.size);
                                outputBuffer.clear();
                                //写入视频播放
                                if (audioTrack != null)
                                    lastAudioPresentationTimeUs = audioBufferInfo.presentationTimeUs;
                                audioTrack.write(mAudioOutTempBuf, 0, audioBufferInfo.size);
                            }
                            mAduioMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        }
                        //
                        break;
                }

                if ((audioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.v(TAG, "buffer stream end");
                    break;
                }
            }//end while


            mAduioMediaCodec.stop();
            mAudioExtractor.release();
            mAduioMediaCodec.release();
            audioTrack.stop();
            audioTrack.release();
        }
        //TODO
        //想了很久，这里还是处理成在音视频线程中分别处理的好.....
        //进行a/v sync
        private boolean sleepAudioRender(MediaCodec mediaCodec, MediaCodec.BufferInfo bufferInfo, int outputBufferIndex) {
            //当时间比较多的时候，就开始等.....
            //Log.i(TAG,String.valueOf(bufferInfo.presentationTimeUs / 1000+","+" tjy time is:"+(System.currentTimeMillis() - startMs)));
            //如果时间差播放时间30ms，开始丢帧
/*            if(!audioIsSeeking&&((bufferInfo.presentationTimeUs / 1000) - seekAudioNormalTimeUs + outOfAudioTimeThreshold < System.currentTimeMillis() - startAudioMs))
            {
                Log.v(TAG, "audio packet too late drop it ... ");
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                return false;
            }*/
            //限定seek的容错时间
            if (audioIsSeeking && Math.abs(bufferInfo.presentationTimeUs / 1000 - seekAudioNormalTimeUs) < 100)
            {
                Log.v(TAG, "audio seek结束....");
                audioThread.seekOver();
            }
            //如果帧的时间大于实际播放时间，则开始休眠
            while (!audioIsSeeking&&((bufferInfo.presentationTimeUs/ 1000 - seekAudioNormalTimeUs > System.currentTimeMillis() - startAudioMs-pauseAudioDuringMs))) {
                try {
                    Log.v(TAG, "audio try to sleep");
                    Thread.sleep(SLEEP_US);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    return false;
                }
            }
            return true;
        }

        public void seekTo(long timeMs) {
            Log.d(TAG, "audio SeekTo Requested to : " + timeMs);
            Log.i(TAG, "audio SampleTime Before SeekTo : " + mAudioExtractor.getSampleTime());
            audioIsSeeking = true;
            startAudioMs = System.currentTimeMillis();
            mAudioExtractor.seekTo(timeMs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            seekAudioNormalTimeUs = mAudioExtractor.getSampleTime() / 1000;
            diffAudioTimeUs = (seekAudioNormalTimeUs - lastAudioPresentationTimeUs / 1000);
            pauseAudioDuringMs = 0;
            Log.d(TAG, "seekTo with seekAudioNormalTimeUs : " + seekAudioNormalTimeUs*1000);
            Log.d(TAG, "seekTo with diff : " + diffAudioTimeUs);
        }

        public void cleanAudioMediaCodec(){
            mAduioMediaCodec=null;
            audioTrack=null;
            mAudioExtractor=null;
        }

        public void seekOver() {
            audioIsSeeking = false;
        }

        public void pausePlay() {
            //首先通过pause，不让extractor喂资料
            isPausing = true;
            pauseAudioStartMs =System.currentTimeMillis();
            audioTrack.flush();
        }

        public void resumePlay() {
            //resume后要重新计算时间
            isPausing = false;
            pauseAudioEndMs =System.currentTimeMillis();
            pauseAudioDuringMs += pauseAudioEndMs-pauseAudioStartMs;
        }
    }

}

