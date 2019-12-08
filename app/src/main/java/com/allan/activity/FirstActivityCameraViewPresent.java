package com.allan.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.allan.AllPermissions;
import com.allan.cameraview.CameraViewDelegate;
import com.allan.cameraview.IViewStatusChangeCallback;
import com.allan.camera2api.MyCameraManager;
import com.allan.utils.CamLog;
import com.allan.utils.MainHandlerUtils;

import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.EasyPermissions;

public class FirstActivityCameraViewPresent implements IViewStatusChangeCallback, MyCameraManager.ModChange {
    private WeakReference<FirstActivity> mActivity;
    private boolean mIsRealSurfaceCreated = false;

    private CameraViewDelegate mCameraView;
    public FirstActivityCameraViewPresent(FirstActivity context) {
        mActivity = new WeakReference<>(context);
        MainHandlerUtils.mainHandler.postDelayed(new Runnable() {
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
    public void initCameraView(View view) {
        mCameraView = new CameraViewDelegate(view);
        mCameraView.setCallback(this);
    }

    private boolean mIsHasPermission = false;

    public void openCamera() {
        if (mActivity.get() == null) {
            return;
        }
        if (AllPermissions.BIG_THAN_6_0) {
            AllPermissions mp = new AllPermissions();
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
                        AllPermissions.RC_ALL_PERMISSION, mp.getPermissions());
            }
        } else {
            CamLog.d("setCameraVieeeeeewww");
            MyCameraManager.instance().openCamera();
        }
    }

    // region convert words transmitId
    private static int convertWordsToTransmitId(String words) {
        if (words == null || words.equals(FirstActivity.MODE_PICTURE_NO_PREVIW)) {
            return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW;
        }

        //TODO 这个方法主要是给默认加载使用；录像一上来就当做预览即可
        if (words.equals(FirstActivity.MODE_PicturePreviewVideo)
                || words.equals(FirstActivity.MODE_PREVIEW_PICTURE)) {
            return MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW;
        }

        if (words.equals(FirstActivity.MODE_PREVIEW)) {
            return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW;
        }

        return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW; //不存在或者修改了都复原
    }
    // endregion

    public void transmitToWordsState(String words) {
        int transmitId = convertWordsToTransmitId(words);
        if (mIsRealSurfaceCreated) {
            MyCameraManager.instance().transmitModById(transmitId); //如果已经显示了 && 是需要View的情况，直接transmit即可。
        } else {
            mCameraView.getView().setVisibility(View.VISIBLE); //显示出来就会触发surfaceCreated就会okay啦。
        }
    }

    @Override
    public void onSurfaceCreated() {
        mIsRealSurfaceCreated = true;
        if (mActivity.get() != null) {
            FirstActivity mainActivity = mActivity.get();
            MyCameraManager myCameraManager = (MyCameraManager) MyCameraManager.instance();
            //这里进行强转。不应该使用。
            CamLog.d("Inited!!!!");
            myCameraManager.create(mainActivity.getApplicationContext(), mCameraView,
                    MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW);
            myCameraManager.addModChanged(mainActivity);
            myCameraManager.addModChanged(FirstActivityCameraViewPresent.this);
            openCamera();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        mIsRealSurfaceCreated = false;
        MyCameraManager.instance().closeSession();
    }

    public void destroy() {
        MyCameraManager.instance().closeCamera();
        mCameraView = null;
    }

    @Override
    public void onModChanged(String newMod) {
        if (mActivity.get() == null) {
            return;
        }

        SharedPreferences sharedPreferences = mActivity.get().getSharedPreferences("private", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("lastMod", newMod).apply();
    }
}
