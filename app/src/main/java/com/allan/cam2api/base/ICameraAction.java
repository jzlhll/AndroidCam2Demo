package com.allan.cam2api.base;

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
public interface ICameraAction {
   void openCamera();

   void closeCamera();

   void transmitModPreview();

   void transmitModPicturePreview();

    void startRecord(String path, String name, IRecordCallback callback);
    void stopRecord();

    void takePicture(String path, String name, ITakePictureCallback callback);
}
