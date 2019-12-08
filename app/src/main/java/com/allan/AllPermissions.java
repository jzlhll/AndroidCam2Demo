package com.allan;

import android.Manifest;
import android.os.Build;

public class AllPermissions {
    public static final int RC_ALL_PERMISSION = 101;
    public static final boolean BIG_THAN_6_0 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

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
