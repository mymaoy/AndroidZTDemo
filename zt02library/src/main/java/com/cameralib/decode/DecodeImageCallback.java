package com.cameralib.decode;

import com.cameralib.google.nozxing.Result;

/**
 * 图片解析二维码回调方法
 */
public interface DecodeImageCallback {

    void decodeSucceed(Result result);

    void decodeFail(int type, String reason);
}
