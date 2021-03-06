package com.allan.camera2api;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Surface;

import com.allan.utils.CamLog;

import java.util.Comparator;
import java.util.List;

/**
 * 抽象类，用于描述，不同的camera session状态
 * 这个类的子类都将是处于camera open之后的状态（StateDied除外）
 */
public abstract class AbstractStateBase {
    /**
     * 监听创建session的状态变化
     */
    protected interface IStateBaseCallback {
    }

    /**
     * 当前类支持的能力组合
     */
    public int getFeatureId() {
        return FeatureUtil.FEATURE_NONE;
    }

    protected MyCameraManager cameraManager;

    protected IStateBaseCallback mStateBaseCb;

    /**
     * 对于在创建会话的时候，必须将所有用到的surface(包含take picture)都贴入createSession里面
     * 与sessionSurfaces差异是：后者用于addTarget使用的时候，这是创建基础使用，不包含takePicture的ImageReader
     */
    protected List<Surface> allIncludePictureSurfaces;
    protected List<Surface> addTargetSurfaces;

    protected AbstractStateBase(MyCameraManager mc) {
        cameraManager = mc;
        step0_createSurfaces();
    }

    /**
     * 在类初始化的时候被调用。你不应该调用它，只需要实现它。
     * <p>
     * 在camera open之后，session创建之前
     * 根据不同的state，组合不同的surface
     */
    protected abstract void step0_createSurfaces();

    /**
     * 根据不同的state，贴入不同的surface
     * <p>
     * 你不应该调用它，只需要实现它
     */
    private void step2_addTargets() {
        for (Surface surface : addTargetSurfaces) {
            cameraManager.getPreviewBuilder().addTarget(surface);
        }
    }

    public void closeSession() {
        if (cameraManager != null) {
            if (cameraManager.getCamSession() != null) {
                cameraManager.getCamSession().close();
                cameraManager.setCamSession(null);
            } else {
                CamLog.d("no camera cam session");
            }
        }
        if (addTargetSurfaces != null) {
            addTargetSurfaces.clear();
        }
        addTargetSurfaces = null;

        if (allIncludePictureSurfaces != null) {
            allIncludePictureSurfaces.clear();
        }
        allIncludePictureSurfaces = null;
    }

    /**
     * 不同的session下有不同的模式
     * 子类可以根据需要覆写该方法。
     */
    protected int step1_getTemplateType() {
        return CameraDevice.TEMPLATE_PREVIEW;
    }

    /**
     * 子类必须实现，而不应该调用
     * 创建一个监听完成session的回调信息，并将StateBaseCb外部监听处理
     */
    protected abstract CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback();

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    public final boolean createSession(IStateBaseCallback cb) {
        mStateBaseCb = cb;
        try {
            cameraManager.setPreviewBuilder(cameraManager.getCameraDevice().createCaptureRequest(step1_getTemplateType()));
            step2_addTargets();
            cameraManager.getCameraDevice().createCaptureSession(allIncludePictureSurfaces, createCameraCaptureSessionStateCallback(), cameraManager.getHandler());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    protected final Size setSize(int wishWidth, int wishHeight) {
        StreamConfigurationMap map = cameraManager.getCameraCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = null;
        if (map != null) {
            sizes = map.getOutputSizes(ImageFormat.JPEG);
        }
        Size needSize = null;

        if (sizes != null) {
            for (Size size : sizes) {
                if (needSize == null) {
                    needSize = size;
                }

                if (size.getHeight() == wishHeight || size.getHeight() == wishWidth) { //TODO 这里是随便写写的。你可以采用google的compare class
                    needSize = size;
                    break;
                }
            }
        }

        CamLog.d("after wish size " + needSize);

        if (needSize == null) {
            throw new RuntimeException("No need Camera Size!");
        }

        cameraManager.setPreviewSize(needSize.getWidth(), needSize.getHeight());
        return needSize;
    }
}
