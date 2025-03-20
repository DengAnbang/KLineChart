package com.github.fujianlian.klinechart.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.BaseKLineChartView;
import com.github.fujianlian.klinechart.KChartConstant;
import com.github.fujianlian.klinechart.R;
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.entity.IMACD;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * macd实现类
 * Created by tifezh on 2016/6/19.
 */

public class MACDDraw implements IChartDraw<IMACD> {

    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDIFPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDEAPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMACDPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private IndicatorLine mDEA = new IndicatorLine();
    private IndicatorLine mDIF = new IndicatorLine();

    /**
     * macd 中柱子的宽度
     */
    private float mMACDWidth = 0;

    public MACDDraw(BaseKLineChartView view) {
        Context context = view.getContext();
        mRedPaint.setColor(ThemeUtils.getThemeColor(context, R.attr.upColor));
        mGreenPaint.setColor(ThemeUtils.getThemeColor(context, R.attr.downColor));

        mDEA.setWidth(1);
        mDEA.setColor(ColorUtil.getMAColor(1));
        mDIF.setWidth(1);
        mDIF.setColor(ColorUtil.getMAColor(0));
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mMACDPaint.setTypeface(customTypeface);
            mDEAPaint.setTypeface(customTypeface);
            mDIFPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IMACD lastPoint, @NonNull IMACD curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        drawMACD(canvas, view, curX, curPoint.getMacd());
    }

    @Override
    public void drawIndicatorLine(@NonNull IMACD point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            mDEA.setFirst(true);
            mDIF.setFirst(true);
            mDEA.getPath().reset();
            mDIF.getPath().reset();
        }
        connPath(mDEA, x, point.getDea(), view);
        connPath(mDIF, x, point.getDif(), view);
        if (isLast) {
            view.drawIndicatorLine(canvas, mDEA);
            view.drawIndicatorLine(canvas, mDIF);
        }
    }

    private void connPath(IndicatorLine line, float x, float value, BaseKLineChartView view) {
        if (line.isFirst()) {
            line.getPath().moveTo(x, view.getChildY(KChartConstant.Sub.MACD, value));
            line.setFirst(false);
        } else {
            line.getPath().lineTo(x, view.getChildY(KChartConstant.Sub.MACD, value));
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
    public void drawText(@NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, float x, float y) {
        IMACD point = (IMACD) view.getItem(position);

        List<Paint> paints = new ArrayList<>();
        paints.add(view.getTextPaint());
        paints.add(mMACDPaint);
        paints.add(mDIFPaint);
        paints.add(mDEAPaint);

        List<String> values = new ArrayList<>();
        values.add(point.getMacdText());
        values.add("MACD:" + view.formatValue(point.getMacd()));
        values.add("DIF:" + view.formatValue(point.getDif()));
        values.add("DEA:" + view.formatValue(point.getDea()));

        for (int i = 0; i < values.size(); i++) {
            Paint paint = paints.get(i);
            String text = values.get(i);
            canvas.drawText(text, x, y, paint);
            x += paint.measureText(text) + view.getTopMaOrBullSpaceValue();

            //检查下一个文本的宽度是否超过屏幕宽度，超过就需要换行
            if (i + 1 < values.size()) {
                int width = view.getChartWidth();
                if (x + paint.measureText(values.get(i + 1)) > width) {
                    x = view.dip2px(4);
                    y += view.getValueTextHeight(mMACDPaint);
                }
            }
        }
    }

    @Override
    public float getMaxValue(IMACD point) {
        return Math.max(point.getMacd(), Math.max(point.getDea(), point.getDif()));
    }

    @Override
    public float getMinValue(IMACD point) {
        return Math.min(point.getMacd(), Math.min(point.getDea(), point.getDif()));
    }

    @Override
    public IValueFormatter getValueFormatter() {
        return new ValueFormatter();
    }

    /**
     * 画macd
     *
     * @param canvas
     * @param x
     * @param macd
     */
    private void drawMACD(Canvas canvas, BaseKLineChartView view, float x, float macd) {
        String name = KChartConstant.Sub.MACD;
        float macdy = view.getChildY(name, macd);
        float r = mMACDWidth / 2;
        float zeroy = view.getChildY(name, 0);
        if (macd > 0) {
            //               left   top   right  bottom
            canvas.drawRect(x - r, macdy, x + r, zeroy, mRedPaint);
        } else {
            canvas.drawRect(x - r, zeroy, x + r, macdy, mGreenPaint);
        }
    }

    /**
     * 设置DIF颜色
     */
    public void setDIFColor(int color) {
        this.mDIFPaint.setColor(color);
    }

    /**
     * 设置DEA颜色
     */
    public void setDEAColor(int color) {
        this.mDEAPaint.setColor(color);
    }

    /**
     * 设置MACD颜色
     */
    public void setMACDColor(int color) {
        this.mMACDPaint.setColor(color);
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    public void setMACDWidth(float MACDWidth) {
        mMACDWidth = MACDWidth;
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width) {
        mDEAPaint.setStrokeWidth(width);
        mDIFPaint.setStrokeWidth(width);
        mMACDPaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mDEAPaint.setTextSize(textSize);
        mDIFPaint.setTextSize(textSize);
        mMACDPaint.setTextSize(textSize);
    }
}
