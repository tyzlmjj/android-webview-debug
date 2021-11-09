package me.majiajie.androidwebviewdebug.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import me.majiajie.androidwebviewdebug.view.PullDownLayout.PullDownChild
import android.view.MotionEvent
import android.webkit.WebView
import androidx.lifecycle.LifecycleOwner
import me.majiajie.androidwebviewdebug.helper.WebViewHandler

/**
 * Created by mjj on 2017/11/24
 */
class MyWebView : WebView, PullDownChild {
    private var mIsTop = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr, 0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        // 绑定生命周期
        (context as? LifecycleOwner)?.lifecycle?.let {
            WebViewHandler(this, it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> mIsTop = false
        }
        return super.onTouchEvent(event)
    }

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        if (scrollY <= 0) {
            if (!mIsTop && deltaY < 0) {
                mIsTop = true
            }
        }
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            maxOverScrollY,
            isTouchEvent
        )
    }

    override fun isTop(): Boolean {
        return mIsTop
    }
}