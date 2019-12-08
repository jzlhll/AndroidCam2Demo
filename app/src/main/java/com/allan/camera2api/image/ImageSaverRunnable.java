package com.allan.camera2api.image;

import android.media.Image;

import com.allan.utils.CamLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaverRunnable implements Runnable{

    /**
     * The JPEG image
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    public ImageSaverRunnable(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        CamLog.d("Image Saverrrr buffers " + bytes.length);
        try (FileOutputStream output = new FileOutputStream(mFile)) {
            output.write(bytes);
            CamLog.d("Image Saverrrr buffers File " + mFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
        }
    }
}
