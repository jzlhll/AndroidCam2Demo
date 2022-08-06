package com.allan.camera2api.image;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.allan.base.IActionTakePicture;
import com.allan.base.ITakePictureCallback;
import com.allan.base.TakePictureCallbackWrap;
import com.allan.camera2api.MyCameraManager;
import com.allan.utils.CamLog;

import java.io.File;

public class TakePictureBuilder implements IActionTakePicture, ImageReader.OnImageAvailableListener {
    private MyCameraManager cameraManager;
    private ImageReader picImgReader;

    private File mNextFile; //TODO 注意看这个代码，需要takePicture传入，生成的；
                            //// 然后onImageAvailable再使用的。可能会有低概率在快速连续2张出现覆盖名字问题

    public TakePictureBuilder(MyCameraManager cameraManager, int width, int height) {
        this.cameraManager = cameraManager;
        picImgReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1); //最大的图片的个数
        picImgReader.setOnImageAvailableListener(this, cameraManager.getHandler());
    }

    public final Surface getSurface() {
        return picImgReader.getSurface();
    }

    public final void release() {
        picImgReader.close();
        cameraManager = null;
        mNextFile = null;
    }

    @Override
    public void takePicture(TakePictureCallbackWrap func) {
        String dir = func.dir;
        String name = func.name;
        final ITakePictureCallback callback = func.callback;

        mNextFile = new File(dir + File.separator + name);
        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    cameraManager.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(picImgReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //如下2个设置自己看
            //setAutoFlash(captureBuilder);

            // Orientation
            //int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    callback.onPictureToken(mNextFile.getPath());
                }
            };

            //camera.camSession.stopRepeating();
            //camera.camSession.abortCaptures();
            CamLog.d("take picture capture " + mNextFile);
            cameraManager.getCamSession().capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        CamLog.d("take picture onImageAvailable ");
        cameraManager.getHandler().post(new ImageSaverRunnable(reader.acquireNextImage(), mNextFile));
    }
}
