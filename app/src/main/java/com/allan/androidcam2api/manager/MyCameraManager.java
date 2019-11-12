package com.allan.androidcam2api.manager;

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

import com.allan.androidcam2api.State.StateDied;
import com.allan.androidcam2api.State.StatePictureAndPreview;
import com.allan.androidcam2api.State.StatePictureAndRecordAndPreview;
import com.allan.androidcam2api.State.StatePreview;
import com.allan.androidcam2api.State.FeatureUtil;
import com.allan.androidcam2api.function.FunctionRecord;
import com.allan.androidcam2api.function.FunctionTakePicture;
import com.allan.androidcam2api.base.ICameraAction;
import com.allan.androidcam2api.base.IRecordCallback;
import com.allan.androidcam2api.State.AbstractStateBase;
import com.allan.androidcam2api.base.ITakePictureCallback;
import com.allan.androidcam2api.utils.CamLog;
import com.allan.androidcam2api.utils.MyToast;
import com.allan.androidcam2api.utils.Singleton;
import com.allan.androidcam2api.utils.WeakHandler;
import com.allan.androidcam2api.view.CameraViewDelegate;

import java.util.ArrayList;

public class MyCameraManager implements WeakHandler.WeakCallback, ICameraAction {

    /**
     * MyCamera其实是所有功能的入口，相当于CameraManager
     * 这里将MyCamera作为单例公开
     * 采用了camera framework的类似方法创建单例模式
     */
    private static final Singleton<MyCameraManager> me = new Singleton<MyCameraManager>() {
        @Override
        public MyCameraManager create() {
            return new MyCameraManager();
        }
    };

    public static MyCameraManager instance() {
        return me.get();
    }

    private MyCameraManager() {
    }

    //基础
    private WeakHandler mCamHandler = null; //camera线程的handler
    private HandlerThread mSubThread = null; //camera线程
    private Context mContext;
    private CameraViewDelegate mCameraView;
    /////camera信息////
    private AbstractStateBase mCurrentSt; //当前preview的状态
    private CameraCharacteristics mCamChars; //特性
    private CameraDevice mCameraDevice; //camera device
    private int mCameraId;

    private String mRecordPath = null;

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

    private CaptureRequest.Builder previewBuilder = null;
    private CameraCaptureSession camSession = null;

