package me.majiajie.barcodereader.ui;

import me.majiajie.barcodereader.decode.DecodeResult;

/**
 * 解码回调
 */
public interface BarcodeDecodeCallBack {

    /**
     * 扫码失败
     */
    void onFailed();

    /**
     * 扫码成功
     * @param result        扫码信息
     * @param thumbnail     缩略图数据
     * @param scaledFactor  缩略图缩放比例
     */
    void onSucceed(DecodeResult result, byte[] thumbnail, float scaledFactor);
}
