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
import com.github.fujianlian.klinechart.base.IChartDraw;
import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.entity.IStochRSI;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.KChartListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StochRSI实现类
 * Created by tifezh on 2016/6/19.
 */
public class StochRSIDraw implements IChartDraw<IStochRSI> {

    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, IndicatorLine> mIndicatorLines = new HashMap<>();

    public StochRSIDraw(BaseKLineChartView view) {
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mTextPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IStochRSI lastPoint, @NonNull IStochRSI curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        // do nothing.
    }

    @Override
    public void drawIndicatorLine(@NonNull IStochRSI point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            for (Map.Entry<String, IndicatorLine> line : mIndicatorLines.entrySet()) {
                line.getValue().setFirst(true);
                line.getValue().getPath().reset();
            }
        }
        List<MAIndicator> stochRSI = point.getStochRSI();
        if (KChartListUtils.isEmpty(stochRSI)) {
            return;
        }
        for (int i = 0; i < stochRSI.size(); i++) {
            MAIndicator ma = stochRSI.get(i);
            if (ma.isNaValue()) {
                continue;
            }
            IndicatorLine line = getCacheIndicatorLine(i, KChartConstant.Sub.STOCHRSI);
            float y = view.getChildY(KChartConstant.Sub.STOCHRSI, (float) ma.getCalcResult());
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
        String key = new StringBuilder()
                .append(name)
                .append(KChartConstant.CACHE_INDICATOR_LINE_KEY_SPLICE)
                .append(index)
                .toString();
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
        IStochRSI point = (IStochRSI) view.getItem(position);
        drawValueText(point.getStochRSI(), canvas, view, x, y);
    }

    private void drawValueText(
            List<MAIndicator> stochRSI,
            Canvas canvas,
            BaseKLineChartView view,
            float x,
            float y
    ) {
        if (KChartListUtils.isEmpty(stochRSI)) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stochRSI.size(); i++) {
            MAIndicator item = stochRSI.get(i);
            builder.delete(0, builder.length());
            builder.append(item.getValue())
                    .append(KChartConstant.TEXT_VALUE_SPLICE);
            if (item.isNaValue()) {
                builder.append(KChartConstant.TEXT_EMPTY_STATUS);
            } else {
                builder.append(view.formatValue((float) item.getCalcResult()));
            }
            String maColor = ColorUtil.getMAColor(i);
            mTextPaint.setColor(Color.parseColor(maColor));

            String text = builder.toString();
            canvas.drawText(text, x, y, mTextPaint);
            x += mTextPaint.measureText(text) + view.getTopMaOrBullSpaceValue();
        }
    }

    @Override
    public float getMaxValue(IStochRSI point) {
        float max = Float.MIN_VALUE;
        List<MAIndicator> stochRSI = point.getStochRSI();
        if (KChartListUtils.isEmpty(stochRSI)) {
            return max;
        }
        for (MAIndicator item : stochRSI) {
            max = Math.max(max, (float) item.getCalcResult());
        }
        return max;
    }

    @Override
    public float getMinValue(IStochRSI point) {
        float min = Float.MAX_VALUE;
        List<MAIndicator> stochRSI = point.getStochRSI();
        if (KChartListUtils.isEmpty(stochRSI)) {
            return min;
        }
        for (MAIndicator item : stochRSI) {
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
