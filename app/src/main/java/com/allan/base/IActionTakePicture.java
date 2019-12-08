package com.allan.base;

public interface IActionTakePicture {
   void takePicture(String dir, String name, final ITakePictureCallback callback);
}
