package com.allan.androidcam2api.base;

/**
 * 这是一个回调方法，用于开始录制，顺便地，传递给录制者，最终直接在调用处
 * 收到返回消息。
 */
public interface IRecordCallback {
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
    void onRecordEnd(String path);


}
