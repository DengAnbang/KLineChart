package com.github.fujianlian.klinechart.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.BaseKLineChartView;
import com.github.fujianlian.klinechart.KChartConstant;
import com.github.fujianlian.klinechart.KLineChartView;
import com.github.fujianlian.klinechart.KLineEntity;
import com.github.fujianlian.klinechart.MarkItemType;
import com.github.fujianlian.klinechart.R;
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.entity.HistorySignEntity;
import com.github.fujianlian.klinechart.entity.ICandle;
import com.github.fujianlian.klinechart.entity.IKLine;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.KLineSelectorEntity;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.entity.TimeQuantum;
import com.github.fujianlian.klinechart.formatter.BigValueFormatter;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.formatter.VolumeValueFormatter;
import com.github.fujianlian.klinechart.utils.KChartListUtils;

import com.github.fujianlian.klinechart.utils.StringUtils;
import com.github.fujianlian.klinechart.utils.ThemeUtils;
import com.github.fujianlian.klinechart.utils.UiUtils;
import com.github.fujianlian.klinechart.utils.ViewUtil;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 主图的实现类
 * Created by tifezh on 2016/6/14.
 */
public class MainDraw implements IChartDraw<ICandle> {
    private static final String SELECTOR_TEXT_SEPARATOR = "_\u3000";
    private static final String CHANGE_RATE_UNIT = "%";
    private static final String CHANGE_RATE_UP = "+";
    private static final String CHANGE_RATE_DOWN = "-";
    private static final float DEFAULT_PATH_RADIUS = 50f;
    private static final float SAR_RADIUS = 2f;
    private static final float SAR_STROKE_WIDTH = 1f;

