package yong.adc_tool;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.nio.ByteBuffer;

import agora.CppLibrary;
import agora.DataTransmission.DataCallback;
import agora.DataTransmission.LibDataTransmission;
import agora.FileIo.FileHelper;
import agora.Libyuv.LibyuvJava;
import agora.collection.CameraHelpAPI1;

/**
 * Created by yong on 2018/10/8.
 */

public class MainActivity  extends Activity implements SurfaceHolder.Callback,CameraHelpAPI1.PreviewFrameCallback,DataCallback{
    private CameraHelpAPI1 cameraHelpAPI1;
    private String filePath = "/sdcard/toolTest.yuv";
    private SurfaceHolder oldHolder = null;
    private LibyuvJava libyuv;
    private int width = 1280;
    private int height = 720;
    protected void onCreate( Bundle savedInstanceState) {
        Log.d("TJY", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.view_test2);
        //surfaceView.getHolder().addCallback(this);
        SurfaceView surfaceView2 = findViewById(R.id.view_test);
        //surfaceView2.getHolder().addCallback(this);
        cameraHelpAPI1 = new CameraHelpAPI1();
        cameraHelpAPI1.addPreviewFrameCallback(this);
        File testFile = new File(filePath);
        if(testFile.exists()){
            testFile.delete();
        }
        CppLibrary.initLibrary();
        libyuv = new LibyuvJava();
        libyuv.connect();
        Log.i("TJY","libyuv test connect");
        LibDataTransmission libData = new LibDataTransmission();
        byte[] test = new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09};
        byte[] testDes = new byte[test.length];
        //TYPE 1
        byte[] returnTest = libData.javaReturnData(test,test.length);
        Log.i("TJY","return_1 Test:"+returnTest[0]);
        //TYPE 2
        libData.javaShareData(test,testDes,test.length);
        Log.i("TJY","return_2 Test:"+testDes[0]);
        //TYPE 3
        ByteBuffer byteBufferCapture = ByteBuffer.allocateDirect(10);
        byteBufferCapture.put(test);
        byteBufferCapture.flip();
        libData.setByteBUffer(byteBufferCapture,byteBufferCapture.limit());
        byte [] test3 = FileHelper.conver(byteBufferCapture);
        Log.i("TJY","return_3 Test:"+test3);
        //TYPE 4
        libData.setDataCallback(this);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("TJY","holder_1:"+holder);
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
            //Log.i("TJY",data+ " "+pout);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void dataCallbackToJava(byte[] data) {
        Log.i("TJY","return_4 Test:"+data);
    }


}
