package com.github.fujianlian.klinechart;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GestureDetectorCompat;

import com.github.fujianlian.klinechart.base.IAdapter;
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.draw.MainDraw;
import com.github.fujianlian.klinechart.draw.VolumeDraw;
import com.github.fujianlian.klinechart.entity.DrawingPoint;
import com.github.fujianlian.klinechart.entity.HistorySignEntity;
import com.github.fujianlian.klinechart.entity.ICandle;
import com.github.fujianlian.klinechart.entity.IKLine;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.KLineOrderEntity;
import com.github.fujianlian.klinechart.entity.LocalValues;
import com.github.fujianlian.klinechart.entity.TimeQuantum;
import com.github.fujianlian.klinechart.formatter.BigValueFormatter;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.DateUtil;
import com.github.fujianlian.klinechart.utils.DoubleEvaluator;
import com.github.fujianlian.klinechart.utils.ThemeUtils;
import com.github.fujianlian.klinechart.utils.ViewUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * k线图
 * Created by tian on 2016/5/3.
 */
public abstract class BaseKLineChartView extends ScrollAndScaleView {
    private static final String TAG = "BaseKLineChartView";
    private static final float ORDER_MIN_Y_NA = -1F;
    private static final int BOTTOM_OFFSET = -1;
    private static final int ITEM_MIN_VISIBLE_COUNT = 5;
    private static final int MAX_INTERVAL_POSITION = 1000;
    private static final int PRICE_ANIMATION_DEFAULT_DURATION = 200;
    private static final int ANIMATION_DEFAULT_DURATION = 500;
    private static final float DEFAULT_FLOAT_VALUE = -1f;
    private static final float WHITE_POINT_RADIUS = 2.5f;//dp
    private static final int PRICE_CHANGE_COMBO_THRESHOLD = 10;
    public static final int PRICE_CHANGE_INCREASE = 1;
    public static final int PRICE_CHANGE_FAIR = 0;
    public static final int PRICE_CHANGE_REDUCE = -1;
    private int mainBgStartColor, mainBgEndColor, volBgStartColor, volBgEndColor, volForgetStartColor, volForgetEndColor;

    private float mTranslateX = Float.MIN_VALUE;
    private float mTranslateY = Float.MIN_VALUE;
    private int mWidth = 0, mHeight = 0;

    private int mTopPadding;

    private int mChildPadding;

    private int mBottomPadding;

    private float mMainScaleY = 1;

    private float mDataLen = 0;

    private float mMainMaxValue = Float.MAX_VALUE;

    private float mMainMinValue = Float.MIN_VALUE;

    private double mMainHighMaxValue = 0;

    private double mMainLowMinValue = 0;

    private int mMainMaxIndex = 0;

    private int mMainMinIndex = 0;

    private int mStartIndex = 0;

    private int mStopIndex = 0;

    private float mPointWidth = 6;

    private int mGridRows = 4;

    private int mGridColumns = 3;

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mTextSelectLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mLastPriceTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mMaxMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectedXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectedYLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectLocationPointFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectLocationPointFrame2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectorFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mLastPricePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mLastPriceCenterSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLastPriceCenterStockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 合约交易标识
    private Paint mOrderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mOrderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int upColor;
    private int downColor;
    private int backgroundColor1;
    private int mainTextColor;
    private int historyOrderTextColor, liqOrderSignBgColor;

    private int priceChangeComboCount = 0;

    private int lastChangeDirection = PRICE_CHANGE_FAIR;

    private int timeLineColor = Color.parseColor("#FF8A96A4");

    private int[] timeLineAreaShadeColors = {
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            timeLineColor,
            Color.TRANSPARENT,
    };
    private float[] timeLineAreaShadePoints = {
            0f, 0.1f, 0.3f, 1f
    };

    private float mSubChartWidget = 0.15F;

    private int mSelectedIndex;

    private IChartDraw mMainDraw;
    private MainDraw mainDraw;

    //子图的draw
    public Map<String, IChartDraw> mSubChartDrawMap = new LinkedHashMap<>();

    //子图的显示区域
    private Map<String, Rect> mSubChartRectMap = new LinkedHashMap<>();

    //要显示的子图列表
    private List<String> mSubChartNames = new ArrayList<>();

    //minValue
    private Map<String, Float> mMinValueMap = new LinkedHashMap<>();

    //maxValue
    private Map<String, Float> mMaxValueMap = new LinkedHashMap<>();

    private Map<String, Float> mScaleYMap = new LinkedHashMap<>();

    private IAdapter mAdapter;

    // 是否在K线中显示相关的合约交易标识
    private boolean isShowOpenOrder = false;
    private boolean isShowPositionOrder = false;
    private boolean isShowHistoryOrder = false;
    private boolean isShowLiquidationOrder = false;
    private boolean isShowStopLimitOrder = false;
    private boolean isShowPlanOrder = false;

    private int mBitWidth, mBitHeight;
    private IValueFormatter mValueFormatter;
    private BigValueFormatter mBigValueFormatter;
    private ValueAnimator mAnimator;

    // 管理订单数据
    private KLineOrderDataHelper orderDataHelper;

    private long mAnimationDuration = 0;

    private float mOverScrollRange = 0;

    private OnSelectedChangedListener mOnSelectedChangedListener = null;

    private OnOpenFullScreenListener mOnOpenFullScreenListener = null;

    private Rect mMainRect;
    private Rect mSrcRect, mDestRect;
    private float mLineWidth;

    private Bitmap mBitmap;
    private Bitmap mBitmapLastPriceArrow;
    private Paint mBitPaint;
    private int mid;
    private float midValue;

    // 记录买、卖历史订单数据，用于信息栏中历史订单文字绘制
    private HistorySignEntity buyOrderInfo;
    private HistorySignEntity sellOrderInfo;
    private HistorySignEntity liqOrderInfo;

    /**
     * 定义一个处理价格变动的属性动画
     */
    private ValueAnimator closePriceChangeAnimation;
    /**
     * 当前的收盘价(作为动画的起点，最新数据的收盘价作为终点)
     */
    private double currentClosePrice = DEFAULT_FLOAT_VALUE;
    /**
     * 动画中的价格，用来UI绘制
     */
    private double animationClosePrice = DEFAULT_FLOAT_VALUE;

    private ObjectAnimator haloAnimation;

    private float haloRadiusRatio;
    private RectF lastPriceRectF;
    private Typeface customTypeface;
    int displayHeight = 0;

    /**
     * 主图顶部ma ema bull等文字间距
     */
    private float mTopMaOrBullSpaceValue;

    private CustomDrawChart mCustomDrawChart;

    //背景绘制Rect
    private final Rect mBgDrawRect = new Rect();

    //k线的时间周期
    private long mTimeInterval = 1;
    private long mOriginTimeInterval;
    private String mInterval;

    private final RectF mSelectorYRectF = new RectF();

    //是否显示k线倒计时
    private boolean isShowCountDownTimer;
    private RectF mLastPriceBgRectF;
    private CountdownTask mCountdownTask;
    private long mCountdownMills;

    //指标线画笔
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //分时线path
    private Path mTimeLinePath = new Path();

