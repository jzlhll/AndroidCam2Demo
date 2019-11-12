package com.allan.cam2api;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.allan.cam2api.manager.MyCameraManager;
import com.allan.cam2api.utils.CamLog;
import com.allan.cam2api.utils.MyToast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements MyCameraManager.ModChange {

    private FloatingActionButton mTabPictureBtn, mRecordBtn;

    private View mView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mCamViewPresent = new MainActivityCameraViewPresent(this);
        mRecordPresent = new MainActivityRecordPresent(this);

        mView = findViewById(R.id.surfaceView);
        mCamViewPresent.setCameraView(mView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabPictureBtn = findViewById(R.id.fab_takepicture_btn);
        mTabPictureBtn.setOnClickListener(mClickListener);

        mRecordBtn = findViewById(R.id.fab_record_btn);
        mRecordBtn.setOnClickListener(mClickListener);

        mTimeView = findViewById(R.id.timeTv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_preview) {
            MyCameraManager.instance().transmitModPreview();
            return true;
        } else if (id == R.id.action_preview_pic) {
            MyCameraManager.instance().transmitModPicturePreview();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int RC_ALL_PERMISSION = 101;
    private static final boolean BIG_THAN_6_0 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private boolean mIsHasPermission = false;

    @AfterPermissionGranted(RC_ALL_PERMISSION)
    public void openCamera() {
        if (BIG_THAN_6_0) {
            ModelPermissions mp = new ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                if (!mIsHasPermission) {
                    mIsHasPermission = true;
                    CamLog.d("has permission setCameraVieeeeeewww");
                    mCamViewPresent.openCamera();
                }
            } else {
                CamLog.d("request setCameraVieeeeeewww");
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_ALL_PERMISSION, mp.getPermissions());
            }
        } else {
            CamLog.d("setCameraVieeeeeewww");
            mCamViewPresent.openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onModChanged(final String newMod) { //监听了MyCamera的模式变化
        mView.post(new Runnable() {
            @Override
            public void run() {
                MyToast.toastNew(getApplicationContext(), mView, newMod);
                setTitle(newMod);
                switch (newMod) {
                    case "Preview": {
                        mRecordBtn.hide();
                        mTabPictureBtn.hide();;
                    }
                    break;
                    case "Picture": {
                        mRecordBtn.hide();;
                        mTabPictureBtn.show();
                    }
                    break;
                    case "Picture&Preview&Video":
                    case "Picture&Preview": {
                        mRecordBtn.show();
                        mTabPictureBtn.show();
                    }
                    break;
                }
            }
        });
    }

}
