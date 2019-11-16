package com.allan.cam2api.states;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.allan.cam2api.manager.MyCameraManager;
import com.allan.cam2api.utils.CamLog;

import java.util.ArrayList;

public class StatePreview extends AbstractStateBase {
    public interface IStatePreviewCallback extends IStateBaseCallback{
        void onPreviewSucceeded();
        void onPreviewFailed();
    }

    public StatePreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getId() {
        return FeatureUtil.FEATURE_PREVIEW;
    }

    @Override
    protected void step0_createSurfaces() {
        camSurfaces = new ArrayList<>();
        setSize(1920, 1080); //这里故意设置一些不同的分辨率,让不同的模式下有不同
        //其实我们可能希望拍照和预览和录像都保持preview size的不变。则这里设置好即可。
        camSurfaces.add(cameraManager.getSurface());
    }

    @Override
    protected void step2_addTargets() {
        for (Surface su : camSurfaces) {
            cameraManager.getPreviewBuilder().addTarget(su);
        }
    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
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
            public void onConfigureFailed(CameraCaptureSession session) {
                CamLog.e("Error Configure Preview!");
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewFailed();
                }
            }
        };
    }
}

