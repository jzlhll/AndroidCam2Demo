package com.allan.cam2api;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.view.Surface;
import android.view.View;

import com.allan.cam2api.function.FunctionRecord;
import com.allan.cam2api.function.FunctionTakePicture;
import com.allan.cam2api.base.ICameraAction;
import com.allan.cam2api.base.IRecordCallback;
import com.allan.cam2api.states.AbstractStateBase;
import com.allan.cam2api.base.ITakePictureCallback;
import com.allan.cam2api.states.image.TakePictureBuilder;
import com.allan.cam2api.utils.CamLog;
import com.allan.cam2api.utils.Singleton;
import com.allan.cam2api.cameraview.CameraViewDelegate;

import java.util.ArrayList;

import static com.allan.cam2api.MyCameraManagerHandler.ACTION_CAMERA_CLOSE;
import static com.allan.cam2api.MyCameraManagerHandler.ACTION_CAMERA_OPEN;
import static com.allan.cam2api.MyCameraManagerHandler.ACTION_START_REC;
import static com.allan.cam2api.MyCameraManagerHandler.ACTION_STOP_REC;
import static com.allan.cam2api.MyCameraManagerHandler.ACTION_TAKE_PICTURE;
import static com.allan.cam2api.MyCameraManagerHandler.TRANSMIT_TO_MODE_PICTURE_PREVIEW;
import static com.allan.cam2api.MyCameraManagerHandler.TRANSMIT_TO_MODE_PREVIEW;

public class MyCameraManager implements ICameraAction {
    private static final Singleton<MyCameraManager> me = new Singleton<MyCameraManager>() {
        @Override
        public MyCameraManager create() {
            return new MyCameraManager();
        }
    };
    /**
     * MyCamera其实是所有功能的入口，相当于CameraManager
     * 这里将MyCamera作为单例公开
     * 采用了camera framework的类似方法创建单例模式
     */
    public static ICameraAction instance() {
        return me.get();
    }

    private MyCameraManager() {
    }

    //基础
    private CameraViewDelegate mCameraView;
    private MyCameraManagerHandler mCamHandler;

    private String mRecordFilePath;
    /////camera信息////
    private AbstractStateBase mCurrentState; //当前preview的状态
    private CameraCharacteristics mCamChars; //特性
    private CameraDevice mCameraDevice; //camera device
    private int mCameraId;
    private CaptureRequest.Builder previewBuilder = null;
    private CameraCaptureSession camSession = null;

    /////////************** end

    public void init(Context context, CameraViewDelegate vd, int defaultTransmitId) {
        mCameraView = vd;
        mCamHandler = new MyCameraManagerHandler(MyCameraManager.this, context, defaultTransmitId);
    }

    public void destroy() {
        mCamHandler.destroy();
    }

    //region 操作MyCamera进入Handler处理的方法们
    @Override
    public void openCamera() {
        mCamHandler.getHandler().sendEmptyMessage(ACTION_CAMERA_OPEN);
    }

    @Override
    public void closeCamera() {
        CamLog.d("close Camera in manage!");
        if (mCamHandler != null && mCamHandler.getHandler() != null) {
            mCamHandler.getHandler().removeCallbacksAndMessages(null);
            mCamHandler.getHandler().sendEmptyMessage(ACTION_CAMERA_CLOSE);
        }
    }

    @Override
    public void transmitModPreview() {
        mCamHandler.getHandler().sendEmptyMessage(TRANSMIT_TO_MODE_PREVIEW);
    }

    @Override
    public void transmitModPicturePreview() {
        mCamHandler.getHandler().sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW);
    }

    /**
     * 在含有picture模式下才能去takePicture
     */
    public void takePicture(String dir, String name, ITakePictureCallback callback) {
        FunctionTakePicture f = new FunctionTakePicture(dir, name, callback);
        mCamHandler.getHandler().sendMessage(mCamHandler.getHandler().obtainMessage(ACTION_TAKE_PICTURE, f));
    }

    /**
     * 不同于拍照，开始录制，是必须切换模式的。因此相当于transmitMod createSession并传递了record
     */
    public void startRecord(String path, String name, IRecordCallback callback) {
        FunctionRecord f = new FunctionRecord(path, name, callback);
        mCamHandler.getHandler().sendMessage(mCamHandler.getHandler().obtainMessage(ACTION_START_REC, f));
    }

    public void stopRecord() {
        mCamHandler.getHandler().sendEmptyMessage(ACTION_STOP_REC);
    }

    //endregion

    //region 注册监听模式变化
    private ArrayList<ModChange> mModChanges = null;

    protected void notifyModChange(String s) {
        if (mModChanges == null) return;
        for (ModChange c : mModChanges) {
            c.onModChanged(s);
        }
    }

    public interface ModChange {
        void onModChanged(String newMod);
    }

    public synchronized void addModChanged(ModChange change) {
        if (mModChanges == null) {
            mModChanges = new ArrayList<>();
        }
        if (!mModChanges.contains(change)) {
            mModChanges.add(change);
        }
    }

    public synchronized void removeModChanged(ModChange change) {
        mModChanges.remove(change);
    }
    //endregion

    //region setter getter
    public CaptureRequest.Builder getPreviewBuilder() {
        return previewBuilder;
    }

    public void setPreviewBuilder(CaptureRequest.Builder previewBuilder) {
        this.previewBuilder = previewBuilder;
    }

    public CameraCaptureSession getCamSession() {
        return camSession;
    }

    public void setCamSession(CameraCaptureSession camSession) {
        this.camSession = camSession;
    }

    public CameraCharacteristics getCameraCharacteristics() {
        return mCamChars;
    }

    public void setCameraCharacteristics(CameraCharacteristics camChars) {
        mCamChars = camChars;
    }

    public void setPreviewSize(int width, int height) {
        mCameraView.setPreviewSize(width, height);
    }

    /**
     * 拿到当前View的Surface
     */
    public Surface getRealViewSurface() {
        return mCameraView.getSurface();
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.mCameraDevice = cameraDevice;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setCameraId(int cameraId) {
         mCameraId = cameraId;
    }

    public CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    public Handler getHandler() {
        return mCamHandler.getHandler();
    }

    View getView() {
        return mCameraView.getView();
    }

    public void setCurrentState(AbstractStateBase state) {
        mCurrentState = state;
    }

    public AbstractStateBase getCurrentState() {
        return mCurrentState;
    }

    public void setRecordFilePath(String filePath) {
        mRecordFilePath = filePath;
    }

    public String getRecordFilePath() {
        return mRecordFilePath;
    }
    //endregion
}
