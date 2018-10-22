package agora;

/**
 * Created by yong on 2018/10/22.
 */

public class LibyuvJava {
    static {
        System.loadLibrary("cpplibrary");
    }

    public void connect(){
        this.testConnectToJni();
    }

    public void fmtToYUV420Planer(byte[] data, int dataLength, int width, int height, int pixFormat, int degree, byte[] pout) {
        this.FMTtoYUV420Planer(data,dataLength,width,height,pixFormat,degree,pout);
    }

    public static native void testConnectToJni();

    public native void FMTtoYUV420Planer(byte[] pData, int nLen, int nWidth, int nHeight, int pixFmt, int nDegree, byte[] pOu);


}
