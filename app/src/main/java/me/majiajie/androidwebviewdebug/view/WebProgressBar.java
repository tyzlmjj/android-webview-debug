package me.majiajie.androidwebviewdebug.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


/**
 * Created by mjj on 2017/9/7
 * <p>
 *      网页加载水平指示条
 * </p>
 */
public class WebProgressBar extends View {

    private ValueAnimator mStartAnim;

    private ValueAnimator mFinishAnim;

    private boolean mIsFinish;

    private float mValue = 0f;

    private Rect mRect;
    private Paint mPaint;

    public WebProgressBar(Context context) {
        this(context, null);
    }

    public WebProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRect = new Rect();

        mPaint = new Paint();
        mPaint.setColor(getColorAccent(context));
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 开始
     */
    public void start() {
        mIsFinish = false;

        if (mStartAnim == null) {
            ValueAnimator animator = ValueAnimator.ofFloat(0.9f);
            animator.setDuration(5_000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mValue = (float) animation.getAnimatedValue();
                }
            });
            mStartAnim = animator;
        }

        if (mFinishAnim != null && mFinishAnim.isStarted()) {
            mFinishAnim.cancel();
        }

        mStartAnim.start();

        invalidate();
    }

    /**
     * 结束
     */
    public void finish() {
        mIsFinish = true;

        if (mFinishAnim == null) {
            ValueAnimator animator = ValueAnimator.ofFloat(1.5f);
            animator.setDuration(800);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mValue = (float) animation.getAnimatedValue();
                }
            });
            mFinishAnim = animator;
        }

        if (mStartAnim != null && mStartAnim.isStarted()) {
            mStartAnim.cancel();
        }

        mFinishAnim.setFloatValues(mValue, 1.5f);
        mFinishAnim.start();

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!mIsFinish) {//开始动画不需要透明
            mPaint.setAlpha(255);
        } else if (mValue >= 0.5f) { // 线条过半之后才使用透明度
            mPaint.setAlpha((int) (255 * (1.5f - mValue)));
        }

        int right = (int) (getWidth() * Math.min(1f, mValue));

        mRect.set(0, 0, right, getHeight());
        canvas.drawRect(mRect, mPaint);

        if ((mStartAnim != null && mStartAnim.isStarted()) || (mFinishAnim != null && mFinishAnim.isStarted())) {
            invalidate();
        }
    }

    /**
     * 获取colorAccent的颜色,需要V7包的支持
     *
     * @param context 上下文
     * @return 0xAARRGGBB
     */
    private int getColorAccent(Context context) {
        Resources res = context.getResources();
        int attrRes = res.getIdentifier("colorAccent", "attr", context.getPackageName());
        if (attrRes == 0) {
            return 0xFF000000;
        }
        return ContextCompat.getColor(context, getResourceId(context, attrRes));
    }

    /**
     * 获取自定义属性的资源ID
     *
     * @param context 上下文
     * @param attrRes 自定义属性
     * @return resourceId
     */
    private int getResourceId(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.resourceId;
    }
}
