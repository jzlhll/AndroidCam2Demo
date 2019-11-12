package com.allan.androidcam2api.base;

/**
 * 这是一个回调方法，用于开始拍照，顺便地，传递给拍照类，最终直接在调用处
 * 收到返回消息。
 */
public interface ITakePictureCallback {
    /**
     * 拍照成功的回调
     */
    void onPictureToken(String path);
}
