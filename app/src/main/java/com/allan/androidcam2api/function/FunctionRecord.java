package com.allan.androidcam2api.function;

import com.allan.androidcam2api.base.IRecordCallback;

public class FunctionRecord {
    public IRecordCallback func;
    public String path;

    public FunctionRecord(String path, IRecordCallback func) {
        this.func = func;
        this.path = path;
    }
}
