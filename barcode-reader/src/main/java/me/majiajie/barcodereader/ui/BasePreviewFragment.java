package me.majiajie.barcodereader.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;
import me.majiajie.barcodereader.ui.view.ScanView;

/**
 * 扫码-相机预览界面基础
 */
public abstract class BasePreviewFragment extends Fragment implements PreviewListener{


    protected Context mContext;

    private FrameLayout mCameraPreview;
    private ScanView mScanView;
    private CheckBox mCheckBoxLight;

    // 解码回调
    private BarcodeDecodeCallBack mDecodeCallBack;

    // 持续性解码帮助类
    private DecodeHandlerHelper mDecodeHandlerThread;

    // 记录是否拥有相机权限
    private boolean mHasPremission = false;

    /**
     * 开始显示相机预览
     */
    protected abstract void startPreview();

    /**
     * 释放相机
     */
    protected abstract void releaseCamera();

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


    }

    @Override
    public void onResume() {
        startPreview();
        super.onResume();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
