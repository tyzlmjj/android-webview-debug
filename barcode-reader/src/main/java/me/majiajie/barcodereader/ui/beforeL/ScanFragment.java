package me.majiajie.barcodereader.ui.beforeL;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;
import me.majiajie.barcodereader.ui.BarcodeDecodeCallBack;
import me.majiajie.barcodereader.ui.CameraUtils;
import me.majiajie.barcodereader.ui.PreviewListener;
import me.majiajie.barcodereader.ui.view.ScanView;

/**
 * 5.0之前扫码使用
 */
public class ScanFragment extends Fragment implements PreviewListener {

    private Context mContext;

    private FrameLayout mCameraPreview;
    private ScanView mScanView;
    private CheckBox mCheckBoxLight;

    private Camera mCamera;
    private CameraPreview mPreview;

    //判断是否支持闪光灯
    private boolean mSupportedFlashModes;

    //判断是否支持摄像头
    private boolean mSupportedCamera = true;

    //解码结果回调
    private BarcodeDecodeCallBack mDecodeCallBack;

    //持续性解码帮助类
    private DecodeHandlerHelper mDecodeHandlerThread;

    /**
     * 闪光灯开关事件
     */
    private CompoundButton.OnCheckedChangeListener lightListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mSupportedFlashModes && mCamera != null) {
                Camera.Parameters params = mCamera.getParameters();
                params.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
            }
        }
    };

    /**
     * 预览视图回调
     */
    private CameraPreview.CameraPreviewListener cameraPreviewListener = new CameraPreview.CameraPreviewListener() {
        @Override
        public void onStartPreview() {
            requestPreviewFrame();
        }
    };

    public ScanFragment() {}

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof BarcodeDecodeCallBack){
            mDecodeCallBack = (BarcodeDecodeCallBack) context;
        } else {
            throw new ClassCastException(context.toString() + "must implements BarcodeDecodeCallBack");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCameraPreview = view.findViewById(R.id.camera_preview);
        mScanView = view.findViewById(R.id.scanView);
        mCheckBoxLight = view.findViewById(R.id.checkBox_light);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initCamera();
        initScanThread();
        initEvent();
    }

    private void initScanThread() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

        Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.CODE_128));

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.TRY_HARDER, true);

        mDecodeHandlerThread = new DecodeHandlerHelper(mDecodeCallBack, hints);
        mDecodeHandlerThread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 假如切换应用（比如按主页键），返回时重建相机。
        // 因为onPause周期中做了释放相机的操作
        if (mCamera == null && mSupportedCamera) {
            initCamera();
        }
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    private void initCamera() {
        // 创建后置摄像头实例
        mCamera = CameraUtils.getCameraInstance();

        //后置摄像头不存在或异常
        if (mCamera == null) {
            mSupportedCamera = false;
            Toast.makeText(mContext, "后置摄像头不存在或无法使用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置显示方向(默认会是横向的)
        mCamera.setDisplayOrientation(90);

        Camera.Parameters params = mCamera.getParameters();

        //检查是否支持自动对焦，并且设置连续对焦
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
            mCamera.cancelAutoFocus();
        }

        //检查是否支持闪光灯
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH) &&
                flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            mSupportedFlashModes = true;
        }

        // 创建预览视图
        mPreview = new CameraPreview(mContext, mCamera);
        mPreview.addCameraPreviewListener(cameraPreviewListener);
        //添加到布局
        mCameraPreview.addView(mPreview);
    }

    private void initEvent() {
        mCheckBoxLight.setOnCheckedChangeListener(lightListener);
    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameraPreview.removeAllViews();
            mCheckBoxLight.setChecked(false);
        }
    }

    @Override
    public void requestPreviewFrame() {
        if (mCamera != null) {
            //存在异常 java.lang.RuntimeException: Camera is being used after Camera.release() was called
            mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    int width = camera.getParameters().getPreviewSize().width;
                    int height = camera.getParameters().getPreviewSize().height;

                    //解码-(这里宽高故意放反的，不是放错了！)
                    mDecodeHandlerThread.decodeCameraDataVertical(data,
                            height, width,
                            mScanView.getFramingRect());
                }
            });
        }
    }
}
