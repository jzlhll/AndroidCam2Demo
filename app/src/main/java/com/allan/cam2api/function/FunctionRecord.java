package com.allan.cam2api.function;

import com.allan.cam2api.base.IRecordCallback;

public class FunctionRecord {
    public IRecordCallback callback;
    public String path;
    public String name;

    public FunctionRecord(String path, String name, IRecordCallback callback) {
        this.callback = callback;
        this.path = path;
        this.name = name;
    }
}
