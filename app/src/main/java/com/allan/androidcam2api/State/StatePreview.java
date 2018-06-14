package com.allan.androidcam2api.State;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Surface;

import com.allan.androidcam2api.base.StateBase;
import com.allan.androidcam2api.MyCameraManager;
import com.allan.androidcam2api.utils.CamLog;

import java.util.ArrayList;
import java.util.Comparator;

public class StatePreview extends StateBase {

    public interface StatePreviewCB extends StateBaseCb{
        void onPreviewSuc();
        void onPreviewErr();
    }

    public StatePreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getId() {
        return 0x001;
    }

    @Override
    protected void createSurfaces() {
        camSurfaces = new ArrayList<>();
        setSize(1920, 1080); //这里故意设置一些不同的分辨率,让不同的模式下有不同
        //其实我们可能希望拍照和预览和录像都保持preview size的不变。则这里设置好即可。
        camSurfaces.add(MyCameraManager.me.get().getSurface());
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    protected final Size setSize(int wishWidth, int wishHeight) {
        StreamConfigurationMap map = MyCameraManager.me.get().getCameraCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
        Size needSize = null;

        for (Size size : sizes) {
            if (needSize == null) {
                needSize = size;
            }

            if (size.getHeight() == wishHeight || size.getHeight() == wishWidth) { //TODO 这里是随便写写的。你可以采用google的compare class
                needSize = size;
                break;
            }
        }

        CamLog.d("after wish size " + needSize);

        if (needSize == null) {
            throw new RuntimeException("No need Camera Size!");
        }

        MyCameraManager.me.get().setPreviewSize(needSize.getWidth(), needSize.getHeight());
        return needSize;
    }

    @Override
    protected void addTarget() {
        for (Surface su : camSurfaces) {
            camera.previewBuilder.addTarget(su);
        }
    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                camera.camSession = session;
                camera.previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //camera.previewBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
                try {
                    camera.camSession.setRepeatingRequest(camera.previewBuilder.build(), null, MyCameraManager.me.get().getHandler());
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if (mStateBaseCb != null) {
                    StatePreviewCB cb = (StatePreviewCB) mStateBaseCb;
                    cb.onPreviewSuc();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                CamLog.e("Error Configure Preview!");
                if (mStateBaseCb != null) {
                    StatePreviewCB cb = (StatePreviewCB) mStateBaseCb;
                    cb.onPreviewErr();
                }
            }
        };
    }
}

