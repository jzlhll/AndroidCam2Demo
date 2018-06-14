package com.allan.androidcam2api.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

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
