package com.github.fujianlian.klinechart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;

import com.github.fujianlian.klinechart.base.IAdapter;
import com.github.fujianlian.klinechart.draw.CCIDraw;
import com.github.fujianlian.klinechart.draw.DMIDraw;
import com.github.fujianlian.klinechart.draw.KDJDraw;
import com.github.fujianlian.klinechart.draw.MACDDraw;
import com.github.fujianlian.klinechart.draw.MainDraw;
import com.github.fujianlian.klinechart.draw.OBVDraw;
import com.github.fujianlian.klinechart.draw.ROCDraw;
import com.github.fujianlian.klinechart.draw.RSIDraw;
import com.github.fujianlian.klinechart.draw.StochRSIDraw;
import com.github.fujianlian.klinechart.draw.TRIXDraw;
import com.github.fujianlian.klinechart.draw.VolumeDraw;
import com.github.fujianlian.klinechart.draw.WRDraw;
import com.github.fujianlian.klinechart.entity.KChartConfig;
import com.github.fujianlian.klinechart.entity.KLineSelectorEntity;
import com.github.fujianlian.klinechart.utils.ThemeUtils;

import java.util.concurrent.TimeUnit;

/**
 * k线图
 * Created by tian on 2016/5/20.
 */
public class KLineChartView extends BaseKLineChartView {
    public static final int LOAD_MORE_STATE_READY = 1;
    public static final int LOAD_MORE_STATE_LOADING = 2;
    public static final int LOAD_MORE_STATE_FORBIDDEN = 3;
    private static final int DATA_COUNT_MAX = 5000;
    private static final int DEFAULT_GRID_ROWS = 4;
    private static final int DEFAULT_GRID_COLUMNS = 3;
    ProgressBar mProgressBar;
    private boolean isRefreshing = false;
    private boolean mLastScrollEnable;
    private boolean mLastScaleEnable;

    private KChartRefreshListener mLoadMoreListener;
    private int mLoadMoreState = LOAD_MORE_STATE_READY;
    private View mLoadMoreView;

    private MACDDraw mMACDDraw;
    private RSIDraw mRSIDraw;
    private OBVDraw mOBVDraw;
    private ROCDraw mROCDraw;
    private CCIDraw mCCIDraw;
    private MainDraw mMainDraw;
    private KDJDraw mKDJDraw;
    private WRDraw mWRDraw;
    private VolumeDraw mVolumeDraw;
    private StochRSIDraw mStochRSIDraw;
    private DMIDraw mDMIDraw;
    private TRIXDraw mTRIXDraw;

    public KLineChartView(Context context) {
        this(context, null);
    }

