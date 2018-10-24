package agora;

/**
 * Created by yong on 2018/10/23.
 */

public class CppLibrary {
    private static boolean isLoad = false;
    public static void initLibrary(){
        if(!isLoad){
            System.loadLibrary("cpplibrary");
            isLoad = true;
        }
    }
}
