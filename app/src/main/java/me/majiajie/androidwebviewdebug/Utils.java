package me.majiajie.androidwebviewdebug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by mjj on 2017/11/25
 */
public class Utils {

    /**
     * 获取系统WebView的版本号
     */
    public static String getWebViewVersion(Context context){
        String version = "unknow";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.google.android.webview",
                    PackageManager.GET_CONFIGURATIONS);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 复制文本
     * @return 判断复制是否成功
     */
    public static boolean copy(Context context,String text) {
        //创建复制内容
        ClipData clip = ClipData.newPlainText("TEXT",text);

        //获取剪贴板管理实例
        ClipboardManager clipboardManager;
        if (Build.VERSION.SDK_INT >= 23){
            clipboardManager = context.getSystemService(ClipboardManager.class);
        } else {
            clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }

        if (clipboardManager == null){
            return false;
        }

        //复制到剪贴板
        clipboardManager.setPrimaryClip(clip);
        return true;
    }

}
