package com.allan.androidcam2api.base;

public class FunctionRecord {
    public IRecordCallback func;
    public String path;

    public FunctionRecord(String path, IRecordCallback func) {
        this.func = func;
        this.path = path;
    }
}
