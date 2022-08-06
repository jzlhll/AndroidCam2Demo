package com.allan.base;
public final class TakePictureCallbackBean {
    public final ITakePictureCallback callback;
    public final String dir;
    public final String name;

    public TakePictureCallbackBean(String dir, String name, ITakePictureCallback callback) {
        this.callback = callback;
        this.dir = dir;
        this.name = name;
    }
}