    //Camera打开回调
    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            setCameraDevice(camera);
            mCurrentSt = new StateDied(null);
            transmitModPicturePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            setCameraDevice(null);
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            setCameraDevice(null);
        }
    };
    /////////************** end

    //region 不用关注的set get方法
    public CameraCharacteristics getCameraCharacteristics() {
        return mCamChars;
    }

    public void setPreviewSize(int width, int height) {
        mCameraView.setPreviewSize(width, height);
    }

    public String getRecordPath() {
        return mRecordPath;
    }

    /**
     * 拿到当前View的Surface
     */
    public Surface getSurface() {
        return mCameraView.getSurface();
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.mCameraDevice = cameraDevice;
    }

    public Context getContext() {
        return mContext;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    public Handler getHandler() {
        return mCamHandler;
    }
    //endregion

    //region 不用关注的Handler的Message what
    private static final int OPEN = 100;
    private static final int CLOSE = 101;
    private static final int MOD_PREVIEW = 102;
    private static final int MOD_PIC = 103;
    private static final int MOD_PREVIEW_PIC = 104;
    private static final int MOD_PREVIEW_RECORD_PIC = 105;

    private static final int STOP_PREVIEW = 107;

    private static final int START_REC = 108;
    private static final int STOP_REC = 109;
    private static final int TAKE_PIC = 110;
    //endregion

    public void init(Context context, CameraViewDelegate vd) {
        mCameraView = vd;
        mContext = context;
        mSubThread = new HandlerThread("Camera-thread");
        mSubThread.start();
        mCamHandler = new WeakHandler(this, mSubThread.getLooper());
    }

    public void destroy() {
        mCamHandler.removeCallbacksAndMessages(null);
        mSubThread.quit();
    }

    private void openCameraFirst(Message msg) {
        CamLog.d("Goto preview mode error cause it's deed");
        Message thisMsg = mCamHandler.obtainMessage();
        thisMsg.copyFrom(msg);
        mCamHandler.sendMessageAtFrontOfQueue(msg); //拷贝一次 发出去
        mCamHandler.sendMessageAtFrontOfQueue(mCamHandler.obtainMessage(OPEN)); //再将开启消息传递到前面去
    }

    @Override
    public void onHandler(Message msg) {
        View toastParentView = mCameraView.getView();
        switch (msg.what) {
            case OPEN: {
                if (mCurrentSt != null) {
                    CamLog.e("Error if statemachine is not null!");
                    return;
                }

                CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                try {
                    String[] list = manager.getCameraIdList();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                setPreviewSize(1920, 1080); //其实如果这里不设置，系统会camera显示的区域更多。我认为谷歌在camera的显示区域这块这些不够好
                int camId = CameraCharacteristics.LENS_FACING_FRONT;
                mCameraId = camId;
                try {
                    mCamChars = manager.getCameraCharacteristics("" + camId);
                    StreamConfigurationMap map = mCamChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map == null) {
                        CamLog.e("map = null");
                        return;
                    }

                    manager.openCamera("" + camId, mCameraStateCallback, mCamHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                    MyToast.alertDialog(mContext, "第一次授权，请重启");
                }
            }
            break;
            case CLOSE: {
                if (mCurrentSt != null) {
                    mCurrentSt.closeSession();
                    mCurrentSt = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
            break;
            case TAKE_PIC: {
                if ((mCurrentSt.getId() & FeatureUtil.FEATURE_PICTURE) == FeatureUtil.FEATURE_PICTURE) {
                    if (mCurrentSt instanceof StatePictureAndPreview) {
                        StatePictureAndPreview spp = (StatePictureAndPreview) mCurrentSt;
                        FunctionTakePicture objStruct = (FunctionTakePicture) msg.obj;
                        spp.takePicture(objStruct.dir, objStruct.name, objStruct.func);
                    } else if (mCurrentSt instanceof StatePictureAndRecordAndPreview) { //TODO record pciture preview
                        StatePictureAndRecordAndPreview sppr = (StatePictureAndRecordAndPreview) mCurrentSt;
                        FunctionTakePicture objStruct = (FunctionTakePicture) msg.obj;
                        sppr.takePicture(objStruct.dir, objStruct.name, objStruct.func);
                    }
                } else { //如果没有Picture属性则报错
                    MyToast.toastNew(getContext(), toastParentView, "No Picture Mod");
                    return;
                }
            }
            break;
            case MOD_PREVIEW_PIC: {
                if (mCurrentSt == null) {
                    openCameraFirst(msg); //达到了状态机，自动进入的目的
                    return;
                }

                if (mCurrentSt.getId() == 0x011) {
                    MyToast.toastNew(getContext(), toastParentView, "Already in this mod");
                    return;
                }
                notifyModChange("Picture&Preview");
                mCurrentSt.closeSession(); //关闭session

                mCurrentSt = new StatePictureAndPreview(MyCameraManager.this);

                try {
                    mCurrentSt.createSession(new StatePictureAndPreview.IStateTakePictureCallback() {
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
            case MOD_PREVIEW: {
                if (mCurrentSt == null) {
                    openCameraFirst(msg); //达到了状态机，自动进入的目的
                    return;
                }
                if (mCurrentSt.getId() == 0x001) {
                    MyToast.toastNew(getContext(), toastParentView, "Already in this mod");
                    return;
                }
                notifyModChange("Preview");
                mCurrentSt.closeSession(); //关闭session

                mCurrentSt = new StatePreview(MyCameraManager.this);

                try {
                    mCurrentSt.createSession(new StatePreview.IStatePreviewCallback() {
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
            case STOP_PREVIEW: {
                if (mCurrentSt != null) {
                    mCurrentSt.closeSession();
                    mCurrentSt = null;
                }
            }
            break;
            case START_REC: {
                if (mCurrentSt == null) {
                    openCameraFirst(msg); //达到了状态机，自动进入的目的
                    return;
                }
                if (mCurrentSt.getId() == 0x111) {
                    MyToast.toastNew(getContext(), toastParentView, "Already in this mod");
                    return;
                }
                mCurrentSt.closeSession(); //关闭session
                FunctionRecord objStruct = (FunctionRecord) msg.obj;
                final IRecordCallback func = objStruct.func;
                mRecordPath = objStruct.path;
                StatePictureAndRecordAndPreview sprp = new StatePictureAndRecordAndPreview(MyCameraManager.this);
                mCurrentSt = sprp;
                try {
                    mCurrentSt.createSession(new StatePictureAndRecordAndPreview.IStateTakePictureRecordCallback() {
                        @Override
                        public void onRecordStart(boolean suc) {
                            transmitModVideoPicturePreview();
                            func.onRecordStart(suc);
                        }

                        @Override
                        public void onRecordError(int err) {
                            //TODO
                            //完成后，退回之前的state，这里直接回到PreviewAndPicture
                            transmitModPicturePreview();
                        }

                        @Override
                        public void onRecordEnd(String path) {
                            //完成后，退回之前的state，这里直接回到PreviewAndPicture
                            transmitModPicturePreview();
                            func.onRecordEnd(path);
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
            case STOP_REC: {
                if (mCurrentSt == null) {
                    MyToast.toastNew(getContext(), toastParentView, "No mod");
                    return;
                }
                if (mCurrentSt.getId() != 0x111) {
                    MyToast.toastNew(getContext(), toastParentView, "Not in recordMod");
                    return;
                }
                StatePictureAndRecordAndPreview sppr = (StatePictureAndRecordAndPreview) mCurrentSt;
                sppr.stopRecord();
            }
            break;

        }
    }

    //region 操作MyCamera进入Handler处理的方法们
    @Override
    public boolean openCamera() {
        mCamHandler.sendEmptyMessage(OPEN);
        return false;
    }

    @Override
    public void closeCamera() {
        mCamHandler.removeCallbacksAndMessages(null);
        mCamHandler.sendEmptyMessage(CLOSE);
    }

    @Override
    public boolean transmitModPreview() {
        mCamHandler.sendEmptyMessage(MOD_PREVIEW);
        return true;
    }

    @Override
    public boolean transmitModPicturePreview() {
        mCamHandler.sendEmptyMessage(MOD_PREVIEW_PIC);
        return false;
    }

    private boolean transmitModVideoPicturePreview() {
        //那么注意了，录制的State并不是直接类似preivew&Picture模式，可以直接进入。
        //这个进入是依赖于record的开启，退出也依赖于record的停止
        notifyModChange("Preview&pic&rec");
        return false;
    }

    public void stopPreview() {
        mCamHandler.sendEmptyMessage(STOP_PREVIEW);
    }

    /**
     * 在含有picture模式下才能去takePicture
     */
    public void takePicture(String dir, String name, ITakePictureCallback func) {
        FunctionTakePicture objStruct = new FunctionTakePicture(dir, name, func);
        mCamHandler.sendMessage(mCamHandler.obtainMessage(TAKE_PIC, objStruct));
    }

    /**
     * 不同于拍照，开始录制，是必须切换模式的。因此相当于transmitMod createSession并传递了record
     */
    public void startRecord(String path, IRecordCallback func) {
        FunctionRecord objStruct = new FunctionRecord(path, func);
        mCamHandler.sendMessage(mCamHandler.obtainMessage(START_REC, objStruct));
    }

    public void stopRecord() {
        mCamHandler.sendEmptyMessage(STOP_REC);
    }

    //endregion

    //region 注册监听模式变化
    private ArrayList<ModChange> mModChanges = null;

    private void notifyModChange(String s) {
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
        mModChanges.add(change);
    }

    public synchronized void removeModChanged(ModChange change) {
        mModChanges.remove(change);
    }
    //endregion
}