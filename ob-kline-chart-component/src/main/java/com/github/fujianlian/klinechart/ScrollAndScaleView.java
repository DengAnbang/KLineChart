package com.github.fujianlian.klinechart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import androidx.core.view.GestureDetectorCompat;

import com.github.fujianlian.klinechart.utils.SwipeUtils;


/**
 * 可以滑动和放大的view
 * Created by tian on 2016/5/3.
 */
public abstract class ScrollAndScaleView extends RelativeLayout implements
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "ScrollAndScaleView";
    protected int mScrollX = 0;
    protected GestureDetectorCompat mDetector;
    protected ScaleGestureDetector mScaleDetector;

    protected boolean isLongPress = false;
    protected boolean isSelectedPoint = false;
    private boolean isTriggerDragChart = false;
    private OverScroller mScroller;

    protected boolean touch = false;

    protected float mScaleX = 1;

    protected float mScaleXMax = 5.0f;

    protected float mScaleXMin = 0.5f;

    private boolean mMultipleTouch = false;

    private boolean mScrollEnable = true;
    private boolean mScaleEnable = true;

    private float mMoveX, mMoveY;
    private float mOffsetX, mOffsetY;
    private float mLastClickX, mLastClickY;
    private boolean isEditDrawBean = false;

    public ScrollAndScaleView(Context context) {
        super(context);
        init();
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mScroller = new OverScroller(getContext());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp");
        doOnSingleClick(e);
        invalidate();
        return false;
    }

    protected void doOnSingleClick(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll");
        if (isEditDrawBean()) {
            return true;
        }
        isSelectedPoint = false;
        if (!isLongPress && !isMultipleTouch()) {
            scrollBy(Math.round(distanceX), 0);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress");
        isLongPress = true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling");
        if (isEditDrawBean()) {
            return true;
        }
        if (!isTouch() && isScrollEnable()) {
            isLongPress = false;
            isSelectedPoint = false;
            mScroller.fling(mScrollX, 0
                    , Math.round(velocityX / mScaleX), 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    0, 0);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!isTouch()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            } else {
                mScroller.forceFinished(true);
            }
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX - Math.round(x / mScaleX), 0);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (!isScrollEnable()) {
            mScroller.forceFinished(true);
            return;
        }
        if (isEditDrawBean()) {
            return;
        }
        int oldX = mScrollX;
        mScrollX = x;
        if (mScrollX < getMinScrollX()) {
            if (isHasData()) {
                mScrollX = getMinScrollX();
            }
            onRightSide();
            mScroller.forceFinished(true);
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX();
            onLeftSide();
            mScroller.forceFinished(true);
        }
        if (mScrollX > getMaxItemScrollX()) {
            Log.d(TAG, "scrollTo onFirstCandleVisible");
            onFirstItemVisible();
        }
        Log.d(TAG, "scrollTo scrollX=" + mScrollX);
        onScrollChanged(mScrollX, 0, oldX, 0);
        invalidate();
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!isScaleEnable()) {
            return false;
        }
        if (isEditDrawBean()) {
            return false;
        }
        float oldScale = mScaleX;
        mScaleX *= detector.getScaleFactor();
        if (mScaleX < mScaleXMin) {
            mScaleX = mScaleXMin;
        } else if (mScaleX > mScaleXMax) {
            mScaleX = mScaleXMax;
        } else {
            onScaleChanged(mScaleX, oldScale);
        }
        return true;
    }

    protected void onScaleChanged(float scale, float oldScale) {
        invalidate();
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent, ACTION_DOWN");
                touch = true;
                mLastClickX = x;
                mLastClickY = y;
                onTouchDown();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent, ACTION_MOVE");
                mMoveX = event.getX();
                mMoveY = event.getY();

                //计算滑动的距离
                mOffsetX = x - mLastClickX;
                mOffsetY = y - mLastClickY;

                requestTouchEvent();

                //长按后移动刷新选中的位置
                if (isLongPress && !isEditDrawBean()) {
                    onLongPress(event);
                }

                if (isEditDrawBean()) {
                    onTouchMove();
                }

                mLastClickX = x;
                mLastClickY = y;
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent, ACTION_UP");
                mMoveX = 0;
                mMoveY = 0;
                touch = false;
                isLongPress = false;

                //抬起手势，将事件还给parent
                isTriggerDragChart = false;
                getParent().requestDisallowInterceptTouchEvent(false);

                onTouchUp();
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent, ACTION_CANCEL");
                touch = false;
                invalidate();
                break;
            default:
                break;
        }
        mMultipleTouch = event.getPointerCount() > 1;
        this.mDetector.onTouchEvent(event);
        this.mScaleDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 在触发Move事件后，判断水平滑动或者长按，请求将手势给自己处理
     */
    private void requestTouchEvent() {
        if (SwipeUtils.isHorizontalScroll(mLastClickX, mLastClickY, mMoveX, mMoveY)) {
            isTriggerDragChart = true;
        }
        getParent().requestDisallowInterceptTouchEvent(
                isTriggerDragChart || isLongPress
        );
    }

    protected abstract void onTouchDown();

    protected abstract void onTouchMove();

    protected abstract void onTouchUp();


    abstract public int getMaxItemScrollX();

    /**
     * 第一根k柱可见
     */
    abstract public void onFirstItemVisible();

    /**
     * 滑到了最左边
     */
    abstract public void onLeftSide();

    /**
     * 滑到了最右边
     */
    abstract public void onRightSide();

    /**
     * 是否在触摸中
     *
     * @return
     */
    public boolean isTouch() {
        return touch;
    }

    /**
     * 获取位移的最小值
     *
     * @return
     */
    public abstract int getMinScrollX();

    /**
     * 判断数据是否填充满屏幕
     *
     * @return
     */
    public abstract boolean isFullScreen();

    /**
     * 判断是否有数据
     *
     * @return
     */
    public abstract boolean isHasData();

    /**
     * 获取位移的最大值
     *
     * @return
     */
    public abstract int getMaxScrollX();

    /**
     * 设置ScrollX
     *
     * @param scrollX
     */
    public void setScrollX(int scrollX) {
        this.mScrollX = scrollX;
        scrollTo(scrollX, 0);
    }

    /**
     * 是否是多指触控
     *
     * @return
     */
    public boolean isMultipleTouch() {
        return mMultipleTouch;
    }

    protected void checkAndFixScrollX() {
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX();
            mScroller.forceFinished(true);
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX();
            mScroller.forceFinished(true);
        }
    }

    public float getScaleXMax() {
        return mScaleXMax;
    }

    public float getScaleXMin() {
        return mScaleXMin;
    }

    public boolean isScrollEnable() {
        return mScrollEnable;
    }

    public boolean isScaleEnable() {
        return mScaleEnable;
    }

    /**
     * 设置缩放的最大值
     */
    public void setScaleXMax(float scaleXMax) {
        mScaleXMax = scaleXMax;
    }

    /**
     * 设置缩放的最小值
     */
    public void setScaleXMin(float scaleXMin) {
        mScaleXMin = scaleXMin;
    }

    /**
     * 设置是否可以滑动
     */
    public void setScrollEnable(boolean scrollEnable) {
        mScrollEnable = scrollEnable;
    }

    /**
     * 设置是否可以缩放
     */
    public void setScaleEnable(boolean scaleEnable) {
        mScaleEnable = scaleEnable;
    }

    @Override
    public float getScaleX() {
        return mScaleX;
    }

    public boolean isEditDrawBean() {
        return isEditDrawBean;
    }

    public void setEditDrawBean(boolean editDrawBean) {
        isEditDrawBean = editDrawBean;
    }

    public float getLastClickX() {
        return mLastClickX;
    }

    public float getLastClickY() {
        return mLastClickY;
    }

    public float getMoveX() {
        return mMoveX;
    }

    public float getMoveY() {
        return mMoveY;
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    public float getOffsetY() {
        return mOffsetY;
    }
}
