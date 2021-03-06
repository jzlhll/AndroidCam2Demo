package com.allan.camera2api;

import android.hardware.camera2.CameraCaptureSession;

/**
 * 一个特例，我只想用这个类来描述camera 没有open或者died的状态
 */
public class StateDied extends AbstractStateBase {
    public StateDied(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getFeatureId() {
        return FeatureUtil.FEATURE_NONE;
    }

    @Override
    protected void step0_createSurfaces() {
    }

    @Override
    protected CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback() {
        return null;
    }
}
