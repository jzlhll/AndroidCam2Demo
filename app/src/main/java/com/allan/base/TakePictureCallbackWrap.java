package com.allan.base;
public final class TakePictureCallbackWrap {
    public final ITakePictureCallback callback;
    public final String dir;
    public final String name;

    public TakePictureCallbackWrap(String dir, String name, ITakePictureCallback callback) {
        this.callback = callback;
        this.dir = dir;
        this.name = name;
    }
}
