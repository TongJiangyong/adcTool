package agora.DataTransmission;

import java.nio.ByteBuffer;

/**
 * Created by yong on 2018/10/22.
 */

public class LibDataTransmission {



    //cpp return数据到java
    public native byte[] javaReturnData(byte[] pData,int length);

    //java和capp共享数据
    public native void javaShareData(byte[] pData, byte[] pOu,int length);


    //使用allocaBuffer共享数据
    public native void setDataCallback(DataCallback dataCallback);

    //使用c++ callback
    public native void setByteBUffer(ByteBuffer byteBuffer,int length);

}
