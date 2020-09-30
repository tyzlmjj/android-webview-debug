package me.majiajie.androidwebviewdebug;

import android.app.Application;
import android.webkit.WebView;

/**
 * Created by mjj on 2017/11/24
 */
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
