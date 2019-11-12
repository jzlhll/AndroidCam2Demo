package com.allan.androidcam2api.State;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Size;

import com.allan.androidcam2api.base.ITakePictureCallback;
import com.allan.androidcam2api.manager.MyCameraManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StatePictureAndPreview extends StatePreview implements ImageReader.OnImageAvailableListener{
    public interface IStateTakePictureCallback extends StatePreview.IStatePreviewCallback {
        void onPictureToken(String path);
    }

    public StatePictureAndPreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    protected void addTarget() {
        cameraManager.getPreviewBuilder().addTarget(camSurfaces.get(0));
    }

    @Override
    protected void createSurfaces() {
        if (cameraManager.getCameraCharacteristics() == null) {
            throw new RuntimeException("No Camera Charact!");
        }
        camSurfaces = new ArrayList<>();
        Size needSize = setSize(1920, 1080);
        camSurfaces.add(cameraManager.getSurface());
        if (mImageReader == null) {
            //**** width和height要传入正确，否则，preview就变大小
            mImageReader = ImageReader.newInstance(needSize.getWidth(), needSize.getHeight(), ImageFormat.JPEG, 1); //最大的图片的个数
            mImageReader.setOnImageAvailableListener(this, cameraManager.getHandler());
        }
        camSurfaces.add(mImageReader.getSurface()); //创建并添加拍照surface
    }

    @Override
    public int getId() {
        return 0x011;
    }

    @Override
    public void closeSession() {
        if (mImageReader != null)
            mImageReader.close();
        super.closeSession();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        cameraManager.getHandler().post(new ImageSaver(reader.acquireNextImage(), mFile));
    }

    public boolean takePicture(String dir, String name, final ITakePictureCallback func) {
        mFile = new File(dir + File.separator + name);
        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    cameraManager.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //如下2个设置自己看
            //setAutoFlash(captureBuilder);

            // Orientation
            //int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    IStateTakePictureCallback cb = (IStateTakePictureCallback) mStateBaseCb;
                    cb.onPictureToken(mFile.getPath());
                    func.onPictureToken(mFile.getPath());
                }
            };

            //camera.camSession.stopRepeating();
            //camera.camSession.abortCaptures();
            cameraManager.getCamSession().capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private File mFile;
    private ImageReader mImageReader;

    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}