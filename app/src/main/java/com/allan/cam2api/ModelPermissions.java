package com.allan.cam2api;

import android.Manifest;

public class ModelPermissions {
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.CAMERA
        };
    }

    public String getShowWords() {
        return "halo，给下存储权限吧？";
    }
}
