package com.lyml;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * Created by LYML on 2018-02-24.
 */

public class SwipeLayout extends LinearLayout {
    public static final int SCROLL_STATE_NORMAL = 0;
    public static final int SCROLL_STATE_LEFT = 1;
    public static final int SCROLL_STATE_RIGHT = 2;

    //默认View
    private int defaultView;
    //滑动状态
    private int scrollState;
    //默认View匹配GrandParent，当非第一个View作为默认View且需要充满布局时使用
    private boolean defaultViewWidthRealMatchParent;
    //滑动事件
    private OnSwipedListener onSwipedListener;

    private int scaledTouchSlop;
    private float downX;
    private float downY;
    private boolean toScroll = false;
    private int lastScrollX;
    private ValueAnimator animator;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        scaledTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeLayout, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SwipeLayout_defaultView)
                this.defaultView = a.getInteger(attr, 0);
            else if (attr == R.styleable.SwipeLayout_scrollState)
                this.scrollState = a.getInteger(attr, SCROLL_STATE_NORMAL);
            else if (attr == R.styleable.SwipeLayout_defaultViewWidthRealMatchParent)
                this.defaultViewWidthRealMatchParent = a.getBoolean(attr, false);
        }
        a.recycle();
    }

    public void setOnSwipedListener(OnSwipedListener onSwipedListener) {
        this.onSwipedListener = onSwipedListener;
    }

    /**
     * 获得当前状态
     * @return
     */
    public int getScrollState() {
        return scrollState;
    }

    /**
     * 切换到状态
     * @param scrollState
     */
    public void setScrollState(int scrollState) {
        if (animator != null && animator.isRunning())
            animator.cancel();
        if (scrollState != this.scrollState) {
            if (onSwipedListener != null)
                onSwipedListener.onSwiped(scrollState, this.scrollState);
        }
        switch (scrollState) {
            case SCROLL_STATE_NORMAL:
                scrollTo(getLeftScrollMax(), 0);
                this.scrollState = scrollState;
                break;
            case SCROLL_STATE_LEFT:
                scrollTo(0, 0);
                this.scrollState = scrollState;
                break;
            case SCROLL_STATE_RIGHT:
                scrollTo(getLeftScrollMax() + getRightScrollMax(), 0);
                this.scrollState = scrollState;
                break;
        }
    }

    /**
     * 平滑滚动到状态
     * @param scrollState
     */
    public void smoothScrollToState(int scrollState) {
        if (animator != null && animator.isRunning())
            animator.cancel();
        if (scrollState != this.scrollState) {
            if (onSwipedListener != null)
                onSwipedListener.onSwiped(scrollState, this.scrollState);
        }
        switch (scrollState) {
            case SCROLL_STATE_NORMAL:
                animator = ValueAnimator.ofInt(getScrollX(), getLeftScrollMax());
                this.scrollState = scrollState;
                break;
            case SCROLL_STATE_LEFT:
                animator = ValueAnimator.ofInt(getScrollX(), 0);
                this.scrollState = scrollState;
                break;
            case SCROLL_STATE_RIGHT:
                animator = ValueAnimator.ofInt(getScrollX(), getLeftScrollMax() + getRightScrollMax());
                this.scrollState = scrollState;
                break;
        }
        animator.removeAllUpdateListeners();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((int) animation.getAnimatedValue(), 0);
            }
        });
        animator.start();
    }

    /**
     * onMeasure前处理defaultView宽度
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //如果要defaultView宽度填满屏幕，需要在onMeasure前处理
        if (defaultViewWidthRealMatchParent) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            LayoutParams para1;
            para1 = (LayoutParams) getChildAt(defaultView).getLayoutParams();
            int width= widthSize - para1.leftMargin - para1.rightMargin;
            if(para1.width!=width) {
                para1.width = width;
                getChildAt(defaultView).setLayoutParams(para1);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * onLayout后处理初始滚动位置
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //onDraw之前滚动布局到默认位置
        scrollTo(getLeftScrollMax(), 0);
        setScrollState(scrollState);
    }

    /**
     * 在子控件上发生横向滑动时拦截Touch事件
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastScrollX = this.getScrollX();
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX();
                float moveY = event.getRawY();
                //发生横向滑动拦截
                if (Math.abs(moveX - downX) > scaledTouchSlop && Math.abs(moveY - downY) <= scaledTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 滚动处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getChildCount() >= 2) {
            int scrollRightMax = getRightScrollMax();
            int scrollLeftMax = getLeftScrollMax();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastScrollX = this.getScrollX();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getRawX();
                    float moveY = event.getRawY();

                    //横向滑动，一旦确定为横向滑动，标志位toScroll为true，只有在ActionUp才能置false
                    if (!toScroll&&(Math.abs(moveX - downX) > scaledTouchSlop && Math.abs(moveY - downY) <= scaledTouchSlop)) {
                        if (animator != null && animator.isRunning())
                            animator.cancel();
                        toScroll = true;
                        //请求父控件不拦截
                        getParent().requestDisallowInterceptTouchEvent(true);
                        //取消事件
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        onTouchEvent(cancelEvent);
                    }

                    if (toScroll) {
                        //滑动限制
                        float scrollX = lastScrollX - (moveX - downX);
                        if (scrollX < 0)
                            scrollX = 0;
                        else if (scrollX > scrollLeftMax + scrollRightMax)
                            scrollX = scrollLeftMax + scrollRightMax;
                        scrollTo((int) scrollX, 0);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (toScroll) {
                        if (getScrollX() > scrollLeftMax + scrollRightMax / 2) {
                            animator = ValueAnimator.ofInt(getScrollX(), scrollLeftMax + scrollRightMax);
                            if (onSwipedListener != null)
                                onSwipedListener.onSwiped(SCROLL_STATE_RIGHT, scrollState);
                            scrollState = SCROLL_STATE_RIGHT;
                        } else if (getScrollX() < scrollLeftMax / 2) {
                            animator = ValueAnimator.ofInt(getScrollX(), 0);
                            if (onSwipedListener != null)
                                onSwipedListener.onSwiped(SCROLL_STATE_LEFT, scrollState);
                            scrollState = SCROLL_STATE_LEFT;
                        } else {
                            animator = ValueAnimator.ofInt(getScrollX(), scrollLeftMax);
                            if (onSwipedListener != null)
                                onSwipedListener.onSwiped(SCROLL_STATE_NORMAL, scrollState);
                            scrollState = SCROLL_STATE_NORMAL;
                        }
                        animator.removeAllUpdateListeners();
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                scrollTo((int) animation.getAnimatedValue(), 0);
                            }
                        });
                        animator.start();
                        toScroll = false;
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 默认控件左边的最大可滑动距离，考虑Margins
     * @return
     */
    private int getLeftScrollMax() {
        int viewCount = this.getChildCount();
        int scroll = 0;
        if (viewCount > defaultView)
            for (int i = 0; i < defaultView; i++)
                scroll += getWidthWithMargin(this.getChildAt(i));
        return scroll - getLeftScrollCorrect();
    }

    /**
     * 默认控件右边的最大滑动距离，考虑Margin
     * @return
     */
    private int getRightScrollMax() {
        int viewCount = this.getChildCount();
        int scroll = 0;
        if (viewCount > defaultView)
            for (int i = defaultView + 1; i < viewCount; i++)
                scroll += getWidthWithMargin(this.getChildAt(i));
        return scroll - getRightScrollCorrect();
    }

    /**
     * 当所有控件宽度和小于屏幕时，修正左边的最大可滚动距离
     * @return
     */
    private int getLeftScrollCorrect() {
        int viewCount = this.getChildCount();
        int width = 0;
        for (int i = defaultView; i < viewCount; i++) {
            width += getWidthWithMargin(this.getChildAt(i));
            if (width >= this.getWidth())
                break;
        }
        if (width < this.getWidth())
            return this.getWidth() - width;
        else
            return 0;
    }

    /**
     * 当所有控件宽度和小于屏幕时和大于屏幕时，修正右边的最大可滚动距离
     * @return
     */
    private int getRightScrollCorrect() {
        int viewCount = this.getChildCount();
        int width = 0;
        for (int i = defaultView; i < viewCount; i++)
            width += getWidthWithMargin(this.getChildAt(i));

        if (width >= this.getWidth())
            return this.getWidth() - getWidthWithMargin(this.getChildAt(defaultView));
        else
            return width - getWidthWithMargin(this.getChildAt(defaultView));
    }

    private int getWidthWithMargin(View view) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        return view.getWidth() + lp.leftMargin + lp.rightMargin;
    }

    public interface OnSwipedListener {
        void onSwiped(int state, int stateOld);
    }
}
