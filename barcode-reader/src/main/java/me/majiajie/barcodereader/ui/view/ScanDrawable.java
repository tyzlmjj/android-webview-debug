package me.majiajie.barcodereader.ui.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ScanDrawable extends Drawable implements Animatable
{
    //扫描动画移动一次的时间
    private final int ANIM_TIME = 3_000;
    //扫描匡以外颜色
    private final int COLOR_OUTSIDE_BACKGROUND = 0x66000000;
    //扫描线条的颜色
    private final int COLOR_LINE_BACKGROUND = 0xFFFFFFFF;

    //扫描匡长度
    private int mScanViewLenght;
    //扫描匡外边距
    private float mMargin;

    //总的宽高
    private int mWidth;
    private int mHeight;

    private Rect mRectTop,mRectBottom,mRectRight,mRectLeft;

    private Paint mPaintOutside;

    private Paint mPaintLine;

    private Path mPathLine,mPathFrame;

    private ValueAnimator mAnimator;

    private float mStartX,mStartY,mEndX,mEndY;

    public ScanDrawable(Context context,int width,int height,float margin)
    {
        mMargin = margin;
        mWidth = width;
        mHeight = height;
        mScanViewLenght = (int) (width - mMargin * 2);

        mPaintOutside = new Paint();
        mPaintOutside.setColor(COLOR_OUTSIDE_BACKGROUND);

        mRectLeft = new Rect(0,(height - mScanViewLenght)/2, (int) mMargin,height - (height - mScanViewLenght)/2);
        mRectTop = new Rect(0,0,width,(height - mScanViewLenght)/2);
        mRectRight = new Rect((int) (width - mMargin),(height - mScanViewLenght)/2,width,height - (height - mScanViewLenght)/2);
        mRectBottom = new Rect(0,height - (height - mScanViewLenght)/2,width,height);

        float one_dp = 1 * context.getResources().getDisplayMetrics().density;

        mPaintLine = new Paint();
        mPaintLine.setColor(COLOR_LINE_BACKGROUND);
        mPaintLine.setStrokeWidth(one_dp*2);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setAntiAlias(true);//抗锯齿
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);//直线头尾圆滑
        mPaintLine.setStrokeJoin(Paint.Join.ROUND);//直线交界处圆滑处理

        //上下移动的线
        mPathLine = new Path();
        mStartX = mMargin + one_dp;
        mStartY = mEndY = (height - mScanViewLenght)/2f;
        mEndX = width - mMargin - one_dp;
        mPathLine.moveTo(mStartX,mStartY);
        mPathLine.lineTo(mEndX,mEndY);

        //画四个角
        float five_dp = 8 * context.getResources().getDisplayMetrics().density;
        mPathFrame = new Path();
        mPathFrame.moveTo(mMargin*1.5f,(mHeight-mScanViewLenght)/2f+mMargin*0.5f + five_dp);
        mPathFrame.lineTo(mMargin*1.5f,(mHeight-mScanViewLenght)/2f+mMargin*0.5f);
        mPathFrame.lineTo(mMargin*1.5f + five_dp,(mHeight-mScanViewLenght)/2f+mMargin*0.5f);

        mPathFrame.moveTo(mMargin*0.5f+mScanViewLenght-five_dp,(mHeight-mScanViewLenght)/2f+mMargin*0.5f);
        mPathFrame.lineTo(mMargin*0.5f+mScanViewLenght,(mHeight-mScanViewLenght)/2f+mMargin*0.5f);
        mPathFrame.lineTo(mMargin*0.5f+mScanViewLenght,(mHeight-mScanViewLenght)/2f+mMargin*0.5f+five_dp);

        mPathFrame.moveTo(mMargin*0.5f+mScanViewLenght,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f-five_dp);
        mPathFrame.lineTo(mMargin*0.5f+mScanViewLenght,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f);
        mPathFrame.lineTo(mMargin*0.5f+mScanViewLenght-five_dp,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f);

        mPathFrame.moveTo(mMargin*1.5f + five_dp,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f);
        mPathFrame.lineTo(mMargin*1.5f,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f);
        mPathFrame.lineTo(mMargin*1.5f,mHeight-(mHeight-mScanViewLenght)/2f-mMargin*0.5f-five_dp);

        setupAnimators();
    }

    @Override
    public void draw(Canvas canvas)
    {
        canvas.drawRect(mRectLeft,mPaintOutside);
        canvas.drawRect(mRectTop,mPaintOutside);
        canvas.drawRect(mRectRight,mPaintOutside);
        canvas.drawRect(mRectBottom,mPaintOutside);

        canvas.drawPath(mPathLine,mPaintLine);
        canvas.drawPath(mPathFrame,mPaintLine);
    }
;
    @Override
    public void setAlpha(int alpha)
    {
        mPaintLine.setAlpha(alpha);
        mPaintOutside.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter)
    {
        mPaintLine.setColorFilter(colorFilter);
        mPaintOutside.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start()
    {
        mAnimator.start();
    }

    @Override
    public void stop()
    {
        mAnimator.end();
    }

    @Override
    public boolean isRunning()
    {
        return mAnimator.isRunning();
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    private void setupAnimators()
    {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f,1f);
        valueAnimator.setDuration(ANIM_TIME);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float n = (float) animation.getAnimatedValue();

                Path path = new Path();
                mStartY = mEndY = (mHeight - mScanViewLenght)/2f + mScanViewLenght*n;
                path.moveTo(mStartX,mStartY);
                path.lineTo(mEndX,mEndY);
                mPathLine = path;

                invalidateSelf();
            }
        });

        mAnimator = valueAnimator;
    }
}
