package me.majiajie.androidwebviewdebug.helper

import android.webkit.WebView
import androidx.lifecycle.*

/**
 * WebView生命周期管理
 */
class WebViewHandler(val webView: WebView, lifecycle: Lifecycle) : DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        webView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        webView.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        webView.destroy()
    }

}