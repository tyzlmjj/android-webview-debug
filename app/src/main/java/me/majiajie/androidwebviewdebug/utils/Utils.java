package me.majiajie.androidwebviewdebug.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.webkit.WebViewCompat;

import java.lang.ref.WeakReference;

/**
 * Created by mjj on 2017/11/25
 */
public class Utils {

    /**
     * 获取系统WebView的版本号
     */
    public static String getWebViewVersion(Context context) {
        String version = "unknow";
        PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(context);
        if (webViewPackageInfo != null) {
            version = webViewPackageInfo.versionName;
        } else {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo("com.google.android.webview",
                        PackageManager.GET_CONFIGURATIONS);
                version = pi.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    /**
     * 复制文本
     *
     * @return 判断复制是否成功
     */
    public static boolean copy(Context context, String text) {
        //创建复制内容
        ClipData clip = ClipData.newPlainText("TEXT", text);

        //获取剪贴板管理实例
        ClipboardManager clipboardManager;
        if (Build.VERSION.SDK_INT >= 23) {
            clipboardManager = context.getSystemService(ClipboardManager.class);
        } else {
            clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }

        if (clipboardManager == null) {
            return false;
        }

        //复制到剪贴板
        clipboardManager.setPrimaryClip(clip);
        return true;
    }

    /**
     * 检查权限
     */
    public static boolean checkPermissions(Activity activity, String[] permissions) {
        boolean result = true;
        for (String permission : permissions) {
            int rc = ActivityCompat.checkSelfPermission(activity, permission);
            if (rc != PackageManager.PERMISSION_GRANTED) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 请求权限
     */
    public static void requestPermissions(Activity activity, final String[] permissions,
                                          final int requestCode, String hintTitle, String hintMessage) {
        final WeakReference<Activity> ac = new WeakReference<>(activity);

        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
                break;
            }
        }
        if (!showRationale) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return;
        }

        Dialog.OnClickListener listener = new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Activity a = ac.get();
                if (a != null) {
                    ActivityCompat.requestPermissions(a, permissions, requestCode);
                }
            }
        };

        new AlertDialog.Builder(activity)
                .setTitle(hintTitle)
                .setMessage(hintMessage)
                .setPositiveButton(android.R.string.ok, listener)
                .setCancelable(false)
                .show();
    }

}
