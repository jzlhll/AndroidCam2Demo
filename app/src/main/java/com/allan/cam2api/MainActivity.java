package com.allan.cam2api;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.allan.cam2api.utils.CamLog;
import com.allan.cam2api.utils.MyToast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.allan.cam2api.MainActivityCameraViewPresent.RC_ALL_PERMISSION;

public class MainActivity extends AppCompatActivity implements MyCameraManager.ModChange {
    private FloatingActionButton mTabPictureBtn, mRecordBtn;

    MainActivityCameraViewPresent mCamViewPresent;
    MainActivityRecordPresent mRecordPresent;

    private TextView mTimeView;

    public TextView getTimeView() {
        return mTimeView;
    }

    public FloatingActionButton getRecordBtn() {
        return mRecordBtn;
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_takepicture_btn: {
                    CamLog.d("click takePicture");
                    mRecordPresent.clickOnTakePicture();
                }
                break;
                case R.id.fab_record_btn: {
                    CamLog.d("click record");
                    mRecordPresent.clickOnRecord();
                    break;
                }
            }
        }
    };

    public static String MODE_PREVIEW;
    public static String MODE_PREVIEW_PICTURE;
    public static String MODE_PicturePreviewVideo;
    public static String MODE_PICTURE_NO_PREVIW;

    private void setMenuTitles() {
        MODE_PICTURE_NO_PREVIW = getResources().getString(R.string.action_picture_only);
        MODE_PREVIEW_PICTURE = getResources().getString(R.string.action_preview_pic);
        MODE_PREVIEW = getResources().getString(R.string.action_preview);
        MODE_PicturePreviewVideo = getResources().getString(R.string.action_record);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mRecordPresent = new MainActivityRecordPresent(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabPictureBtn = findViewById(R.id.fab_takepicture_btn);
        mTabPictureBtn.setOnClickListener(mClickListener);

        mRecordBtn = findViewById(R.id.fab_record_btn);
        mRecordBtn.setOnClickListener(mClickListener);

        mTimeView = findViewById(R.id.timeTv);

        setMenuTitles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        String gotoMod = null;
        if (id == R.id.action_preview) {
            gotoMod = MODE_PREVIEW;
        } else if (id == R.id.action_preview_pic) {
            gotoMod = MODE_PREVIEW_PICTURE;
        } else if (id == R.id.action_picture_only) {
            gotoMod = MODE_PICTURE_NO_PREVIW;
        }

        if (gotoMod != null) {
            mCamViewPresent.transmitToWordsState(gotoMod);
        }

        return super.onOptionsItemSelected(item);
    }

    @AfterPermissionGranted(RC_ALL_PERMISSION) //这个注释方法还不能放到其他地方，必须放在activity中
    private void openCameraPresent() {
        mCamViewPresent.openCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onModChanged(final String newMod) { //监听了MyCamera的模式变化
        mTimeView.post(new Runnable() {
            @Override
            public void run() {
                MyToast.toastNew(getApplicationContext(), mTimeView, newMod);
                setTitle(newMod);
                if (newMod.equals(MODE_PREVIEW)) {
                    mRecordBtn.hide();
                    mTabPictureBtn.hide();
                } else if (newMod.equals(MODE_PicturePreviewVideo) || newMod.equals(MODE_PREVIEW_PICTURE)) {
                    mRecordBtn.show();
                    mTabPictureBtn.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamViewPresent = new MainActivityCameraViewPresent(this);
        mCamViewPresent.initCameraView(findViewById(R.id.surfaceView));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamViewPresent.destroy();
    }
}