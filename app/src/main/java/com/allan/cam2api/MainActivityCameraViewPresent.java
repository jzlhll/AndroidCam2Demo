package com.allan.cam2api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.allan.cam2api.cameraview.CameraViewDelegate;
import com.allan.cam2api.cameraview.IViewStatusChangeCallback;
import com.allan.cam2api.utils.CamLog;
import com.allan.cam2api.utils.WeakHandler;

import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivityCameraViewPresent implements WeakHandler.WeakCallback, IViewStatusChangeCallback, MyCameraManager.ModChange {
    private WeakReference<MainActivity> mActivity;
    private WeakHandler mMainHandler;
    private boolean mIsRealSurfaceCreated = false;

    private CameraViewDelegate mCameraView;
    MainActivityCameraViewPresent(MainActivity context) {
        mActivity = new WeakReference<>(context);
        mMainHandler = new WeakHandler(this, Looper.getMainLooper());

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivity.get() == null) {
                    return;
                }
                SharedPreferences sp = mActivity.get().getSharedPreferences("private", Context.MODE_PRIVATE);
                String mod = sp.getString("lastMod", null);
                transmitToWordsState(mod);
            }
        }, 50);
    }

    //TODO 注意：由于xml默认都是关闭；不会触发surfaceCreated。这里我们将读取上次的记录来初始化状态。
    void initCameraView(View view) {
        mCameraView = new CameraViewDelegate(view);
        mCameraView.setCallback(this);
    }

    static final int RC_ALL_PERMISSION = 101;
    private static final boolean BIG_THAN_6_0 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private boolean mIsHasPermission = false;

    void openCamera() {
        if (mActivity.get() == null) {
            return;
        }
        if (BIG_THAN_6_0) {
            ModelPermissions mp = new ModelPermissions();
            if (EasyPermissions.hasPermissions(mActivity.get(), mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                if (!mIsHasPermission) {
                    mIsHasPermission = true;
                    CamLog.d("has permission setCameraVieeeeeewww");
                    MyCameraManager.instance().openCamera();
                }
            } else {
                CamLog.d("request setCameraVieeeeeewww");
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(mActivity.get(), mp.getShowWords(),
                        RC_ALL_PERMISSION, mp.getPermissions());
            }
        } else {
            CamLog.d("setCameraVieeeeeewww");
            MyCameraManager.instance().openCamera();
        }
    }

    void transmitToWordsState(String words) {
        int transmitId = MyCameraManagerHandler.convertWordsToTransmitId(words);
        if (mIsRealSurfaceCreated) {
            MyCameraManager.instance().transmitModById(transmitId); //如果已经显示了 && 是需要View的情况，直接transmit即可。
        } else {
            mCameraView.getView().setVisibility(View.VISIBLE); //显示出来就会触发surfaceCreated就会okay啦。
        }
    }

    @Override
    public void onSurfaceCreated() {
        mIsRealSurfaceCreated = true;
        _SurfaceCreated(MyCameraManagerHandler.TRANSMIT_TO_MODE_PICTURE_PREVIEW);
    }

    private void _SurfaceCreated(int transmitId) {
        if (mActivity.get() != null) {
            MainActivity mainActivity = mActivity.get();
            MyCameraManager myCameraManager = (MyCameraManager) MyCameraManager.instance();
            //这里进行强转。不应该使用。
            CamLog.d("Inited!!!!");
            myCameraManager.create(mainActivity.getApplicationContext(), mCameraView, transmitId);
            myCameraManager.addModChanged(mainActivity);
            myCameraManager.addModChanged(MainActivityCameraViewPresent.this);
            openCamera();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        mIsRealSurfaceCreated = false;
        MyCameraManager.instance().closeSession();
    }

    void destroy() {
        MyCameraManager.instance().closeCamera();
    }

    @Override
    public void onModChanged(String newMod) {
        if (mActivity.get() == null) {
            return;
        }

        SharedPreferences sharedPreferences = mActivity.get().getSharedPreferences("private", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("lastMod", newMod).apply();
    }

    @Override
    public void onHandler(Message msg) {
    }
}
