package com.allan.cam2api.states;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.util.Size;

import androidx.annotation.NonNull;

import com.allan.cam2api.states.image.IActionTakePicture;
import com.allan.cam2api.states.image.ImageSaver;
import com.allan.cam2api.base.ITakePictureCallback;
import com.allan.cam2api.manager.MyCameraManager;

import java.io.File;
import java.util.ArrayList;

public class StatePicture extends AbstractStateBase implements ImageReader.OnImageAvailableListener, IActionTakePicture {
    public StatePicture(MyCameraManager cd) {
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
        return FeatureUtil.FEATURE_PICTURE;
    }

    @Override
    public void closeSession() {
        if (mImageReader != null)
            mImageReader.close();
        super.closeSession();
    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return null;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        cameraManager.getHandler().post(new ImageSaver(reader.acquireNextImage(), mFile));
    }

    @Override
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
                    StatePictureAndPreview.IStateTakePictureCallback cb = (StatePictureAndPreview.IStateTakePictureCallback) mStateBaseCb;
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
}

