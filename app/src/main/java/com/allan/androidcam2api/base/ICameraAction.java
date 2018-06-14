package com.allan.androidcam2api.base;

/**
 * MyCamera的方法抽象
 *
 * @code MyCamera
 */
public interface ICameraAction {
    public boolean openCamera();

    public void closeCamera();

    public boolean transmitModPreview();

    public boolean transmitModPicture();

    public boolean transmitModPicturePreview();

    //public boolean transmitModVideoPicturePreview();
}
