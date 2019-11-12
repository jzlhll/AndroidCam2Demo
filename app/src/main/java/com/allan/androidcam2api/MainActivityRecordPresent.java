package com.allan.androidcam2api;

import android.os.Environment;
import android.os.SystemClock;
import android.view.View;

import com.allan.androidcam2api.base.IRecordCallback;
import com.allan.androidcam2api.base.ITakePictureCallback;
import com.allan.androidcam2api.utils.CamLog;
import com.allan.androidcam2api.utils.MyToast;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivityRecordPresent {
    private WeakReference<MainActivity> mActivity;

    boolean isRecording = false;

    private int mTimeSec = 0;

    private int mRunnableIndex = 0;
    private long mRunnableLastTime = 0L;
    private Runnable mTimeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                return;
            }

            if (mRunnableLastTime == 0L) {
                mRunnableLastTime = SystemClock.elapsedRealtime();
            }
            mainActivity.getTimeView().setText(String.format("RecTime: %d", mTimeSec++));
            long delayTime = 1000;
            if (mRunnableIndex++ == 10) {
                mRunnableIndex = 0; //每10s 修正delay的时间
                long cur = SystemClock.elapsedRealtime();
                delayTime = 1000 - (cur - 10000 - mRunnableLastTime);
                if (delayTime < 0) {
                    delayTime = 0;
                }
                mRunnableLastTime = cur;
            }
            mainActivity.getTimeView().postDelayed(mTimeUpdateRunnable, delayTime);
        }
    };

    private String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    void clickOnTakePicture() {
        MyCameraManager.me.get().takePicture(SD_PATH, "pic" + System.currentTimeMillis() + ".png", new ITakePictureCallback() {
            @Override
            public void onPictureToken(String path) {
                if (mActivity.get() != null) {
                    MyToast.toastNew(mActivity.get(), mActivity.get().getTimeView(), "Saved: " + path);
                }
            }
        });
    }

    void clickOnRecord() {
        if (!isRecording) {
            MyCameraManager.me.get().startRecord(SD_PATH + File.separator + "test.mp4", new IRecordCallback() {
                @Override
                public void onRecordStart(boolean suc) {
                    CamLog.d("onRecordStart suc " + suc);
                    isRecording = true;
                    if (mActivity.get() == null) {return;}
                    mActivity.get().getTimeView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity mainActivity = mActivity.get();
                            if (mainActivity == null) {return;}
                            mRunnableLastTime = 0;
                            mRunnableIndex = 0;
                            mainActivity.getRecordBtn().setImageResource(R.drawable.take_rec);
                            mainActivity.getTimeView().setVisibility(View.VISIBLE);
                            mainActivity.getTimeView().getHandler().post(mTimeUpdateRunnable);
                        }
                    });
                }

                @Override
                public void onRecordEnd(String path) {
                    isRecording = false;
                    CamLog.d("onRecordEnd " + path);
                    MainActivity activity = mActivity.get();
                    if (activity == null) {return;}

                    MyToast.toastNew(activity, activity.getTimeView(), "Saved: " + path);
                    activity.getTimeView().getHandler().removeCallbacks(mTimeUpdateRunnable);
                    activity.getTimeView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity mainActivity = mActivity.get();
                            if (mainActivity == null) {return;}
                            mTimeSec = 0;
                            mainActivity.getRecordBtn().setImageResource(R.drawable.take_recing);
                            mainActivity.getTimeView().setVisibility(View.GONE);
                        }
                    });
                }
            });
        } else {
            MyCameraManager.me.get().stopRecord();
        }
    }

    MainActivityRecordPresent(MainActivity context) {
        mActivity = new WeakReference<>(context);
    }
}
