package com.github.fujianlian.klinechart.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.BaseKLineChartView;
import com.github.fujianlian.klinechart.R;
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.entity.IVolume;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.formatter.BigValueFormatter;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.ThemeUtils;
import com.github.fujianlian.klinechart.utils.ViewUtil;

/**
 * Created by hjm on 2017/11/14 17:49.
 */
public class VolumeDraw implements IChartDraw<IVolume> {

    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int pillarWidth = 0;
    private IndicatorLine mM5 = new IndicatorLine();
    private IndicatorLine mM10 = new IndicatorLine();

    public VolumeDraw(BaseKLineChartView view) {
        Context context = view.getContext();
        mRedPaint.setColor(ThemeUtils.getThemeColor(context, R.attr.upColor));
        mGreenPaint.setColor(ThemeUtils.getThemeColor(context, R.attr.downColor));
        pillarWidth = ViewUtil.dp2px(context, 6);
        mM5.setWidth(1);
        mM5.setColor(ColorUtil.getMAColor(0));
        mM10.setWidth(1);
        mM10.setColor(ColorUtil.getMAColor(1));
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            ma5Paint.setTypeface(customTypeface);
            ma10Paint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(
            @Nullable IVolume lastPoint, @NonNull IVolume curPoint, float lastX, float curX,
            @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        drawHistogram(canvas, curPoint, lastPoint, curX, view, position);
    }

    @Override
    public void drawIndicatorLine(@NonNull IVolume point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            mM5.setFirst(true);
            mM10.setFirst(true);
            mM5.getPath().reset();
            mM10.getPath().reset();
        }
        connPath(mM5, x, point.getMA5Volume(), view);
        connPath(mM10, x, point.getMA10Volume(), view);
        if (isLast) {
            view.drawIndicatorLine(canvas, mM5);
            view.drawIndicatorLine(canvas, mM10);
        }
    }

    private void connPath(IndicatorLine line, float x, float value, BaseKLineChartView view) {
        if (value != 0f) {
            if (line.isFirst()) {
                line.getPath().moveTo(x, view.getVolY(value));
                line.setFirst(false);
            } else {
                line.getPath().lineTo(x, view.getVolY(value));
            }
        }
    }

    @Override
    public void drawTimeLine(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, Path path, float startX, float curX) {
        // do nothing.
    }

    @Override
    public void drawBreathingLight(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, float stopX, float stopValue, float haloRadiusRatio) {
        // do nothing.
    }

    @Override
    public void drawText(
            @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, float x, float y) {
        IVolume point = (IVolume) view.getItem(position);
        String text = "VOL:" + view.getBigValueFormatter().format(point.getVolume());
        canvas.drawText(text, x, y, view.getTextPaint());
        x += view.getTextPaint().measureText(text) + view.getTopMaOrBullSpaceValue();
        text = "MA5:" + view.getBigValueFormatter().format(point.getMA5Volume());
        canvas.drawText(text, x, y, ma5Paint);
        x += ma5Paint.measureText(text) + view.getTopMaOrBullSpaceValue();
        text = "MA10:" + view.getBigValueFormatter().format(point.getMA10Volume());
        canvas.drawText(text, x, y, ma10Paint);
    }

    private void drawHistogram(
            Canvas canvas, IVolume curPoint, IVolume lastPoint, float curX,
            BaseKLineChartView view, int position) {

        float r = pillarWidth / 2;
        float top = view.getVolY(curPoint.getVolume());
        Rect volRect = view.getVolRect();
        if (volRect == null) {
            return;
        }
        float bottom = volRect.bottom;
        if (curPoint.getClosePrice() >= curPoint.getOpenPrice()) {//涨
            canvas.drawRect(curX - r, top, curX + r, bottom, mRedPaint);
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom, mGreenPaint);
        }

    }

    @Override
    public float getMaxValue(IVolume point) {
        return Math.max(point.getVolume(), Math.max(point.getMA5Volume(), point.getMA10Volume()));
    }

    @Override
    public float getMinValue(IVolume point) {
        return Math.min(point.getVolume(), Math.min(point.getMA5Volume(), point.getMA10Volume()));
    }

    @Override
    public IValueFormatter getValueFormatter() {
        return new BigValueFormatter();
    }

    /**
     * 设置 MA5 线的颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        this.ma5Paint.setColor(color);
    }

    /**
     * 设置 MA10 线的颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        this.ma10Paint.setColor(color);
    }

    public void setLineWidth(float width) {
        this.ma5Paint.setStrokeWidth(width);
        this.ma10Paint.setStrokeWidth(width);
    }

    /**
     * @param width
     */
    public void setCandleWidth(int width) {
        this.pillarWidth = width;
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        this.ma5Paint.setTextSize(textSize);
        this.ma10Paint.setTextSize(textSize);
    }

}
