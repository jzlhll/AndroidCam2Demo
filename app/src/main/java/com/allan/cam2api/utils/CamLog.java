package com.allan.cam2api.utils;

import android.util.Log;

public class CamLog {
    private static final String TAG = "allan_cam";

    public static void d(String s) {
        Log.w(TAG, s);
    }

    public static void e(String s) {
        Log.e(TAG, s);
    }

    public static void d(String tag, String s) {
        Log.w(TAG, "" + tag + ": " + s);
    }

    public static void e(String tag, String s) {
        Log.e(TAG, "" + tag + ": " + s);
    }
}
