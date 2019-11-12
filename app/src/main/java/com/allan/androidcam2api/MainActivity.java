package com.allan.androidcam2api;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.allan.androidcam2api.utils.AllPermission;
import com.allan.androidcam2api.utils.CamLog;
import com.allan.androidcam2api.utils.MyToast;

public class MainActivity extends AppCompatActivity implements AllPermission.PermissionGrant, MyCameraManager.ModChange {

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
        protected void onResume() {
            super.onResume();
            AllPermission.requestMultiPermissions(this, this);
        }

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
                MyCameraManager.me.get().transmitModPreview();
                return true;
            } else if (id == R.id.action_preview_pic) {
                MyCameraManager.me.get().transmitModPicturePreview();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case AllPermission.CODE_RECORD_AUDIO:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_RECORD_AUDIO", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_GET_ACCOUNTS:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_GET_ACCOUNTS", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_READ_PHONE_STATE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_PHONE_STATE", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_CALL_PHONE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CALL_PHONE", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_CAMERA:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CAMERA", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_ACCESS_FINE_LOCATION:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_ACCESS_COARSE_LOCATION:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_READ_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case AllPermission.CODE_WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
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
                            mRecordBtn.setVisibility(View.GONE);
                            mTabPictureBtn.setVisibility(View.GONE);
                        }
                        break;
                        case "Picture": {
                            mRecordBtn.setVisibility(View.GONE);
                            mTabPictureBtn.setVisibility(View.VISIBLE);
                        }
                        break;
                        case "Picture&Preview&Video":
                        case "Picture&Preview": {
                            mRecordBtn.setVisibility(View.VISIBLE);
                            mTabPictureBtn.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            });
        }

    }
