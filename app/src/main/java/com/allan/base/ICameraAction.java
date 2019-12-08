package com.allan.base;

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
public interface ICameraAction {
    void openCamera();

    void closeSession();

    void closeCamera();

    void transmitModById(int transmitId);

    void startRecord(String path, String name, IRecordCallback callback);

    void stopRecord();

    void takePicture(String path, String name, ITakePictureCallback callback);
}
