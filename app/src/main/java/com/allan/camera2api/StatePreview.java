package com.allan.camera2api;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;

import androidx.annotation.NonNull;

import com.allan.utils.CamLog;

import java.util.ArrayList;

public class StatePreview extends AbstractStateBase {
    public interface IStatePreviewCallback extends IStateBaseCallback{
        void onPreviewSucceeded();
        void onPreviewFailed();
    }

    Size mNeedSize = null;

    public StatePreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getFeatureId() {
        return FeatureUtil.FEATURE_PREVIEW;
    }

    @Override
    protected void step0_createSurfaces() {
        addTargetSurfaces = new ArrayList<>();
        allIncludePictureSurfaces = new ArrayList<>();
        mNeedSize = setSize(1920, 1080); //这里故意设置一些不同的分辨率,让不同的模式下有不同
        //其实我们可能希望拍照和预览和录像都保持preview size的不变。则这里设置好即可。
        addTargetSurfaces.add(cameraManager.getRealViewSurface());
        allIncludePictureSurfaces.add(cameraManager.getRealViewSurface());
    }

    @Override
    protected CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback() {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                cameraManager.setCamSession(session);
                cameraManager.getPreviewBuilder().set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //camera.previewBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
                try {
                    cameraManager.getCamSession().setRepeatingRequest(cameraManager.getPreviewBuilder().build(),
                            null, cameraManager.getHandler());
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewSucceeded();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                CamLog.e("Error Configure Preview!");
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewFailed();
                }
            }
        };
    }
}

