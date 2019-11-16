package com.allan.cam2api.states;

import android.hardware.camera2.CameraCaptureSession;

import com.allan.cam2api.manager.MyCameraManager;

/**
 * 一个特例，我只想用这个类来描述camera 没有open或者died的状态
 */
public class StateDied extends AbstractStateBase {
    public StateDied(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getId() {
        return FeatureUtil.FEATURE_NONE;
    }

    @Override
    protected void step0_createSurfaces() {
    }

    @Override
    protected void step2_addTargets() {

    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return null;
    }
}