    public KLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initAttrs(attrs);
    }

    private void initView() {
        // k线控件只显示为LTR方向
        setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        mProgressBar = new ProgressBar(getContext());
        LayoutParams layoutParams = new LayoutParams(dp2px(50), dp2px(50));
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mProgressBar, layoutParams);
        mProgressBar.setVisibility(GONE);

        mLoadMoreView = LayoutInflater.from(getContext()).inflate(
                R.layout.view_loading_kchart_load_more, this, false);
        addView(mLoadMoreView);
        mLoadMoreView.setVisibility(View.GONE);

        mVolumeDraw = new VolumeDraw(this);
        mMACDDraw = new MACDDraw(this);
        mWRDraw = new WRDraw(this);
        mKDJDraw = new KDJDraw(this);
        mRSIDraw = new RSIDraw(this);
        mOBVDraw = new OBVDraw(this);
        mStochRSIDraw = new StochRSIDraw(this);
        mROCDraw = new ROCDraw(this);
        mCCIDraw = new CCIDraw(this);
        mDMIDraw = new DMIDraw(this);
        mTRIXDraw = new TRIXDraw(this);
        mMainDraw = new MainDraw(this);

        mSubChartDrawMap.put(KChartConstant.Sub.VOL, mVolumeDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.MACD, mMACDDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.KDJ, mKDJDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.RSI, mRSIDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.WR, mWRDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.OBV, mOBVDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.STOCHRSI, mStochRSIDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.ROC, mROCDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.CCI, mCCIDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.DMI, mDMIDraw);
        mSubChartDrawMap.put(KChartConstant.Sub.TRIX, mTRIXDraw);

        setMainDraw(mMainDraw);

        initTypeface(getContext());
        initChildDrawTypeface();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.KLineChartView);
        try {
            //public
            setPointWidth(array.getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)));
            setTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)));
            setTopMaOrBollTextSize(array.getDimension(R.styleable.KLineChartView_kc_top_ma_or_bull_text_size, getDimension(R.dimen.chart_text_size)));
            setTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_text)));
            setMTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_selector_text_size)));
            setSelectLightTextSize(getDimension(R.dimen.chart_text_size));
            setLastPriceTextSize(getDimension(R.dimen.chart_text_size));
            setMTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, ThemeUtils.getThemeColor(getContext(), R.attr.textPrimary)));
            setSelectLightTextColor(ThemeUtils.getThemeColor(getContext(), R.attr.chart_selector_price_text_color));
            setLastPriceTextColor(ThemeUtils.getThemeColor(getContext(), R.attr.primary));
            setLineWidth(array.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width)));
            setBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_background_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_bac)));
            setSelectPointColor(array.getColor(R.styleable.KLineChartView_kc_background_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_selector_price_bg_color)));

            setSelectedXLineColor(ThemeUtils.getThemeColor(getContext(), R.attr.textPrimary));
            setSelectedXLineWidth(dip2px(1));

            setSelectedYLineColor(ThemeUtils.getThemeColor(getContext(), R.attr.textPrimary));
            setSelectedYLineWidth(dip2px(1));

            setBackgroundColor(
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_main_background_start_color),
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_main_background_end_color),
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_vol_background_start_color),
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_vol_background_end_color)
            );

            setVolForgetColor(
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_vol_forget_start_color),
                    ThemeUtils.getThemeColor(getContext(), R.attr.chart_vol_forget_end_color)
            );

            setOrderSignColor(
                    ThemeUtils.getThemeColor(getContext(), R.attr.upColor),
                    ThemeUtils.getThemeColor(getContext(), R.attr.downColor),
                    ThemeUtils.getThemeColor(getContext(), R.attr.bgSecondary),
                    ThemeUtils.getThemeColor(getContext(), R.attr.textTitle),
                    Color.parseColor("#fff0f0"),
                    ThemeUtils.getThemeColor(getContext(), R.attr.tintsOrangeBase),
                    ThemeUtils.getThemeColor(getContext(), R.attr.fillModalButtonDefault),
                    ThemeUtils.getThemeColor(getContext(), R.attr.textTertiary),
                    ThemeUtils.getThemeColor(getContext(), R.attr.textSecondary)
            );

            setLastPriceColor(
                    ThemeUtils.getThemeColor(getContext(), R.attr.bgSecondary),
                    ThemeUtils.getThemeColor(getContext(), R.attr.primary),
                    ThemeUtils.getThemeColor(getContext(), R.attr.primary)
            );

            setGridLineWidth(array.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)));
            setGridLineColor(array.getColor(R.styleable.KLineChartView_kc_grid_line_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_grid_line)));
            //macd
            setMACDWidth(array.getDimension(R.styleable.KLineChartView_kc_macd_width, getDimension(R.dimen.chart_line_width)));
            setDIFColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma5)));
            setDEAColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma10)));
            setMACDColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma30)));
            //kdj
            setKColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma5)));
            setDColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma10)));
            setJColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma30)));
            //main
            setMa5Color(array.getColor(R.styleable.KLineChartView_kc_dif_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma5)));
            setMa10Color(array.getColor(R.styleable.KLineChartView_kc_dea_color, ThemeUtils.getThemeColor(getContext(), R.attr.chart_ma10)));
            setCandleWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)));
            setCandleLineWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)));
            setSelectorBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_selector_background_color, ThemeUtils.getThemeColor(getContext(), R.attr.dividerPrimary)));
            setSelectorTextSize(array.getDimension(R.styleable.KLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)));
            setCandleSolid(array.getBoolean(R.styleable.KLineChartView_kc_candle_solid, true));
            setSubChartWidget(array.getFloat(R.styleable.KLineChartView_kc_vol_rect_widget, 0.15F));
            setTopPadding(array.getDimension(R.styleable.KLineChartView_kc_top_padding, getDimension(R.dimen.chart_top_padding)));

            int gridRows = array.getInt(R.styleable.KLineChartView_kc_grid_rows, DEFAULT_GRID_ROWS);
            int gridColumns = array.getInt(R.styleable.KLineChartView_kc_grid_columns, DEFAULT_GRID_COLUMNS);
            setGridRows(gridRows);
            setGridColumns(gridColumns);

            int orientation = array.getInt(R.styleable.KLineChartView_kc_orientation, KLineOrientation.VERTICAL);
            setKLineOrientation(orientation);
        } finally {
            array.recycle();
        }
    }

    private float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }

    @Override
    public void onLeftSide() {
    }

    @Override
    public void onRightSide() {
    }

    @Override
    public void onFirstItemVisible() {
        loadMore();
    }

    private void loadMore() {
        if (mLoadMoreListener == null || mLoadMoreState != LOAD_MORE_STATE_READY) {
            return;
        }
        IAdapter adapter = getAdapter();
//        if (adapter == null || adapter.getCount() >= DATA_COUNT_MAX) {
        //todo 暂时放开数量限制
        if (adapter == null) {
            // 最多加载5000条数据
            return;
        }
        long currentStartTime = 0;
        Object firstEntity = adapter.getItem(0);
        if (firstEntity instanceof KLineEntity) {
            currentStartTime = TimeUnit.MILLISECONDS.toSeconds(((KLineEntity) firstEntity).getLongDate());
        }
        if (currentStartTime <= 0) {
            // 当前还没有数据，不要进行加载更多操作
            return;
        }
        mLoadMoreState = LOAD_MORE_STATE_LOADING;
        if (mLoadMoreView != null) {
            mLoadMoreView.setVisibility(View.VISIBLE);
        }
        mLoadMoreListener.onLoadMoreBegin(currentStartTime);
    }

    public void justShowLoading() {
        if (!isRefreshing) {
            isLongPress = false;
            isRefreshing = true;
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mLastScaleEnable = isScaleEnable();
            mLastScrollEnable = isScrollEnable();
            super.setScrollEnable(false);
            super.setScaleEnable(false);
        }
    }

    private void hideLoading() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        super.setScrollEnable(mLastScrollEnable);
        super.setScaleEnable(mLastScaleEnable);
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        isRefreshing = false;
        hideLoading();
    }

    /**
     * 刷新完成，没有数据
     */
    public void refreshEnd() {
        isRefreshing = false;
        hideLoading();
        startCountdownTask();
    }

    /**
     * 重置加载更多
     */
    public void resetLoadMore() {
        mLoadMoreState = LOAD_MORE_STATE_READY;
        if (mLoadMoreView != null) {
            mLoadMoreView.setVisibility(View.GONE);
        }
    }

    /**
     * 没有更多数据了
     */
    public void setNoMoreData() {
        mLoadMoreState = LOAD_MORE_STATE_FORBIDDEN;
        if (mLoadMoreView != null) {
            mLoadMoreView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置k线显示必要的配置
     *
     * @param config
     */
    public void setKChatConfig(KChartConfig config) {
        setInterval(config.getInterval());
        setFutures(config.isFutures());
        getCustomDrawChart().setDrawVisible(config.isShowCustomDraw());
        setTimeInterval(config.getTimeInterval());
        setOriginTimeInterval(config.getTimeInterval());
        changeMainDrawType(config.getMainNames());
        if (config.isShowSubChart()) {
            setSubChartNames(config.getSubNames());
        }
        setProKLine(config.isShowSubChart());
        setValueFormatter(config.getFormatter());
        setMainDrawLine(config.isTimeLine());
        startAnimation();
        refreshEnd();
        updateOverScrollRange();
        resetDefaultScrollX();
    }

    /**
     * 切换交易对时，重置相关设置
     */
    public void resetSwitchSymbol() {
        setShowOrderSign(true);
    }

    public interface KChartRefreshListener {
        /**
         * 加载更多
         */
        void onLoadMoreBegin(long currentStartTime);
    }

    public interface KChartKLineSelectorListener {
        void showSelector(KLineSelectorEntity selectorEntity);

        void closeSelector();

        void onOrderInfoClick();

        void onClickOrderHideShow(boolean show);
    }

    @Override
    public void setScaleEnable(boolean scaleEnable) {
        if (isRefreshing) {
            throw new IllegalStateException("请勿在刷新状态设置属性");
        }
        super.setScaleEnable(scaleEnable);

    }

    @Override
    public void setScrollEnable(boolean scrollEnable) {
        if (isRefreshing) {
            throw new IllegalStateException("请勿在刷新状态设置属性");
        }
        super.setScrollEnable(scrollEnable);
    }

    /**
     * 设置DIF颜色
     */
    public void setDIFColor(int color) {
        mMACDDraw.setDIFColor(color);
    }

    /**
     * 设置DEA颜色
     */
    public void setDEAColor(int color) {
        mMACDDraw.setDEAColor(color);
    }

    /**
     * 设置MACD颜色
     */
    public void setMACDColor(int color) {
        mMACDDraw.setMACDColor(color);
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    public void setMACDWidth(float MACDWidth) {
        mMACDDraw.setMACDWidth(MACDWidth);
    }

    /**
     * 设置K颜色
     */
    public void setKColor(int color) {
        mKDJDraw.setKColor(color);
    }

    /**
     * 设置D颜色
     */
    public void setDColor(int color) {
        mKDJDraw.setDColor(color);
    }

    /**
     * 设置J颜色
     */
    public void setJColor(int color) {
        mKDJDraw.setJColor(color);
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        mVolumeDraw.setMa5Color(color);
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        mVolumeDraw.setMa10Color(color);
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    public void setSelectorTextSize(float textSize) {
        mMainDraw.setSelectorTextSize(textSize);
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    public void setSelectorBackgroundColor(int color) {
        mMainDraw.setSelectorBackgroundColor(color);
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mMainDraw.setCandleWidth(candleWidth);
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mMainDraw.setCandleLineWidth(candleLineWidth);
    }

    /**
     * 蜡烛是否空心
     */
    public void setCandleSolid(boolean candleSolid) {
        mMainDraw.setCandleSolid(candleSolid);
    }

    /**
     * 设置分时线的圆角弧度
     *
     * @param radius
     */
    public void setTimeLinePathRadius(float radius) {
        mMainDraw.setTimeLinePathRadius(radius);
    }

    @Override
    public void setTextSize(float textSize) {
        super.setTextSize(textSize);
        mRSIDraw.setTextSize(textSize);
        mMACDDraw.setTextSize(textSize);
        mKDJDraw.setTextSize(textSize);
        mWRDraw.setTextSize(textSize);
        mOBVDraw.setTextSize(textSize);
        mROCDraw.setTextSize(textSize);
        mCCIDraw.setTextSize(textSize);
        mVolumeDraw.setTextSize(textSize);
        mStochRSIDraw.setTextSize(textSize);
        mTRIXDraw.setTextSize(textSize);
        mDMIDraw.setTextSize(textSize);
    }

    /**
     * 设置顶部ma字体大小
     *
     * @param textSize
     */
    private void setTopMaOrBollTextSize(float textSize) {
        mMainDraw.setTextSize(textSize);
    }

    @Override
    public void setLineWidth(float lineWidth) {
        super.setLineWidth(lineWidth);
        mMainDraw.setLineWidth(lineWidth);
        mMACDDraw.setLineWidth(lineWidth);
        mKDJDraw.setLineWidth(lineWidth);
        mVolumeDraw.setLineWidth(lineWidth);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mMainDraw.setSelectorTextColor(color);
    }

    /**
     * 设置加载更多监听
     */
    public void setLoadMoreListener(KChartRefreshListener loadMoreListener) {
        this.mLoadMoreListener = loadMoreListener;
    }

    public void setSelectorListener(KLineChartView.KChartKLineSelectorListener selectorListener) {
        if (mMainDraw != null) {
            mMainDraw.setSelectorListener(selectorListener);
        }
    }

    public void setKChartSelectorStyle(String style) {
        if (mMainDraw != null) {
            mMainDraw.setSelectorStyle(style);
        }
    }

    private void setMainDrawLine(boolean isLine) {
        mMainDraw.setLine(isLine);
        invalidate();
    }

    private int startX;
    private int startY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dX = (int) (ev.getX() - startX);
                int dY = (int) (ev.getY() - startX);
                if (Math.abs(dX) > Math.abs(dY)) {
                    //左右滑动
                    return true;
                } else {
                    //上下滑动
                    return false;
                }
            case MotionEvent.ACTION_UP:
                break;
            default:
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 如果触摸事件在信息栏中的订单数据范围内，则只响应订单数据点击事件
        if (mMainDraw.isEventInOrderInfo((int) ev.getX(), (int) ev.getY())) {
            if (MotionEvent.ACTION_UP == ev.getAction()) {
                mMainDraw.onOrderInfoClick();
                resetSelectedPoint();
            }
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (!isRefreshing) {
            super.onLongPress(e);
        }
    }

}
