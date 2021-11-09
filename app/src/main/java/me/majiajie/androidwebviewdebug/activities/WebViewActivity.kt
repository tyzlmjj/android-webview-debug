package me.majiajie.androidwebviewdebug.activities

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.text.TextUtils
import android.widget.Toast
import android.content.pm.ActivityInfo
import android.webkit.WebView.WebViewTransport
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.*
import androidx.appcompat.widget.AppCompatEditText
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import me.majiajie.androidwebviewdebug.view.MyWebView
import me.majiajie.androidwebviewdebug.R
import me.majiajie.androidwebviewdebug.utils.Utils
import me.majiajie.androidwebviewdebug.view.WebProgressBar
import me.majiajie.barcode.scanning.ScanBarcodeContract
import me.majiajie.barcode.scanning.bean.BarcodeFormat
import me.majiajie.barcode.scanning.bean.ScanConfig
import java.util.*

class WebViewActivity : AppCompatActivity() {

    private val mToolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val mWebProgressBar: WebProgressBar by lazy { findViewById<WebProgressBar>(R.id.webProgressBar) }
    private val mTvUrl: TextView by lazy { findViewById<TextView>(R.id.tv_url) }
    private val mTvWebviewVersion: TextView by lazy { findViewById<TextView>(R.id.tv_webview_version) }
    private val mWebView: MyWebView by lazy { findViewById<MyWebView>(R.id.webView) }

    /**
     * 扫描二维码
     */
    private val mScanBarcodeLauncher = registerForActivityResult(ScanBarcodeContract()) { text ->
        if (!text.isNullOrBlank() && text.contains(".")) {
            if (text.lowercase(Locale.getDefault()).startsWith("http")) {
                mWebView.loadUrl(text)
            } else {
                mWebView.loadUrl(String.format("https://%s", text))
            }
        }
    }

    /**
     * 外部存储权限相关
     */
    private val REQUEST_EXTERNAL_STORAGE_PERMISSION = 122
    private val EXTERNAL_STORAGE_PERMISSION = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * 定位权限相关
     */
    private val REQUEST_LOCATION_PERMISSION = 120
    private val LOCATION_PERMISSION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * 网页请求文件的相关变量（只支持5.0及以上系统）
     */
    private val REQUEST_FILE = 121
    private var mWebFilePathCallback21: ValueCallback<Array<Uri>>? = null
    private var mWebFileIntent: Intent? = null

    /**
     * 网页请求位置信息时存储的临时变量
     */
    private var mTmpOrigin: String? = null
    private var mTmpGeolocationCallback: GeolocationPermissions.Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(mToolbar)
        configWebView()

