package com.allan.cam2api.states.image;

import com.allan.cam2api.base.ITakePictureCallback;

public interface IActionTakePicture {
   boolean takePicture(String dir, String name, final ITakePictureCallback callback);
}
