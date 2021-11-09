package me.majiajie.androidwebviewdebug

import android.app.Application
import android.webkit.WebView

/**
 * Created by mjj on 2017/11/24
 */
class APP : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
    }
}