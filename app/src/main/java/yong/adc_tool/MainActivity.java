package yong.adc_tool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import agora.CppLibrary;
import agora.DataTransmission.DataCallback;
import agora.DataTransmission.LibDataTransmission;
import agora.FileIo.FileHelper;
import agora.Libyuv.LibyuvJava;
import agora.Libyuv.RgbaType;
import agora.collection.CameraHelpAPI1;
import agora.conversion.openGlBase.gles.GlUtil;

/**
 * Created by yong on 2018/10/8.
 * sample code try to use cpplibrary/javalibrary
 */

public class MainActivity  extends Activity implements SurfaceHolder.Callback,CameraHelpAPI1.PreviewFrameCallback,DataCallback{
    private static final String TAG = MainActivity.TAG;
    private CameraHelpAPI1 cameraHelpAPI1;
    private String filePath = "/sdcard/toolTest.yuv";
    private SurfaceHolder oldHolder = null;
    private LibyuvJava libyuv;
    private int width = 1280;
    private int height = 720;
    protected void onCreate( Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.view_test2);
        SurfaceView surfaceView2 = findViewById(R.id.view_test);
        cameraHelpAPI1 = new CameraHelpAPI1();
        cameraHelpAPI1.addPreviewFrameCallback(this);
        File testFile = new File(filePath);
        if(testFile.exists()){
            testFile.delete();
        }
        CppLibrary.initLibrary();
        libyuv = new LibyuvJava();
        libyuv.connect();
        Log.i(TAG,"libyuv test connect");
        LibDataTransmission libData = new LibDataTransmission();
        byte[] test = new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09};
        byte[] testDes = new byte[test.length];
        //TYPE 1
        byte[] returnTest = libData.javaReturnData(test,test.length);
        Log.i(TAG,"return_1 Test:"+returnTest[0]);
        //TYPE 2
        libData.javaShareData(test,testDes,test.length);
        Log.i(TAG,"return_2 Test:"+testDes[0]);
        //TYPE 3
        ByteBuffer byteBufferCapture = ByteBuffer.allocateDirect(10);
        byteBufferCapture.put(test);
        byteBufferCapture.flip();
        libData.setByteBUffer(byteBufferCapture,byteBufferCapture.limit());
        byte [] test3 = FileHelper.conver(byteBufferCapture);
        Log.i(TAG,"return_3 Test:"+test3);
        //TYPE 4
        libData.setDataCallback(this);
        readFromRGBA();
    }

    public void readFromRGBA(){
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        width=bitmap.getWidth();
        height=bitmap.getHeight();
        File file=new File("/sdcard/cache.yuv");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteBuffer buffer=ByteBuffer.allocate(bitmap.getWidth()*bitmap.getHeight()*4);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] yuvData=new byte[bitmap.getWidth()*bitmap.getHeight()*3/2];
        libyuv.rgbaToYUV420Planer(RgbaType.ABGR_TO_I420,buffer.array(),yuvData,bitmap.getWidth(),bitmap.getHeight());
        Log.e("wuwang","width*height:"+bitmap.getWidth()+"/"+bitmap.getHeight());
        try {
            os.write(yuvData);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG,"holder_1:"+holder);
        cameraHelpAPI1.openCameraForPreview(holder,1,1280,720,20,true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraHelpAPI1.releaseCamera();
        //cameraHelpAPI1.removePreviewFrameCallback(this);
    }

    @Override
    public void onPreviewFrameCallback(byte[] data) {
        try {
            //FileHelper.dumpRawData(filePath,data);
            byte[] pout = new byte[data.length];
            libyuv.fmtToYUV420Planer(data,data.length,width,height,0,270,pout);
            //Log.i(TAG,data+ " "+pout);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void dataCallbackToJava(byte[] data) {
        Log.i(TAG,"return_4 Test:"+data);
    }


}
