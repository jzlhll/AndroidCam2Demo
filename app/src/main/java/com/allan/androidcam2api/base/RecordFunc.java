package com.allan.androidcam2api.base;

/**
 * 这是一个回调方法，用于开始录制，顺便地，传递给录制者，最终直接在调用处
 * 收到返回消息。
 */
public interface RecordFunc{
    /**
     * 录制开始的提示
     * @param suc 是否成功
     */
    void onRecordStart(boolean suc);

    /**
     * 录制完成的提示
     *
     * @param path 录制完成的名字
     */
    void onRecordOver(String path);

    /**
     * 用于MyCamera内部封装msg.obj
     */
    public class ObjStruct{
        public RecordFunc func;
        public String path;

        public ObjStruct(String path, RecordFunc func) {
            this.func = func;
            this.path = path;
        }
    }
}
