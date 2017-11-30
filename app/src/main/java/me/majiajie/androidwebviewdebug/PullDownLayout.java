package me.majiajie.androidwebviewdebug;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * Created by mjj on 2017/11/24
 * <p>
 *     下拉视图
 * </p>
 */
public class PullDownLayout extends FrameLayout {

    /**
     * 手指下拉临界点
     */
    private final int SLOP = 8;

    /**
     * 视图最大下拉距离(DP)
     */
    private final int MAX_VIEW_MOVE = 150;

    /**
     * 阻尼系数
     */
    private final float DAMP = 3f;

    /**
     * 视图最大下拉距离（px）
     */
    private float mMaxViewMove;

    /**
     * 需要下拉的子视图
     */
    private View mPullDownChildView;

    /**
     * 子视图的接口实现
     */
    private PullDownChild mPullDownChild;

    /**
     * 需要下拉的子视图需要实现的接口
     */
    public interface PullDownChild{

        /**
         * 判断是否在顶部
         */
        boolean isTop();

    }

    public PullDownLayout(@NonNull Context context) {
        this(context,null);
    }

    public PullDownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullDownLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaxViewMove = MAX_VIEW_MOVE * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            View view = getChildAt(i);
            if (view instanceof PullDownChild){
                mPullDownChildView = view;
                mPullDownChild = (PullDownChild) mPullDownChildView;
                break;
            }
        }

        if (mPullDownChildView == null){
            throw new ClassCastException("should be a child implements PullDownChild");
        }
    }

    // 记录当前活跃的指针
    private int mActivePointerId = INVALID_POINTER_ID;

    // 记录手指最后触摸点Y
    private float mLastTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isMoreDown = false;
        int action = ev.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = ev.getActionIndex();

                mLastTouchY = ev.getY(pointerIndex);

                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    final float y = ev.getY(pointerIndex);

                    float deltaY = y - mLastTouchY;
                    isMoreDown = deltaY > SLOP;

                    mLastTouchY = y;

                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        if (isMoreDown && mPullDownChild.isTop()){
            // 置0
            mTmpY = 0f;
            // 暂停可能存在的回复动画
            ValueAnimator animator = getGoBackAnim();
            if (animator.isStarted()){
                animator.cancel();
            }
            return true;
        } else {
            return false;
        }
    }

    // 记录手指在Y轴上的移动距离
    private float mTmpY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        // 用于判断是否需要销毁触摸事件
        boolean shouldMove = true;

        int action = ev.getAction();
        switch(action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    final float y = ev.getY(pointerIndex);

                    mTmpY += y - mLastTouchY;
                    mLastTouchY = y;

                    // 视图下移距离计算
                    int topMove = (int) DampingUtils.getViewMove(mTmpY,DAMP,mMaxViewMove);
                    // 控制视图下移
                    mPullDownChildView.layout(0,topMove,mPullDownChildView.getWidth(),mPullDownChildView.getHeight() + topMove);
                    // 当滑动回顶部之后不再获取Touch事件
                    shouldMove = mTmpY > 0;
                }

                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                shouldMove = false;
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        // 如果不需要再拖动，就将视图移回顶部
        if (!shouldMove){
            goBackTop();
        }
        return shouldMove;
    }

    /**
     * 回到顶部
     */
    private void goBackTop() {
        if (mPullDownChildView != null){
            getGoBackAnim().start();
        }
    }
    private ValueAnimator mGobackAnimator;

    /**
     * 回到顶部部的动画
     */
    private ValueAnimator getGoBackAnim(){
        if (mGobackAnimator == null){
            ValueAnimator animator = ValueAnimator.ofFloat(1f,0f);
            animator.setDuration(500);
            animator.addUpdateListener(mGoBackListener);
            mGobackAnimator = animator;
        }
        return mGobackAnimator;
    }

    /**
     * 视图回弹动画监听
     */
    ValueAnimator.AnimatorUpdateListener mGoBackListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float n = (float) valueAnimator.getAnimatedValue();
            int topMove = (int) (mPullDownChildView.getTop() * n);
            mPullDownChildView.layout(0,topMove,mPullDownChildView.getWidth(),mPullDownChildView.getHeight() + topMove);
        }
    };
}
