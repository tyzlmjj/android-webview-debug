package me.majiajie.androidwebviewdebug;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

import me.majiajie.barcodereader.BarcodeFormat;
import me.majiajie.barcodereader.ScanActivity;
import me.majiajie.barcodereader.decode.DecodeResult;

public class WebViewActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private WebProgressBar mWebProgressBar;
    private TextView mTvUrl;
    private TextView mTvWebviewVersion;
    private WebView mWebView;

    /**
     * 外部存储权限相关
     */
    private final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 122;
    private final String[] EXTERNAL_STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 定位权限相关
     */
    private final int REQUEST_LOCATION_PERMISSION = 120;
    private final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};

    /**
     * 网页请求文件的相关变量（只支持5.0及以上系统）
     */
    private final int REQUEST_FILE = 121;
    private ValueCallback<Uri[]> mWebFilePathCallback21;
    private Intent mWebFileIntent;

    /**
     * 网页请求位置信息时存储的临时变量
     */
    private String mTmpOrigin;
    private GeolocationPermissions.Callback mTmpGeolocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mToolbar = findViewById(R.id.toolbar);
        mWebProgressBar = findViewById(R.id.webProgressBar);
        mTvUrl = findViewById(R.id.tv_url);
        mTvWebviewVersion = findViewById(R.id.tv_webview_version);
        mWebView = findViewById(R.id.webView);

        setSupportActionBar(mToolbar);

        configWebView();

        // 显示WebView版本
        mTvWebviewVersion.setText(getString(R.string.webview_version_text, Utils.getWebViewVersion(this)));

//        mWebView.loadUrl("about:blank");
        mWebView.loadUrl("http://192.168.62.143:8001/#/course?clientcode=C1528945549IFY&operaterid=P1519803300TLO&paper_id=56&_k=euy9uq");
//        mWebView.loadUrl("http://shopweb.yis-soft.com/#/auoutus?versionnum=2.5.2&versioncode=115");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ScanActivity.REQUEST_CODE:// 扫码返回
                    DecodeResult decodeResult = ScanActivity.getResult(data);
                    String urlStr = decodeResult.getText();
                    if (!TextUtils.isEmpty(urlStr) && urlStr.contains(".")) {
                        if (urlStr.toLowerCase().startsWith("http")) {
                            mWebView.loadUrl(urlStr);
                        } else {
                            mWebView.loadUrl(String.format("http://%s", urlStr));
                        }
                    }
                    break;
                case REQUEST_FILE:// 网页请求文件返回（api21以上）
                    if (mWebFilePathCallback21 != null) {
                        Uri[] results = null;
                        if (data != null) {
                            String dataString = data.getDataString();
                            ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                results = new Uri[clipData.getItemCount()];
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    ClipData.Item item = clipData.getItemAt(i);
                                    results[i] = item.getUri();
                                }
                            }
                            if (dataString != null)
                                results = new Uri[]{Uri.parse(dataString)};
                        }
                        mWebFilePathCallback21.onReceiveValue(results);
                        mWebFilePathCallback21 = null;
                    }
                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_FILE:// 网页请求文件返回（api21以上）
                    if (mWebFilePathCallback21 != null) {
                        mWebFilePathCallback21.onReceiveValue(null);
                        mWebFilePathCallback21 = null;
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_setting).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_reload://重新加载
                mWebView.reload();
                return true;
            case R.id.action_copy://复制
                copyUrl();
                return true;
            case R.id.action_scan://扫二维码
                ScanActivity.startActivityForResult(this, 0, new int[]{BarcodeFormat.QR_CODE});
                return true;
            case R.id.action_input:// 手动输入
                showInputDialog();
                return true;
            case R.id.action_setting://设置
