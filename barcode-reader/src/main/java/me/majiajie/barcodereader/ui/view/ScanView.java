package me.majiajie.barcodereader.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


public class ScanView extends View implements Animatable {
    private ScanDrawable mDrawable;

    private Rect mRect;

    private float mMargin;

    public ScanView(Context context) {
        super(context);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //扫描框的外边距
        mMargin = 48 * getContext().getResources().getDisplayMetrics().density;

        //计算扫描框的范围
        int tem = (int) (w - 2f * mMargin);
        Rect rect = new Rect((int) mMargin, (h - tem) / 2, (int) (w - mMargin), h - (h - tem) / 2);

        //获取屏幕像素
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point screenResolution = new Point();
        display.getSize(screenResolution);

        //将屏幕像素与预览视图的像素进行缩放计算（这里ScanView大小必须和预览视图一样大）
        rect.left = rect.left * w / screenResolution.x;
        rect.right = rect.right * w / screenResolution.x;
        rect.top = rect.top * h / screenResolution.y;
        rect.bottom = rect.bottom * h / screenResolution.y;
        mRect = rect;

        //创建扫描视图
        mDrawable = new ScanDrawable(getContext(), w, h, mMargin);
        mDrawable.setCallback(this);
        //开始扫描动画
        start();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        super.invalidateDrawable(drawable);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDrawable.draw(canvas);
    }

    @Override
    public void start() {
        if (mDrawable != null) {
            mDrawable.start();
        }
    }

    @Override
    public void stop() {
        if (mDrawable != null) {
            mDrawable.stop();
        }
    }

    @Override
    public boolean isRunning() {
        if (mDrawable != null) {
            return mDrawable.isRunning();
        }
        return false;
    }

    public Rect getFramingRect() {
        return mRect;
    }
}
