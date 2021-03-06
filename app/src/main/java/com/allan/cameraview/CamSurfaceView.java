package com.allan.cameraview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.allan.utils.CamLog;

public class CamSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private IViewStatusChangeCallback mCallback;

    public void setCallback(IViewStatusChangeCallback mCallback) {
        this.mCallback = mCallback;
    }

    public CamSurfaceView(Context context) {
        super(context);
        init();
    }

    public CamSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CamSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CamLog.d("SurfaceCreated");
        if (mCallback != null) mCallback.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        CamLog.d("SurfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CamLog.d("surfaceDestroyed");
        if (mCallback != null) mCallback.onSurfaceDestroyed();
    }

}