        // 显示WebView版本
        mTvWebviewVersion.text = getString(
            R.string.webview_version_text,
            Utils.getWebViewVersion(this)
        )
        mWebView.loadUrl("about:blank")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webview, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_setting).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {//重新加载
                mWebView.reload()
                return true
            }
            R.id.action_scan -> {//扫二维码
                mScanBarcodeLauncher.launch(
                    ScanConfig(formats = listOf(BarcodeFormat.QR_CODE))
                )
            }
            R.id.action_copy -> {//复制
                copyUrl()
                return true
            }
            R.id.action_input -> {// 手动输入
                showInputDialog()
                return true
            }
            R.id.action_setting -> //设置
                return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_FILE -> {// 网页请求文件返回（api21以上）
                    mWebFilePathCallback21 ?: return
                    val dataString = data?.dataString
                    val clipData = data?.clipData
                    val results = if (clipData != null && clipData.itemCount > 0) {
                        (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
                            .toTypedArray()
                    } else if (dataString != null) {
                        arrayOf(Uri.parse(dataString))
                    } else {
                        null
                    }
                    results?.let {
                        mWebFilePathCallback21?.onReceiveValue(it)
                    }
                    mWebFilePathCallback21 = null
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_FILE -> {// 网页请求文件返回（api21以上）
                    mWebFilePathCallback21?.onReceiveValue(null)
                    mWebFilePathCallback21 = null
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var grant = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                grant = false
                break
            }
        }
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE_PERMISSION -> {
                // 外部存储权限
                if (grant && mWebFileIntent != null) {
                    startActivityForResult(mWebFileIntent, REQUEST_FILE)
                } else {
                    if (mWebFilePathCallback21 != null) {
                        mWebFilePathCallback21!!.onReceiveValue(null)
                        mWebFilePathCallback21 = null
                        mWebFileIntent = null
                    }
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                // 定位权限
                if (grant && !TextUtils.isEmpty(mTmpOrigin) && mTmpGeolocationCallback != null) {
                    showWebLocationPermissionsDialog(mTmpOrigin, mTmpGeolocationCallback!!)
                } else {
                    if (mTmpGeolocationCallback != null) {
                        mTmpGeolocationCallback!!.invoke(mTmpOrigin, false, false)
                    }
                    mTmpGeolocationCallback = null
                    mTmpOrigin = null
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        }
    }

    /**
     * 配置WebView
     */
    private fun configWebView() {
        val settings = mWebView.settings
        // 应用 JavaScript
        settings.javaScriptEnabled = true
        // 大小适配
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        // 支持缩放
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        // 允许使用地理位置
        settings.setGeolocationEnabled(true)
        // 在5.0以上显示HTTP图片
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        //支持js调用window.open方法
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 允许开启多窗口
        settings.setSupportMultipleWindows(true)

        // 设置 WebViewClient
        mWebView.webViewClient = MyWebViewClient()
        // 设置 WebChromeClient
        mWebView.webChromeClient = MyWebChromeClient()
    }

    /**
     * 显示输入网址的提示框
     */
    private fun showInputDialog() {
        val holder = InputDialogViewHolder.newInstance(this)
        AlertDialog.Builder(this)
            .setTitle("输入网址")
            .setView(holder.itemView)
            .setPositiveButton("确定") { dialog, which ->
                val url = holder.edtUrl.text.toString()
                mWebView.loadUrl(url)
            }.show()
    }

    /**
     * 显示网页提供者
     */
    private fun showUrlAuthority(url: String?) {
        if (url.isNullOrBlank()) {
            mTvUrl.text = ""
        } else {
            val uri = Uri.parse(url)
            mTvUrl.text = getString(R.string.url_text, uri.authority)
        }
    }

    /**
     * 复制当前网页链接地址
     */
    private fun copyUrl() {
        val url = mWebView.url
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.hint_copy_empty, Toast.LENGTH_SHORT).show()
            return
        }
        if (Utils.copy(this, url)) {
            Toast.makeText(this, R.string.hint_copy_succeed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.hint_copy_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 显示网页请求位置信息的Dialog
     */
    private fun showWebLocationPermissionsDialog(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        val remember = false
        val builder = AlertDialog.Builder(this@WebViewActivity)
        builder.setTitle(R.string.location_dialog_title)
        builder.setMessage(getString(R.string.location_dialog_message, origin))
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                callback?.invoke(
                    origin,
                    true,
                    remember
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                callback?.invoke(
                    origin,
                    false,
                    remember
                )
            }
        val alert = builder.create()
        alert.show()
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            view ?: return
            if (mCustomView != null) {
                callback?.onCustomViewHidden()
                return
            }
            mCustomView = view
            mCustomView?.setBackgroundColor(Color.BLACK)
            (window.decorView as ViewGroup).addView(mCustomView)
            mCustomViewCallback = callback
            mWebView.visibility = View.GONE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        override fun onHideCustomView() {
            mWebView.visibility = View.VISIBLE
            if (mCustomView == null) {
                return
            }
            mCustomView!!.visibility = View.GONE
            (window.decorView as ViewGroup).removeView(mCustomView)
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomView = null
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            super.onHideCustomView()
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            view ?: return false
            val newWebView = WebView(view.context)
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                    return true
                }
            }
            val transport = resultMsg?.obj as? WebViewTransport ?: return false
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }

        @TargetApi(21)
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            mWebFilePathCallback21 = filePathCallback
            val intent = fileChooserParams?.createIntent() ?: return false
            if (intent.resolveActivity(packageManager) == null) {
                Toast.makeText(this@WebViewActivity, "不支持此操作!", Toast.LENGTH_SHORT).show()
                return false
            }
            if (Utils.checkPermissions(this@WebViewActivity, EXTERNAL_STORAGE_PERMISSION)) {
                startActivityForResult(intent, REQUEST_FILE)
            } else {
                mWebFileIntent = intent
                Utils.requestPermissions(
                    this@WebViewActivity,
                    EXTERNAL_STORAGE_PERMISSION,
                    REQUEST_EXTERNAL_STORAGE_PERMISSION,
                    "权限提醒",
                    "需要[读写手机存储]的权限才能使用此功能"
                )
            }
            return true
        }

        override fun onReceivedTitle(webView: WebView?, url: String?) {
            super.onReceivedTitle(webView, url)
            if (url.isNullOrBlank()) { //没有标题就显示链接地址
                mToolbar.subtitle = webView?.url
            } else { //显示标题
                mToolbar.subtitle = url
            }
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            if (Utils.checkPermissions(this@WebViewActivity, LOCATION_PERMISSION)) {
                // 有定位权限，显示给网页授权的Dialog
                mTmpOrigin = null
                mTmpGeolocationCallback = null
                showWebLocationPermissionsDialog(origin, callback)
            } else {
                // 没有定位权限，就去申请权限
                mTmpOrigin = origin
                mTmpGeolocationCallback = callback
                Utils.requestPermissions(
                    this@WebViewActivity, LOCATION_PERMISSION, REQUEST_LOCATION_PERMISSION,
                    "权限提醒", getString(R.string.location_permission_hint)
                )
            }
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(webView: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(webView, url, favicon)
            mWebProgressBar.start()
            showUrlAuthority(url)
            // 阻塞图片加载
            webView?.settings?.blockNetworkImage = true
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            super.onPageFinished(webView, url)
            mWebProgressBar.finish()
            // 使图片可以加载
            webView?.settings?.blockNetworkImage = false
        }

        override fun shouldOverrideUrlLoading(webView: WebView?, url: String?): Boolean {
            if (!url.isNullOrBlank()) {
                val uri = Uri.parse(url)
                val scheme = uri.scheme
                if (!scheme.isNullOrBlank() && scheme.lowercase(Locale.getDefault())
                        .startsWith("http")
                ) {
                    Log.i("asd", "load: $url")
                    val head: MutableMap<String, String?> = HashMap()
                    head["Referer"] = webView?.originalUrl
                    mWebView.loadUrl(url, head)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = uri
                    if (intent.resolveActivity(this@WebViewActivity.packageManager) != null) {
                        this@WebViewActivity.startActivity(intent)
                    }
                    Log.i("asd", "no load: $url")
                }
            }
            return true
        }

    }

    internal class InputDialogViewHolder private constructor(val itemView: View) {

        val edtUrl: AppCompatEditText = itemView.findViewById(R.id.edt_url)

        companion object {
            fun newInstance(context: Context?): InputDialogViewHolder {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null, false)
                return InputDialogViewHolder(view)
            }
        }

    }
}