//                Intent intent = new Intent(this,SettingsActivity.class);
//                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean grant = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                grant = false;
                break;
            }
        }

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERMISSION: {// 外部存储权限
                if (grant && mWebFileIntent != null) {
                    startActivityForResult(mWebFileIntent, REQUEST_FILE);
                } else {
                    if (mWebFilePathCallback21 != null) {
                        mWebFilePathCallback21.onReceiveValue(null);
                        mWebFilePathCallback21 = null;
                        mWebFileIntent = null;
                    }

                }
                break;
            }
            case REQUEST_LOCATION_PERMISSION: {// 定位权限
                if (grant && !TextUtils.isEmpty(mTmpOrigin) && mTmpGeolocationCallback != null) {
                    showWebLocationPermissionsDialog(mTmpOrigin, mTmpGeolocationCallback);
                } else {
                    if (mTmpGeolocationCallback != null) {
                        mTmpGeolocationCallback.invoke(mTmpOrigin, false, false);
                    }
                    mTmpGeolocationCallback = null;
                    mTmpOrigin = null;
                }
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
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
        // 在5.0以上显示HTTP图片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //支持js调用window.open方法
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 允许开启多窗口
        settings.setSupportMultipleWindows(true);

        // 设置 WebViewClient
        mWebView.setWebViewClient(new MyWebViewClient());
        // 设置 WebChromeClient
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    /**
     * 显示输入网址的提示框
     */
    private void showInputDialog() {
        final InputDialogViewHolder holder = InputDialogViewHolder.newInstance(this);
        new AlertDialog.Builder(this)
                .setTitle("输入网址")
                .setView(holder.itemView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = holder.edtUrl.getText().toString();
                        mWebView.loadUrl(url);
                    }
                }).show();
    }

    /**
     * 显示网页提供者
     */
    private void showUrlAuthority(String url) {
        Uri uri = Uri.parse(url);
        mTvUrl.setText(getString(R.string.url_text, uri.getAuthority()));
    }

    /**
     * 复制当前网页链接地址
     */
    private void copyUrl() {
        String url = mWebView.getUrl();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.hint_copy_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.copy(this, url)) {
            Toast.makeText(this, R.string.hint_copy_succeed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.hint_copy_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示网页请求位置信息的Dialog
     */
    private void showWebLocationPermissionsDialog(final String origin, final GeolocationPermissions.Callback callback) {
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

    private class MyWebChromeClient extends WebChromeClient {

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            mCustomView.setBackgroundColor(Color.BLACK);
            ((ViewGroup)getWindow().getDecorView()).addView(mCustomView);
            mCustomViewCallback = callback;
            mWebView.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            mWebView.setVisibility(View.VISIBLE);
            if (mCustomView == null) {
                return;
            }
            mCustomView.setVisibility(View.GONE);
            ((ViewGroup)getWindow().getDecorView()).removeView(mCustomView);
            mCustomViewCallback.onCustomViewHidden();
            mCustomView = null;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            super.onHideCustomView();
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWebView = new WebView(view.getContext());
            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @TargetApi(21)
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback,
                                         WebChromeClient.FileChooserParams fileChooserParams) {
            mWebFilePathCallback21 = filePathCallback;
            Intent intent = fileChooserParams.createIntent();

            if (intent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(WebViewActivity.this, "不支持此操作!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Utils.checkPermissions(WebViewActivity.this, EXTERNAL_STORAGE_PERMISSION)) {
                startActivityForResult(intent, REQUEST_FILE);
            } else {
                mWebFileIntent = intent;
                Utils.requestPermissions(WebViewActivity.this,
                        EXTERNAL_STORAGE_PERMISSION, REQUEST_EXTERNAL_STORAGE_PERMISSION
                        , "权限提醒", "需要[读写手机存储]的权限才能使用此功能");
            }
            return true;
        }

        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            if (TextUtils.isEmpty(s)) {//没有标题就显示链接地址
                mToolbar.setSubtitle(webView.getUrl());
            } else {//显示标题
                mToolbar.setSubtitle(s);
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {

            if (Utils.checkPermissions(WebViewActivity.this, LOCATION_PERMISSION)) {
                // 有定位权限，显示给网页授权的Dialog
                mTmpOrigin = null;
                mTmpGeolocationCallback = null;
                showWebLocationPermissionsDialog(origin, callback);
            } else {
                // 没有定位权限，就去申请权限
                mTmpOrigin = origin;
                mTmpGeolocationCallback = callback;
                Utils.requestPermissions(WebViewActivity.this, LOCATION_PERMISSION, REQUEST_LOCATION_PERMISSION,
                        "权限提醒", getString(R.string.location_permission_hint));
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            mWebProgressBar.start();
            showUrlAuthority(s);
            // 阻塞图片加载
            webView.getSettings().setBlockNetworkImage(true);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            mWebProgressBar.finish();
            // 使图片可以加载
            webView.getSettings().setBlockNetworkImage(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            if (!TextUtils.isEmpty(s)) {
                Uri uri = Uri.parse(s);
                String scheme = uri.getScheme();
                if (!TextUtils.isEmpty(scheme) && scheme.toLowerCase().startsWith("http")) {
                    Log.i("asd", "load: " + s);

                    Map<String, String> head = new HashMap<>();
                    head.put("Referer", webView.getOriginalUrl());
                    mWebView.loadUrl(s, head);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    if (intent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                        WebViewActivity.this.startActivity(intent);
                    }
                    Log.i("asd", "no load: " + s);
                }
            }
            return true;
        }

//        @TargetApi(21)
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Log.i("asd", "request.isRedirect(): " + " " + request.getUrl().toString());
//            return super.shouldOverrideUrlLoading(view, request);
//        }
    }

    static class InputDialogViewHolder {

        public static InputDialogViewHolder newInstance(Context context) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null, false);
            return new InputDialogViewHolder(view);
        }

        private View itemView;
        private AppCompatEditText edtUrl;

        private InputDialogViewHolder(View itemView) {
            this.itemView = itemView;
            edtUrl = itemView.findViewById(R.id.edt_url);
        }

    }
}
