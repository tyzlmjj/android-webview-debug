package me.majiajie.barcodereader.ui.afterL;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;

import me.majiajie.barcodereader.ui.PreviewListener;

/**
 * 5.0及以上扫码
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanFragment extends Fragment implements PreviewListener
{

    @Override
    public void requestPreviewFrame() {

    }
}
