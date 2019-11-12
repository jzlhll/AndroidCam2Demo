package com.allan.androidcam2api.base;

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
