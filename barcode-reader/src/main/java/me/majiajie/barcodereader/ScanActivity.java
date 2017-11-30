package me.majiajie.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.ui.BarcodeDecodeCallBack;
import me.majiajie.barcodereader.ui.PreviewListener;
import me.majiajie.barcodereader.ui.RequestPermissionFragment;
import me.majiajie.barcodereader.ui.beforeL.ScanFragment;

/**
 * <p>
 *      扫码
 * </p>
 */
public class ScanActivity extends AppCompatActivity implements BarcodeDecodeCallBack,RequestPermissionFragment.RequestPermissionsCallback{

    public static final int REQUEST_CODE = 110;

    private static final String ARG_DECODE_RESULT = "ARG_DECODE_RESULT";

    public static void startActivityForResult(Activity activity){
        Intent intent = new Intent(activity,ScanActivity.class);
        activity.startActivityForResult(intent,REQUEST_CODE);
    }

    public static DecodeResult getResult(Intent data){
        return data.getParcelableExtra(ARG_DECODE_RESULT);
    }

    // 请求权限的Fragment的TAG
    private static final String REQUEST_PERMISSION_TAG = "REQUEST_PERMISSION_TAG";
    // 相机权限
    private final String[] CAMERA_PERMISSION =  {Manifest.permission.CAMERA};

    private PreviewListener mPreviewListener;

    private RequestPermissionFragment mRequestPermissionFragment;

    private boolean once = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // 显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 添加请求权限的Fragment
        mRequestPermissionFragment = (RequestPermissionFragment) getSupportFragmentManager().findFragmentByTag(REQUEST_PERMISSION_TAG);
        if (mRequestPermissionFragment == null){
            mRequestPermissionFragment = RequestPermissionFragment.newInstance(CAMERA_PERMISSION,getString(R.string.dialog_hint_camera_permission));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(mRequestPermissionFragment,REQUEST_PERMISSION_TAG).commit();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.scan_menu,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int i = item.getItemId();
//        if (i == android.R.id.home) {
//            onBackPressed();
//            return true;
//        } else if (i == R.id.action_create_desktop_shortcuts) {
//            // 创建桌面快捷方式
//
//            return true;
//        } else if (i == R.id.action_images) {
//            // 打开相册
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (once) {
            once = false;
            // 检查相机权限
            if (mRequestPermissionFragment.checkPermissions()) {
                startScanBarcode();
            } else {
                mRequestPermissionFragment.requestPermissions();
            }
        }
    }

    @Override
    public void onFailed() {
        // 没有扫码到条码继续扫码
        mPreviewListener.requestPreviewFrame();
    }

    @Override
    public void onSucceed(DecodeResult result, byte[] thumbnail, float scaledFactor) {
        Intent intent = new Intent();
        intent.putExtra(ARG_DECODE_RESULT,result);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(boolean grantResult) {
        if (grantResult){
            startScanBarcode();
        } else {
            Toast.makeText(this,R.string.hint_no_camera_permission,Toast.LENGTH_LONG).show();
        }
    }

    private void startScanBarcode() {
        ScanFragment fragment = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.layout_fragment);
        if (fragment == null){
            fragment = ScanFragment.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.layout_fragment, fragment).commitAllowingStateLoss();
        }
        mPreviewListener = fragment;
    }

}
