package com.allan.androidcam2api;

import android.view.View;

import com.allan.androidcam2api.manager.MyCameraManager;
import com.allan.androidcam2api.view.CameraViewDelegate;
import com.allan.androidcam2api.view.IViewStatusChangeCallback;

import java.lang.ref.WeakReference;

public class MainActivityCameraViewPresent implements IViewStatusChangeCallback{
    private WeakReference<MainActivity> mActivity;
    private CameraViewDelegate mCameraView;

    MainActivityCameraViewPresent(MainActivity context) {
        mActivity = new WeakReference<>(context);
    }

    void setCameraView(View view) {
        mCameraView = new CameraViewDelegate(view);
        mCameraView.setCallback(this);
    }

    @Override
    public void onSurfaceCreated() {
        if (mActivity.get() != null) {
            MainActivity mainActivity = mActivity.get();
            MyCameraManager.instance().init(mainActivity, mCameraView);
            MyCameraManager.instance().addModChanged(mainActivity);
            MyCameraManager.instance().openCamera();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        MyCameraManager.instance().closeCamera();
    }
}
