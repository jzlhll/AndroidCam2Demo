package com.allan.cam2api;

import android.view.View;

import com.allan.cam2api.manager.MyCameraManager;
import com.allan.cam2api.cameraview.CameraViewDelegate;
import com.allan.cam2api.cameraview.IViewStatusChangeCallback;
import com.allan.cam2api.utils.CamLog;

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

    public void openCamera() {
        MyCameraManager.instance().openCamera();
    }

    @Override
    public void onSurfaceCreated() {
        if (mActivity.get() != null) {
            MainActivity mainActivity = mActivity.get();
            MyCameraManager myCameraManager = (MyCameraManager) MyCameraManager.instance();
            //这里进行强转。不应该使用。
            CamLog.d("Inited!!!!");
            myCameraManager.init(mainActivity, mCameraView);
            myCameraManager.addModChanged(mainActivity);
            mainActivity.openCamera();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        MyCameraManager.instance().closeCamera();
    }
}
