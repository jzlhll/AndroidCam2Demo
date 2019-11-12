package com.allan.cam2api.function;

import com.allan.cam2api.base.ITakePictureCallback;

public class FunctionTakePicture {
    public ITakePictureCallback callback;
    public String dir;
    public String name;

    public FunctionTakePicture(String dir, String name, ITakePictureCallback callback) {
        this.callback = callback;
        this.dir = dir;
        this.name = name;
    }
}
