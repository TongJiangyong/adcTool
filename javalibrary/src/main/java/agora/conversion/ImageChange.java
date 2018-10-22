package agora.conversion;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import agora.conversion.openGlBase.gles.GlUtil;


/**
 * Created by yong on 2018/10/14.
 */

public class ImageChange {


    //TODO uncheck
    public static int[] I420toARGB(byte[] yuv, int width, int height) {
        boolean invertHeight = false;
        if (height < 0) {
            height = -height;
            invertHeight = true;
        }
        boolean invertWidth = false;
        if (width < 0) {
            width = -width;
            invertWidth = true;
        }
        int iterations = width * height;
        int[] rgb = new int[iterations];

        for (int i = 0; i < iterations; i++) {
            int nearest = (i / width) / 2 * (width / 2) + (i % width) / 2;
            int y = yuv[i] & 0x000000ff;
            int u = yuv[iterations + nearest] & 0x000000ff;
            int v = yuv[iterations + iterations / 4 + nearest] & 0x000000ff;
            int b = (int) (y + 1.8556 * (u - 128));

            int g = (int) (y - (0.4681 * (v - 128) + 0.1872 * (u - 128)));

            int r = (int) (y + 1.5748 * (v - 128));
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            if (g > 255) {
                g = 255;
            } else if (g < 0) {
                g = 0;
            }
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            int targetPosition = i;
            if (invertHeight) {
                targetPosition = ((height - 1) - targetPosition / width) * width + (targetPosition % width);
            }
            if (invertWidth) {
                targetPosition = (targetPosition / width) * width + (width - 1) - (targetPosition % width);
            }
            rgb[targetPosition] = (0xff000000) | (0x00ff0000 & r << 16) | (0x0000ff00 & g << 8) | (0x000000ff & b);
        }
        return rgb;

    }



    /**
     * Saves the EGL surface to a file.
     * <p>
     * Expects that this object's EGL surface is current.
     */
    public void saveFrame(File file,int width,int height) throws IOException {
        // glReadPixels fills in a "direct" ByteBuffer with what is essentially big-endian RGBA
        // data (i.e. a byte of red, followed by a byte of green...).  While the Bitmap
        // constructor that takes an int[] wants little-endian ARGB (blue/red swapped), the
        // Bitmap "copy pixels" method wants the same format GL provides.
        //
        // Ideally we'OffscreenSurface have some way to re-use the ByteBuffer, especially if we're calling
        // here often.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside down relative to what appears on screen if the
        // typical GL conventions are used.

        String filename = file.toString();
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        GlUtil.checkGlError("glReadPixels");
        buf.rewind();

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }
    }
}
