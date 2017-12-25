package me.majiajie.androidwebviewdebug;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import me.majiajie.barcodereader.BarcodeFormat;
import me.majiajie.barcodereader.ScanActivity;
import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.helper.RequestPermissionFragment;

public class WebViewActivity extends AppCompatActivity implements RequestPermissionFragment.RequestPermissionsCallback {

    private final String REQUEST_LOCATION_PERMISSION_TAG = "REQUEST_LOCATION_PERMISSION_TAG";
    private final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};

    private Toolbar mToolbar;
    private WebProgressBar mWebProgressBar;
    private TextView mTvUrl;
    private TextView mTvWebviewVersion;
    private MyWebView mWebView;

    // 网页请求位置信息时存储的临时变量
    private String mTmpOrigin;
    private GeolocationPermissions.Callback mTmpGeolocationCallback;

    /**
     * 请求定位权限的Fragment
     */
    private RequestPermissionFragment mRequestLocationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mToolbar = findViewById(R.id.toolbar);
        mWebProgressBar = findViewById(R.id.webProgressBar);
        mTvUrl = findViewById(R.id.tv_url);
        mTvWebviewVersion = findViewById(R.id.tv_webview_version);
        mWebView = findViewById(R.id.webView);

        showToolbar(mToolbar);

        configWebView();

        // 显示WebView版本
        mTvWebviewVersion.setText(getString(R.string.webview_version_text,Utils.getWebViewVersion(this)));

        // 添加请求位置权限的Fragment
        mRequestLocationPermission = (RequestPermissionFragment) getSupportFragmentManager().findFragmentByTag(REQUEST_LOCATION_PERMISSION_TAG);
        if (mRequestLocationPermission == null){
            mRequestLocationPermission = RequestPermissionFragment.newInstance(LOCATION_PERMISSION,getString(R.string.location_permission_hint));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(mRequestLocationPermission,REQUEST_LOCATION_PERMISSION_TAG).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case ScanActivity.REQUEST_CODE:// 扫码返回
                    DecodeResult decodeResult = ScanActivity.getResult(data);
                    mWebView.loadUrl(decodeResult.getText());
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_reload://重新加载
                mWebView.reload();
                return true;
            case R.id.action_copy://复制
                copyUrl();
                return true;
            case R.id.action_scan://扫二维码
                ScanActivity.startActivityForResult(this,0,new int[]{BarcodeFormat.QR_CODE});
                return true;
            case R.id.action_setting://设置
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()){
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(boolean grantResult) {
        if (grantResult && !TextUtils.isEmpty(mTmpOrigin) && mTmpGeolocationCallback != null){
            showWebLocationPermissionsDialog(mTmpOrigin,mTmpGeolocationCallback);
        }
    }

    /**
     * 配置WebView
     */
    private void configWebView() {
        WebSettings settings = mWebView.getSettings();
        // 应用 JavaScript
        settings.setJavaScriptEnabled(true);
        // 大小适配
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        // 支持缩放
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // 允许使用地理位置
        settings.setGeolocationEnabled(true);

        // 设置 WebViewClient
        mWebView.setWebViewClient(new MyWebViewClient());
        // 设置 WebChromeClient
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    /**
     * 显示网页提供者
     */
    private void showUrlAuthority(String url){
        Uri uri = Uri.parse(url);
        mTvUrl.setText(getString(R.string.url_text,uri.getAuthority()));
    }

    /**
     * 复制当前网页链接地址
     */
    private void copyUrl() {
        String url = mWebView.getUrl();
        if (TextUtils.isEmpty(url)){
            Toast.makeText(this,R.string.hint_copy_empty,Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.copy(this,url)){
            Toast.makeText(this,R.string.hint_copy_succeed,Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,R.string.hint_copy_failed,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示Toolbar
     */
    private void showToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            if (TextUtils.isEmpty(s)){//没有标题就显示链接地址
                mToolbar.setSubtitle(webView.getUrl());
            } else {//显示标题
                mToolbar.setSubtitle(s);
            }
        }
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback) {
            if(mRequestLocationPermission.checkPermissions()) {
                mTmpOrigin = null;
                mTmpGeolocationCallback = null;
                showWebLocationPermissionsDialog(origin,callback);
            } else {
                mTmpOrigin = origin;
                mTmpGeolocationCallback = callback;
                mRequestLocationPermission.requestPermissions();
            }
        }
    }

    /**
     * 显示网页请求位置信息的Dialog
     */
    private void showWebLocationPermissionsDialog(final String origin,final GeolocationPermissions.Callback callback) {
        final boolean remember = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
        builder.setTitle(R.string.location_dialog_title);
        builder.setMessage(getString(R.string.location_dialog_message, origin))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        callback.invoke(origin, true, remember);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        callback.invoke(origin, false, remember);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            mWebProgressBar.start();
            showUrlAuthority(s);
            Log.i("asd","onPageStarted: " + s);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            mWebProgressBar.finish();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            if (!TextUtils.isEmpty(s)){
                Uri uri = Uri.parse(s);
                if (uri.getScheme().startsWith("http")){
                    Log.i("asd","load: " + s);
                    mWebView.loadUrl(s);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    if (intent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                        WebViewActivity.this.startActivity(intent);
                    }
                    Log.i("asd","no load: " + s);
                }
            }
            return true;
        }
    }
}
