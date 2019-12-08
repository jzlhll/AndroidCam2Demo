package com.allan.utils;

import android.os.Looper;

public class MainHandlerUtils {
    public static WeakHandler mainHandler = new WeakHandler(null, Looper.getMainLooper());
}
