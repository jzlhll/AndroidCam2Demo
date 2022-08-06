package com.allan.base;
public final class RecordCallbackBean {
    public final IRecordCallback callback;
    public final String path;
    public final String name;

    public RecordCallbackBean(String path, String name, IRecordCallback callback) {
        this.callback = callback;
        this.path = path;
        this.name = name;
    }
}
