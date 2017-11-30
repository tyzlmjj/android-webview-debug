package me.majiajie.barcodereader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeHandler;
import me.majiajie.barcodereader.decode.DecodeResult;


/**
 * 扫码结果返还
 */
public class CallBackHandler extends Handler
{
    private BarcodeDecodeCallBack mCallBack;

    public CallBackHandler(Looper looper, BarcodeDecodeCallBack mCallBack) {
        super(looper);
        this.mCallBack = mCallBack;
    }

    @Override
    public void handleMessage(Message msg)
    {
        int what = msg.what;

        if(what == R.id.decode_succeeded)//扫码成功
        {
            DecodeResult result = (DecodeResult) msg.obj;
            Bundle bundle = msg.getData();
            byte[] b = bundle.getByteArray(DecodeHandler.BARCODE_BITMAP);
            float scaledFactor = bundle.getFloat(DecodeHandler.BARCODE_SCALED_FACTOR);
            mCallBack.onSucceed(result,b,scaledFactor);
        }
        else if(what == R.id.decode_failed)//扫码失败
        {
            mCallBack.onFailed();
        }
    }
}
