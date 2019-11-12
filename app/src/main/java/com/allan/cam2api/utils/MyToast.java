package com.allan.cam2api.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MyToast {
    public static void toastOld(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static void toastNew(final Context context, View parent, String s) {
        Snackbar.make(parent, s, Snackbar.LENGTH_LONG).show();
    }

    public static void toastNew(final Context context, View parent, String s, String action, View.OnClickListener listener) {
        Snackbar.make(parent, s, Snackbar.LENGTH_LONG).setAction(action, listener).show();
    }

    public static void alertDialog(final Context context, String warningInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning").setMessage(warningInfo).setPositiveButton("OK", null);
        builder.create().show();
    }
}
