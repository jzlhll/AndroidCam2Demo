package com.allan.androidcam2api;

import android.Manifest;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
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

import com.allan.androidcam2api.base.RecordFunc;
import com.allan.androidcam2api.base.TakePhotoFunc;
import com.allan.androidcam2api.utils.AllPermission;
import com.allan.androidcam2api.utils.CamLog;
import com.allan.androidcam2api.utils.MyToast;
import com.allan.androidcam2api.view.CamSurfaceView;
import com.allan.androidcam2api.view.IViewDecorator;

import java.io.File;

public class MainActivity extends AppCompatActivity implements AllPermission.PermissionGrant, CamSurfaceView.MyCallback, MyCameraManager.ModChange {

    private FloatingActionButton mTakePicFab, mRecordFab;

    private View mView;
    private TextView mTimeView;
    private int mTimeSec = 0;
    private boolean mRecStarted = false;
    private Runnable mTimeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mTimeView.setText("" + mTimeSec++);
            mTimeView.postDelayed(mTimeUpdateRunnable, 1000);
        }
    };

    private IViewDecorator mViewDecorator;

    private static String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_takepic: {
                    CamLog.d("Sd path " + SD_PATH);
                    MyCameraManager.me.get().takePicture(SD_PATH, "pic.png", new TakePhotoFunc() {
                        @Override
                        public void onPictureToken(String path) {
                            MyToast.toastNew(getApplicationContext(), mView, "Get in Activity " + path);
                        }
                    });
                }
                break;
                case R.id.fab_rec: {
                    if (!mRecStarted) {
                        MyCameraManager.me.get().startRecord(SD_PATH + File.separator + "test.mp4", new RecordFunc() {
                            @Override
                            public void onRecordStart(boolean suc) {
                                CamLog.d("onRecordStart suc " + suc);
                                mRecStarted = true;
                                mView.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTimeView.getHandler().post(mTimeUpdateRunnable);
                                        mTimeView.getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mRecordFab.setImageResource(R.drawable.take_rec);
                                                mTimeView.setVisibility(View.VISIBLE);
                                            }
                                        });

                                    }
                                });
                            }

                            @Override
                            public void onRecordOver(String path) {
                                mRecStarted = false;
                                CamLog.d("onRecordOver " + path);
                                mTimeView.getHandler().removeCallbacks(mTimeUpdateRunnable);
                                mTimeView.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTimeSec = 0;
                                        mRecordFab.setImageResource(R.drawable.take_recing);
                                        mTimeView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    } else {
                        MyCameraManager.me.get().stopRecord();
                    }
                    break;
                }
            }
        }

    };

        @Override
        protected void onResume() {
            super.onResume();
            String[] ss = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            };
            AllPermission.requestMultiPermissions(this, this);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_main);

            mView = findViewById(R.id.surfaceView);

            mViewDecorator = new IViewDecorator(mView);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mTakePicFab = (FloatingActionButton) findViewById(R.id.fab_takepic);
            mTakePicFab.setOnClickListener(mClickListener);

            mRecordFab = (FloatingActionButton) findViewById(R.id.fab_rec);
            mRecordFab.setOnClickListener(mClickListener);

            mTimeView = findViewById(R.id.timeTv);
            mViewDecorator.setCallback(this);
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
        public void onChanged(final String newMod) { //监听了MyCamera的模式变化
            mView.post(new Runnable() {
                @Override
                public void run() {
                    MyToast.toastNew(getApplicationContext(), mView, newMod);
                    setTitle(newMod);
                    switch (newMod) {
                        case "Preview": {
                            mRecordFab.setVisibility(View.GONE);
                            mTakePicFab.setVisibility(View.GONE);
                        }
                        break;
                        case "Picture": {
                            mRecordFab.setVisibility(View.GONE);
                            mTakePicFab.setVisibility(View.VISIBLE);
                        }
                        break;
                        case "Picture&Preview": {
                            mRecordFab.setVisibility(View.VISIBLE);
                            mTakePicFab.setVisibility(View.VISIBLE);
                        }
                        break;
                        case "Picture&Preview&Video": {
                            mRecordFab.setVisibility(View.VISIBLE);
                            mTakePicFab.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            });
        }

        @Override
        public void pleaseStart() {
            MyCameraManager.me.get().init(mView.getContext(), mView, mViewDecorator);
            MyCameraManager.me.get().addModChanged(this);
            MyCameraManager.me.get().openCamera();
        }

        @Override
        public void pleaseStop() {
            MyCameraManager.me.get().closeCamera();
        }
    }
