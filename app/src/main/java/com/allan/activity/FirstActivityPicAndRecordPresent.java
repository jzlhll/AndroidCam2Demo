package com.allan.activity;

import android.os.Environment;
import android.os.SystemClock;
import android.view.View;

import com.allan.base.IRecordCallback;
import com.allan.base.ITakePictureCallback;
import com.allan.camera2api.MyCameraManager;
import com.allan.utils.CamLog;
import com.allan.utils.MyToast;

import java.lang.ref.WeakReference;

public class FirstActivityPicAndRecordPresent {
    private WeakReference<FirstActivity> mActivity;

    boolean isRecording = false;

    private int mTimeSec = 0;

    private int mRunnableIndex = 0;
    private long mRunnableLastTime = 0L;
    private Runnable mTimeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            FirstActivity mainActivity = mActivity.get();
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

    public void clickOnTakePicture() {
        MyCameraManager.instance().takePicture(SD_PATH, "pic" + System.currentTimeMillis() + ".png", new ITakePictureCallback() {
            @Override
            public void onPictureToken(String path) {
                if (mActivity.get() != null) {
                    MyToast.toastNew(mActivity.get(), mActivity.get().getTimeView(), "Saved: " + path);
                }
            }
        });
    }

    public void clickOnRecord() {
        if (!isRecording) {
            MyCameraManager.instance().startRecord(SD_PATH,"test.mp4", new IRecordCallback() {
                @Override
                public void onRecordFailed(int err) {
                    CamLog.d("onRecordFailed " + err);
                }

                @Override
                public void onRecordStart(boolean suc) {
                    CamLog.d("onRecordStart suc " + suc);
                    isRecording = true;
                    if (mActivity.get() == null) {return;}
                    mActivity.get().getTimeView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            FirstActivity mainActivity = mActivity.get();
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
                    FirstActivity activity = mActivity.get();
                    if (activity == null) {return;}

                    MyToast.toastNew(activity, activity.getTimeView(), "Saved: " + path);
                    activity.getTimeView().getHandler().removeCallbacks(mTimeUpdateRunnable);
                    activity.getTimeView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            FirstActivity mainActivity = mActivity.get();
                            if (mainActivity == null) {return;}
                            mTimeSec = 0;
                            mainActivity.getRecordBtn().setImageResource(R.drawable.take_recing);
                            mainActivity.getTimeView().setVisibility(View.GONE);
                        }
                    });
                }
            });
        } else {
            MyCameraManager.instance().stopRecord();
        }
    }

    public FirstActivityPicAndRecordPresent(FirstActivity context) {
        mActivity = new WeakReference<>(context);
    }
}
