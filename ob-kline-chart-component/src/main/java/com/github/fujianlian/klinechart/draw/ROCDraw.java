package com.github.fujianlian.klinechart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
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
import com.github.fujianlian.klinechart.entity.IROC;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.CalcParamsUtil;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.KChartListUtils;
import com.github.fujianlian.klinechart.utils.ThemeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OBV实现类
 * Created by tifezh on 2016/6/19.
 */
public class ROCDraw implements IChartDraw<IROC> {

    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, IndicatorLine> mIndicatorLines = new HashMap<>();
    private final int mCalcParamsTextColor;

    public ROCDraw(BaseKLineChartView view) {
        mCalcParamsTextColor = ThemeUtils.getThemeColor(view.getContext(), R.attr.textSecondary);
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mTextPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IROC lastPoint, @NonNull IROC curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        // do nothing.
    }

    @Override
    public void drawIndicatorLine(@NonNull IROC point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            for (Map.Entry<String, IndicatorLine> line : mIndicatorLines.entrySet()) {
                line.getValue().setFirst(true);
                line.getValue().getPath().reset();
            }
        }
        List<MAIndicator> items = point.getROC();
        if (KChartListUtils.isEmpty(point.getROC())) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            MAIndicator ma = items.get(i);
            if (ma.isNaValue()) {
                continue;
            }
            IndicatorLine line = getCacheIndicatorLine(i, KChartConstant.Sub.ROC);
            float y = view.getChildY(KChartConstant.Sub.ROC, (float) ma.getCalcResult());
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
        IROC point = (IROC) view.getItem(position);
        drawValueText(point.getROC(), canvas, view, x, y);
    }

    private void drawValueText(List<MAIndicator> items, Canvas canvas,
                               BaseKLineChartView view, float x, float y) {

        int[] rocPeriod = CalcParamsUtil.getROCPeriod();
        String displayText = CalcParamsUtil.getCalcParamsDisplayText(
                KChartConstant.Sub.ROC,
                String.valueOf(rocPeriod[0]),
                String.valueOf(rocPeriod[1])
        );
        mTextPaint.setColor(mCalcParamsTextColor);
        canvas.drawText(displayText, x, y, mTextPaint);
        x += mTextPaint.measureText(displayText) + view.getTopMaOrBullSpaceValue();

        if (KChartListUtils.isEmpty(items)) {
            return;
        }
        String text;
        for (int i = 0; i < items.size(); i++) {
            MAIndicator item = items.get(i);
            text = item.getValue()
                    + KChartConstant.TEXT_VALUE_SPLICE;
            if (item.isNaValue()) {
                text += KChartConstant.TEXT_EMPTY_STATUS;
            } else {
                text += view.formatValue((float) item.getCalcResult());
            }
            String maColor = ColorUtil.getMAColor(i);
            mTextPaint.setColor(Color.parseColor(maColor));
            canvas.drawText(text, x, y, mTextPaint);
            x += mTextPaint.measureText(text) + view.getTopMaOrBullSpaceValue();
        }
    }

    @Override
    public float getMaxValue(IROC point) {
        List<MAIndicator> items = point.getROC();
        if (KChartListUtils.isEmpty(items)) {
            return 0f;
        }
        float max = Float.NEGATIVE_INFINITY;
        for (MAIndicator item : items) {
            max = Math.max(max, (float) item.getCalcResult());
        }
        return max;
    }

    @Override
    public float getMinValue(IROC point) {
        List<MAIndicator> items = point.getROC();
        if (KChartListUtils.isEmpty(items)) {
            return 0f;
        }
        float min = Float.POSITIVE_INFINITY;
        for (MAIndicator item : items) {
            min = Math.min(min, (float) item.getCalcResult());
        }
        return min;
    }

    @Override
    public IValueFormatter getValueFormatter() {
        return new ValueFormatter();
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }
}
