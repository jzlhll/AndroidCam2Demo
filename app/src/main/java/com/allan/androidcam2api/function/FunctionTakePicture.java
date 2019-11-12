package com.allan.androidcam2api.function;

import com.allan.androidcam2api.base.ITakePictureCallback;

public class FunctionTakePicture {
    public ITakePictureCallback func;
    public String dir;
    public String name;

    public FunctionTakePicture(String dir, String name, ITakePictureCallback func) {
        this.func = func;
        this.dir = dir;
        this.name = name;
    }
}
