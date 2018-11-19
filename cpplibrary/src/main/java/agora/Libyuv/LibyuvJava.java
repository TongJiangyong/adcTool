package agora.Libyuv;

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


    public void rgbaToYUV420Planer(int type,byte[] rgba,byte[] yuv,int width,int height) {
        this.RGBAoYUV420Planer(type,rgba,yuv, width,height);
    }

    //rgba也会有很多类型，所以我们加一个type的值，来表示rgba是什么类型
    //也可以用直接写一个rgba转yuv的，rgba和yuv类型都不固定，用type来表示所有类型的rgba到yuv的转换
    public static native int RGBAoYUV420Planer(int type,byte[] rgba,byte[] yuv,int width,int height);

}
