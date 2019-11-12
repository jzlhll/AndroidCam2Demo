package com.allan.androidcam2api.view;

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.allan.androidcam2api.utils.CamLog;

import java.lang.ref.WeakReference;

public class CameraViewDelegate {
    private WeakReference<View> mRefView;
    private boolean mIsSurfaceView;

    public CameraViewDelegate(View view) {
        mRefView = new WeakReference<>(view);
        if (view instanceof CamSurfaceView) {
            mIsSurfaceView = true;
        } else if (view instanceof CamTextureView) {
            mIsSurfaceView = false;
        } else {
            throw new RuntimeException("Error");
        }
    }

    public View getView() {
        return mRefView.get();
    }

    public Surface getSurface() {
        View v = mRefView.get();
        if (v == null) {
            return null;
        }

        if (!mIsSurfaceView) {
            return new Surface(((TextureView) v).getSurfaceTexture());
        } else {
            return ((SurfaceView) v).getHolder().getSurface();
        }
    }

    public void setCallback(IViewStatusChangeCallback cb) {
        View v = mRefView.get();
        if (v == null) {
            return;
        }
        if (!mIsSurfaceView) {
            ((CamTextureView) v).setCallback(cb);
        } else {
            ((CamSurfaceView) v).setCallback(cb);
        }
    }

    private void setAspectRatio(int width, int height) {
        View v = mRefView.get();
        if (v == null) {
            return;
        }
        //google 使用setAspectRatio最终通过onMeasure()修改
        //https://blog.csdn.net/yin_guochao/article/details/45796927
        //而onMeasure()控制的是可见区域，并不一定能变化我实际想要的比例
        //mViewSurface.setAspectRatio(width, height);
    }

    public void setPreviewSize(final int width, final int height) {
        CamLog.d("Size: setPreviewSize previewSize " + width + "*" + height);
        View v = mRefView.get();
        if (v == null) {
            return;
        }

        CamLog.d("Size: setPreviewSizeinit camView " + v.getWidth() + "*" + v.getHeight());
        v.post(new Runnable() {
            @Override
            public void run() {
                View v2 = mRefView.get();
                if (v2 == null) {
                    return;
                }
                //Camera2介绍的知识都比较少，介绍surfaceView+cam2就更少。
                // http://book2s.com/java/src/package/android/hardware/camera2/cts/testcases/camera2surfaceviewtestcase.html#ee5c9b91de5483feb8b8f4ecb4f0691b
                //找了很久，才从国外网站找到这个api，注意它的描述，可能需要换到主线程
                //mViewSurface.getHolder().setFixedSize(width, height);
                //一般地，推荐使用TextureView
                //从上述来看，并不需要设置fixSize，只需要搞正确view的大小即可
                setAspectRatio(v2.getHeight() * height / width , v2.getHeight());
            }
        });
    }
}