    private float mCandleWidth = 0;
    private float multipleCandleWidth = 1;//默认一倍
    private float mCandleLineWidth = 0;
    private float mTimeLinePathRadius = DEFAULT_PATH_RADIUS;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBreathPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBreathHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint maPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint dottedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]

    private Paint mSelectorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int upColor;
    private int downColor;
    private int liqColor;
    private int colorGray2;
    private int mainTextColor;

    private Context mContext;

    private boolean mCandleSolid = true;
    // 是否分时
    private boolean isLine = false;
    private List<String> mSelectedIndicators;
    private KLineChartView kChartView;
    private Path mPath = new Path();
    private String selectorStyle = "POPUP";
    private KLineChartView.KChartKLineSelectorListener selectorListener;

    //selector背景
    private RectF mSelectorRectF = new RectF();

    private boolean isRtlLocale = UiUtils.isRtl();

    // 记录信息栏数据
    private final Map<String, String> selectorInfoMap = new LinkedHashMap<>();

    // 记录信息栏中历史订单的Rect
    private final HashMap<String, Rect> mOrderHistoryRectMap = new HashMap<>();

    private final Map<String, IndicatorLine> mIndicatorLines = new HashMap<>();

    private final List<MAIndicator> mSarModels = new ArrayList<>();

    private Paint mSARPaint;
    private int mSARUpColor;
    private int mSARDownColor;
    private final float mMarkViewMargin;
    private final float mMarkViewPadding;

    public MainDraw(KLineChartView view) {
        Context context = view.getContext();
        kChartView = view;
        mContext = context;
        upColor = ThemeUtils.getThemeColor(context, R.attr.upColor);
        downColor = ThemeUtils.getThemeColor(context, R.attr.downColor);
        liqColor = ThemeUtils.getThemeColor(context, R.attr.tintsOrangeBase);
        colorGray2 = ThemeUtils.getThemeColor(view.getContext(), R.attr.textSecondary);
        mainTextColor = ThemeUtils.getThemeColor(kChartView.getContext(), R.attr.textTitle);
        mSARUpColor = ThemeUtils.getThemeColor(context, R.attr.tintsRedBase);
        mSARDownColor = ThemeUtils.getThemeColor(context, R.attr.tintsBlueBase);
        mRedPaint.setColor(upColor);
        mGreenPaint.setColor(downColor);
        mLinePaint.setColor(ThemeUtils.getThemeColor(context, R.attr.chart_line));
        mLinePaint.setPathEffect(new CornerPathEffect(DEFAULT_PATH_RADIUS));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mBreathPointPaint.setColor(Color.WHITE);
        mBreathPointPaint.setStyle(Paint.Style.FILL);
        mBreathHaloPaint.setStyle(Paint.Style.FILL);
        paint.setColor(ThemeUtils.getThemeColor(context, R.attr.chart_line_background));
        paint.setPathEffect(new CornerPathEffect(mTimeLinePathRadius));

        float dashWidth = ViewUtil.dp2px(mContext, 4);
        float dashGap = ViewUtil.dp2px(mContext, 4);

        dottedLinePaint.setAntiAlias(true);
        dottedLinePaint.setColor(ThemeUtils.getThemeColor(context, R.attr.primary));
        dottedLinePaint.setStyle(Paint.Style.STROKE);
        dottedLinePaint.setStrokeWidth(ViewUtil.dp2px(mContext, 0.5f));
        dottedLinePaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));

        mMarkViewMargin = view.getResources().getDimension(R.dimen.dp_4);
        mMarkViewPadding = mMarkViewMargin;
    }

    public void setSelectorListener(KLineChartView.KChartKLineSelectorListener selectorListener) {
        this.selectorListener = selectorListener;
    }

    public void setSelectedIndicators(List<String> indicators) {
        this.mSelectedIndicators = indicators;
    }

    public boolean isEmptyIndicators() {
        return mSelectedIndicators == null || mSelectedIndicators.isEmpty();
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mSelectorTextPaint.setTypeface(customTypeface);
            maPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable ICandle lastPoint, @NonNull ICandle curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        if (isLine) {
            return;
        }
        if (isNewest) {
            // 如果是最后一个蜡烛，就用动画的值来绘制
            drawCandle(view, canvas, curX, curPoint.getHighPrice(), curPoint.getLowPrice(), curPoint.getOpenPrice(), animationPrice);
        } else {
            drawCandle(view, canvas, curX, curPoint.getHighPrice(), curPoint.getLowPrice(), curPoint.getOpenPrice(), curPoint.getClosePrice());
        }
    }

    @Override
    public void drawIndicatorLine(@NonNull ICandle point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isLine || isEmptyIndicators()) {
            return;
        }

        if (isFirst) {
            for (Map.Entry<String, IndicatorLine> line : mIndicatorLines.entrySet()) {
                line.getValue().setFirst(true);
                line.getValue().getPath().reset();
            }
        }

        for (String name : mSelectedIndicators) {
            switch (name) {
                case KChartConstant.Main.MA:
                    drawMALine(KChartConstant.Main.MA, point.getMa(), canvas, view, x, isFirst, isLast);
                    break;
                case KChartConstant.Main.EMA:
                    drawMALine(KChartConstant.Main.EMA, point.getEma(), canvas, view, x, isFirst, isLast);
                    break;
                case KChartConstant.Main.BOLL:
                    drawMALine(KChartConstant.Main.BOLL, point.getBoll(), canvas, view, x, isFirst, isLast);
                    break;
                case KChartConstant.Main.SAR:
                    drawSAR(point, canvas, view, x);
                    break;
                default:
                    break;
            }
        }
    }

    private void drawMALine(String name, List<MAIndicator> maList, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (KChartListUtils.isEmpty(maList)) {
            return;
        }
        for (int i = 0; i < maList.size(); i++) {
            MAIndicator ma = maList.get(i);
            IndicatorLine line = getCacheIndicatorLine(i, name);
            if (ma.getCalcResult() == 0) {
                continue;
            }
            float y = view.getMainY((float) ma.getCalcResult());
            if (line.isFirst()) {
                line.getPath().moveTo(x, y);
                line.setFirst(false);
                continue;
            }
            line.getPath().lineTo(x, y);
            if (isLast) {
                line.setColor(ma.getColor());
                line.setWidth(ma.getPaintWidth());
                view.drawIndicatorLine(canvas, line);
            }
        }
    }

    private void drawSAR(@NonNull ICandle point, Canvas canvas, BaseKLineChartView view, float x) {
        MAIndicator sar = point.getSar();
        if (sar == null) {
            return;
        }
        float y = view.getMainY((float) sar.getCalcResult());
        Paint sarPaint = getSARPaint();
        if (sar.getCalcResult() > point.getClosePrice()) {
            sarPaint.setColor(mSARUpColor);
        } else {
            sarPaint.setColor(mSARDownColor);
        }
        canvas.drawCircle(x, y, view.dip2px(SAR_RADIUS), sarPaint);
    }

    private Paint getSARPaint() {
        if (mSARPaint == null) {
            mSARPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSARPaint.setStyle(Paint.Style.STROKE);
            mSARPaint.setStrokeWidth(kChartView.dip2px(SAR_STROKE_WIDTH));
        }
        return mSARPaint;
    }

    @NonNull
    private IndicatorLine getCacheIndicatorLine(int index, String name) {
        String key = name + KChartConstant.CACHE_INDICATOR_LINE_KEY_SPLICE + index;
        IndicatorLine line = mIndicatorLines.get(key);
        if (line == null) {
            line = new IndicatorLine();
            mIndicatorLines.put(key, line);
        }
        return line;
    }

    /**
     * 绘制分时线及其区域
     *
     * @param view
     * @param canvas
     * @param path
     * @param startX
     * @param curX
     */
    @Override
    public void drawTimeLine(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, Path path, float startX, float curX) {
        if (isLine) {
            view.drawTimeLine(canvas, mLinePaint, path);
            view.drawTimeLineArea(canvas, paint, path, startX, curX);
        }
    }

    @Override
    public void drawBreathingLight(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, float stopX, float stopValue, float haloRadiusRatio) {
        if (isLine) {
            view.drawBreathingLightPoint(canvas, mBreathPointPaint, mBreathHaloPaint, stopX, stopValue, haloRadiusRatio);
        }
    }

    /**
     * 画一条虚线指向最近成交价
     *
     * @param lastPoint
     * @param lastX
     * @param canvas
     * @param view
     */
    public void drawLastPriceLine(@Nullable ICandle lastPoint, float lastX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float mWidth) {
        try {
            assert lastPoint != null;
            mPath.reset();
            mPath.moveTo(lastX + mCandleWidth / 2, view.getMainY(lastPoint.getClosePrice()));
            mPath.lineTo(mWidth, view.getMainY(lastPoint.getClosePrice()));
            canvas.drawPath(mPath, dottedLinePaint);
            //canvas.drawLine(lastX+mCandleWidth/2, view.getMainY(lastPoint.getClosePrice()), lastX+ViewUtil.Dp2Px(mContext,30), view.getMainY(lastPoint.getClosePrice()), dottedLinePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 画一条虚线指向最近成交价(供动画使用)
     *
     * @param canvas
     * @param lastX
     * @param animationClosePrice
     * @param view
     * @param mWidth
     */
    public void drawLastPriceLine(@NonNull Canvas canvas, float lastX, float animationClosePrice, @NonNull BaseKLineChartView view, float mWidth) {
        try {
            mPath.reset();
            mPath.moveTo(lastX + mCandleWidth / 2, view.getMainY(animationClosePrice));
            mPath.lineTo(mWidth, view.getMainY(animationClosePrice));
            canvas.drawPath(mPath, dottedLinePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawLastPriceLineFull(@NonNull Canvas canvas, float lastX, float y, @NonNull BaseKLineChartView view, float mWidth) {
        try {
            mPath.reset();
            mPath.moveTo(lastX + mCandleWidth / 2, y);
            mPath.lineTo(mWidth, y);
            canvas.drawPath(mPath, dottedLinePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawText(@NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, float x, float y) {
        if (!isLine && !isEmptyIndicators()) {
            ICandle point = (IKLine) view.getItem(position);
            y = y - 5;
            for (String name : mSelectedIndicators) {
                switch (name) {
                    case KChartConstant.Main.MA:
                        y = drawMAText(point.getMa(), canvas, view, x, y, KChartConstant.Main.MA);
                        break;
                    case KChartConstant.Main.EMA:
                        y = drawMAText(point.getEma(), canvas, view, x, y, KChartConstant.Main.EMA);
                        break;
                    case KChartConstant.Main.BOLL:
                        y = drawMAText(point.getBoll(), canvas, view, x, y, KChartConstant.Main.BOLL);
                        break;
                    case KChartConstant.Main.SAR:
                        mSarModels.clear();
                        mSarModels.add(point.getSar());
                        y = drawMAText(mSarModels, canvas, view, x, y, KChartConstant.Main.SAR);
                        break;
                    default:
                        break;
                }
            }
        }

        if (view.isShowSelector()) {
            if (KChartConstant.SelectorStyle.NO_POPUP.equals(selectorStyle)) {
                return;
            }
            if (KChartConstant.SelectorStyle.TOP_POPUP.equals(selectorStyle)) {
                notifySelectorListener(view);
            } else {
                drawSelector(view, canvas);
            }
        } else {
            if (selectorListener != null) {
                selectorListener.closeSelector();
            }
            // 不显示信息栏时清空历史订单的Rect，防止误报点击事件
            mOrderHistoryRectMap.clear();
        }
    }

    private float drawMAText(List<MAIndicator> ma, Canvas canvas,
                             BaseKLineChartView view, float x, float y, String prefix) {
        if (ma == null || ma.size() == 0) {
            return y;
        }

        List<String> values = new ArrayList<>();
        for (int i = 0; i < ma.size(); i++) {
            MAIndicator item = ma.get(i);
            String text = (!TextUtils.isEmpty(item.getPrefix()) ? item.getPrefix() : prefix)
                    + item.getValue() + KChartConstant.TEXT_VALUE_SPLICE;
            if (item.getCalcResult() == 0) {
                text += KChartConstant.TEXT_EMPTY_STATUS;
            } else {
                text += view.formatValueD(item.getCalcResult());
            }
            values.add(text);
        }

        for (int i = 0; i < values.size(); i++) {
            String text = values.get(i);
            maPaint.setColor(Color.parseColor(ma.get(i).getColor()));
            canvas.drawText(text, x, y, maPaint);
            x += maPaint.measureText(text) + view.getTopMaOrBullSpaceValue();

            //检查下一个文本的宽度是否超过屏幕宽度，超过就需要换行
            if (i + 1 < values.size()) {
                int width = view.getChartWidth();
                if (x + maPaint.measureText(values.get(i + 1)) > width) {
                    x = view.dip2px(4);
                    y += view.getValueTextHeight(maPaint);
                }
            }
        }
        return y + view.getValueTextHeight(maPaint);
    }

    private void notifySelectorListener(BaseKLineChartView view) {
        int index = view.getSelectedIndex();
        ICandle iCandle = (ICandle) view.getItem(index);
        KLineSelectorEntity selectorEntity = new KLineSelectorEntity();
        selectorEntity.setOpen(view.formatValueD(iCandle.getOpenPriceD()));
        selectorEntity.setHigh(view.formatValueD(iCandle.getHighPriceD()));
        selectorEntity.setLow(view.formatValueD(iCandle.getLowPriceD()));
        selectorEntity.setClose(view.formatValueD(iCandle.getClosePriceD()));

        // 添加历史订单数据
        if (kChartView.getBuyOrderInfo() != null) {
            selectorEntity.setBuyOrder(kChartView.getBuyOrderInfo());
        }
        if (kChartView.getSellOrderInfo() != null) {
            selectorEntity.setSellOrder(kChartView.getSellOrderInfo());
        }
        if (kChartView.getLiqOrderInfo() != null) {
            selectorEntity.setLiqOrder(kChartView.getLiqOrderInfo());
        }

        if (view.isFutures()) {
            String[] changeValues = getChangeValues(view, iCandle, index);
            selectorEntity.setChange(changeValues[0]);
            selectorEntity.setChangeRate(changeValues[1]);
            selectorEntity.setRange(getRangeValue(iCandle));
        } else {
            if (iCandle.getOpenPrice() != 0) {
                String changeRate = get2XXDa((iCandle.getClosePrice() - iCandle.getOpenPrice()) / iCandle.getOpenPrice() * 100);
                changeRate = changeRate + "%";
//                changeRate = PercentFormatUtil.addPercentSign(DecimalFormatTool.getFormattedDecimalV2(changeRate));
                selectorEntity.setChangeRate(changeRate);
            } else {
                selectorEntity.setChangeRate(KChartConstant.TEXT_EMPTY_STATUS);
            }
            selectorEntity.setChange(view.formatValueD(iCandle.getClosePriceD() - iCandle.getOpenPriceD()));
        }

        if (view.getValueFormatter() instanceof VolumeValueFormatter) {
            VolumeValueFormatter volumeValueFormatter = (VolumeValueFormatter) view.getValueFormatter();
            if (volumeValueFormatter != null) {
                selectorEntity.setVol(volumeValueFormatter.formatVolumeD(iCandle.getVolumeD()));
            }
        } else {
            selectorEntity.setVol(new BigValueFormatter().formatD(iCandle.getVolumeD()));
        }
        selectorEntity.setTime(getDisplayDate(index, iCandle));
        selectorEntity.setUp(iCandle.getClosePrice() - iCandle.getOpenPrice() > 0);
        if (view.isFutures()) {
            String amount = new BigValueFormatter().formatD(iCandle.getAmountD());
            selectorEntity.setAmount(amount);
        }
        selectorEntity.setFutures(view.isFutures());
        if (selectorListener != null) {
            selectorListener.showSelector(selectorEntity);
        }
    }

    private String getDisplayDate(int index, ICandle iCandle) {
        String timeFormat = "yyyy.MM.dd HH:mm";
        if (TimeQuantum.SECOND1.getInterval().equals(kChartView.getInterval())) {
            timeFormat = "yyyy.MM.dd HH:mm:ss";
        }
        return KLineEntity.timeStamp2Date(String.valueOf(iCandle.getLongDate()), timeFormat);
    }

    @Override
    public float getMaxValue(ICandle point) {
        if (isLine || isEmptyIndicators()) {
            return point.getHighPrice();
        }
        float max = point.getHighPrice();
        for (String name : mSelectedIndicators) {
            switch (name) {
                case KChartConstant.Main.MA:
                    max = Math.max(max, getMAMaxValue(max, point.getMa()));
                    break;
                case KChartConstant.Main.EMA:
                    max = Math.max(max, getMAMaxValue(max, point.getEma()));
                    break;
                case KChartConstant.Main.BOLL:
                    max = Math.max(max, getMAMaxValue(max, point.getBoll()));
                    break;
                case KChartConstant.Main.SAR:
                    max = Math.max(max, getSARMaxValue(point));
                    break;
                default:
                    break;
            }
        }
        return max;
    }

    private float getMAMaxValue(float max, List<MAIndicator> ma) {
        if (ma == null || ma.isEmpty()) {
            return max;
        }
        for (MAIndicator item : ma) {
            max = (float) Math.max(max, item.getCalcResult());
        }
        return max;
    }

    private float getSARMaxValue(ICandle point) {
        float max = point.getHighPrice();
        MAIndicator sar = point.getSar();
        if (sar == null) {
            return max;
        }
        return (float) Math.max(max, sar.getCalcResult());
    }

    @Override
    public float getMinValue(ICandle point) {
        if (isLine || isEmptyIndicators()) {
            return point.getLowPrice();
        }
        float min = point.getLowPrice();
        for (String name : mSelectedIndicators) {
            switch (name) {
                case KChartConstant.Main.MA:
                    min = Math.min(min, getMAMinValue(min, point.getMa()));
                    break;
                case KChartConstant.Main.EMA:
                    min = Math.min(min, getMAMinValue(min, point.getEma()));
                    break;
                case KChartConstant.Main.BOLL:
                    min = Math.min(min, getMAMinValue(min, point.getBoll()));
                    break;
                case KChartConstant.Main.SAR:
                    min = Math.min(min, getSARMinValue(point));
                    break;
                default:
                    break;
            }
        }
        if (min == 0f) {
            return point.getLowPrice();
        }
        return min;
    }

    private float getMAMinValue(float min, List<MAIndicator> ma) {
        if (ma == null || ma.isEmpty()) {
            return min;
        }
        for (MAIndicator item : ma) {
            min = (float) Math.min(min, item.getCalcResult());
        }
        return min;
    }

    private float getSARMinValue(ICandle point) {
        float min = point.getLowPrice();
        MAIndicator sar = point.getSar();
        if (sar == null) {
            return min;
        }
        min = (float) Math.min(min, sar.getCalcResult());
        return min;
    }

    @Override
    public IValueFormatter getValueFormatter() {
        return new ValueFormatter();
    }

    /**
     * 画Candle
     *
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    public void drawCandle(BaseKLineChartView view, Canvas canvas, float x, float high, float low, float open, float close) {
        high = view.getMainY(high);
        low = view.getMainY(low);
        open = view.getMainY(open);
        close = view.getMainY(close);
        float r = mCandleWidth / 2;
        float lineR = mCandleLineWidth / 2;
        if (open > close) {
            //实心
            if (mCandleSolid) {
                if (Math.abs(open - close) < ViewUtil.dp2px(mContext, 0.8f)) {
                    canvas.drawRect(x - r, close, x + r, close + ViewUtil.dp2px(mContext, 0.8f), mRedPaint);
                } else {
                    canvas.drawRect(x - r, close, x + r, open, mRedPaint);
                }
                canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
            } else {
                mRedPaint.setStrokeWidth(mCandleLineWidth);
                canvas.drawLine(x, high, x, close, mRedPaint);
                canvas.drawLine(x, open, x, low, mRedPaint);
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, mRedPaint);
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, mRedPaint);
                mRedPaint.setStrokeWidth(mCandleLineWidth * view.getScaleX());
                canvas.drawLine(x - r, open, x + r, open, mRedPaint);
                canvas.drawLine(x - r, close, x + r, close, mRedPaint);
            }
        } else if (open < close) {
            if (Math.abs(close - open) < ViewUtil.dp2px(mContext, 0.8f)) {
                canvas.drawRect(x - r, open, x + r, open + ViewUtil.dp2px(mContext, 0.8f), mGreenPaint);
            } else {
                canvas.drawRect(x - r, open, x + r, close, mGreenPaint);
            }
            canvas.drawRect(x - lineR, high, x + lineR, low, mGreenPaint);
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, mRedPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
        }
    }

    private void drawSelector(BaseKLineChartView view, Canvas canvas) {
        Paint.FontMetrics metrics = mSelectorTextPaint.getFontMetrics();
        float textHeight = metrics.descent - metrics.ascent;
        int index = view.getSelectedIndex();
        float padding = mMarkViewPadding;
        float margin = mMarkViewMargin;
        float textSpace = padding / 2;
        float width = 0;
        float left;
        float top = margin + view.getTopPadding();

        //通过文本的最大宽度得到selector的宽度
        Map<String, String> map = getSelectorMap(view, index);
        float height = textSpace * (map.size() - 1) + textHeight * map.size() + padding * 2;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String text = view.getLocalValues().getText(entry.getKey()) + SELECTOR_TEXT_SEPARATOR + entry.getValue();
            width = Math.max(width, mSelectorTextPaint.measureText(text));
        }
        width += padding * 2;

        //根据选中的点，判断selector绘制在左边还是右边
        float x = view.translateXtoX(view.getX(index));
        if (x > view.getChartWidth() / 2F) {
            left = margin;
        } else {
            left = view.getChartWidth() - width - margin;
        }

        //绘制selector的背景
        mSelectorRectF.set(left, top, left + width, top + height);
        canvas.drawRoundRect(mSelectorRectF, ViewUtil.dp2px(mContext, 4), ViewUtil.dp2px(mContext, 4), mSelectorBackgroundPaint);

        boolean hasBuyHistoryInfo = false;
        boolean hasSellHistoryInfo = false;
        boolean hasLiqInfo = false;
        float y = top + padding + (textHeight - metrics.bottom - metrics.top) / 2;

        //绘制selector的文本
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (MarkItemType.BUY_ORDER_INFO.equals(key)) {
                // 绘制买入历史订单
                drawSelectorHistoryInfo(canvas, left, y, padding, textSpace, textHeight, width, key);
                y += textHeight + textSpace;
                hasBuyHistoryInfo = true;
                continue;
            } else if (MarkItemType.SELL_ORDER_INFO.equals(key)) {
                // 绘制卖出历史订单
                drawSelectorHistoryInfo(canvas, left, y, padding, textSpace, textHeight, width, key);
                y += textHeight + textSpace;
                hasSellHistoryInfo = true;
                continue;
            } else if (MarkItemType.LIQUIDATION_ORDER_INFO.equals(key)) {
                // 绘制强平订单
                drawSelectorHistoryInfo(canvas, left, y, padding, textSpace, textHeight, width, key);
                y += textHeight + textSpace;
                hasLiqInfo = true;
                continue;
            }

            mSelectorTextPaint.setColor(colorGray2);
            canvas.drawText(view.getLocalValues().getText(key), left + padding, y, mSelectorTextPaint);

            if (value.startsWith(CHANGE_RATE_DOWN)) {
                mSelectorTextPaint.setColor(downColor);
            } else if (value.startsWith(CHANGE_RATE_UP)) {
                mSelectorTextPaint.setColor(upColor);
            } else {
                mSelectorTextPaint.setColor(mainTextColor);
            }

            float valueX = left + width - calculateWidth(value) - padding;
            canvas.drawText(value, valueX, y, mSelectorTextPaint);
            y += textHeight + textSpace;
        }

        // 如果没有绘制历史买入或卖出的信息，则清理对应的Rect
        if (!hasBuyHistoryInfo) {
            mOrderHistoryRectMap.put(MarkItemType.BUY_ORDER_INFO, null);
        }
        if (!hasSellHistoryInfo) {
            mOrderHistoryRectMap.put(MarkItemType.SELL_ORDER_INFO, null);
        }
        if (!hasLiqInfo) {
            mOrderHistoryRectMap.put(MarkItemType.LIQUIDATION_ORDER_INFO, null);
        }
    }

    /**
     * 绘制历史买入或卖出
     */
    private void drawSelectorHistoryInfo(Canvas canvas, float left, float y, float padding, float textSpace,
                                         float textHeight, float width, String itemType) {
        HistorySignEntity entity = null;
        if (MarkItemType.LIQUIDATION_ORDER_INFO.equals(itemType)) {
            entity = kChartView.getLiqOrderInfo();
            mSelectorTextPaint.setColor(liqColor);
        } else if (MarkItemType.BUY_ORDER_INFO.equals(itemType)) {
            entity = kChartView.getBuyOrderInfo();
            mSelectorTextPaint.setColor(upColor);
        } else if (MarkItemType.SELL_ORDER_INFO.equals(itemType)) {
            entity = kChartView.getSellOrderInfo();
            mSelectorTextPaint.setColor(downColor);
        }
        if (entity == null) {
            return;
        }

        canvas.drawText(entity.getPrefix(), left + padding, y, mSelectorTextPaint);

        mSelectorTextPaint.setColor(ThemeUtils.getThemeColor(mContext, R.attr.iconSecondary));
        mSelectorTextPaint.setFakeBoldText(true);
        mSelectorTextPaint.setTextScaleX(0.5f);
        String value = ">"; // R.drawable.ic_vector_view_more5
        float valueX = left + width - calculateWidth(value) - padding;
        canvas.drawText(value, valueX, y, mSelectorTextPaint);
        mSelectorTextPaint.setFakeBoldText(false);
        mSelectorTextPaint.setTextScaleX(1f);

        mSelectorTextPaint.setColor(mainTextColor);
        String key = entity.getDisplayInfo().replace(entity.getPrefix(), "");
        if (isRtlLocale) {
            key = StringUtils.forceLtr(key);
        }
        float keyWidth = mSelectorTextPaint.measureText(key);
        canvas.drawText(key, valueX - keyWidth - padding, y, mSelectorTextPaint);

        // 记录信息栏中历史订单的Rect，用来定位订单信息点击事件
        Rect orderRect = new Rect((int) (left), (int) (y - textHeight - textSpace / 2), (int) (left + width), (int) (y + textSpace / 2));
        mOrderHistoryRectMap.put(itemType, orderRect);
    }

    /**
     * 触摸事件是否在历史订单信息显示的范围内
     */
    public boolean isEventInOrderInfo(int x, int y) {
        Rect buyRect = mOrderHistoryRectMap.get(MarkItemType.BUY_ORDER_INFO);
        Rect sellRect = mOrderHistoryRectMap.get(MarkItemType.SELL_ORDER_INFO);
        return (buyRect != null && buyRect.contains(x, y))
                || (sellRect != null && sellRect.contains(x, y))
                || isEventInLiqOrderInfo(x, y);
    }

    public boolean isEventInLiqOrderInfo(int x, int y) {
        Rect rect = mOrderHistoryRectMap.get(MarkItemType.LIQUIDATION_ORDER_INFO);
        return rect != null && rect.contains(x, y);
    }

    private String[] getChangeValues(BaseKLineChartView view, ICandle point, int index) {
        String change = KChartConstant.TEXT_EMPTY_STATUS;
        String changeRate = KChartConstant.TEXT_EMPTY_STATUS;
        if (index > 0) {
            double preClose = ((ICandle) view.getItem(index - 1)).getClosePriceD();
            if (preClose != 0) {
                double closeChange = point.getClosePriceD() - preClose;
                if (closeChangeIsUp(closeChange)) {
                    change = CHANGE_RATE_UP + view.formatValueD(closeChange);
                } else {
                    change = view.formatValueD(closeChange);
                }
                if (closeChangeIsUp(closeChange)) {
                    changeRate = CHANGE_RATE_UP + calculateChangRate(closeChange, preClose);
                } else {
                    changeRate = calculateChangRate(closeChange, preClose);
                }
            }
        }
        return new String[]{change, changeRate};
    }

    /**
     * 获取振幅
     * 振幅 =（最高价-最低价）/ 开盘价
     *
     * @param point
     * @return
     */
    private String getRangeValue(ICandle point) {
        double result = (point.getHighPriceD() - point.getLowPriceD()) / point.getOpenPriceD();

        return result + "%";
//        return PercentFormatUtil.addPercentSign(DecimalFormatTool.getFormattedDecimalV2(get2XXDa(result * 100)));
    }

    private Map<String, String> getSelectorMap(BaseKLineChartView view, int index) {
        ICandle point = (ICandle) view.getItem(index);

        selectorInfoMap.clear();
        selectorInfoMap.put(MarkItemType.TIME, getDisplayDate(index, point));
        selectorInfoMap.put(MarkItemType.OPEN, view.formatValueD(point.getOpenPriceD()));
        selectorInfoMap.put(MarkItemType.HIGH, view.formatValueD(point.getHighPriceD()));
        selectorInfoMap.put(MarkItemType.LOW, view.formatValueD(point.getLowPriceD()));
        selectorInfoMap.put(MarkItemType.CLOSE, view.formatValueD(point.getClosePriceD()));

        String change;
        String changeRate;
        String range = "";
        //合约使用上一根k线的收盘价进行涨跌幅，涨跌额计算
        if (view.isFutures()) {
            String[] changeValues = getChangeValues(view, point, index);
            change = changeValues[0];
            changeRate = changeValues[1];

            range = getRangeValue(point);
        } else {
            //涨跌额
            double closeChange = point.getClosePriceD() - point.getOpenPriceD();
            if (closeChangeIsUp(closeChange)) {
                change = CHANGE_RATE_UP + view.formatValueD(closeChange);
            } else {
                change = view.formatValueD(closeChange);
            }

            //涨跌率
            changeRate = KChartConstant.TEXT_EMPTY_STATUS;
            if (point.getOpenPriceD() != 0) {
                if (closeChangeIsUp(closeChange)) {
                    changeRate = CHANGE_RATE_UP + calculateChangRate(closeChange, point.getOpenPrice());
                } else {
                    changeRate = calculateChangRate(closeChange, point.getOpenPrice());
                }
            }
        }

        //成交量
        String vol = KChartConstant.TEXT_EMPTY_STATUS;
        if (view.getValueFormatter() instanceof VolumeValueFormatter) {
            VolumeValueFormatter formatter = (VolumeValueFormatter) view.getValueFormatter();
            if (formatter != null) {
                vol = formatter.formatVolume(point.getVolume());
            }
        } else {
            vol = new BigValueFormatter().format(point.getVolume());
        }

        selectorInfoMap.put(MarkItemType.CHANGE, change);
        selectorInfoMap.put(MarkItemType.CHANGE_RATE, changeRate);

        if (view.isFutures()) {
            selectorInfoMap.put(MarkItemType.RANGE, range);
            String amount = new BigValueFormatter().formatD(point.getAmountD());
            selectorInfoMap.put(MarkItemType.AMOUNT, amount);
        }

        selectorInfoMap.put(MarkItemType.VOL, vol);

        // 添加历史订单数据
        if (kChartView.getBuyOrderInfo() != null) {
            selectorInfoMap.put(MarkItemType.BUY_ORDER_INFO, kChartView.getBuyOrderInfo().getDisplayInfo());
        }
        if (kChartView.getSellOrderInfo() != null) {
            selectorInfoMap.put(MarkItemType.SELL_ORDER_INFO, kChartView.getSellOrderInfo().getDisplayInfo());
        }
        if (kChartView.getLiqOrderInfo() != null) {
            selectorInfoMap.put(MarkItemType.LIQUIDATION_ORDER_INFO, kChartView.getLiqOrderInfo().getDisplayInfo());
        }
        return selectorInfoMap;
    }

    /**
     * 计算涨跌率
     *
     * @param closeChange
     * @param openPrice
     * @return
     */
    private String calculateChangRate(double closeChange, double openPrice) {

        String s = get2XXDa(closeChange / openPrice * 100);
        return s + "%";
//        return PercentFormatUtil.addPercentSign(DecimalFormatTool.getFormattedDecimalV2(s));
    }

    /**
     * 判断是否为上涨
     *
     * @param closeChange
     * @return
     */
    private boolean closeChangeIsUp(double closeChange) {
        return closeChange > 0;
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private int calculateWidth(String text) {
        Rect rect = new Rect();
        mSelectorTextPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.width() + 5;
    }

    public static String get2XXDa(double data) {
        BigDecimal bigDecimal = BigDecimal.valueOf(data);
        return bigDecimal
                .setScale(2, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mCandleWidth = candleWidth;

    }

    public float getCandleWidth() {
        return mCandleWidth;
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mCandleLineWidth = candleLineWidth;
    }

    /**
     * 设置选择器文字颜色
     *
     * @param color
     */
    public void setSelectorTextColor(int color) {
        mSelectorTextPaint.setColor(color);
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    public void setSelectorTextSize(float textSize) {
        mSelectorTextPaint.setTextSize(textSize);
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    public void setSelectorBackgroundColor(int color) {
        mSelectorBackgroundPaint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width) {
        maPaint.setStrokeWidth(width);
        mLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        maPaint.setTextSize(textSize);
    }

    /**
     * 蜡烛是否实心
     */
    public void setCandleSolid(boolean candleSolid) {
        mCandleSolid = candleSolid;
    }

    /**
     * 设置分时线拐角的弧度(值越大越圆润，值越小越尖锐)
     *
     * @param radius
     */
    public void setTimeLinePathRadius(float radius) {
        mTimeLinePathRadius = radius;
        mLinePaint.setPathEffect(new CornerPathEffect(mTimeLinePathRadius));
        paint.setPathEffect(new CornerPathEffect(mTimeLinePathRadius));
    }

    public void setLine(boolean line) {
        if (isLine != line) {
            isLine = line;
            if (isLine) {
                kChartView.setCandleWidth(kChartView.dp2px(7f * multipleCandleWidth));
            } else {
                kChartView.setCandleWidth(kChartView.dp2px(6f * multipleCandleWidth));
            }
        }
    }

    public float getMultipleCandleWidth() {
        return multipleCandleWidth;
    }

    public void setMultipleCandleWidth(float multipleCandleWidth) {
        this.multipleCandleWidth = multipleCandleWidth;
    }

    public boolean isLine() {
        return isLine;
    }

    public void setSelectorStyle(String style) {
        selectorStyle = style;
    }

    /**
     * 信息栏中的历史订单文字被点击
     */
    public void onOrderInfoClick() {
        if (selectorListener != null) {
            selectorListener.onOrderInfoClick();
        }
    }

    /**
     * 订单显示与隐藏按钮点击
     *
     * @param isShowOrderSign
     */
    public void onClickOrderHideShowButton(boolean isShowOrderSign) {
        if (selectorListener != null) {
            selectorListener.onClickOrderHideShow(isShowOrderSign);
        }
    }
}
