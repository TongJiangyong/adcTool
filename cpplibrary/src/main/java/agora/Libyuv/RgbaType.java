package agora.Libyuv;

/**
 * Created by yong on 2018/11/19.
 */

public class RgbaType {

    //0-3 表示转换类型
    //4-7 表示rgba_stride的宽度的倍数
    //8-11 表示yuv_stride宽度移位数
    //12-15 表示uv左移位数
    public static final int RGBA_TO_I420=0x01001040;
    public static final int ABGR_TO_I420=0x01001041;
    public static final int BGRA_TO_I420=0x01001042;
    public static final int ARGB_TO_I420=0x01001043;
    public static final int RGB24_TO_I420=0x01001034;
    public static final int RGB565_TO_I420=0x01001025;
}
