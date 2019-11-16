package com.allan.cam2api.manager;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.allan.cam2api.states.AbstractStateBase;
import com.allan.cam2api.states.FeatureUtil;
import com.allan.cam2api.states.StateDied;
import com.allan.cam2api.states.StatePictureAndPreview;
import com.allan.cam2api.states.StatePictureAndRecordAndPreview;
import com.allan.cam2api.states.StatePreview;
import com.allan.cam2api.states.image.IActionTakePicture;
import com.allan.cam2api.states.record.IActionRecord;
import com.allan.cam2api.base.IRecordCallback;
import com.allan.cam2api.function.FunctionRecord;
import com.allan.cam2api.function.FunctionTakePicture;
import com.allan.cam2api.utils.CamLog;
import com.allan.cam2api.utils.MyToast;
import com.allan.cam2api.utils.WeakHandler;

import java.io.File;
import java.lang.ref.WeakReference;

public class MyCameraManagerHandler implements WeakHandler.WeakCallback {
    private WeakHandler camHandler; //camera线程的handler
    WeakHandler getHandler() {return camHandler;}

    private MyCameraManager mManager;
    private WeakReference<Context> mWfContext;

    private HandlerThread mSubThread; //camera线程

    static final int ACTION_CAMERA_OPEN = 100;
    static final int ACTION_CAMERA_CLOSE = 101;

    static final int ACTION_START_REC = 5;  //其实就是将其他状态升级到录制状态去
    static final int TRANSMIT_TO_MODE_RECORD = ACTION_START_REC;  //其实就是将其他状态升级到录制状态去

    static final int ACTION_STOP_REC = 106;
    static final int ACTION_TAKE_PICTURE = 107;

    static final int TRANSMIT_TO_MODE_PREVIEW = 102;
    static final int TRANSMIT_TO_MODE_PICTURE_PREVIEW = 103;

    private int mDefaultTransmitIndex;

    MyCameraManagerHandler(MyCameraManager manager, Context context) {
        init(manager, context, TRANSMIT_TO_MODE_PICTURE_PREVIEW);
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
        camHandler.sendMessageAtFrontOfQueue(camHandler.obtainMessage(ACTION_CAMERA_OPEN)); //再将开启消息传递到前面去。这样的话，就优先开启
    }

    @Override
    public void onHandler(Message msg) {
        View toastParentView = mManager.getView();
        switch (msg.what) {
            case ACTION_CAMERA_OPEN: {
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
            case ACTION_CAMERA_CLOSE: {
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
            case ACTION_TAKE_PICTURE: {
                AbstractStateBase st = mManager.getCurrentState();
                int featureId = st.getId();
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
            case TRANSMIT_TO_MODE_PICTURE_PREVIEW: {
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PICTURE_PREVIEW mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }

                if (curSt.getId() == (FeatureUtil.FEATURE_PREVIEW |FeatureUtil.FEATURE_PICTURE)) {
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Already in this mod");
                    }
                    return;
                }

                mManager.notifyModChange("Picture&Preview");
                curSt.closeSession(); //关闭session

                curSt = new StatePictureAndPreview(mManager);
                mManager.setCurrentState(curSt);

                try {
                    curSt.createSession(new StatePictureAndPreview.IStateTakePictureCallback() {
                        @Override
                        public void onPictureToken(String path) {
                            CamLog.d("onToken in path" + path);
                        }

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
            case TRANSMIT_TO_MODE_PREVIEW: {
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PREVIEW mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }
                if (curSt.getId() == FeatureUtil.FEATURE_PREVIEW) {
                    if (mWfContext.get() != null) {
                        MyToast.toastNew(mWfContext.get(), toastParentView, "Already in this mod");
                    }
                    return;
                }

                mManager.notifyModChange("Preview");
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
            case ACTION_START_REC: { //其实就是将其他状态升级到录制状态去
                AbstractStateBase curSt = mManager.getCurrentState();
                if (curSt == null) {
                    CamLog.d("Goto (ACTION_START_REC / transmit to RECORD) mode error cause it's deed");
                    ifCurrentStNullOpenCameraFirst(msg);
                    return;
                }
                if (curSt.getId() == (FeatureUtil.FEATURE_PICTURE | FeatureUtil.FEATURE_PREVIEW | FeatureUtil.FEATURE_RECORD_VIDEO)) {
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
                            mManager.notifyModChange("Preview&pic&rec");
                            callback.onRecordStart(suc);
                        }

                        @Override
                        public void onRecordError(int err) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordFailed(err);
                            camHandler.sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW);
                        }

                        @Override
                        public void onRecordEnd(String path) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordEnd(path);
                            camHandler.sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW);
                        }

                        @Override
                        public void onPictureToken(String path) {
                            CamLog.d("rec:onToken in path" + path);
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
            case ACTION_STOP_REC: {
                if (mManager.getCurrentState() == null ||
                        mManager.getCurrentState().getId() !=
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
