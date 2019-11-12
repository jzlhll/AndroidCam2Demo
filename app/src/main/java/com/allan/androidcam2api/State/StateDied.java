package com.allan.androidcam2api.State;

import android.hardware.camera2.CameraCaptureSession;

import com.allan.androidcam2api.manager.MyCameraManager;

/**
 * 一个特例，我只想用这个类来描述camera 没有open或者died的状态
 */
public class StateDied extends AbstractStateBase {
    public StateDied(MyCameraManager mc) {
        super(mc);
    }

    @Override
    public int getId() {
        return FeatureUtil.FEATURE_NONE;
    }

    @Override
    protected void createSurfaces() {
    }

    @Override
    protected void addTarget() {

    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return null;
    }
}
