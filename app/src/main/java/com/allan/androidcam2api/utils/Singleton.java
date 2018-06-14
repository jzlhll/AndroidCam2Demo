package com.allan.androidcam2api.utils;

public abstract class Singleton<T> {

    protected T instance;

    public abstract T create();

    public final T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = create();
                }
            }
        }
        return instance;
    }
}
