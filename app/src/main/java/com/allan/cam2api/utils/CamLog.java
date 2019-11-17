package com.allan.cam2api.utils;

import android.util.Log;

import com.allan.cam2api.BuildConfig;

public class CamLog {
    private static final String TAG = "allan_cam";

    public static void d(String s) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, s);
        } else {
            Log.d(TAG, s);
        }
    }

    public static void e(String s) {
        Log.e(TAG, s);
    }

    public static void d(String tag, String s) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "" + tag + ": " + s);
        } else {
            Log.d(TAG, "" + tag + ": " + s);
        }
    }

    public static void e(String tag, String s) {
        Log.e(TAG, "" + tag + ": " + s);
    }
}
