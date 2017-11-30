package me.majiajie.barcodereader.decode;


import android.graphics.Rect;
import android.os.Build;
import android.os.HandlerThread;

import com.google.zxing.DecodeHintType;

import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.ui.BarcodeDecodeCallBack;

/**
 * 持续性解码线程管理
 */
public class DecodeHandlerHelper {

    private final String TAG = "DecodeHandlerHelper";

    private DecodeHandler decodeHandler;

    private final BarcodeDecodeCallBack decodeCallBack;

    private final Map<DecodeHintType, Object> hints;

    private HandlerThread mHandlerThread;

    private boolean isStop;

    public DecodeHandlerHelper(BarcodeDecodeCallBack callBack, Map<DecodeHintType, Object> hints) {
        this.decodeCallBack = callBack;
        this.hints = hints;

        mHandlerThread = new HandlerThread(TAG);
    }

    /**
     * 开启背景线程
     */
    public void start() {
        isStop = false;

        mHandlerThread.start();
        decodeHandler = new DecodeHandler(mHandlerThread.getLooper(), decodeCallBack, hints);
    }

    /**
     * 解码
     *
     * @param data   图像数据
     * @param width  图像宽
     * @param height 图像高
     * @param rect   解码区域
     */
    public void decode(byte[] data, int width, int height, Rect rect) {
        if (!isStop) {
            DecodeBean bean = new DecodeBean(data, width, height, rect);
            decodeHandler.obtainMessage(R.id.decode, bean).sendToTarget();
        }
    }

    /**
     * 相机获取的数据是横向的，如果需要竖向扫描解码，需要调用这个方法
     *
     * @param data   图像数据
     * @param width  图像宽
     * @param height 图像高
     * @param rect   解码区域
     */
    public void decodeCameraDataVertical(byte[] data, int width, int height, Rect rect) {
        if (!isStop) {
            DecodeBean bean = new DecodeBean(data, width, height, rect);
            decodeHandler.obtainMessage(R.id.decode_camera_data_vertical, bean).sendToTarget();
        }
    }

    /**
     * 停止背景线程
     */
    public void stopThread() {
        isStop = true;

        decodeHandler.removeMessages(R.id.decode);
        decodeHandler.removeMessages(R.id.decode_camera_data_vertical);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            decodeHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