    private boolean isFutures;
    private boolean isProKLine = true;
    private float mLastFocusY;
    private HashMap<String, RectF> mOrderSignRectF;
    private HashMap<String, Float> mOrderSignDimens;
    private boolean isShowOrderSign = true;
    private float mOrderSignMinX, mOrderSignMinY = ORDER_MIN_Y_NA;
    private int mOrdersBgColor, mOrdersStrokeColor, mOrdersTextColor;
    private int mLastPriceBgColor, mLastPriceStrokeColor, mLastPriceTextColor;

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyChanged();
            startCountdownTask();
        }

        @Override
        public void onInvalidated() {
            notifyChanged();
        }
    };
    //显示/隐藏订单按钮
    private Bitmap mOrderSignHideBitmap, mOrderSignShowBitmap;

    //可复用的通用图像Rect，使用getBitmapRect()方法获取
    private Rect mBitmapRect;
    private int mKLineOrientation = KLineOrientation.VERTICAL;

    public BaseKLineChartView(Context context) {
        super(context);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        initBitmap();
        initBitmapLastPriceArrow();
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mChildPadding = (int) getResources().getDimension(R.dimen.child_top_padding);
        mBottomPadding = (int) getResources().getDimension(R.dimen.chart_bottom_padding);

        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        haloAnimation = ObjectAnimator.ofFloat(this, "haloRadius", 0.5f, 3f);
        haloAnimation.setDuration(500);
        haloAnimation.setRepeatCount(ValueAnimator.INFINITE);
        haloAnimation.setRepeatMode(ValueAnimator.REVERSE);
        haloAnimation.start();

        mSelectorFramePaint.setStrokeWidth(dip2px(0.6f));
        mSelectorFramePaint.setStyle(Paint.Style.STROKE);
        mSelectorFramePaint.setColor(ThemeUtils.getThemeColor(getContext(), R.attr.chart_selector_price_bg_color));

        mLastPriceCenterStockPaint.setColor(ThemeUtils.getThemeColor(getContext(), R.attr.primary));
        mLastPriceCenterStockPaint.setStrokeWidth(dip2px(1));
        mLastPriceCenterStockPaint.setStyle(Paint.Style.STROKE);

        mLastPriceCenterSolidPaint.setColor(ThemeUtils.getThemeColor(getContext(), R.attr.chart_main_background_start_color));
        mLastPriceCenterSolidPaint.setStyle(Paint.Style.FILL);

        mSelectLocationPointFramePaint.setColor(ThemeUtils.getThemeColor(getContext(), R.attr.chart_location_fill_color));
        mSelectLocationPointFramePaint.setStyle(Paint.Style.FILL);

        mSelectLocationPointFrame2Paint.setColor(ThemeUtils.getThemeColor(getContext(), R.attr.chart_location_stroke_color));
        mSelectLocationPointFrame2Paint.setStrokeWidth(dip2px(1));
        mSelectLocationPointFrame2Paint.setStyle(Paint.Style.FILL);

        mLinePaint.setStyle(Paint.Style.STROKE);

        float dashWidth = dip2px(1);
        float dashGap = dip2px(1);

        //选中的横线
        mSelectedXLinePaint.setAntiAlias(true);
        mSelectedXLinePaint.setStyle(Paint.Style.STROKE);
        mSelectedXLinePaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));

        //选中的竖线
        mSelectedYLinePaint.setAntiAlias(true);
        mSelectedYLinePaint.setStyle(Paint.Style.STROKE);
        mSelectedYLinePaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));

        mTopMaOrBullSpaceValue = dip2px(KChartConstant.DEFAULT_TOP_MA_OR_BULL_SPACE_DP);
        mCustomDrawChart = new CustomDrawChart(this);
    }

    private void initOrderSignDimens() {
        float halfHeight = getResources().getDimension(R.dimen.dp_14) / 2;
        float paddingH = getResources().getDimension(R.dimen.dp_4);
        float corner = getResources().getDimension(R.dimen.dp_2);
        float strokeWidth = getResources().getDimension(R.dimen.dp_0_5);
        float marginH = getResources().getDimension(R.dimen.dp_12);
        float dashWidth = getResources().getDimension(R.dimen.dp_2);
        float dashGap = getResources().getDimension(R.dimen.dp_2);
        float textSize = getResources().getDimension(R.dimen.text_minimum);
        float showHideButtonSize = getResources().getDimension(R.dimen.dp_16);
        float showHideMarginEnd = getResources().getDimension(R.dimen.dp_4);
        float showHideMarginTop = getResources().getDimension(R.dimen.dp_80);
        float ordersHeight = getResources().getDimension(R.dimen.dp_15);

        //横屏下，间距为3倍
        if (KLineOrientation.HORIZONTAL == mKLineOrientation) {
            marginH = marginH * 3;
        }

        mOrderSignDimens.put(OrderSignDimens.HALF_HEIGHT, halfHeight);
        mOrderSignDimens.put(OrderSignDimens.PADDING_H, paddingH);
        mOrderSignDimens.put(OrderSignDimens.CORNER, corner);
        mOrderSignDimens.put(OrderSignDimens.STROKE_WIDTH, strokeWidth);
        mOrderSignDimens.put(OrderSignDimens.MARGIN_H, marginH);
        mOrderSignDimens.put(OrderSignDimens.DASH_WIDTH, dashWidth);
        mOrderSignDimens.put(OrderSignDimens.DASH_GAP, dashGap);
        mOrderSignDimens.put(OrderSignDimens.TEXT_SIZE, textSize);
        mOrderSignDimens.put(OrderSignDimens.SHOW_HIDE_BUTTON_SIZE, showHideButtonSize);
        mOrderSignDimens.put(OrderSignDimens.SHOW_HIDE_BUTTON_MARGIN_END, showHideMarginEnd);
        mOrderSignDimens.put(OrderSignDimens.SHOW_HIDE_BUTTON_MARGIN_TOP, showHideMarginTop);
        mOrderSignDimens.put(OrderSignDimens.ORDERS_HEIGHT, ordersHeight);
    }

    public Typeface getCustomTypeface() {
        return customTypeface;
    }

    protected void initTypeface(@NonNull Context context) {
        // Try resolving fontFamily as a font resource.
        if (!context.isRestricted()) {
//            customTypeface = FontManager.getInstance(context).getRegularTypeFace();
        }

        if (customTypeface == null) {
            return;
        }

        mTextPaint.setTypeface(customTypeface);
        mLastPriceTextPaint.setTypeface(customTypeface);
        mTextSelectLightPaint.setTypeface(customTypeface);
        mMaxMinPaint.setTypeface(customTypeface);
        mOrderPaint.setTypeface(customTypeface);
        mOrderTextPaint.setTypeface(customTypeface);
        mCustomDrawChart.setTypeface(customTypeface);
    }

    /**
     * 供光晕放大缩小动画使用
     *
     * @param radiusRatio
     */
    public void setHaloRadius(float radiusRatio) {
        haloRadiusRatio = radiusRatio;
        postInvalidate();
    }

    public void setOrderDataHelper(KLineOrderDataHelper helper) {
        orderDataHelper = helper;
    }

    private void initBitmap() {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        mBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.app_logo_title_1);
        mBitWidth = mBitmap.getWidth();
        mBitHeight = mBitmap.getHeight();
    }

    private void initBitmapLastPriceArrow() {
        mBitmapLastPriceArrow = getBitmapFromVectorDrawable(getContext(), R.drawable.app_logo_title_1);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public int dip2px(float dpValue) {
        return ViewUtil.dp2px(getContext(), dpValue);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        displayHeight = h - mTopPadding - mBottomPadding;
        initRect();
        setTranslateXFromScrollX(mScrollX);
    }

    /**
     * 重置k线距离右边的宽度
     */
    public void resetDefaultScrollX() {
        setScrollX((int) -mOverScrollRange);
        isSelectedPoint = false;
    }

    /**
     * 获取最后一根k线距离右边的默认宽度
     *
     * @return 一个网格的段度+2根k线的宽度
     */
    private float getDefaultScrollRange() {
        float kLineWidth = mainDraw.getCandleWidth() * mScaleX;
        //列宽度
        int mColumnSpace = mWidth / mGridColumns;
        //右边移动最大设置一个列宽度
        return mColumnSpace + kLineWidth * 2;
    }

    /**
     * 更新最后一根k线距离右边的最大宽度
     */
    public void updateOverScrollRange() {
        float scrollX = calculateWidth(formatValueD(currentClosePrice)) + dip2px(50);
        setOverScrollRange(scrollX);
    }

    private int getSubChartCount() {
        return mSubChartNames.size();
    }

    private void initRect() {
        if (displayHeight == 0) {
            return;
        }

        if (getSubChartCount() == 0) {
            mMainRect = new Rect(0, mTopPadding, mWidth, displayHeight + mTopPadding);
        } else {
            float childWidget = mSubChartWidget * getSubChartCount();
            if (childWidget > 0.6F) {
                childWidget = 0.6F;
            }
            float mainRectWidget = 1.0F - childWidget;

            //主图高度
            int mainHeight = (int) (displayHeight * mainRectWidget);

            //单个子图的高度
            int childHeight = (int) (displayHeight * childWidget) / getSubChartCount();

            //主图的范围
            mMainRect = new Rect(0, mTopPadding, mWidth, mTopPadding + mainHeight);

            //子图的范围
            int top = mMainRect.bottom + mChildPadding;
            int bottom = mMainRect.bottom + childHeight;
            Rect rect = null;
            for (int i = 0; i < getSubChartCount(); i++) {
                String name = mSubChartNames.get(i);
                if (i == 0) {
                    rect = new Rect(0, top, mWidth, bottom);
                } else {
                    rect = new Rect(0, rect.bottom + mChildPadding,
                            mWidth, rect.bottom + childHeight);
                }
                mSubChartRectMap.put(name, rect);
            }
        }

        //设置绘制事件响应区域
        mCustomDrawChart.setMainRegion(0, 0, mWidth, mHeight);

        //设置绘制区域
        mCustomDrawChart.setMainDrawRectF(0, 0, mWidth, mMainRect.bottom);
    }

    /**
     * 设置副图的权重
     *
     * @param subChartWidget
     */
    public void setSubChartWidget(float subChartWidget) {
        this.mSubChartWidget = subChartWidget;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas);
    }

    public void drawChart(Canvas canvas) {
        drawMainBackground(canvas);
        if (mWidth == 0 || mMainRect.height() == 0 || getItemCount() == 0) {
            drawChildChartBackground(canvas);
            drawLogo(canvas);
            return;
        }
        resetValue();
        calculateValue();
        canvas.save();
        canvas.scale(1, 1);

        List<Integer> gridXIndexList = getGridXIndexList();
        float[] gridXValues = new float[gridXIndexList.size()];
        for (int i = 0; i < gridXIndexList.size(); i++) {
            Integer index = gridXIndexList.get(i);
            gridXValues[i] = translateXtoX(getX(index));
        }

        //主图
        drawGird(canvas, gridXValues);
        drawMainGridText(canvas);
        drawLogo(canvas);
        drawKChart(canvas);
        drawMainTimeLine(canvas);
        drawMainIndicatorLine(canvas);
        drawCustomBeans(canvas);
        drawContractHistoryOrder(canvas);

        //订单
        if (isFutures) {
            if (isShowOrderSign) {
                drawContractOpenOrder(canvas);
                drawContractStopLimitOrder(canvas);
                drawContractPlanOrder(canvas);
            }
            drawContractOrderShowHideButton(canvas);
            drawContractPositionOrder(canvas);
        } else {
            drawContractOpenOrder(canvas);
            drawContractPositionOrder(canvas);
            drawContractStopLimitOrder(canvas);
            drawContractPlanOrder(canvas);
        }

        //子图
        drawChildChartBackground(canvas);
        drawChildGrid(canvas, gridXValues);
        drawGridXTimeText(canvas, gridXIndexList, gridXValues);
        drawChildKChart(canvas);
        drawChildIndicatorLine(canvas);
        drawChildForegroundColor(canvas);
        drawGridChildText(canvas);


        drawSelectorLine(canvas);
        drawMaxAndMin(canvas);
        drawPriceLine(canvas);
        drawSelectorValueXY(canvas);
        drawValue(canvas, isShowSelector() ? mSelectedIndex : getLastItemIndex());
        canvas.restore();
    }

    private void resetValue() {
        //重置订单标记最小y轴
        mOrderSignMinY = ORDER_MIN_Y_NA;

        //重置显示/隐藏订单按钮
        RectF orderSignRectF = getOrderSignRectF(OrderSignRectFName.SHOW_HIDE_BUTTON_RECT_F);
        orderSignRectF.set(0, 0, 0, 0);
    }

    private void drawCustomBeans(Canvas canvas) {
        mCustomDrawChart.drawCustomBeans(canvas);
    }

    private void drawChildForegroundColor(Canvas canvas) {
        for (int n = 0; n < mSubChartNames.size(); n++) {
            String name = mSubChartNames.get(n);
            Rect rect = mSubChartRectMap.get(name);
            if (rect != null) {
                mBgDrawRect.set(rect.left, rect.top - mChildPadding, rect.right, rect.bottom);
                drawBackgroundRect(canvas, mBgDrawRect, volForgetStartColor, volForgetEndColor);
            }
        }
    }

    private void drawMainBackground(Canvas canvas) {
        mBgDrawRect.set(mMainRect.left, 0, mMainRect.right, mMainRect.bottom);
        drawBackgroundRect(canvas, mBgDrawRect, mainBgStartColor, mainBgEndColor);
    }

    private void drawChildChartBackground(Canvas canvas) {
        for (int i = 0; i < getSubChartCount(); i++) {
            String name = mSubChartNames.get(i);
            Rect rect = mSubChartRectMap.get(name);
            if (rect != null) {
                mBgDrawRect.set(rect.left, rect.top - mChildPadding, rect.right, rect.bottom);
                drawBackgroundRect(canvas, mBgDrawRect, volBgStartColor, volBgEndColor);
            }
        }

        //绘制底部日期背景
        int top;
        Rect lastRect = getLastRect();
        if (lastRect != null) {
            top = lastRect.bottom;
        } else {
            top = mMainRect.bottom;
        }
        mBgDrawRect.set(0, top, mWidth, top + mBottomPadding + dip2px(1));
        drawBackgroundRect(canvas, mBgDrawRect, volBgStartColor, volBgEndColor);
    }

    private void drawBackgroundRect(Canvas canvas, Rect rect, int startColor, int endColor) {
        LinearGradient gradient = new LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                startColor,
                endColor,
                Shader.TileMode.CLAMP);
        mBackgroundPaint.setShader(gradient);
        canvas.drawRect(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                mBackgroundPaint);
    }

    public float getMainY(float value) {
        float y = (mMainMaxValue - value) * mMainScaleY + mMainRect.top;
        if (y > mMainRect.bottom) {
            return mMainRect.bottom;
        }
        if (y < mMainRect.top) {
            return mMainRect.top;
        }
        return y;
    }

    public float getMainYToPrice(float value) {
        return mMainMaxValue - Math.min(value - mMainRect.top, mMainRect.height()) / mMainScaleY;
    }

    public float getDrawMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mMainRect.top;
    }

    public float getDrawMainYToPrice(float value) {
        return mMainMaxValue - (value - mMainRect.top) / mMainScaleY;
    }

    public float getMainBottom() {
        return mMainRect.bottom;
    }

    public float getVolY(float value) {
        String name = KChartConstant.Sub.VOL;
        Rect rect = mSubChartRectMap.get(KChartConstant.Sub.VOL);
        if (rect == null) {
            return 0;
        }
        float v = mMaxValueMap.get(name) - value;
        if (v <= 0) {
            return rect.top + 1;
        }
        return borderCheck(v * mScaleYMap.get(name) + rect.top, rect.bottom);
    }

    /**
     * Y轴值的极限值检测
     * 用于解决v的值和VolRect的高度一致时，会导致柱子显示空
     *
     * @param v
     * @param bottom
     * @return
     */
    private float borderCheck(float v, int bottom) {
        return v >= bottom ? bottom - 1 : v;
    }

    public float getChildY(String name, float value) {
        //设置一个底部偏移量，避免最大值刚好绘制在底部
        float top = mSubChartRectMap.get(name).top + dp2px(BOTTOM_OFFSET);
        return (mMaxValueMap.get(name) - value) * mScaleYMap.get(name)
                + top;
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY(float y) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        return y + fontMetrics.descent - fontMetrics.ascent;
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY1(float y) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    /**
     * 画logo
     *
     * @param canvas
     */
    private void drawLogo(Canvas canvas) {
        int left = mMainRect.right / 2 - mBitWidth / 2;
        // 计算上边位置
        int top = mMainRect.bottom / 2 - mBitHeight / 2;
        mSrcRect = new Rect(0, 0, mBitWidth, mBitHeight);
        mDestRect = new Rect(left, top, left + mBitWidth, top + mBitHeight);
        mBitPaint.setAlpha(255);
        canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, mBitPaint);
    }


    /**
     * 画表格
     *
     * @param canvas
     */
    private void drawGird(Canvas canvas, float[] gridXValues) {
        //主图横向的grid
        int rowSpace = mMainRect.height() / mGridRows;
        for (int i = 0; i <= mGridRows; i++) {
            float y = rowSpace * i + mMainRect.top;
            canvas.drawLine(0, y, mWidth, y, mGridPaint);
        }

        //纵向的grid
        float y = mMainRect.bottom;
        for (float x : gridXValues) {
            canvas.drawLine(x, 0, x, y, mGridPaint);
        }
    }

    private void drawChildGrid(Canvas canvas, float[] gridXValues) {
        //绘制子图的横向分割线
        for (int n = 0; n < mSubChartNames.size(); n++) {
            String name = mSubChartNames.get(n);
            Rect rect = mSubChartRectMap.get(name);
            canvas.drawLine(0, rect.bottom, mWidth, rect.bottom, mGridPaint);
        }

        //纵向的grid
        float y = mMainRect.bottom;
        Rect lastRect = getLastRect();
        if (lastRect != null) {
            y = lastRect.bottom;
        }
        for (float x : gridXValues) {
            canvas.drawLine(x, mMainRect.bottom, x, y, mGridPaint);
        }
    }

    /**
     * 通过垂直分割宽度，获取需要显示垂直分割线的k柱索引集合
     *
     * @return 垂直分割线k柱子索引
     */
    private List<Integer> getGridXIndexList() {
        int columnSpace = mWidth / mGridColumns;
        int candleQuantity = Math.round(columnSpace / (mPointWidth * mScaleX));
        List<Integer> indexList = new ArrayList<>();

        Log.d(TAG, "drawVerticalGridLine, columnSpace=" + columnSpace
                + ", candleQuantity=" + candleQuantity
                + ", pointWidth=" + (mPointWidth * mScaleX)
        );

        for (int i = 0; i < mStopIndex; i += candleQuantity) {
            if (i >= mStartIndex) {
                indexList.add(i);
            }
        }
        return indexList;
    }

    /**
     * 画k线图
     *
     * @param canvas
     */
    private void drawKChart(Canvas canvas) {
        //保存之前的平移，缩放

        canvas.save();
        canvas.translate(mTranslateX * mScaleX, 0);
        canvas.scale(mScaleX, 1);
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            Object currentPoint = getItem(i);
            float currentPointX = getX(i);
            Object lastPoint;
            float lastX;
            if (i == 0) {
                lastPoint = currentPoint;
                lastX = currentPointX;
            } else {
                lastPoint = getItem(i - 1);
                lastX = getX(i - 1);
            }
            boolean isNewest = i == (mAdapter.getCount() - 1);

            if (mMainDraw != null) {
                mMainDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i, isNewest, (float) animationClosePrice);
            }

        }

        //还原 平移缩放
        canvas.restore();
    }



    private void drawChildKChart(Canvas canvas) {
        //保存之前的平移，缩放
        canvas.save();
        canvas.translate(mTranslateX * mScaleX, 0);
        canvas.scale(mScaleX, 1);
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            Object currentPoint = getItem(i);
            float currentPointX = getX(i);
            Object lastPoint;
            float lastX;
            if (i == 0) {
                lastPoint = currentPoint;
                lastX = currentPointX;
            } else {
                lastPoint = getItem(i - 1);
                lastX = getX(i - 1);
            }
            boolean isNewest = i == (mAdapter.getCount() - 1);
            for (int n = 0; n < mSubChartNames.size(); n++) {
                String name = mSubChartNames.get(n);
                IChartDraw draw = mSubChartDrawMap.get(name);
                draw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i, isNewest, (float) animationClosePrice);
            }
        }
        //还原 平移缩放
        canvas.restore();
    }

    private void drawMainTimeLine(Canvas canvas) {
        mTimeLinePath.reset();
        float timeLineStartX = 0f;
        float timeLineEndX = 0f;
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            Object point = getItem(i);
            float x = translateXtoX(getX(i));
            if (i == mStartIndex) {
                timeLineStartX = x;
            }
            if (i == mStopIndex) {
                timeLineEndX = x;
            }
            boolean isNewest = i == (mAdapter.getCount() - 1);
            if (i == mStartIndex) {
                mTimeLinePath.moveTo(x, getMainY(((ICandle) point).getClosePrice()));
            } else {
                if (isNewest) {
                    mTimeLinePath.lineTo(x, getMainY((float) animationClosePrice));
                } else {
                    mTimeLinePath.lineTo(x, getMainY(((ICandle) point).getClosePrice()));
                }
            }
        }

        // 画分时线
        if (mMainDraw != null) {
            if (priceChangeComboCount >= PRICE_CHANGE_COMBO_THRESHOLD && lastChangeDirection != PRICE_CHANGE_FAIR) {
                if (lastChangeDirection == PRICE_CHANGE_INCREASE) {
                    timeLineColor = ThemeUtils.getThemeColor(getContext(), R.attr.upColor);
                } else {
                    timeLineColor = ThemeUtils.getThemeColor(getContext(), R.attr.downColor);
                }
            } else {
                timeLineColor = ThemeUtils.getThemeColor(getContext(), R.attr.chart_line);
            }
            timeLineAreaShadeColors[2] = timeLineColor;
            mMainDraw.drawTimeLine(this, canvas, mTimeLinePath, timeLineStartX, timeLineEndX);
        }

        // 绘制分时线的呼吸灯
        if (mMainDraw != null && (mStopIndex == mAdapter.getCount() - 1)) {
            mMainDraw.drawBreathingLight(this, canvas, timeLineEndX, (float) animationClosePrice, haloRadiusRatio);
        }
    }

    private void drawMainIndicatorLine(Canvas canvas) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            float x = translateXtoX(getX(i));
            boolean isFirst = i == mStartIndex;
            if (mMainDraw != null) {
                mMainDraw.drawIndicatorLine(getItem(i), canvas, this, x, isFirst, i == mStopIndex);
            }
        }
    }

    private void drawChildIndicatorLine(Canvas canvas) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            float x = translateXtoX(getX(i));
            boolean isFirst = i == mStartIndex;
            for (int n = 0; n < mSubChartNames.size(); n++) {
                String name = mSubChartNames.get(n);
                IChartDraw draw = mSubChartDrawMap.get(name);
                draw.drawIndicatorLine(getItem(i), canvas, this, x, isFirst, i == mStopIndex);
            }
        }
    }

    /**
     * 画合约历史订单
     */
    private void drawContractHistoryOrder(Canvas canvas) {
        if ((!isShowHistoryOrder && !isShowLiquidationOrder) || mainDraw.isLine()) {
            buyOrderInfo = null;
            sellOrderInfo = null;
            liqOrderInfo = null;
            return;
        }
        // 保存之前的平移
        canvas.save();
        canvas.translate(mTranslateX * mScaleX, 0);
        mOrderTextPaint.setTextSize(getResources().getDimension(R.dimen.text_minimum));
        mOrderTextPaint.setColor(historyOrderTextColor);
        mOrderPaint.setStrokeWidth(0);
        mOrderPaint.setStyle(Paint.Style.FILL);
        mOrderPaint.setAlpha(255);
        boolean isSignClicked = false;
        // 是否正在绘制同一帧，开始绘制前此标记为false
        boolean isSameFrame = false;
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            Object currentPoint = getItem(i);
            float currentPointX = getX(i) * mScaleX;
            ICandle candle = (ICandle) currentPoint;
            long curDate = candle.getLongDate();
            long dateOffset;
            if ((i + 1) <= mStopIndex) {
                long postDate = ((ICandle) getItem(i + 1)).getLongDate();
                dateOffset = postDate - curDate;
            } else if ((i - 1) >= mStartIndex) {
                long preDate = ((ICandle) getItem(i - 1)).getLongDate();
                dateOffset = curDate - preDate;
            } else {
                continue;
            }
            // 根据起始日期获取历史订单数据
            Pair<HistorySignEntity, HistorySignEntity> info = null;
            if (isShowHistoryOrder) {
                info = orderDataHelper.getHistoryInfoByDateRange(
                        curDate,
                        curDate + dateOffset,
                        isSameFrame
                );
            }

            Pair<HistorySignEntity, HistorySignEntity> liqInfo = null;
            if (isShowLiquidationOrder) {
                liqInfo = orderDataHelper.getLiquidationInfoByDateRange(
                        curDate,
                        curDate + dateOffset,
                        isSameFrame
                );
            }

            // 已经开始绘制，for循环后续部分按同一帧处理
            isSameFrame = true;
            HistorySignEntity buyHistory = null;
            HistorySignEntity sellHistory = null;
            if (info != null) {
                buyHistory = info.first;
                sellHistory = info.second;
            }

            HistorySignEntity buyLiq = null;
            HistorySignEntity sellLiq = null;
            if (liqInfo != null) {
                buyLiq = liqInfo.first;
                sellLiq = liqInfo.second;
            }

            drawHistoryOrderSign(
                    canvas,
                    buyHistory,
                    buyLiq,
                    true,
                    currentPointX,
                    getMainY(candle.getLowPrice())
            );

            drawHistoryOrderSign(
                    canvas,
                    sellHistory,
                    sellLiq,
                    false,
                    currentPointX,
                    getMainY(candle.getHighPrice())
            );

            boolean isShowSign = buyHistory != null
                    || sellHistory != null
                    || buyLiq != null
                    || sellLiq != null;

            //强平订单，取最后一笔用于悬浮窗展示，并累计订单数量
            HistorySignEntity liqOrder = null;
            if (buyLiq != null && sellLiq != null) {
                if (buyLiq.getTime() > sellLiq.getTime()) {
                    liqOrder = buyLiq;
                } else {
                    liqOrder = sellLiq;
                }
                liqOrder.setOrderCount(buyLiq.getOrderCount() + sellLiq.getOrderCount());
            } else {
                if (buyLiq != null) {
                    liqOrder = buyLiq;
                } else if (sellLiq != null) {
                    liqOrder = sellLiq;
                }
            }

            // 如果当前选中Y轴存在历史订单并且有发生点击或长按事件，则记录下历史订单信息
            if (isShowSign && isSelectedPoint && i == mSelectedIndex) {
                buyOrderInfo = buyHistory;
                sellOrderInfo = sellHistory;
                liqOrderInfo = liqOrder;
                isSignClicked = true;
            }
        }
        // 如果所有的历史订单标识都没有被点击，则清空相关记录
        if (!isSignClicked) {
            buyOrderInfo = null;
            sellOrderInfo = null;
            liqOrderInfo = null;
        }
        //还原平移
        canvas.restore();
    }

    private void drawHistoryOrderSign(
            Canvas canvas,
            @Nullable HistorySignEntity historySign,
            @Nullable HistorySignEntity liqSign,
            boolean isBuy,
            float x,
            float y
    ) {
        float signSizeOffset;
        int marginOffset;
        if (isBuy) {
            signSizeOffset = dp2px(14);
            marginOffset = dip2px(4);
        } else {
            signSizeOffset = -dp2px(14);
            marginOffset = -dip2px(4);
        }

        float firstSignY = y + marginOffset;
        float secondSignY = firstSignY + signSizeOffset + marginOffset;

        float historyY;
        float liqY;

        //历史订单和强平订单都存在，需要按照时间顺序绘制，时间越小，离k线蜡烛图越近
        if (historySign != null && liqSign != null) {
            if (historySign.getTime() > liqSign.getTime()) {
                historyY = secondSignY;
                liqY = firstSignY;
            } else {
                historyY = firstSignY;
                liqY = secondSignY;
            }
        } else {
            historyY = firstSignY;
            liqY = firstSignY;
        }

        //绘制历史订单标识
        float radius = signSizeOffset / 2;
        if (historySign != null) {
            mOrderPaint.setColor(isBuy ? upColor : downColor);
            drawHistoryOrderSignLabel(canvas, x, historyY, radius, historySign.getLabel());
        }

        //绘制强平订单标识
        if (liqSign != null) {
            mOrderPaint.setColor(liqOrderSignBgColor);
            drawHistoryOrderSignLabel(canvas, x, liqY, radius, liqSign.getLabel());
        }
    }

    private void drawHistoryOrderSignLabel(
            Canvas canvas,
            float x,
            float y,
            float radius,
            String label
    ) {
        float cy = y + radius;
        canvas.drawCircle(x, cy, Math.abs(radius), mOrderPaint);
        float labelWidth = mOrderTextPaint.measureText(label);
        canvas.drawText(
                label,
                x - labelWidth / 2,
                ViewUtil.getTextBaseLine(mOrderTextPaint, cy),
                mOrderTextPaint
        );
    }

    /**
     * 画合约当前委托
     */
    private void drawContractOpenOrder(Canvas canvas) {
        if (!isShowOpenOrder || mainDraw.isLine() || orderDataHelper == null) {
            return;
        }
        List<KLineOrderEntity> list = orderDataHelper.getOpenOrderData();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (KLineOrderEntity entity : list) {
            try {
                float tempPrice = entity.getPriceFloat();
                if (tempPrice < mMainMinValue || tempPrice > mMainMaxValue) {
                    continue;
                }
                drawContractOrderSign(canvas, entity, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 画合约当前持仓
     */
    private void drawContractPositionOrder(Canvas canvas) {
        if (!isShowPositionOrder || mainDraw.isLine() || orderDataHelper == null) {
            return;
        }
        List<KLineOrderEntity> list = orderDataHelper.getPositionOrderData();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (KLineOrderEntity entity : list) {
            try {
                float tempPrice = entity.getPriceFloat();
                if (tempPrice < mMainMinValue || tempPrice > mMainMaxValue) {
                    continue;
                }
                drawContractOrderSignPNL(canvas, entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 画合约止盈止损
     */
    private void drawContractStopLimitOrder(Canvas canvas) {
        if (!isShowStopLimitOrder || mainDraw.isLine() || orderDataHelper == null) {
            return;
        }
        List<KLineOrderEntity> list = orderDataHelper.getStopLimitOrderData();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (KLineOrderEntity entity : list) {
            try {
                float price1 = entity.getHighPriceFloat();
                if (price1 >= mMainMinValue && price1 <= mMainMaxValue) {
                    drawContractOrderSign(canvas, entity, true);
                }
                float price2 = entity.getLowPriceFloat();
                if (price2 >= mMainMinValue && price2 <= mMainMaxValue) {
                    drawContractOrderSign(canvas, entity, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 画合约计划单
     */
    private void drawContractPlanOrder(Canvas canvas) {
        if (!isShowPlanOrder || mainDraw.isLine() || orderDataHelper == null) {
            return;
        }
        List<KLineOrderEntity> list = orderDataHelper.getPlanOrderData();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (KLineOrderEntity entity : list) {
            try {
                float tempPrice = entity.getPriceFloat();
                if (tempPrice < mMainMinValue || tempPrice > mMainMaxValue) {
                    continue;
                }
                drawContractOrderSign(canvas, entity, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void drawContractOrderSign(@NonNull Canvas canvas, @NonNull KLineOrderEntity entity, boolean isHigh) {
        float halfHeight = getOrderSignDimens(OrderSignDimens.HALF_HEIGHT);
        float paddingH = getOrderSignDimens(OrderSignDimens.PADDING_H);
        float corner = getOrderSignDimens(OrderSignDimens.CORNER);
        float strokeWidth = getOrderSignDimens(OrderSignDimens.STROKE_WIDTH);
        float marginH = getOrderSignDimens(OrderSignDimens.MARGIN_H);
        float dashWidth = getOrderSignDimens(OrderSignDimens.DASH_WIDTH);
        float dashGap = getOrderSignDimens(OrderSignDimens.DASH_GAP);
        float priceFloat;
        String title;
        String textPrice;
        if (entity.getOrderType() == KLineOrderDataHelper.ORDER_TYPE_STOP) {
            priceFloat = isHigh ? entity.getHighPriceFloat() : entity.getLowPriceFloat();

            title = isHigh
                    ? entity.parseLeftHighTitle(formatValueStr(entity.getHighVol(), false))
                    : entity.parseLeftLowTitle(formatValueStr(entity.getLowVol(), false));

            textPrice = isHigh
                    ? formatValueStr(entity.getHighPrice(), true)
                    : formatValueStr(entity.getLowPrice(), true);
        } else {
            priceFloat = entity.getPriceFloat();
            title = entity.parseLeftTitle(formatValueStr(entity.getVol(), false));
            textPrice = formatValueStr(entity.getPrice(), true);
        }
        String leftText = title;
        float baseLineY = getMainY(priceFloat);

        mOrderTextPaint.setTextSize(getOrderSignDimens(OrderSignDimens.TEXT_SIZE));

        float rightRectFWidth = mOrderTextPaint.measureText(textPrice) + paddingH * 2;
        float leftRectFWidth = mOrderTextPaint.measureText(leftText) + paddingH * 2;
        float left = mWidth - rightRectFWidth - marginH - leftRectFWidth;

        //设置背景样式
        mOrderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mOrderPaint.setStrokeWidth(strokeWidth);
        mOrderPaint.setPathEffect(null);
        mOrderPaint.setAlpha(255);
        mOrderPaint.setColor(backgroundColor1);

        //绘制左侧视图背景
        RectF leftRectF = getOrderSignRectF(OrderSignRectFName.LEFT_RECT_F);
        leftRectF.set(
                left,
                baseLineY - halfHeight,
                left + leftRectFWidth,
                baseLineY + halfHeight
        );
        canvas.drawRoundRect(leftRectF, corner, corner, mOrderPaint);

        //绘制右侧视图背景
        float rightOffset = paddingH / 2;
        RectF rightRectF = getOrderSignRectF(OrderSignRectFName.RIGHT_RECT_F);
        rightRectF.set(
                mWidth - rightRectFWidth + rightOffset,
                leftRectF.top,
                mWidth + rightOffset,
                leftRectF.bottom
        );
        canvas.drawRoundRect(rightRectF, corner, corner, mOrderPaint);

        //视图边框样式，根据做多做空取涨跌色
        mOrderPaint.setColor(entity.isLong() ? upColor : downColor);
        mOrderPaint.setStyle(Paint.Style.STROKE);
        mOrderPaint.setStrokeWidth(strokeWidth);

        //绘制左侧视图边框
        canvas.drawRoundRect(leftRectF, corner, corner, mOrderPaint);

        //绘制右侧视图边框
        canvas.drawRoundRect(rightRectF, corner, corner, mOrderPaint);

        //绘制虚线
        mOrderPaint.setStrokeWidth(strokeWidth * 2);
        mOrderPaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));
        canvas.drawLine(leftRectF.right, baseLineY, rightRectF.left, baseLineY, mOrderPaint);

        //设置文字颜色，根据做多做空取涨跌色
        mOrderTextPaint.setColor(entity.isLong() ? upColor : downColor);

        //绘制左侧文字
        canvas.drawText(
                leftText,
                leftRectF.left + paddingH,
                ViewUtil.getTextBaseLine(mOrderTextPaint, leftRectF.centerY()),
                mOrderTextPaint
        );

        //绘制右侧文字
        canvas.drawText(
                textPrice,
                rightRectF.left + paddingH,
                ViewUtil.getTextBaseLine(mOrderTextPaint, leftRectF.centerY()),
                mOrderTextPaint
        );

        //记录最小的委托订单，用于绘制隐藏订单按钮
        if (mOrderSignMinY == ORDER_MIN_Y_NA) {
            mOrderSignMinY = baseLineY;
            mOrderSignMinX = leftRectF.left;
        } else {
            if (baseLineY <= mOrderSignMinY) {
                mOrderSignMinY = baseLineY;
                mOrderSignMinX = leftRectF.left;
            }
        }
    }

    /**
     * 绘制合约当前持仓
     *
     * @param canvas
     * @param entity
     */
    private void drawContractOrderSignPNL(Canvas canvas, @NonNull KLineOrderEntity entity) {
        float halfHeight = getOrderSignDimens(OrderSignDimens.HALF_HEIGHT);
        float paddingH = getOrderSignDimens(OrderSignDimens.PADDING_H);
        float corner = getOrderSignDimens(OrderSignDimens.CORNER);
        float strokeWidth = getOrderSignDimens(OrderSignDimens.STROKE_WIDTH) * 2;
        float dashWidth = getOrderSignDimens(OrderSignDimens.DASH_WIDTH) * 2;
        float dashGap = getOrderSignDimens(OrderSignDimens.DASH_GAP) * 2;

        float priceFloat = entity.getPriceFloat();
        String title = entity.parsePNLTitle(formatValueStr(entity.getProfit(), false));
        String textPrice = formatValueStr(entity.getPrice(), true);
        String textVol = formatValueStr(entity.getVol(), false);
        float baseLineY = getMainY(priceFloat);

        mOrderTextPaint.setTextSize(getOrderSignDimens(OrderSignDimens.TEXT_SIZE));

        float titleWidth = mOrderTextPaint.measureText(title) + paddingH * 2;
        float volWidth = mOrderTextPaint.measureText(textVol) + paddingH * 2;
        float leftRectFWidth = titleWidth + volWidth + strokeWidth;
        float rightRectFWidth = mOrderTextPaint.measureText(textPrice) + paddingH * 2;

        RectF leftRectF = getOrderSignRectF(OrderSignRectFName.PNL_LEFT_RECT_F);
        leftRectF.set(0,
                baseLineY - halfHeight,
                leftRectFWidth,
                baseLineY + halfHeight
        );

        //绘制左侧背景色
        mOrderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mOrderPaint.setStrokeWidth(strokeWidth);
        mOrderPaint.setPathEffect(null);
        mOrderPaint.setColor(backgroundColor1);
        mOrderPaint.setAlpha(216);
        canvas.drawRoundRect(leftRectF, corner, corner, mOrderPaint);

        //边框色根据做多做空取涨跌色
        mOrderPaint.setColor(entity.isLong() ? upColor : downColor);

        //绘制左侧边框
        mOrderPaint.setStyle(Paint.Style.STROKE);
        mOrderPaint.setStrokeWidth(strokeWidth);
        mOrderPaint.setAlpha(51);
        canvas.drawRoundRect(leftRectF, corner, corner, mOrderPaint);

        //绘制完边框后更换画笔样式为FILL_AND_STROKE
        mOrderPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //绘制分割线
        float dividerX = leftRectF.right - volWidth - strokeWidth;
        canvas.drawLine(dividerX, leftRectF.top, dividerX, leftRectF.bottom, mOrderPaint);

        //绘制右侧背景色
        float rightOffset = paddingH / 2F;
        RectF rightRectF = getOrderSignRectF(OrderSignRectFName.PNL_RIGHT_RECT_F);
        rightRectF.set(
                mWidth - rightRectFWidth + rightOffset,
                leftRectF.top,
                mWidth + rightOffset,
                leftRectF.bottom
        );
        canvas.drawRoundRect(rightRectF, corner, corner, mOrderPaint);

        //绘制虚线
        mOrderPaint.setAlpha(255);
        mOrderPaint.setStrokeWidth(strokeWidth);
        mOrderPaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));
        canvas.drawLine(leftRectF.right, baseLineY, rightRectF.left, baseLineY, mOrderPaint);

        //PNL的文字颜色根据收益取涨跌色
        mOrderTextPaint.setColor(entity.getProfitFloat() > 0 ? upColor : downColor);

        //绘制PNL
        canvas.drawText(
                title,
                leftRectF.left + paddingH,
                ViewUtil.getTextBaseLine(mOrderTextPaint, leftRectF.centerY()),
                mOrderTextPaint
        );

        //数量和右侧价格根据做多做空取涨跌色
        mOrderTextPaint.setColor(entity.isLong() ? upColor : downColor);

        //绘制数量
        canvas.drawText(
                textVol,
                leftRectF.right - volWidth + paddingH,
                ViewUtil.getTextBaseLine(mOrderTextPaint, leftRectF.centerY()),
                mOrderTextPaint
        );

        //绘制右侧价格
        canvas.drawText(
                textPrice,
                rightRectF.left + paddingH,
                ViewUtil.getTextBaseLine(mOrderTextPaint, leftRectF.centerY()),
                mOrderTextPaint
        );
    }

    /**
     * 绘制订单显示/隐藏按钮
     *
     * @param canvas
     */
    private void drawContractOrderShowHideButton(Canvas canvas) {
        Bitmap bitmap = getShowHideButtonBitmap();
        float buttonSize = getOrderSignDimens(OrderSignDimens.SHOW_HIDE_BUTTON_SIZE);
        float marginEnd = getOrderSignDimens(OrderSignDimens.SHOW_HIDE_BUTTON_MARGIN_END);
        float marginTop = getOrderSignDimens(OrderSignDimens.SHOW_HIDE_BUTTON_MARGIN_TOP);
        RectF destRectF = getOrderSignRectF(OrderSignRectFName.SHOW_HIDE_BUTTON_RECT_F);
        if (isShowOrderSign) {
            if (mOrderSignMinY == ORDER_MIN_Y_NA) {
                return;
            }
            if (mOrderSignMinY < mMainRect.top || mOrderSignMinY > mMainRect.bottom) {
                return;
            }
            destRectF.set(
                    mOrderSignMinX - buttonSize - marginEnd,
                    mOrderSignMinY - buttonSize / 2,
                    mOrderSignMinX - marginEnd,
                    mOrderSignMinY + buttonSize / 2
            );
        } else {
            float strokeWidth = getOrderSignDimens(OrderSignDimens.STROKE_WIDTH);
            float corner = getOrderSignDimens(OrderSignDimens.CORNER);
            float paddingH = getOrderSignDimens(OrderSignDimens.PADDING_H);
            float ordersHeight = getOrderSignDimens(OrderSignDimens.ORDERS_HEIGHT);

            mOrderTextPaint.setTextSize(getOrderSignDimens(OrderSignDimens.TEXT_SIZE));

            String textOrders = getLocalValues().getText(MarkItemType.ORDERS);
            float textOrdersWidth = mOrderTextPaint.measureText(textOrders) + paddingH * 2;
            float rightOffset = paddingH / 2F;

            RectF ordersRectF = getOrderSignRectF(OrderSignRectFName.ORDERS_RECT_F);
            ordersRectF.set(
                    mWidth - textOrdersWidth + rightOffset,
                    marginTop,
                    mWidth + rightOffset,
                    marginTop + ordersHeight
            );

            destRectF.set(
                    ordersRectF.left - marginEnd - buttonSize,
                    ordersRectF.centerY() - buttonSize / 2,
                    ordersRectF.left - marginEnd,
                    ordersRectF.centerY() + buttonSize / 2
            );

            mOrderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mOrderPaint.setStrokeWidth(strokeWidth);
            mOrderPaint.setPathEffect(null);
            mOrderPaint.setColor(mOrdersBgColor);
            mOrderPaint.setAlpha(255);
            canvas.drawRoundRect(ordersRectF, corner, corner, mOrderPaint);

            mOrderPaint.setStyle(Paint.Style.STROKE);
            mOrderPaint.setColor(mOrdersStrokeColor);
            canvas.drawRoundRect(ordersRectF, corner, corner, mOrderPaint);

            mOrderTextPaint.setColor(mOrdersTextColor);
            canvas.drawText(
                    textOrders,
                    ordersRectF.left + paddingH,
                    ViewUtil.getTextBaseLine(mOrderTextPaint, ordersRectF.centerY()),
                    mOrderTextPaint
            );
        }

        mBitPaint.setAlpha(255);

        Rect srcRect = getBitmapRect();
        srcRect.set(0, 0, (int) buttonSize, (int) buttonSize);
        canvas.drawBitmap(bitmap, srcRect, destRectF, mBitPaint);
    }

    private Bitmap getShowHideButtonBitmap() {
        Bitmap bitmap;
        if (isShowOrderSign) {
            if (mOrderSignHideBitmap == null) {
                mOrderSignHideBitmap = getBitmapFromVectorDrawable(
                        getContext(),
                        R.drawable.app_logo_title_1
                );
            }
            bitmap = mOrderSignHideBitmap;
        } else {
            if (mOrderSignShowBitmap == null) {
                mOrderSignShowBitmap = getBitmapFromVectorDrawable(
                        getContext(),
                        R.drawable.app_logo_title_1
                );
            }
            bitmap = mOrderSignShowBitmap;
        }
        return bitmap;
    }

    @NonNull
    private Rect getBitmapRect() {
        if (mBitmapRect == null) {
            mBitmapRect = new Rect();
        }
        return mBitmapRect;
    }

    @NonNull
    public LocalValues getLocalValues() {
        return ConfigController.getInstance().getLocalValues();
    }

    /**
     * 画选择线
     *
     * @param canvas
     */
    private void drawSelectorLine(Canvas canvas) {
        if (mCustomDrawChart.isOpenDraw()) {
            return;
        }

        //画选择线
        if (isShowSelector()) {
            float x = translateXtoX(getX(mSelectedIndex));
            // k线图横线
            canvas.drawLine(0, mLastFocusY, mWidth, mLastFocusY, mSelectedXLinePaint);

            //绘制选择线
            float top = mMainRect.top;
            float bottom = mMainRect.bottom;
            if (mSubChartNames.size() > 0) {
                String name = mSubChartNames.get(mSubChartNames.size() - 1);
                bottom = mSubChartRectMap.get(name).bottom;
            }
            canvas.drawLine(x, top, x, bottom, mSelectedYLinePaint);
        }
    }

    /**
     * 画渐变线
     *
     * @param paint
     * @param start
     * @param stop
     * @param gradientColors
     */
    private void drawGradient(Paint paint, PointF start, PointF stop, int... gradientColors) {
        LinearGradient linearGradient = new LinearGradient(start.x, start.y, stop.x, stop.y, gradientColors, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
    }

    /**
     * 画渐变线
     *
     * @param paint
     * @param start
     * @param stop
     * @param gradientColors
     */
    private void drawGradient(Paint paint, PointF start, PointF stop, int[] gradientColors, float[] gradientPoints) {
        Log.d(TAG, "drawGradient " +
                "start x=" + start.x + ", start y=" + start.y +
                "stop x=" + stop.x + ", stop x=" + stop.y
        );
        LinearGradient linearGradient = new LinearGradient(start.x, start.y, stop.x, stop.y, gradientColors, gradientPoints, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private float calculateWidth(String text) {
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.width() + 5;
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private Rect calculateMaxMin(String text) {
        Rect rect = new Rect();
        mMaxMinPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    /**
     * 绘制主图网格右边的文字
     *
     * @param canvas
     */
    private void drawMainGridText(Canvas canvas) {
        int margin = dip2px(4);
        int marginDiv = dip2px(2);

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        //--------------画上方k线图的值-------------
        if (mMainDraw != null) {
            canvas.drawText(formatValue(mMainMaxValue), mWidth - calculateWidth(formatValue(mMainMaxValue)) - margin, baseLine + mMainRect.top - textHeight, mTextPaint);
            canvas.drawText(formatValue(mMainMinValue), mWidth - calculateWidth(formatValue(mMainMinValue)) - margin, mMainRect.bottom - textHeight + baseLine - dip2px(1), mTextPaint);
            float rowValue = (mMainMaxValue - mMainMinValue) / mGridRows;
            float rowSpace = mMainRect.height() / mGridRows;
            for (int i = 1; i < mGridRows; i++) {
                String text = formatValue(rowValue * (mGridRows - i) + mMainMinValue);
                canvas.drawText(text, mWidth - calculateWidth(text) - margin, fixTextY(rowSpace * i + mMainRect.top) - textHeight - marginDiv, mTextPaint);
            }
        }
    }

    /**
     * 绘制子图右边的文字
     *
     * @param canvas
     */
    private void drawGridChildText(Canvas canvas) {
        int margin = dip2px(4);
        int marginDiv = dip2px(2);

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        for (int n = 0; n < mSubChartNames.size(); n++) {
            String name = mSubChartNames.get(n);
            Rect rect = mSubChartRectMap.get(name);
            IChartDraw draw = mSubChartDrawMap.get(name);

            //子图最大值
            String maxValue;
            if (KChartConstant.Sub.VOL.equals(name)) {
                maxValue = getBigValueFormatter().format(mMaxValueMap.get(name));
            } else {
                maxValue = formatValue(mMaxValueMap.get(name));
            }
            float maxX = mWidth - calculateWidth(maxValue) - margin;
            float maxY = mMainRect.bottom + baseLine;
            if (n > 0) {
                String preName = mSubChartNames.get(n - 1);
                Rect preRect = mSubChartRectMap.get(preName);
                maxY = preRect.bottom + baseLine;
            }
            canvas.drawText(maxValue, maxX, maxY, mTextPaint);

            //子图最小值
            if (KChartConstant.Sub.VOL.equals(name)) {
                String minValue = getBigValueFormatter().format(mMinValueMap.get(name));
                float minX = mWidth - calculateWidth(minValue) - margin;
                float minY = rect.bottom - marginDiv;
                canvas.drawText(minValue, minX, minY, mTextPaint);
            }
        }
    }

    public Rect getLastRect() {
        if (mSubChartNames.size() > 0) {
            String name = mSubChartNames.get(mSubChartNames.size() - 1);
            return mSubChartRectMap.get(name);
        }
        return null;
    }

    /**
     * 绘制垂直分割线坐标下的时间文本
     *
     * @param canvas    画布
     * @param indexList 索引
     * @param xValues   x轴
     */
    private void drawGridXTimeText(
            Canvas canvas,
            List<Integer> indexList,
            float[] xValues
    ) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;

        float y;
        Rect lastRect = getLastRect();
        if (lastRect != null) {
            y = lastRect.bottom + baseLine + 2;
        } else {
            y = mMainRect.bottom + baseLine + 2;
        }

        for (int i = 0; i < indexList.size(); i++) {
            Integer index = indexList.get(i);
            String text = mAdapter.getDate(index, mInterval);
            float x = xValues[i];
            canvas.drawText(text, x - mTextPaint.measureText(text) / 2, y, mTextPaint);
        }
    }

    private boolean isClickLastPriceRectF(float x, float y) {
        if (lastPriceRectF == null) {
            return false;
        }
        return lastPriceRectF.contains(x, y);
    }

    private boolean isClickMainRectF(float y) {
        return mMainRect != null
                && y >= mMainRect.top
                && y <= mMainRect.bottom;
    }

    /**
     * 绘制最左边的价格线
     *
     * @param canvas
     */
    private void drawPriceLine(Canvas canvas) {
        float margin = dip2px(4);
        float padding = dip2px(2);
        float radius = dip2px(2);
        float y = getMainY((float) animationClosePrice);

        String priceText = formatValueD(animationClosePrice);
        float priceTextWidth = mLastPriceTextPaint.measureText(priceText);
        float priceBgWidth = priceTextWidth + padding * 2;

        Paint.FontMetrics metrics = mLastPriceTextPaint.getFontMetrics();
        float textHeight = metrics.descent - metrics.ascent;

        String countdownText = "";
        float countdownTextWidth = 0.0f;
        boolean showCountdown = isShowCountDownTimer
                && !TextUtils.isEmpty(mInterval)
                && !TimeQuantum.SECOND1.getInterval().equals(mInterval);

        RectF bgRectF = getLastPriceBgRectF();
        if (showCountdown) {
            countdownText = DateUtil.getKLineCountdownDisplayText(
                    mInterval,
                    DateUtil.formatCountdownTime(mCountdownMills, true)
            );

            countdownTextWidth = mLastPriceTextPaint.measureText(countdownText);
            float countdownBgWidth = countdownTextWidth + padding * 2;
            float bgWidth = Math.max(priceBgWidth, countdownBgWidth);
            float bgHeight = textHeight * 2 + padding * 3;
            float left = mWidth - bgWidth - margin;
            bgRectF.set(
                    left,
                    y - bgHeight / 2,
                    left + bgWidth,
                    y + bgHeight / 2
            );
        } else {
            float bgHeight = textHeight + padding * 2;
            float left = mWidth - priceBgWidth - margin;
            bgRectF.set(
                    left,
                    y - bgHeight / 2,
                    left + priceBgWidth,
                    y + bgHeight / 2
            );
        }

        if (translateXtoX(Math.abs(getX(mStopIndex))) < mWidth - bgRectF.width() - margin) {
            //绘制虚线
            ((MainDraw) mMainDraw).drawLastPriceLine(
                    canvas,
                    translateXtoX(getX(mStopIndex)),
                    (float) animationClosePrice,
                    this,
                    mWidth - margin
            );

            //绘制背景
            mLastPricePaint.setColor(mLastPriceBgColor);
            mLastPricePaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(bgRectF, radius, radius, mLastPricePaint);

            //绘制边框
            mLastPricePaint.setColor(mLastPriceStrokeColor);
            mLastPricePaint.setStyle(Paint.Style.STROKE);
            mLastPricePaint.setStrokeWidth(dp2px(1));
            canvas.drawRoundRect(bgRectF, radius, radius, mLastPricePaint);

            if (showCountdown && !TextUtils.isEmpty(countdownText)) {
                float textOffsetY = (bgRectF.height() - padding * 2) / 4;
                float priceTextY = ViewUtil.getTextBaseLine(
                        mLastPriceTextPaint,
                        bgRectF.centerY() - textOffsetY
                );
                canvas.drawText(
                        priceText,
                        bgRectF.right - padding - priceTextWidth,
                        priceTextY,
                        mLastPriceTextPaint
                );
                float countdownTextY = ViewUtil.getTextBaseLine(
                        mLastPriceTextPaint,
                        bgRectF.centerY() + textOffsetY
                );
                canvas.drawText(
                        countdownText,
                        bgRectF.right - padding - countdownTextWidth,
                        countdownTextY,
                        mLastPriceTextPaint
                );
            } else {
                canvas.drawText(
                        priceText,
                        bgRectF.left + padding,
                        ViewUtil.getTextBaseLine(mLastPriceTextPaint, bgRectF.centerY()),
                        mLastPriceTextPaint
                );
            }
            lastPriceRectF = null;
        } else {
            drawPriceLine2(canvas);
        }
    }

    private RectF getLastPriceBgRectF() {
        if (mLastPriceBgRectF == null) {
            mLastPriceBgRectF = new RectF();
        }
        return mLastPriceBgRectF;
    }

    /**
     * 绘制往左滑动后的价格线
     *
     * @param canvas
     */
    private void drawPriceLine2(Canvas canvas) {
        if (mCustomDrawChart.isOpenDraw()) {
            return;
        }

        String text1 = formatValueD(animationClosePrice);
        float textWidth = mLastPriceTextPaint.measureText(text1);
        Paint.FontMetrics lastPriceFM = mLastPriceTextPaint.getFontMetrics();
        float lastPriceTextHeight = lastPriceFM.descent - lastPriceFM.ascent;

        int gridWidth = mWidth / mGridColumns;
        float x = mWidth - gridWidth - textWidth - dip2px(12) - 1;
        float mainY = getMainY((float) animationClosePrice);
        if (mainY < mMainRect.top) {
            mainY = mMainRect.top;
        }
        int w1 = dip2px(6);
        int w2 = dip2px(3);
        int w3 = dip2px(4);
        int w4 = dip2px(2);
        float textLeftX = x - w1;
        int arrowSize = dip2px(10);
        float subArrowSize = arrowSize >> 1;

        lastPriceRectF = new RectF();
        lastPriceRectF.left = textLeftX - w1;
        lastPriceRectF.top = mainY - lastPriceTextHeight / 2 - w2;
        lastPriceRectF.right = x + textWidth + arrowSize + w4;
        lastPriceRectF.bottom = mainY + lastPriceTextHeight / 2 + w2;

        ((MainDraw) mMainDraw).drawLastPriceLineFull(canvas, 0, mainY, this, mWidth);
        mLastPriceCenterSolidPaint.setAlpha(216);
        canvas.drawRoundRect(lastPriceRectF, dip2px(180), dip2px(180), mLastPriceCenterSolidPaint);
        canvas.drawRoundRect(lastPriceRectF, dip2px(180), dip2px(180), mLastPriceCenterStockPaint);

        mBitPaint.setAlpha(255);
        float arrowRight = lastPriceRectF.right - w3;
        Rect rect = new Rect(0, 0, arrowSize, arrowSize);
        RectF dstRect = new RectF(arrowRight - arrowSize, mainY - subArrowSize, arrowRight, mainY + subArrowSize);
        canvas.drawBitmap(mBitmapLastPriceArrow, rect, dstRect, mBitPaint);

        canvas.drawText(
                text1,
                textLeftX,
                ViewUtil.getTextBaseLine(mLastPriceTextPaint, mainY),
                mLastPriceTextPaint
        );
    }

    /**
     * 绘制选择的 x 和 y 轴的值
     *
     * @param canvas
     */
    private void drawSelectorValueXY(Canvas canvas) {
        if (!isShowSelector()) {
            return;
        }

        int textDY = dip2px(1);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        float y = mLastFocusY;
        float textWidth;

        //画选中k线后的x和y轴的价格、选中的点
        // 画Y值
        IKLine point = (IKLine) getItem(mSelectedIndex);
        float w1 = dip2px(5);
        float w2 = dip2px(3);
        float r = textHeight / 2 + w2;
        String text;
        text = formatValue(getMainYToPrice(y));

        float x;
        textWidth = mTextPaint.measureText(text);
        if (translateXtoX(getX(mSelectedIndex)) < getChartWidth() / 2F) {
            x = 1;
            Path path = new Path();
            path.moveTo(x, y - r);
            path.lineTo(x, y + r);
            path.lineTo(textWidth + 2 * w1, y + r);
            path.lineTo(textWidth + 2 * w1 + w2, y);
            path.lineTo(textWidth + 2 * w1, y - r);
            path.close();
            canvas.drawPath(path, mSelectPointPaint);
            canvas.drawPath(path, mSelectorFramePaint);
            canvas.drawText(text, x + w1, fixTextY1(y) + textDY, mTextSelectLightPaint);
        } else {
            x = mWidth - textWidth - 1 - 2 * w1 - w2;
            Path path = new Path();
            path.moveTo(x, y);
            path.lineTo(x + w2, y + r);
            path.lineTo(mWidth - 2, y + r);
            path.lineTo(mWidth - 2, y - r);
            path.lineTo(x + w2, y - r);
            path.close();
            canvas.drawPath(path, mSelectPointPaint);
            canvas.drawPath(path, mSelectorFramePaint);
            canvas.drawText(text, x + w1 + w2, fixTextY1(y) + textDY, mTextSelectLightPaint);
        }

        // 画最底部选中的X值
        String date;
        if (TimeQuantum.SECOND1.getInterval().equals(mInterval)) {
            date = KLineEntity.timeStamp2Date(String.valueOf(point.getLongDate()), "");
        } else {
            date = mAdapter.getDate(mSelectedIndex, mInterval);
        }
        textWidth = mTextPaint.measureText(date);
        r = textHeight / 2;
        x = translateXtoX(getX(mSelectedIndex));
        Rect lastRect = getLastRect();
        if (lastRect != null) {
            y = lastRect.bottom;
        } else {
            y = mMainRect.bottom;
        }

        if (x < textWidth + 2 * w1) {
            x = 1 + textWidth / 2 + w1;
        } else if (mWidth - x < textWidth + 2 * w1) {
            x = mWidth - 1 - textWidth / 2 - w1;
        }
        mSelectorYRectF.set(
                (int) (x - textWidth / 2 - w1),
                (int) y,
                (int) (x + textWidth / 2 + w1),
                (int) (y + baseLine + r) - 3
        );
        canvas.drawRoundRect(mSelectorYRectF, dip2px(2), dip2px(2), mSelectPointPaint);
        canvas.drawText(date, x - textWidth / 2, y + baseLine + textDY, mTextSelectLightPaint);
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private void drawMaxAndMin(Canvas canvas) {
        if (!mainDraw.isLine()) {
            IKLine maxEntry = null, minEntry = null;
            boolean firstInit = true;


            //绘制最大值和最小值
            float x = translateXtoX(getX(mMainMinIndex));
            float y = getMainY((float) mMainLowMinValue);
            String LowString = "── " + getValueFormatter().formatD(mMainLowMinValue);
            //计算显示位置
            //计算文本宽度
            int lowStringWidth = calculateMaxMin(LowString).width();
            int lowStringHeight = calculateMaxMin(LowString).height();
            if (x < getWidth() / 2) {
                //画右边
                canvas.drawText(LowString, x, y + lowStringHeight / 2, mMaxMinPaint);
            } else {
                //画左边
                LowString = getValueFormatter().formatD(mMainLowMinValue) + " ──";
                canvas.drawText(LowString, x - lowStringWidth, y + lowStringHeight / 2, mMaxMinPaint);
            }

            x = translateXtoX(getX(mMainMaxIndex));
            y = getMainY((float) mMainHighMaxValue);

            String highString = "── " + getValueFormatter().formatD(mMainHighMaxValue);
            int highStringWidth = calculateMaxMin(highString).width();
            int highStringHeight = calculateMaxMin(highString).height();
            if (x < getWidth() / 2) {
                //画右边
                canvas.drawText(highString, x, y + highStringHeight / 2, mMaxMinPaint);
            } else {
                //画左边
                highString = getValueFormatter().formatD(mMainHighMaxValue) + " ──";
                canvas.drawText(highString, x - highStringWidth, y + highStringHeight / 2, mMaxMinPaint);
            }

        }
    }

    public float getValueTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * 画值
     *
     * @param canvas
     * @param position 显示某个点的值
     */
    private void drawValue(Canvas canvas, int position) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        if (position < 0) {
            position = mStopIndex;
        }
        if (position >= 0 && position < getItemCount()) {
            for (int n = 0; n < mSubChartNames.size(); n++) {
                String name = mSubChartNames.get(n);
                IChartDraw draw = mSubChartDrawMap.get(name);
                float y;
                if (n == 0) {
                    y = mMainRect.bottom + baseLine;
                } else {
                    String preName = mSubChartNames.get(n - 1);
                    Rect preRect = mSubChartRectMap.get(preName);
                    y = preRect.bottom + baseLine;
                }
                draw.drawText(canvas, this, position, dip2px(4), y + dip2px(4));
            }
            if (mMainDraw != null) {
                float y = baseLine + mMainRect.top - textHeight;
                if (isProKLine) {
                    y = y - dip2px(16);
                }
                mMainDraw.drawText(canvas, this, position, dip2px(4), y);
            }
        }
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 格式化值
     */
    public String formatValue(float value) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().format(value);
    }

    /**
     * 格式化值
     */
    public String formatValueD(double value) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().formatD(value);
    }

    /**
     * 格式化值
     */
    public String formatValueStr(String value, boolean trailingZero) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().formatStr(value, trailingZero);
    }

    public IChartDraw getVolDraw() {
        return mSubChartDrawMap.get(KChartConstant.Sub.VOL);
    }

    /**
     * 重新计算并刷新线条
     */
    public void notifyChanged() {
        //判断小于100个点线宽增加一倍
        if (getAdapter().getCount() < 100) {
            mainDraw.setCandleWidth(dp2px(6f * 1.2f));
            mainDraw.setMultipleCandleWidth(1.2f);
            setPointWidth(dp2px(7f * 1.2f));

            VolumeDraw volumeDraw = (VolumeDraw) getVolDraw();
            if (volumeDraw != null) {
                volumeDraw.setCandleWidth(dp2px(6f * 1.2f));
            }
        } else {
            mainDraw.setCandleWidth(dp2px(6f));
            setPointWidth(dp2px(7f));
            mainDraw.setMultipleCandleWidth(1);
            VolumeDraw volumeDraw = (VolumeDraw) getVolDraw();
            if (volumeDraw != null) {
                volumeDraw.setCandleWidth(dp2px(6f));
            }
        }

        if (getItemCount() != 0) {
            mDataLen = getItemCount() * mPointWidth;
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);
        } else {
            //第一次设置adapter,默认滑动到最右端,空出一格空白,这个时候还没有回调onSizeChanged,所以改为onSizeChanged中初始化设置
//            setScrollX(0);
        }

        if (closePriceChangeAnimation != null) {
            closePriceChangeAnimation.cancel();
            closePriceChangeAnimation = null;
        }
        if (mAdapter.getCount() > 0) {
            double lastClosePrice = ((ICandle) getItem(mAdapter.getCount() - 1)).getClosePriceD();
            if (Double.compare(currentClosePrice, DEFAULT_FLOAT_VALUE) == 0) {
                // 避免初次绘制的时候从0开始绘制
                animationClosePrice = lastClosePrice;
            } else {
                int compareResult = Double.compare(lastClosePrice, currentClosePrice);
                if (compareResult == lastChangeDirection) {
                    priceChangeComboCount++;
                } else {
                    priceChangeComboCount = 0;
                }

                if (priceChangeComboCount >= PRICE_CHANGE_COMBO_THRESHOLD) {
                    haloAnimation.setDuration(ANIMATION_DEFAULT_DURATION / 2);
                } else {
                    haloAnimation.setDuration(ANIMATION_DEFAULT_DURATION);
                }

                closePriceChangeAnimation = ValueAnimator.ofObject(new DoubleEvaluator(), currentClosePrice, lastClosePrice);
                closePriceChangeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animationClosePrice = (double) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                closePriceChangeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                closePriceChangeAnimation.setDuration(PRICE_ANIMATION_DEFAULT_DURATION);
                closePriceChangeAnimation.start();
            }
            currentClosePrice = lastClosePrice;
        } else {
            currentClosePrice = DEFAULT_FLOAT_VALUE;
        }
    }

    /**
     * 供属性动画调用
     *
     * @param animationClosePrice
     */
    public void setAnimationClosePrice(float animationClosePrice) {
        this.animationClosePrice = animationClosePrice;
        postInvalidate();
    }

    private void calculateSelectedX(float x) {
        mSelectedIndex = getSelectedIndex(x);
    }

    /**
     * 通过当前的x轴位置，获取选中的k线的index
     *
     * @param x
     * @return
     */
    private int getSelectedIndex(float x) {
        int index = indexOfTranslateX(xToTranslateX(x));
        if (index < mStartIndex) {
            index = mStartIndex;
        }
        if (index > mStopIndex) {
            index = mStopIndex;
        }
        return index;
    }

    @Override
    protected void onTouchDown() {
        mCustomDrawChart.onTouchDown();
    }

    @Override
    protected void onTouchUp() {
        mCustomDrawChart.onTouchUp();
    }

    @Override
    protected void onTouchMove() {
        mCustomDrawChart.onTouchMove();
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        //画图模式不需要响应长按事件
        if (mCustomDrawChart.isOpenDraw()) {
            return;
        }
        handleLongPress(e);
    }

    private void handleLongPress(MotionEvent e) {
        if (getItemCount() == 0) {
            return;
        }
        touchXToSelectedIndex(e.getX());
        touchYToLastFocusY(e.getY());
        if (isClickMainRectF(e.getY())) {
            isSelectedPoint = true;
        }
        invalidate();
    }

    private void touchYToLastFocusY(float y) {
        if (mMainRect == null) {
            return;
        }
        IKLine point = (IKLine) getItem(mSelectedIndex);
        if (y > mMainRect.bottom) {
            y = getMainY(point.getClosePrice());
        } else if (y < mMainRect.top) {
            y = mMainRect.top;
        }
        mLastFocusY = y;
    }

    @Override
    protected void doOnSingleClick(MotionEvent e) {
        super.doOnSingleClick(e);
        if (mCustomDrawChart.isOpenDraw()) {
            mCustomDrawChart.onTouchClick();
        } else {
            handleSingleClick(e);
        }
    }

    private void handleSingleClick(MotionEvent e) {
        //点击选中，再次点击取消选中
        if (isSelectedPoint) {
            isSelectedPoint = false;
            return;
        }

        //点击最新价按钮
        if (isClickLastPriceRectF(e.getX(), e.getY())) {
            setScrollX((int) -(mOverScrollRange / mScaleX));
            return;
        }

        //点击显示/隐藏订单
        RectF orderSignRectF = getOrderSignRectF(OrderSignRectFName.SHOW_HIDE_BUTTON_RECT_F);
        if (orderSignRectF.contains(e.getX(), e.getY())) {
            isShowOrderSign = !isShowOrderSign;
            mainDraw.onClickOrderHideShowButton(isShowOrderSign);
            return;
        }

        //点击主图
        if (isClickMainRectF(e.getY()) && getItemCount() > 0) {
            touchXToSelectedIndex(e.getX());
            mLastFocusY = e.getY();
            isSelectedPoint = true;
        }
    }

    /**
     * 将x转换为k线的index
     *
     * @param x 当前触摸的x
     */
    private void touchXToSelectedIndex(float x) {
        int lastIndex = mSelectedIndex;
        calculateSelectedX(x);
        if (lastIndex != mSelectedIndex) {
            onSelectedChanged(this, getItem(mSelectedIndex), mSelectedIndex);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        setTranslateXFromScrollX(mScrollX);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        checkAndFixScrollX();
        setTranslateXFromScrollX(mScrollX);
        updateOverScrollRange();
        super.onScaleChanged(scale, oldScale);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (haloAnimation != null) {
            haloAnimation.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // 避免动画带来的内存泄漏
        if (haloAnimation != null) {
            haloAnimation.cancel();
        }

        if (closePriceChangeAnimation != null) {
            closePriceChangeAnimation.cancel();
        }

        stopCountdownTask();
        super.onDetachedFromWindow();
    }

    /**
     * 计算当前的显示区域
     */
    private void calculateValue() {
        if (!isSelectedPoint) {
            mSelectedIndex = -1;
        }

        for (int i = 0; i < getSubChartCount(); i++) {
            String name = mSubChartNames.get(i);
            //这些指标需要处理负数的情况，所以最大值和最小值不一样
            if (KChartConstant.Sub.OBV.equals(name)
                    || KChartConstant.Sub.ROC.equals(name)
                    || KChartConstant.Sub.CCI.equals(name)
            ) {
                mMinValueMap.put(name, Float.POSITIVE_INFINITY);
                mMaxValueMap.put(name, Float.NEGATIVE_INFINITY);
            } else {
                mMinValueMap.put(name, Float.MAX_VALUE);
                mMaxValueMap.put(name, Float.MIN_VALUE);
            }
        }

        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;
        mStartIndex = indexOfTranslateX(xToTranslateX(0));
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth));
        mMainMaxIndex = mStartIndex;
        mMainMinIndex = mStartIndex;
        mMainHighMaxValue = Double.MIN_VALUE;
        mMainLowMinValue = Double.MAX_VALUE;

        for (int i = mStartIndex; i <= mStopIndex; i++) {
            IKLine point = (IKLine) getItem(i);
            if (mMainDraw != null) {
                mMainMaxValue = Math.max(mMainMaxValue, mMainDraw.getMaxValue(point));
                mMainMinValue = Math.min(mMainMinValue, mMainDraw.getMinValue(point));
                if (mMainHighMaxValue != Math.max(mMainHighMaxValue, point.getHighPriceD())) {
                    mMainHighMaxValue = point.getHighPriceD();
                    mMainMaxIndex = i;
                }
                if (mMainLowMinValue != Math.min(mMainLowMinValue, point.getLowPriceD())) {
                    mMainLowMinValue = point.getLowPriceD();
                    mMainMinIndex = i;
                }
            }

            calculateSubChartMinMaxValue(point);
        }

        if (mMainMaxValue != mMainMinValue) {
            float padding = (mMainMaxValue - mMainMinValue) * 0.05f;
            mMainMaxValue += padding;
            if (mMainMinValue >= padding) {
                mMainMinValue -= padding;
            } else {
                mMainMinValue = 0;
            }
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += Math.abs(mMainMaxValue * 0.05f);
            mMainMinValue -= Math.abs(mMainMinValue * 0.05f);
            if (mMainMaxValue == 0) {
                mMainMaxValue = 1;
            }
        }

        mMainScaleY = mMainRect.height() * 1f / (mMainMaxValue - mMainMinValue);
        checkSubChartMinAndMax();
        if (mAnimator.isRunning()) {
            float value = (float) mAnimator.getAnimatedValue();
            mStopIndex = mStartIndex + Math.round(value * (mStopIndex - mStartIndex));
        }
    }

    private void calculateSubChartMinMaxValue(IKLine point) {
        for (int i = 0; i < getSubChartCount(); i++) {
            String name = mSubChartNames.get(i);
            IChartDraw draw = mSubChartDrawMap.get(name);
            mMaxValueMap.put(name, Math.max(mMaxValueMap.get(name), draw.getMaxValue(point)));
            mMinValueMap.put(name, Math.min(mMinValueMap.get(name), draw.getMinValue(point)));
        }
    }

    private void checkSubChartMinAndMax() {
        for (int i = 0; i < getSubChartCount(); i++) {
            String name = mSubChartNames.get(i);
            Rect rect = mSubChartRectMap.get(name);
            if (KChartConstant.Sub.VOL.equals(name)) {
                if (Math.abs(mMaxValueMap.get(name)) < 0.01) {
                    mMaxValueMap.put(name, 15.00f);
                }
            } else {
                Float maxValue = mMaxValueMap.get(name);
                Float minValue = mMinValueMap.get(name);
                if (maxValue.equals(minValue)) {
                    //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
                    maxValue += Math.abs(maxValue * 0.05f);
                    minValue -= Math.abs(minValue * 0.05f);
                    mMaxValueMap.put(name, maxValue);
                    mMinValueMap.put(name, minValue);
                }
            }
            float scaleY = rect.height() * 1f / (mMaxValueMap.get(name) - mMinValueMap.get(name));
            mScaleYMap.put(name, scaleY);
        }
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    }

    /**
     * 获取平移的最大值
     *
     * @return
     */
    private float getMaxTranslateX() {
        return mPointWidth / 2;
    }

    @Override
    public int getMinScrollX() {
        return (int) -getOverRange(ITEM_MIN_VISIBLE_COUNT + 1);
    }

    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX() + getOverRange(ITEM_MIN_VISIBLE_COUNT));
    }

    @Override
    public int getMaxItemScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }

    private float getOverRange(int size) {
        return mWidth / mScaleX - mPointWidth * size;
    }

    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, getLastItemIndex());
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint);
    }

    public void drawIndicatorLine(Canvas canvas, IndicatorLine line) {
        mLinePaint.setColor(Color.parseColor(line.getColor()));
        mLinePaint.setStrokeWidth(dip2px(line.getWidth()));
        canvas.drawPath(line.getPath(), mLinePaint);
    }

    /**
     * 通过Path绘制分时线（实现圆角）
     *
     * @param canvas
     * @param paint
     * @param path
     */
    public void drawTimeLine(Canvas canvas, Paint paint, Path path) {
        paint.setColor(timeLineColor);
        canvas.drawPath(path, paint);
    }

    /**
     * 绘制分时线的区域
     *
     * @param canvas
     * @param paint
     * @param path
     * @param startX
     * @param stopX
     */
    public void drawTimeLineArea(Canvas canvas, Paint paint, Path path, float startX, float stopX) {
        drawGradient(paint, new PointF(0, getMainY(mMainMaxValue)), new PointF(0, mMainRect.bottom), timeLineAreaShadeColors, timeLineAreaShadePoints);
        Path path5 = new Path();
        path5.addPath(path);
        path5.lineTo(stopX, displayHeight + mTopPadding + mBottomPadding);
        path5.lineTo(startX, displayHeight + mTopPadding + mBottomPadding);
        path5.close();
        canvas.drawPath(path5, paint);
    }

    /**
     * 绘制分时线最新节点的呼吸灯效果
     *
     * @param canvas
     * @param paint
     * @param mBreathHaloPaint
     * @param stopX
     * @param stopValue
     * @param haloRadiusRatio
     */
    public void drawBreathingLightPoint(Canvas canvas, Paint paint, Paint mBreathHaloPaint, float stopX, float stopValue, float haloRadiusRatio) {
        float radius = dip2px(WHITE_POINT_RADIUS);
        float haloRadius = radius * haloRadiusRatio;
        if (Float.compare(haloRadius, 0) > 0) {
            mBreathHaloPaint.setShader(
                    new RadialGradient(stopX,
                            getMainY(stopValue),
                            radius * haloRadiusRatio, timeLineColor,
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP));
            canvas.drawCircle(stopX, getMainY(stopValue), radius * haloRadiusRatio, mBreathHaloPaint);
        }
        canvas.drawCircle(stopX, getMainY(stopValue), radius, paint);
    }

    /**
     * 在主区域画分时线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainMinuteLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        Path path5 = new Path();
        path5.moveTo(startX, displayHeight + mTopPadding + mBottomPadding);
        path5.lineTo(startX, getMainY(startValue));
        path5.lineTo(stopX, getMainY(stopValue));
        path5.lineTo(stopX, displayHeight + mTopPadding + mBottomPadding);
        path5.close();
        canvas.drawPath(path5, paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawChildLine(Canvas canvas, Paint paint, float startX, float startValue,
                              float stopX, float stopValue, String name) {
        canvas.drawLine(
                startX,
                getChildY(name, startValue),
                stopX, getChildY(name, stopValue),
                paint
        );
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawVolLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getVolY(startValue), stopX, getVolY(stopValue), paint);
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    public Object getItem(int position) {
        if (mAdapter != null) {
            return mAdapter.getItem(position);
        } else {
            return null;
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
     */
    public float getX(int position) {
        return position * mPointWidth;
    }

    /**
     * 获取适配器
     *
     * @return
     */
    public IAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }

    /**
     * 获取ValueFormatter
     *
     * @return
     */
    public IValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    public void setValueFormatter(IValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;
    }

    /**
     * 获取K、M、B单位缩进Formatter
     *
     * @return
     */
    @NonNull
    public IValueFormatter getBigValueFormatter() {
        if (mBigValueFormatter == null) {
            mBigValueFormatter = new BigValueFormatter();
        }
        return mBigValueFormatter;
    }

    /**
     * 获取主区域的 IChartDraw
     *
     * @return IChartDraw
     */
    public IChartDraw getMainDraw() {
        return mMainDraw;
    }

    /**
     * 设置主区域的 IChartDraw
     *
     * @param mainDraw IChartDraw
     */
    public void setMainDraw(IChartDraw mainDraw) {
        mMainDraw = mainDraw;
        this.mainDraw = (MainDraw) mMainDraw;
    }

    /**
     * 二分查找当前值的index
     *
     * @return
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        try {//
            if (end == start) {
                return start;
            }
            if (end - start == 1) {
                float startValue = getX(start);
                float endValue = getX(end);
                return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
            }
            mid = start + (end - start) / 2;
            midValue = getX(mid);
            if (translateX < midValue) {
                return indexOfTranslateX(translateX, start, mid);
            } else if (translateX > midValue) {
                return indexOfTranslateX(translateX, mid, end);
            } else {
                return mid;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return mid;
        }
    }

    /**
     * 设置数据适配器
     */
    public void setAdapter(IAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        notifyChanged();
    }

    public int getLastItemIndex() {
        return Math.max(getItemCount() - 1, 0);
    }

    private int getItemCount() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getCount();
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    /**
     * 设置动画时间
     */
    public void setAnimationDuration(long duration) {
        if (mAnimator != null) {
            mAnimator.setDuration(duration);
        }
    }

    /**
     * 设置表格行数
     */
    public void setGridRows(int gridRows) {
        if (gridRows < 1) {
            gridRows = 1;
        }
        mGridRows = gridRows;
    }

    /**
     * 设置表格列数
     */
    public void setGridColumns(int gridColumns) {
        if (gridColumns < 1) {
            gridColumns = 1;
        }
        mGridColumns = gridColumns;
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    public float xToTranslateX(float x) {
        return -mTranslateX + x / mScaleX;
    }

//    public float yToTranslateY(float y){
//        return
//    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    public float translateXtoX(float translateX) {
        return (translateX + mTranslateX) * mScaleX;
    }

    /**
     * 获取上方padding
     */
    public float getTopPadding() {
        return mTopPadding;
    }

    /**
     * 获取上方padding
     */
    public float getChildPadding() {
        return mChildPadding;
    }

    /**
     * 获取子试图上方padding
     */
    public float getmChildScaleYPadding() {
        return mChildPadding;
    }

    /**
     * 获取图的宽度
     *
     * @return
     */
    public int getChartWidth() {
        return mWidth;
    }

    public Rect getmMainRect() {
        return mMainRect;
    }

    /**
     * 是否显示选择器
     *
     * @return
     */
    public boolean isShowSelector() {
        if (mCustomDrawChart.isOpenDraw()) {
            return false;
        }
        if (mMainRect == null) {
            return false;
        }
        return isSelectedPoint;
    }

    public void setProKLine(boolean proKLine) {
        isProKLine = proKLine;
    }

    public void resetSelectedPoint() {
        isSelectedPoint = false;
    }

    /**
     * 获取选择索引
     */
    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public Rect getVolRect() {
        return mSubChartRectMap.get(KChartConstant.Sub.VOL);
    }

    /**
     * 设置选择监听
     */
    public void setOnSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void setOnOpenFullScreenListener(OnOpenFullScreenListener onOpenFullScreenListener) {
        this.mOnOpenFullScreenListener = onOpenFullScreenListener;
    }

    public void onSelectedChanged(BaseKLineChartView view, Object point, int index) {
        if (this.mOnSelectedChangedListener != null) {
            mOnSelectedChangedListener.onSelectedChanged(view, point, index);
        }
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    @Override
    public boolean isFullScreen() {
        return mDataLen >= mWidth / mScaleX;
    }

    @Override
    public boolean isHasData() {
        return mDataLen > 0;
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        mOverScrollRange = overScrollRange;
    }

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    public void setTopPadding(float topPadding) {
        mTopPadding = (int) topPadding;
    }

    /**
     * 设置下方padding
     *
     * @param bottomPadding
     */
    public void setBottomPadding(int bottomPadding) {
        mBottomPadding = bottomPadding;
    }

    /**
     * 设置表格线宽度
     */
    public void setGridLineWidth(float width) {
        mGridPaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setGridLineColor(int color) {
        mGridPaint.setColor(color);
    }

    /**
     * 设置选择器横线宽度
     */
    public void setSelectedXLineWidth(float width) {
        mSelectedXLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器横线颜色
     */
    public void setSelectedXLineColor(int color) {
        mSelectedXLinePaint.setColor(color);
    }

    /**
     * 设置选择器竖线宽度
     */
    public void setSelectedYLineWidth(float width) {
        mSelectedYLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器竖线颜色
     */
    public void setSelectedYLineColor(int color) {
        mSelectedYLinePaint.setColor(color);
    }

    /**
     * 主图背景色、副图背景色
     *
     * @param mainBgStartColor
     * @param mainBgEndColor
     * @param volBgStartColor
     * @param volBgEndColor
     */
    public void setBackgroundColor(int mainBgStartColor, int mainBgEndColor, int volBgStartColor, int volBgEndColor) {
        this.mainBgStartColor = mainBgStartColor;
        this.mainBgEndColor = mainBgEndColor;
        this.volBgStartColor = volBgStartColor;
        this.volBgEndColor = volBgEndColor;
    }

    /**
     * 副图前景色
     *
     * @param volForgetStartColor
     * @param volForgetEndColor
     */
    public void setVolForgetColor(int volForgetStartColor, int volForgetEndColor) {
        this.volForgetStartColor = volForgetStartColor;
        this.volForgetEndColor = volForgetEndColor;
    }

    /**
     * 订单标识颜色
     */
    public void setOrderSignColor(int upColor,
                                  int downColor,
                                  int backgroundColor1,
                                  int mainTextColor,
                                  int historyOrderTextColor,
                                  int historyOrderSignBgColor,
                                  int ordersBgColor,
                                  int ordersStrokeColor,
                                  int ordersTextColor
    ) {
        this.upColor = upColor;
        this.downColor = downColor;
        this.backgroundColor1 = backgroundColor1;
        this.mainTextColor = mainTextColor;
        this.historyOrderTextColor = historyOrderTextColor;
        this.liqOrderSignBgColor = historyOrderSignBgColor;
        this.mOrdersBgColor = ordersBgColor;
        this.mOrdersStrokeColor = ordersStrokeColor;
        this.mOrdersTextColor = ordersTextColor;
    }

    public void setLastPriceColor(int lastPriceBgColor, int lastPriceStrokeColor, int lastPriceTextColor) {
        this.mLastPriceBgColor = lastPriceBgColor;
        this.mLastPriceStrokeColor = lastPriceStrokeColor;
        this.mLastPriceTextColor = lastPriceTextColor;
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    public void setMTextColor(int color) {
        mMaxMinPaint.setColor(color);
    }

    public void setSelectLightTextColor(int color) {
        mTextSelectLightPaint.setColor(color);
    }

    public void setLastPriceTextColor(int color) {
        mLastPriceTextPaint.setColor(color);
    }

    /**
     * 设置最大值/最小值文字大小
     */
    public void setMTextSize(float textSize) {
        mMaxMinPaint.setTextSize(textSize);
    }

    /**
     * 设置选择器选中的文字大小
     */
    public void setSelectLightTextSize(float textSize) {
        mTextSelectLightPaint.setTextSize(textSize);
    }

    /**
     * 设置选择器选中的文字大小
     */
    public void setLastPriceTextSize(float textSize) {
        mLastPriceTextPaint.setTextSize(textSize);
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(int color) {
//        mBackgroundPaint.setColor(color);
    }

    /**
     * 设置选中point 值显示背景
     */
    public void setSelectPointColor(int color) {
        mSelectPointPaint.setColor(color);
    }

    /**
     * 选中点变化时的监听
     */
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseKLineChartView view, Object point, int index);
    }

    public interface OnOpenFullScreenListener {
        void onOpen();
    }

    /**
     * 获取文字大小
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * 获取曲线宽度
     */
    public float getLineWidth() {
        return mLineWidth;
    }

    /**
     * 设置曲线的宽度
     */
    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    /**
     * 设置每个点的宽度
     */
    public void setPointWidth(float pointWidth) {
        mPointWidth = pointWidth;
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    public int getDisplayHeight() {
        return displayHeight + mTopPadding + mBottomPadding;
    }

    public void setPriceChangeDirection(int direction) {
        this.lastChangeDirection = direction;
    }

    protected void initChildDrawTypeface() {
        if (mMainDraw != null) {
            mMainDraw.setTypeface(getCustomTypeface());
        }
        for (Map.Entry<String, IChartDraw> entry : mSubChartDrawMap.entrySet()) {
            entry.getValue().setTypeface(getCustomTypeface());
        }
    }

    /**
     * @param names 主图指标列表
     */
    public void changeMainDrawType(List<String> names) {
        //隐藏选择器
        isSelectedPoint = false;

        if (mainDraw != null) {
            mainDraw.setSelectedIndicators(names);
            invalidate();
        }
    }

    /**
     * 子图MACD/KDJ/RSI/WR切换
     *
     * @param names
     */
    public void setSubChartNames(List<String> names) {
        mSubChartNames.clear();
        if (names != null && names.size() > 0) {
            mSubChartNames.addAll(names);
            //如果选中成交量图，需要固定展示在主图的下方
            if (mSubChartNames.contains(KChartConstant.Sub.VOL)) {
                mSubChartNames.remove(KChartConstant.Sub.VOL);
                mSubChartNames.add(0, KChartConstant.Sub.VOL);
            }
        }
        initRect();
        invalidate();
    }

    /**
     * 是否显示合约当前委托
     */
    public void setShowOpenOrder(boolean show) {
        isShowOpenOrder = show;
    }

    /**
     * 是否显示合约持仓
     */
    public void setShowPositionOrder(boolean show) {
        isShowPositionOrder = show;
    }

    /**
     * 是否显示合约历史委托
     */
    public void setShowHistoryOrder(boolean show) {
        isShowHistoryOrder = show;
    }

    /**
     * 是否显示合约强制平仓订单
     *
     * @param showLiquidationOrder
     */
    public void setShowLiquidationOrder(boolean showLiquidationOrder) {
        isShowLiquidationOrder = showLiquidationOrder;
    }

    /**
     * 是否显示合约止盈止损
     */
    public void setShowStopLimitOrder(boolean show) {
        isShowStopLimitOrder = show;
    }

    /**
     * 是否显示计划委托
     */
    public void setShowPlanOrder(boolean show) {
        isShowPlanOrder = show;
    }

    protected void setShowOrderSign(boolean showOrderSign) {
        isShowOrderSign = showOrderSign;
    }

    protected void setKLineOrientation(int orientation) {
        this.mKLineOrientation = orientation;
    }

    public void setShowCountdownTimer(boolean show) {
        this.isShowCountDownTimer = show;
        if (isShowCountDownTimer) {
            startCountdownTask();
        } else {
            stopCountdownTask();
        }
    }

    public void stopCountdownTask() {
        if (mCountdownTask != null) {
            mCountdownTask.stop();
        }
    }

    public HistorySignEntity getBuyOrderInfo() {
        return buyOrderInfo;
    }

    public HistorySignEntity getSellOrderInfo() {
        return sellOrderInfo;
    }

    public HistorySignEntity getLiqOrderInfo() {
        return liqOrderInfo;
    }

    public float getTopMaOrBullSpaceValue() {
        return mTopMaOrBullSpaceValue;
    }

    public void setTimeInterval(long timeInterval) {
        this.mTimeInterval = Math.max(1, timeInterval);
        Log.d(TAG, "setTimeInterval mTimeInterval = " + mTimeInterval);
    }

    public void setOriginTimeInterval(long timeInterval) {
        this.mOriginTimeInterval = timeInterval;
    }

    public void setInterval(String interval) {
        this.mInterval = interval;
    }

    public boolean isFutures() {
        return isFutures;
    }

    public void setFutures(boolean futures) {
        isFutures = futures;
    }

    public String getInterval() {
        return mInterval;
    }

    public void calculateLocation(List<DrawingPoint> points) {
        for (DrawingPoint point : points) {
            if (!point.isUseRawX()) {
                point.setX(pointTimeToX(point.getTime()));
                point.setY(getDrawMainY(point.getPrice()));
            }
        }
    }

    private int checkLimitIntervalPosition(int position) {
        return Math.min(position, MAX_INTERVAL_POSITION);
    }

    public float pointTimeToX(long time) {
        int position = mAdapter.getKeyPosition(time);
        if (position < 0) {
            int lastPosition = getLastItemIndex();
            long last = mAdapter.getKey(lastPosition);
            long first = mAdapter.getKey(0);
            if (time > last) {
                int intervalPosition = checkLimitIntervalPosition((int) ((time - last) / mTimeInterval));
                position = lastPosition + intervalPosition;
                Log.d(TAG, "pointTimeToX if (time > last) pointTimeToX:" + intervalPosition);
                Log.d(TAG, "pointTimeToX if (time > last) lastPosition:" + lastPosition + ", last:" + last);
            } else if (time < first) {
                int intervalPosition = (int) ((first - time) / mTimeInterval);
                position = -intervalPosition;
                Log.d(TAG, "pointTimeToX if (time < first) pointTimeToX:" + intervalPosition);
                Log.d(TAG, "pointTimeToX if (time < first) firstPosition:" + 0 + ", first:" + first);
            }
        }
        return translateXtoX(getX(position));
    }

    public long pointXToTime(float x) {
        int index = indexOfTranslateX(xToTranslateX(x));
        if (index == 0 || index == getLastItemIndex()) {
            return getOutOfBoundsTime(x);
        }
        IKLine point = (IKLine) getItem(index);
        return point.getLongDate();
    }

    public long getOutOfBoundsTime(float x) {
        float lastPointX = translateXtoX(getX(getLastItemIndex()));
        float firstPointX = translateXtoX(getX(0));
        Log.d(TAG, "getOutOfBoundsTime lastPointX = " + lastPointX
                + ", firstPointX = " + firstPointX
                + ", pointX = " + x);
        if (x > lastPointX) {
            IKLine lastPoint = (IKLine) getItem(getLastItemIndex());
            int intervalSize = (int) ((x - lastPointX) / (mPointWidth * mScaleX));
            Log.d(TAG, "getOutOfBoundsTime intervalSize = " + intervalSize
                    + ", lastPointTime = " + lastPoint.getLongDate()
                    + ", mTimeInterval = " + mTimeInterval);
            return lastPoint.getLongDate() + intervalSize * mTimeInterval;
        } else if (x < firstPointX) {
            IKLine firstPoint = (IKLine) getItem(0);
            float distance;
            //计算第一根柱子和x的距离，x>0说明在屏幕内，x<=0说明在屏幕左侧外
            if (x > 0) {
                distance = firstPointX - x;
            } else {
                distance = Math.abs(x) + firstPointX;
            }
            //用距离除以一根柱子的宽度，得到间隔多少根柱子
            int intervalSize = (int) (distance / (mPointWidth * mScaleX));
            Log.d(TAG, "getOutOfBoundsTime intervalSize = " + intervalSize
                    + ", firstPointTime = " + firstPoint.getLongDate()
                    + ", mTimeInterval = " + mTimeInterval);
            return firstPoint.getLongDate() - intervalSize * mTimeInterval;
        }
        int index = indexOfTranslateX(xToTranslateX(x));
        IKLine point = (IKLine) getItem(index);
        return point.getLongDate();
    }

    public CustomDrawChart getCustomDrawChart() {
        return mCustomDrawChart;
    }

    protected void startCountdownTask() {
        if (!isShowCountDownTimer) {
            return;
        }
        if (TextUtils.isEmpty(mInterval)
                || TimeQuantum.SECOND1.getInterval().equals(mInterval)) {
            return;
        }
        KLineEntity item = (KLineEntity) getItem(getLastItemIndex());
        if (item == null) {
            return;
        }
        if (mCountdownTask == null) {
            mCountdownTask = new CountdownTask(mCountdownListener);
        }
        mCountdownTask.begin(mInterval, mOriginTimeInterval, item.getLongDate());
    }

    CountdownTask.CountdownListener mCountdownListener = new CountdownTask.CountdownListener() {
        @Override
        public void onCountdownChanged(long countdown) {
            mCountdownMills = countdown;
            invalidate();
        }
    };

    @NonNull
    private RectF getOrderSignRectF(String name) {
        if (mOrderSignRectF == null) {
            mOrderSignRectF = new HashMap<>();
        }
        RectF rectF = mOrderSignRectF.get(name);
        if (rectF == null) {
            rectF = new RectF();
            mOrderSignRectF.put(name, rectF);
        }
        return rectF;
    }

    private float getOrderSignDimens(String name) {
        if (mOrderSignDimens == null) {
            mOrderSignDimens = new HashMap<>();
            initOrderSignDimens();
        }
        Float value = mOrderSignDimens.get(name);
        if (value == null) {
            return 0f;
        }
        return value;
    }

    private static class OrderSignRectFName {
        private static final String LEFT_RECT_F = "leftRectF";
        private static final String RIGHT_RECT_F = "rightRectF";
        private static final String PNL_LEFT_RECT_F = "pnlLeftRectF";
        private static final String PNL_RIGHT_RECT_F = "pnlRightRectF";
        private static final String SHOW_HIDE_BUTTON_RECT_F = "showHideButtonRectF";
        private static final String ORDERS_RECT_F = "ordersRectF";
    }

    private static class OrderSignDimens {
        private static final String HALF_HEIGHT = "halfHeight";
        private static final String PADDING_H = "paddingH";
        private static final String CORNER = "corner";
        private static final String STROKE_WIDTH = "strokeWidth";
        private static final String MARGIN_H = "marginH";
        private static final String DASH_WIDTH = "dashWidth";
        private static final String DASH_GAP = "dashGap";
        private static final String TEXT_SIZE = "textSize";
        private static final String SHOW_HIDE_BUTTON_SIZE = "showHideButtonSize";
        private static final String SHOW_HIDE_BUTTON_MARGIN_END = "showHideButtonMarginEnd";
        private static final String SHOW_HIDE_BUTTON_MARGIN_TOP = "showHideButtonMarginTop";
        private static final String ORDERS_HEIGHT = "ordersHeight";
    }
}
