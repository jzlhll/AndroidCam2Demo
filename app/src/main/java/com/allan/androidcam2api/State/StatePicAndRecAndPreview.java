package com.allan.androidcam2api.State;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Size;

import com.allan.androidcam2api.MyCameraManager;
import com.allan.androidcam2api.base.StateBase;
import com.allan.androidcam2api.utils.CamLog;

public class StatePicAndRecAndPreview extends StatePicAndPreview implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    private MediaRecorder mMediaRecorder;

    public StatePicAndRecAndPreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    public int getId() {
        return 0x111;
    }

    public MediaRecorder getMediaRecorder() {
        return mMediaRecorder;
    }

    public interface StatePPRCB extends StatePicAndPreview.StateTakePicCb {
        void onRecordStart(boolean suc);

        void onError(int err);

        void onRecordOver(String path);
    }

    @Override
    protected void addTarget() {
        camera.previewBuilder.addTarget(camSurfaces.get(0)); //将preview和record的surface传入
        camera.previewBuilder.addTarget(camSurfaces.get(2));
    }

    @Override
    protected CameraCaptureSession.StateCallback createStateCallback() {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                camera.camSession = cameraCaptureSession;
                camera.previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                try {
                    camera.camSession.setRepeatingRequest(camera.previewBuilder.build(), null, camera.getHandler());
                    if (mMediaRecorder != null) {
                        mMediaRecorder.start();
                    } else {
                        CamLog.e("error!!!! mediaRecord is null");
                    }
                    StatePPRCB statePPRCB = (StatePPRCB) mStateBaseCb;
                    statePPRCB.onRecordStart(true);
                } catch (CameraAccessException e) {
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                StatePPRCB statePPRCB = (StatePPRCB) mStateBaseCb;
                statePPRCB.onRecordStart(false);
            }
        };
    }

    @Override
    protected int getTemplateType() {
        return CameraDevice.TEMPLATE_RECORD;
    }

    @Override
    public boolean createSession(StateBaseCb cb) {
        return super.createSession(cb);
    }

    public void stopRecord() {
        if (mMediaRecorder == null) return;
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        StatePPRCB statePPRCB = (StatePPRCB) mStateBaseCb;
        statePPRCB.onRecordOver(camera.getRecordPath());
    }

    @Override
    protected void createSurfaces() {
        super.createSurfaces();
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setOnInfoListener(this);

            if (true) { //TODO 是否支持音频
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            StreamConfigurationMap map = camera.getCameraCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(MediaRecorder.class);

            Size needSize = null;

            for (Size si : sizes) { //挑选录制分辨率
                if (needSize == null) {
                    needSize = si;
                }

                if (camera.getCameraId() == CameraCharacteristics.LENS_FACING_FRONT
                        && (si.getHeight() == 1080 || si.getHeight() == 1920)) {
                    needSize = si;
                    break;
                } else if (camera.getCameraId() == CameraCharacteristics.LENS_FACING_BACK
                        && (si.getHeight() == 720 || si.getHeight() == 1280)) {
                    needSize = si;
                    break;
                }
            }

            mMediaRecorder.setVideoSize(needSize.getWidth(), needSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            CamcorderProfile camPro = CamcorderProfile.get(camera.getCameraId(),
                    camera.getCameraId() == CameraCharacteristics.LENS_FACING_FRONT ?
                            CamcorderProfile.QUALITY_1080P : CamcorderProfile.QUALITY_720P);
            if (true) {
                mMediaRecorder.setAudioEncoder(camPro.audioCodec);
                mMediaRecorder.setAudioChannels(camPro.audioChannels);
                // mMediaRecorder.setAudioSamplingRate(camPro.audioSampleRate);
                mMediaRecorder.setAudioSamplingRate(16000);
                mMediaRecorder.setAudioEncodingBitRate(camPro.audioBitRate);
            }
            mMediaRecorder.setVideoEncodingBitRate(camPro.videoBitRate / 2); //码率，自行调节，我希望录制小一点码率
            mMediaRecorder.setVideoFrameRate(camPro.videoFrameRate);
            CamLog.d("Video frame " + camPro.videoFrameRate + " bitRate " + camPro.videoBitRate / 2);
            // mMediaRecorder.setMaxDuration(video.duration);
            // mMediaRecorder.setMaxDuration(30000/*video.duration*/);
            mMediaRecorder.setOutputFile(camera.getRecordPath());
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }

        camSurfaces.add(mMediaRecorder.getSurface());
    }

    @Override
    public void closeSession() {
        stopRecord();
        super.closeSession();
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        StatePPRCB statePPRCB = (StatePPRCB) mStateBaseCb;
        statePPRCB.onError(i);
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
        //TODO file reach file finish
    }
}

