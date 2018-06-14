package com.allan.androidcam2api.view;

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.allan.androidcam2api.utils.CamLog;

public class IViewDecorator {
    private CamSurfaceView mViewSurface = null;
    private CamTextureView mViewTexture = null;

    public IViewDecorator(View view) {
        if (view instanceof SurfaceView) {
            mViewSurface = (CamSurfaceView) view;
        } else if (view instanceof TextureView) {
            mViewTexture = (CamTextureView) view;
        } else {
            throw new RuntimeException("错误的view");
        }
    }

    public Surface getSurface() {
        if (mViewSurface == null) {
            Surface surface = new Surface(mViewTexture.getSurfaceTexture());
            return surface;
        }

        return mViewSurface.getHolder().getSurface();
    }

    public void setCallback(CamSurfaceView.MyCallback cb) {
        if (mViewSurface != null) {
            mViewSurface.setCallback(cb);
        } else {
            mViewTexture.setCallback(cb);
        }
    }

    private void setAspectRatio(int width, int height) {
        if (mViewSurface != null) {
            //google 使用setAspectRatio最终通过onMeasure()修改
            //https://blog.csdn.net/yin_guochao/article/details/45796927
            //而onMeasure()控制的是可见区域，并不一定能变化我实际想要的比例
            //mViewSurface.setAspectRatio(width, height);
        } else {
            //mViewTexture.setAspectRatio(width, height);
        }
    }

    public void setPreviewSize(final int width, final int height) {
        CamLog.d("Size: setPreviewSize previewSize " + width + "*" + height);

        if (mViewSurface != null) {
            CamLog.d("Size: setPreviewSizeinit camView " + mViewSurface.getWidth() + "*" + mViewSurface.getHeight());
            mViewSurface.post(new Runnable() {
                @Override
                public void run() {

                    //Camera2介绍的知识都比较少，介绍surfaceView+cam2就更少。
                    // http://book2s.com/java/src/package/android/hardware/camera2/cts/testcases/camera2surfaceviewtestcase.html#ee5c9b91de5483feb8b8f4ecb4f0691b
                    //找了很久，才从国外网站找到这个api，注意它的描述，可能需要换到主线程
                    //mViewSurface.getHolder().setFixedSize(width, height);
                    //一般地，推荐使用TextureView
                    //从上述来看，并不需要设置fixSize，只需要搞正确view的大小即可
                    setAspectRatio(mViewSurface.getHeight() * height / width , mViewSurface.getHeight());
                }
            });
        } else {
            mViewTexture.post(new Runnable() {
                @Override
                public void run() {
                    CamLog.d("Size: setPreviewSizeinit camView " + mViewTexture.getWidth() + "*" + mViewTexture.getHeight());
                    mViewTexture.getSurfaceTexture().setDefaultBufferSize(width, height);
                    //从上述来看，并不需要设置fixSize，只需要搞正确view的大小即可
                    setAspectRatio(mViewTexture.getHeight() * height / width, mViewTexture.getHeight());
                }
            });
        }
    }
}
