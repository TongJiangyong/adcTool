package agora.collection;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by yong on 2018/10/8.
 */

public class CameraHelpAPI1 implements Camera.PreviewCallback{

    public static final String TAG = "CameraHelpAPI1";
    private Camera mCamera;
    private int previewWidth;
    private int previewHeight;
    private SurfaceTexture surfaceTexture;
    private List<PreviewFrameCallback> previewFrameCallbackList;

    public CameraHelpAPI1(){
        previewFrameCallbackList = new ArrayList<>(1);
    }

    public Camera getCameraInstance(int id) {
        if(mCamera!=null){
            mCamera = Camera.open(id);
        }
        return mCamera;
    }

    /**
     * @return the default rear/back facing camera on the device. Returns null if camera is not
     * available.
     */
    public  Camera getDefaultBackFacingCameraInstance() {
        if(mCamera!=null){
            mCamera = getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        return mCamera;
    }

    /**
     * @return the default front facing camera on the device. Returns null if camera is not
     * available.
     */
    public Camera getDefaultFrontFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }


    /**
     *
     * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
     *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
     * @return the default camera on the device. Returns null if camera is not available.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Camera getDefaultCamera(int position) {
        // Find the total number of cameras available
        int  mNumberOfCameras = Camera.getNumberOfCameras();

        Log.i(TAG, "getDefaultCamera number:"+mNumberOfCameras+" position:"+position);

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);
            }
        }

        return null;
    }


    public synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }





    /**
     * Attempts to find a fixed preview frame rate that matches the desired frame rate.
     * <p>
     * It doesn't seem like there's a great deal of flexibility here.
     * <p>
     * TODO: follow the recipe from http://stackoverflow.com/questions/22639336/#22645327
     *
     * @return The expected frame rate, in thousands of frames per second.
     */
    public int chooseFixedPreviewFps(int desiredThousandFps) {
        desiredThousandFps = desiredThousandFps*1000;
        Camera.Parameters parms = mCamera.getParameters();
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            //Log.i(TAG, "entry: " + entry[0] + " - " + entry[1]);
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }

        int[] tmp = new int[2];
        parms.getPreviewFpsRange(tmp);
        int guess;
        if (tmp[0] == tmp[1]) {
            guess = tmp[0];
        } else {
            guess = tmp[1] / 2;     // shrug
        }

        Log.i(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
        return guess;
    }


    /**
     * Iterate over supported camera preview sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param w The width of the view.
     * @param h The height of the view.
     * @return Best match camera preview size to fit in the view.
     */
    public  Camera.Size getOptimalPreviewSize(int w, int h) {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Log.i(TAG, "getOptimalPreviewSize");


        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        previewWidth = optimalSize.width;
        previewHeight = optimalSize.height;
        parameters.setPreviewSize(previewWidth, previewHeight);
        return optimalSize;
    }

    /**
     *
     * @param imageFormat
     * NV21
     */
    public void setPreviewFormat(int imageFormat){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(imageFormat);
    }

    public boolean setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.surfaceTexture = surfaceTexture;
        boolean isSetTextureOk = false;
        if(mCamera!=null){
            try {
                this.mCamera.setPreviewTexture(this.surfaceTexture);
                isSetTextureOk = true;
            } catch (IOException e) {
                e.printStackTrace();
                isSetTextureOk = false;
            }
        }
        return isSetTextureOk;
    }

    /**
     *
     * @param rotation
     * 0,90,180,270
     */
    public void setDisplayOrientation(int rotation){
        mCamera.setDisplayOrientation(rotation);
    }

    public void enablePreviewCallback(){
        mCamera.setPreviewCallback(this);
    }


    /**
     *  a example of how to use this tool
     *  need to make sure the holder is ready
     * @param holder fsdfsdf
     * @param cameraId
     * @param width fsdfsdf
     * @param height
     * @param fps
     * @param enableCallback
     * @return
     */
    public boolean openCameraForPreview(SurfaceHolder holder,int cameraId,int width,int height,int fps,boolean enableCallback){
        releaseCamera();
        mCamera = getDefaultCamera(cameraId);
        if(mCamera!=null&&holder!=null){
            Camera.Size optimalSize = this.getOptimalPreviewSize(width, height);
            int previewFrameRate = this.chooseFixedPreviewFps(fps);
            //this.setDisplayOrientation(90);
            this.setPreviewFormat(ImageFormat.NV21);
            if(enableCallback){
                this.enablePreviewCallback();
            }
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            mCamera.startPreview();
            return true;
        }else{
            return false;
        }


    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        for(PreviewFrameCallback p:previewFrameCallbackList){
            p.onPreviewFrameCallback(data);
        }
    }

    public void addPreviewFrameCallback(PreviewFrameCallback previewFrameCallback){
        previewFrameCallbackList.add(previewFrameCallback);
    }

    public void removePreviewFrameCallback(PreviewFrameCallback previewFrameCallback){
        previewFrameCallbackList.remove(previewFrameCallback);
    }

    public void cleanPreviewFrameCallback(){
        previewFrameCallbackList.clear();
    }

    public interface PreviewFrameCallback{
        void onPreviewFrameCallback(byte[] data);
    }
}
