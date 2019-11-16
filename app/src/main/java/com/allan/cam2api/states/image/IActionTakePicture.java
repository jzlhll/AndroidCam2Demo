package com.allan.cam2api.states.image;

import com.allan.cam2api.base.ITakePictureCallback;

public interface IActionTakePicture {
   void takePicture(String dir, String name, final ITakePictureCallback callback);
}
