package com.allan.androidcam2api.State;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.view.Surface;

import com.allan.androidcam2api.manager.MyCameraManager;
import com.allan.androidcam2api.utils.CamLog;

import java.util.List;

/**
 * 抽象类，用于描述，不同的camera session状态
 * 这个类的子类都将是处于camera open之后的状态（StateDied除外）
 */
public abstract class AbstractStateBase {
    /**
     * 监听创建session的状态变化
     */
    public interface IStateBaseCallback {
    }

    /**
     * 当前类支持的能力组合
     */
    public int getId() {
        return FeatureUtil.FEATURE_NONE;
    }

    protected MyCameraManager cameraManager;

    protected IStateBaseCallback mStateBaseCb;

    protected List<Surface> camSurfaces;

    public AbstractStateBase(MyCameraManager mc) {
        cameraManager = mc;
        createSurfaces();
    }

    /**
     * 在类初始化的时候被调用。你不应该调用它，只需要实现它。
     * <p>
     * 在camera open之后，session创建之前
     * 根据不同的state，组合不同的surface
     */
    protected abstract void createSurfaces();

    /**
     * 根据不同的state，贴入不同的surface
     * <p>
     * 你不应该调用它，只需要实现它
     */
    protected abstract void addTarget();

    public void closeSession() {
        if (cameraManager != null) {
            if (cameraManager.getCamSession() != null) {
                cameraManager.getCamSession().close();
                cameraManager.setCamSession(null);
            } else {
                CamLog.d("no camera cam session");
            }
        }
        if (camSurfaces != null) {
            camSurfaces.clear();
        }
        camSurfaces = null;
    }

    /**
     * 不同的session下有不同的模式
     * 子类可以根据需要覆写该方法。
     */
    protected int getTemplateType() {
        return CameraDevice.TEMPLATE_PREVIEW;
    }

    /**
     * 子类必须实现，而不应该调用
     * 创建一个监听完成session的回调信息，并将StateBaseCb外部监听处理
     */
    protected abstract CameraCaptureSession.StateCallback createStateCallback();

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    public boolean createSession(IStateBaseCallback cb) {
        mStateBaseCb = cb;
        try {
            cameraManager.setPreviewBuilder(cameraManager.getCameraDevice().createCaptureRequest(getTemplateType()));
            addTarget();
            cameraManager.getCameraDevice().createCaptureSession(camSurfaces, createStateCallback(), cameraManager.getHandler());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
