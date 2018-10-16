package yong.adc_tool;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import adc_tool.FileIo.FileHelper;
import adc_tool.collection.CameraHelpAPI1;
import adc_tool.collection.CameraHelpAPI2;
/**
 * Created by yong on 2018/10/8.
 */

public class MainActivity  extends Activity implements SurfaceHolder.Callback,CameraHelpAPI1.PreviewFrameCallback {
    private CameraHelpAPI1 cameraHelpAPI1;
    private CameraHelpAPI1 CameraHelpAPI2;
    private String filePath = "/sdcard/toolTest.yuv";
    private SurfaceHolder oldHolder = null;
    protected void onCreate( Bundle savedInstanceState) {
        Log.d("TJY", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.view_test2);
        surfaceView.getHolder().addCallback(this);
        SurfaceView surfaceView2 = findViewById(R.id.view_test);
        surfaceView2.getHolder().addCallback(this);
        cameraHelpAPI1 = new CameraHelpAPI1();
        CameraHelpAPI2 = new CameraHelpAPI1();
        cameraHelpAPI1.addPreviewFrameCallback(this);
        CameraHelpAPI2.addPreviewFrameCallback(this);
        File testFile = new File(filePath);
        if(testFile.exists()){
            testFile.delete();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("TJY","holder_1:"+holder);
        if(oldHolder == null){
            cameraHelpAPI1.openCameraForPreview(holder,1,1280,720,20,true);
            oldHolder = holder;
            return;
        }
        Log.i("TJY","holder_2:"+holder);
        CameraHelpAPI2.openCameraForPreview(holder,0,1920,1080,20,true);


        //holder.setFixedSize(640,480);
        //CameraHelpAPI2.setupCamera(640, 480);
        //CameraHelpAPI2.openCamera(holder.getSurface());
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
