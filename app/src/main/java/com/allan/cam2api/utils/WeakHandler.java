package com.allan.cam2api.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class WeakHandler extends Handler {
    public interface WeakCallback {
        void onHandler(Message msg);
    }

    private WeakReference<WeakCallback> wf;

    public WeakHandler(WeakCallback t, Looper looper) {
        super(looper);
        wf = new WeakReference<>(t);
    }

    @Override
    public final void handleMessage(@NonNull Message msg) {
        WeakCallback wc = wf.get();
        if (wc == null) return;
        wc.onHandler(msg);
    }
}
