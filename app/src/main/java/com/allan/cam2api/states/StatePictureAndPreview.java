package com.allan.cam2api.states;

import com.allan.cam2api.base.ITakePictureCallback;
import com.allan.cam2api.MyCameraManager;
import com.allan.cam2api.states.image.IActionTakePicture;
import com.allan.cam2api.states.image.TakePictureBuilder;

public class StatePictureAndPreview extends StatePreview implements IActionTakePicture {
    private TakePictureBuilder mTakePic;

    public StatePictureAndPreview(MyCameraManager cd) {
        super(cd);
    }

    @Override
    protected void step0_createSurfaces() {
        super.step0_createSurfaces();
        //由于super中有添加了preview的surface。这里处理拍照即可
        mTakePic = new TakePictureBuilder(cameraManager, mNeedSize.getWidth(), mNeedSize.getHeight());
        allIncludePictureSurfaces.add(mTakePic.getSurface()); //这个添加到allIncludePictureSurfaces 不需要添加到target里面
    }

    @Override
    public int getFeatureId() {
        return FeatureUtil.FEATURE_PICTURE | FeatureUtil.FEATURE_PREVIEW;
    }

    @Override
    public void closeSession() {
        mTakePic.release();
        super.closeSession();
    }

    @Override
    public void takePicture(String dir, String name, ITakePictureCallback callback) {
        mTakePic.takePicture(dir, name, callback);
    }
}
