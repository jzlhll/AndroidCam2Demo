package com.allan.androidcam2api.base;

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
public interface ICameraAction {
   boolean openCamera();

   void closeCamera();

   boolean transmitModPreview();

   boolean transmitModPicturePreview();

    //public boolean transmitModVideoPicturePreview();
}
