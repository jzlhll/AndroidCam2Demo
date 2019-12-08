package com.allan.base;
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
