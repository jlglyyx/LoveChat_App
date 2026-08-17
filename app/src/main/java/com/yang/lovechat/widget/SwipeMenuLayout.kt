package com.yang.lovechat.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.OverScroller
import com.yang.lovechat.util.dip2px
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class SwipeMenuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var leftMenu: View? = null
    private var content: View? = null
    private var rightMenu: View? = null

    private var downX = 0f
    private var lastX = 0f
    private var downY = 0f
    private val scroller = OverScroller(context)

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private var isDragging = false
    private var totalHeight = 0
    private var velocityTracker: VelocityTracker? = null

    private var isOpen = -1

    var allowLeftMenu = true

    var allowRightMenu = true

    private val radius = 10f.dip2px(context).toFloat()

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mViewCache: SwipeMenuLayout? = null
    }

    init {


        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount in 1..3) { "BiDirectionSwipeMenuLayout must have 1-3 children" }

        var maxChildHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxChildHeight = max(maxChildHeight, child.measuredHeight)
        }

        totalHeight = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            maxChildHeight
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.measuredHeight != totalHeight) {
                val childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, child.layoutParams.width)
                val childHeightSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY)
                child.measure(childWidthSpec, childHeightSpec)
            }
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight)


    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            when (i) {
                0 -> {
                    leftMenu = child
                    val leftWidth = if (allowLeftMenu) child.measuredWidth else 0
                    child.layout(-leftWidth, 0, 0, totalHeight)
                }
                1 -> {
                    content = child
                    child.layout(0, 0, child.measuredWidth, totalHeight)
                }
                2 -> {
                    rightMenu = child
                    val rightWidth = if (allowRightMenu) child.measuredWidth else 0
                    val cl = content?.measuredWidth ?: 0
                    child.layout(cl, 0, cl + rightWidth, totalHeight)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        if (!allowLeftMenu && !allowRightMenu) return false

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                lastX = ev.x
                downY = ev.y
                isDragging = false

                if (mViewCache != null && mViewCache != this) {
                    mViewCache?.closeMenu()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) return true

                val dx = abs(ev.x - downX)
                val dy = abs(ev.y - downY)

                if (dx > touchSlop && dx > dy * 1.5f) {
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                if (dy > touchSlop) {
                    return false
                }
            }
        }
        return isDragging


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {


        if (!allowLeftMenu && !allowRightMenu) return false

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) scroller.abortAnimation()
                lastX = event.x
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX

                if (!isDragging) {
                    val deltaX = abs(event.x - downX)
                    val deltaY = abs(event.y - downY)
                    if (deltaX > touchSlop && deltaX > deltaY * 1.2f) {
                        isDragging = true
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }

                if (isDragging) {
                    val targetX = clampScroll(scrollX - dx.toInt())
                    scrollTo(targetX, 0)
                    lastX = event.x
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val vx = velocityTracker?.xVelocity ?: 0f
                    handleFling(vx)
                } else {
                    if (scrollX != 0) closeMenu()
                }
                releaseVelocityTracker()
            }
        }
        return true

    }

    private fun releaseVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
        isDragging = false
    }

    private fun handleFling(vx: Float) {
        val leftWidth = leftMenu?.measuredWidth ?: 0
        val rightWidth = rightMenu?.measuredWidth ?: 0

        when {
            vx < -minFlingVelocity -> { // 向左快速划
                if (scrollX > 0) openRightMenu() else closeMenu()
            }
            vx > minFlingVelocity -> { // 向右快速划
                if (scrollX < 0) openLeftMenu() else closeMenu()
            }
            else -> settleScroll()
        }
    }

    private fun clampScroll(x: Int): Int {
        val leftLimit = if (allowLeftMenu) -(leftMenu?.measuredWidth ?: 0) else 0
        val rightLimit = if (allowRightMenu) rightMenu?.measuredWidth ?: 0 else 0
        return min(max(x, leftLimit), rightLimit)
    }

    private fun settleScroll() {
        val leftWidth = leftMenu?.measuredWidth ?: 0
        val rightWidth = rightMenu?.measuredWidth ?: 0

        val threshold = 0.33f

        if (scrollX > 0) {
            if (scrollX > rightWidth * threshold) openRightMenu() else closeMenu()
        } else if (scrollX < 0) {
            if (abs(scrollX) > leftWidth * threshold) openLeftMenu() else closeMenu()
        }
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    fun closeMenu() {
        isOpen = -1
        if (this == mViewCache) {
            mViewCache = null
        }
        scroller.startScroll(scrollX, 0, -scrollX, 0, 200)
        invalidate()
    }

    fun openLeftMenu() {

        if (!allowLeftMenu) return

        val leftWidth = leftMenu?.measuredWidth ?: return
        isOpen = 0
        mViewCache = this
        scroller.startScroll(scrollX, 0, -leftWidth - scrollX, 0, 200)
        invalidate()
    }

    fun openRightMenu() {

        if (!allowRightMenu) return

        val rightWidth = rightMenu?.measuredWidth ?: return
        isOpen = 1
        mViewCache = this
        scroller.startScroll(scrollX, 0, rightWidth - scrollX, 0, 200)
        invalidate()
    }

    fun isMenuOpen(): Boolean = isOpen != -1

    fun getOpenState(): Int = isOpen

    override fun onDetachedFromWindow() {
        if (this == mViewCache) {
            smoothClose()
            mViewCache = null
        }
        super.onDetachedFromWindow()
    }

    fun smoothClose() {
        if (scrollX != 0) {
            scroller.startScroll(scrollX, 0, -scrollX, 0, 200)
            invalidate()
        }
        isOpen = -1
    }

    fun getViewCache(): SwipeMenuLayout? = mViewCache
}
