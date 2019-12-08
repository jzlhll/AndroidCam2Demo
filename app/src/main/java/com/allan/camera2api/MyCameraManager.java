package com.allan.camera2api;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;

import com.allan.activity.FirstActivity;
import com.allan.base.FunctionRecord;
import com.allan.base.FunctionTakePicture;
import com.allan.base.IActionRecord;
import com.allan.base.IActionTakePicture;
import com.allan.base.ICameraAction;
import com.allan.base.IRecordCallback;
import com.allan.base.ITakePictureCallback;
import com.allan.utils.CamLog;
import com.allan.utils.MyToast;
import com.allan.utils.Singleton;
import com.allan.cameraview.CameraViewDelegate;
import com.allan.utils.WeakHandler;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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

    public void create(Context context, CameraViewDelegate vd, int defaultTransmitId) {
        mCameraView = vd;
        if (mCamHandler == null) {
            mCamHandler = new MyCameraManagerHandler(MyCameraManager.this, context, defaultTransmitId);
        }
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
    public void closeSession() {
        CamLog.d("close Session in manage!");
        if (mCamHandler != null && mCamHandler.getHandler() != null) {
            mCamHandler.getHandler().sendEmptyMessage(ACTION_CLOSE_SESSION);
        }
    }

    @Override
    public void transmitModById(int transmitId) {
        mCamHandler.getHandler().sendEmptyMessage(transmitId);
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

    public static final int ACTION_CAMERA_OPEN = 11;
    public static final int ACTION_CAMERA_CLOSE = 12;
    public static final int ACTION_CLOSE_SESSION = 13;

    public static final int ACTION_START_REC = 5;  //其实就是将其他状态升级到录制状态去
    public static final int TRANSMIT_TO_MODE_RECORD = ACTION_START_REC;  //其实就是将其他状态升级到录制状态去

    public static final int ACTION_TAKE_PICTURE = 21;
    public static final int ACTION_STOP_REC = 22;

    public static final int TRANSMIT_TO_MODE_PREVIEW = 103;
    public static final int TRANSMIT_TO_MODE_PICTURE_PREVIEW = 104;
}

class MyCameraManagerHandler implements WeakHandler.WeakCallback {
    private WeakHandler camHandler; //camera线程的handler
    WeakHandler getHandler() {return camHandler;}

    private MyCameraManager mManager;
    private WeakReference<Context> mWfContext;

    private HandlerThread mSubThread; //camera线程

    private int mDefaultTransmitIndex;

    MyCameraManagerHandler(MyCameraManager manager, Context context) {
        init(manager, context, MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW);
    }

    MyCameraManagerHandler(MyCameraManager manager, Context context, int defaultTransmitIndex) {
        init(manager, context, defaultTransmitIndex);
    }

    private void init(MyCameraManager manager, Context context, int defaultTransmitIndex) {
        mManager = manager;
        mDefaultTransmitIndex = defaultTransmitIndex;
        mWfContext = new WeakReference<>(context);
        mSubThread = new HandlerThread("Camera-thread");
        mSubThread.start();
        camHandler = new WeakHandler(this, mSubThread.getLooper());
    }

    void destroy() {
        camHandler.removeCallbacksAndMessages(null);
        mSubThread.quit();
    }

    //Camera打开回调
    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mManager.setCameraDevice(camera);
            camHandler.sendEmptyMessage(mDefaultTransmitIndex);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mManager.setCameraDevice(null);
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mManager.setCameraDevice(null);
        }
    };

    private void ifCurrentStNullOpenCameraFirst(Message msg) {
        Message thisMsg = camHandler.obtainMessage();
        thisMsg.copyFrom(msg);
        camHandler.sendMessageAtFrontOfQueue(msg); //拷贝一次 发出去
        camHandler.sendMessageAtFrontOfQueue(camHandler.obtainMessage(MyCameraManager.ACTION_CAMERA_OPEN)); //再将开启消息传递到前面去。这样的话，就优先开启
    }

    @Override
    public void handleMessage(Message msg) {
        View toastParentView = mManager.getView();
        switch (msg.what) {
            case MyCameraManager.ACTION_CAMERA_OPEN: {
                if (mManager.getCurrentState() != null) {
                    CamLog.e("Error if state machine is not null!");
                    return;
                }
                Context mContext = mWfContext.get();
                if (mContext == null) {
                    return;
                }
                CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                try {
                    String[] list = manager != null ? manager.getCameraIdList() : null;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                mManager.setPreviewSize(1920, 1080); //TODO 其实如果这里不设置，系统会camera显示的区域更多。我认为谷歌在camera的显示区域这块这些不够好

                int camId = CameraCharacteristics.LENS_FACING_FRONT;
                mManager.setCameraId(camId);

                mManager.setCurrentState(new StateDied(mManager));

                try {
                    assert manager != null;
                    mManager.setCameraCharacteristics(manager.getCameraCharacteristics("" + camId));
                    StreamConfigurationMap map = mManager.getCameraCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map == null) {
                        CamLog.e("map = null");
                        return;
                    }

                    manager.openCamera("" + camId, mCameraStateCallback, camHandler);
                } catch (CameraAccessException | NullPointerException | SecurityException e) {
                    e.printStackTrace();
                }
            }
            break;
            case MyCameraManager.ACTION_CAMERA_CLOSE: {
                CamLog.d("close Camera in ACTION_CAMERA _CLOSE!");
                if (mManager.getCurrentState() != null) {
                    mManager.getCurrentState().closeSession();
                    mManager.setCurrentState(null);
                }
                if (null != mManager.getCameraDevice()) {
                    mManager.getCameraDevice().close();
                    mManager.setCameraDevice(null);
                }
            }
            break;
            case MyCameraManager.ACTION_CLOSE_SESSION:
                CamLog.d("close Camera in ACTION_CAMERA _CLOSE!");
                if (mManager.getCurrentState() != null) {
                    mManager.getCurrentState().closeSession();
                    mManager.setCurrentState(new StateDied(mManager));
                }
                break;
            case MyCameraManager.ACTION_TAKE_PICTURE: {
                AbstractStateBase st = mManager.getCurrentState();
                int featureId = st.getFeatureId();
                if ((featureId & FeatureUtil.FEATURE_PICTURE) == FeatureUtil.FEATURE_PICTURE) {
                    if (st instanceof IActionTakePicture) {
                        IActionTakePicture action = (IActionTakePicture) st;
                        FunctionTakePicture func = (FunctionTakePicture) msg.obj;
                        action.takePicture(func.dir, func.name, func.callback);
                    } else {
                        Context mContext = mWfContext.get();
                        if (mContext == null) {
                            return;
                        }
                        MyToast.toastNew(mContext, toastParentView, "不可能，必须实现了IActionTakePicture接口");
                    }
                } else { //如果没有Picture属性则报错
                    Context mContext = mWfContext.get();
                    if (mContext == null) {
                        return;
                    }
                    MyToast.toastNew(mContext, toastParentView, "No Picture Feature");
                    return;
                }
            }
            break;
            case MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW: {
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PICTURE_PREVIEW mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }

                if (curSt.getFeatureId() == (FeatureUtil.FEATURE_PREVIEW |FeatureUtil.FEATURE_PICTURE)) {
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Already in this mod");
                    }
                    return;
                }

                mManager.notifyModChange(FirstActivity.MODE_PREVIEW_PICTURE);
                curSt.closeSession(); //关闭session

                curSt = new StatePictureAndPreview(mManager);
                mManager.setCurrentState(curSt);

                try {
                    curSt.createSession(new StatePictureAndPreview.IStatePreviewCallback() {
                        @Override
                        public void onPreviewSucceeded() {
                            CamLog.d("onPreviewSucceeded in myacmera");
                        }

                        @Override
                        public void onPreviewFailed() {
                            CamLog.d("onPreviewFailed in myacmera");
                        }
                    });
                } catch (Exception e) {
                    CamLog.e("start preview err0");
                    e.printStackTrace();
                }
            }
            break;
            case MyCameraManager.TRANSMIT_TO_MODE_PREVIEW: {
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PREVIEW mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }
                if (curSt.getFeatureId() == FeatureUtil.FEATURE_PREVIEW) {
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Already in this mod");
                    }
                    return;
                }

                mManager.notifyModChange(FirstActivity.MODE_PREVIEW);
                curSt.closeSession(); //关闭session

                curSt = new StatePreview(mManager);
                mManager.setCurrentState(curSt);

                try {
                    curSt.createSession(new StatePreview.IStatePreviewCallback() {
                        @Override
                        public void onPreviewSucceeded() {
                            CamLog.d("onPreviewSucceeded in myacmera");
                        }

                        @Override
                        public void onPreviewFailed() {
                            CamLog.d("onPreviewFailed in myacmera");
                        }
                    });
                } catch (Exception e) {
                    CamLog.e("start preview err0");
                    e.printStackTrace();
                }
            }
            break;
            //case TRANSMIT_TO_MODE_RECORD:
            case MyCameraManager.ACTION_START_REC: { //其实就是将其他状态升级到录制状态去
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto (ACTION_START_REC / transmit to RECORD) mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }
                if (curSt.getFeatureId() == (FeatureUtil.FEATURE_PICTURE | FeatureUtil.FEATURE_PREVIEW | FeatureUtil.FEATURE_RECORD_VIDEO)) {
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Already in this mod");
                    }
                    return;
                }

                curSt.closeSession(); //关闭session

                FunctionRecord func = (FunctionRecord) msg.obj;
                final IRecordCallback callback = func.callback;
                CamLog.d("setRecordPath ");
                mManager.setRecordFilePath(func.path + File.separator + func.name); //TODO 由于createSurfaces是在构造函数中调用，没法直接传递参数
                curSt = new StatePictureAndRecordAndPreview(mManager);
                mManager.setCurrentState(curSt);
                try {
                    curSt.createSession(new StatePictureAndRecordAndPreview.IStateTakePictureRecordCallback() {
                        @Override
                        public void onRecordStart(boolean suc) {
                            mManager.notifyModChange(FirstActivity.MODE_PicturePreviewVideo);
                            callback.onRecordStart(suc);
                        }

                        @Override
                        public void onRecordError(int err) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordFailed(err);
                            camHandler.sendEmptyMessage(MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW);
                        }

                        @Override
                        public void onRecordEnd(String path) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordEnd(path);
                            camHandler.sendEmptyMessage(MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW);
                        }

                        @Override
                        public void onPreviewSucceeded() {
                            CamLog.d("rec:onPreviewSucceeded in myacmera");
                        }

                        @Override
                        public void onPreviewFailed() {
                            CamLog.d("rec:onPreviewFailed in myacmera");
                        }
                    });
                } catch (Exception e) {
                    CamLog.e("rec:start preview err0");
                    e.printStackTrace();
                }
            }
            break;
            case MyCameraManager.ACTION_STOP_REC: {
                if (mManager.getCurrentState() == null ||
                        mManager.getCurrentState().getFeatureId() !=
                                (FeatureUtil.FEATURE_PICTURE | FeatureUtil.FEATURE_PREVIEW | FeatureUtil.FEATURE_RECORD_VIDEO)) {
                    CamLog.d("为null不用工作。");
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Not in recordMod");
                    }
                    return;
                }

                IActionRecord actionRecord = (IActionRecord) mManager.getCurrentState();
                actionRecord.stopRecord();
            }
            break;

        }
    }
}
