package com.github.fujianlian.klinechart.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.BaseKLineChartView;
import com.github.fujianlian.klinechart.KChartConstant;
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.entity.IKDJ;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.ColorUtil;

/**
 * KDJ实现类
 * Created by tifezh on 2016/6/19.
 */

public class KDJDraw implements IChartDraw<IKDJ> {

    private Paint mKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mJPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private IndicatorLine mK = new IndicatorLine();
    private IndicatorLine mD = new IndicatorLine();
    private IndicatorLine mJ = new IndicatorLine();

    public KDJDraw(BaseKLineChartView view) {
        mK.setWidth(1);
        mD.setWidth(1);
        mJ.setWidth(1);
        mK.setColor(ColorUtil.getMAColor(0));
        mD.setColor(ColorUtil.getMAColor(1));
        mJ.setColor(ColorUtil.getMAColor(2));
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mKPaint.setTypeface(customTypeface);
            mDPaint.setTypeface(customTypeface);
            mJPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IKDJ lastPoint, @NonNull IKDJ curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        // do nothing.
    }

    @Override
    public void drawIndicatorLine(@NonNull IKDJ point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            mK.setFirst(true);
            mD.setFirst(true);
            mJ.setFirst(true);
            mK.getPath().reset();
            mD.getPath().reset();
            mJ.getPath().reset();
        }
        connPath(mK, x, point.getK(), view);
        connPath(mD, x, point.getD(), view);
        connPath(mJ, x, point.getJ(), view);
        if (isLast) {
            view.drawIndicatorLine(canvas, mK);
            view.drawIndicatorLine(canvas, mD);
            view.drawIndicatorLine(canvas, mJ);
        }
    }

    private void connPath(IndicatorLine line, float x, float value, BaseKLineChartView view) {
        if (value != 0) {
            if (line.isFirst()) {
                line.getPath().moveTo(x, view.getChildY(KChartConstant.Sub.KDJ, value));
                line.setFirst(false);
            } else {
                line.getPath().lineTo(x, view.getChildY(KChartConstant.Sub.KDJ, value));
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
    public void drawText(@NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, float x, float y) {
        IKDJ point = (IKDJ) view.getItem(position);
        if (point.getK() != 0) {
            String text = point.getKDJText();
            canvas.drawText(text, x, y, view.getTextPaint());
            x += view.getTextPaint().measureText(text) + view.getTopMaOrBullSpaceValue();
            text = "K:" + view.formatValue(point.getK());
            canvas.drawText(text, x, y, mKPaint);
            x += mKPaint.measureText(text) + view.getTopMaOrBullSpaceValue();
            if (point.getD() != 0) {
                text = "D:" + view.formatValue(point.getD());
                canvas.drawText(text, x, y, mDPaint);
                x += mDPaint.measureText(text) + view.getTopMaOrBullSpaceValue();
                text = "J:" + view.formatValue(point.getJ());
                canvas.drawText(text, x, y, mJPaint);
            }
        }
    }

    @Override
    public float getMaxValue(IKDJ point) {
        return Math.max(point.getK(), Math.max(point.getD(), point.getJ()));
    }

    @Override
    public float getMinValue(IKDJ point) {
        return Math.min(point.getK(), Math.min(point.getD(), point.getJ()));
    }

    @Override
    public IValueFormatter getValueFormatter() {
        return new ValueFormatter();
    }

    /**
     * 设置K颜色
     */
    public void setKColor(int color) {
        mKPaint.setColor(color);
    }

    /**
     * 设置D颜色
     */
    public void setDColor(int color) {
        mDPaint.setColor(color);
    }

    /**
     * 设置J颜色
     */
    public void setJColor(int color) {
        mJPaint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width) {
        mKPaint.setStrokeWidth(width);
        mDPaint.setStrokeWidth(width);
        mJPaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mKPaint.setTextSize(textSize);
        mDPaint.setTextSize(textSize);
        mJPaint.setTextSize(textSize);
    }
}
