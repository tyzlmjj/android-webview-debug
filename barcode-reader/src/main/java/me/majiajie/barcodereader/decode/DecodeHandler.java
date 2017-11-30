package me.majiajie.barcodereader.decode;


import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.ui.BarcodeDecodeCallBack;
import me.majiajie.barcodereader.ui.CallBackHandler;

public class DecodeHandler extends Handler {
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    private final CallBackHandler resultHandler;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    public DecodeHandler(BarcodeDecodeCallBack callBack, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.resultHandler = new CallBackHandler(Looper.getMainLooper(), callBack);
    }

    public DecodeHandler(Looper looper, BarcodeDecodeCallBack callBack, Map<DecodeHintType, Object> hints) {
        super(looper);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.resultHandler = new CallBackHandler(Looper.getMainLooper(), callBack);
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }

        int what = message.what;

        if (what == R.id.decode) {
            decode((DecodeBean) message.obj);
        } else if (what == R.id.decode_camera_data_vertical) {
            conversionDataToVertical((DecodeBean) message.obj);
        } else if (what == R.id.quit) {
            running = false;
            Looper.myLooper().quit();
        }
    }

    /**
     * 将相机获取的数据变为竖向的然后解码
     */
    private void conversionDataToVertical(DecodeBean decodeBean) {
        //这里宽高给的是反的，因为生产的预览图数据是横向的
        int height = decodeBean.getWidth();
        int width = decodeBean.getHeight();

        byte[] oldData = decodeBean.getData();
        int max_size = oldData.length;
        //将横向的图像数据改为竖向
        byte[] newData = new byte[max_size];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //防止在特殊情况下宽高不准
                int tem_a = x * height + height - y - 1;
                int tem_b = x + y * width;
                if (tem_a >= max_size || tem_b >= max_size) {
                    continue;
                }
                newData[tem_a] = oldData[tem_b];
            }
        }
        decodeBean.setData(newData);

        decode(decodeBean);
    }

    /**
     * 解码数据，每次都使用同一个解码实例。
     */
    private void decode(DecodeBean decodeBean) {
        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource
                (decodeBean.getData(), decodeBean.getWidth(), decodeBean.getHeight(), decodeBean.getRect());
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        if (rawResult != null) {
            if (resultHandler != null) {
                DecodeResult decodeResult = new DecodeResult(rawResult);
                Message message = resultHandler.obtainMessage(R.id.decode_succeeded, decodeResult);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (resultHandler != null) {
                Message message = resultHandler.obtainMessage(R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

    /**
     * 创建缩略图并存放到Bundle
     */
    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    /**
     * 构建解码需要的数据
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect rect) {
        if (rect == null) {
            return null;
        }

        Rect newRect = new Rect(rect);

        //当设定的扫描框的宽大于数据宽度时进行修正
        if (newRect.right > width) {
            float n = (float) width / (float) (newRect.left + newRect.right);

            newRect.left = (int) (newRect.left * n);
            newRect.right = (int) (newRect.right * n);
        }

        //当设定的扫描框的高大于数据高度时进行修正
        if (newRect.bottom > height) {
            float n = (float) height / (float) (newRect.top + newRect.bottom);

            newRect.top = (int) (newRect.top * n);
            newRect.bottom = (int) (newRect.bottom * n);
        }

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, newRect.left, newRect.top,
                newRect.width(), newRect.height(), false);
    }
}
