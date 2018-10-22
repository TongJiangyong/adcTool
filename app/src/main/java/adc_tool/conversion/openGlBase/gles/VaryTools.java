package adc_tool.conversion.openGlBase.gles;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

/**
 * Created by yong on 2018/3/12.
 */

public class VaryTools {

    private float[] mMatrixCamera ={
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };
    private float[] mMatrixProjection = new float[16];
    private float[] mMatrixCurrent =   new float[16];  //原始矩阵


    private Stack<float[]> mStack;      //变换矩阵堆栈

    public VaryTools() {
        mStack = new Stack<float[]>();
        Matrix.setIdentityM(mMatrixProjection, 0);
        Matrix.setIdentityM(mMatrixCurrent, 0);
    }

    public void pushMatrix() {
        mStack.push(Arrays.copyOf(mMatrixCurrent, 16));
    }

    public void popMatrix() {
        mMatrixCurrent = mStack.pop();
    }

    public void clearStack() {
        mStack.clear();
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(mMatrixCurrent, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(mMatrixCurrent, 0, angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(mMatrixCurrent, 0, x, y, z);
    }
    public void setCamera(float ex, float ey, float ez, float cx, float cy, float cz, float ux, float uy, float uz) {
        Matrix.setLookAtM(mMatrixCamera, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz);
    }

    public void frustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    public float[] getFinalMatrix() {
        float[] ans = new float[16];
        Matrix.multiplyMM(ans, 0, mMatrixCamera, 0, mMatrixCurrent, 0);
        Matrix.multiplyMM(ans,0,mMatrixProjection,0,ans,0);
        return ans;
    }

}
