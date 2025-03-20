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
import com.github.fujianlian.klinechart.entity.IRSI;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.KChartListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RSI实现类
 * Created by tifezh on 2016/6/19.
 */
public class RSIDraw implements IChartDraw<IRSI> {

    private final Paint mRSI1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, IndicatorLine> mIndicatorLines = new HashMap<>();

    public RSIDraw(BaseKLineChartView view) {

    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mRSI1Paint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IRSI lastPoint, @NonNull IRSI curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        // do nothing.
    }

    @Override
    public void drawIndicatorLine(@NonNull IRSI point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            for (Map.Entry<String, IndicatorLine> line : mIndicatorLines.entrySet()) {
                line.getValue().setFirst(true);
                line.getValue().getPath().reset();
            }
        }
        List<MAIndicator> rsiList = point.getRsi();
        if (KChartListUtils.isEmpty(point.getRsi())) {
            return;
        }
        for (int i = 0; i < rsiList.size(); i++) {
            MAIndicator ma = rsiList.get(i);
            IndicatorLine line = getCacheIndicatorLine(i, KChartConstant.Sub.RSI);
            if (ma.getCalcResult() == 0) {
                continue;
            }
            float y = view.getChildY(KChartConstant.Sub.RSI, (float) ma.getCalcResult());
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
        IRSI point = (IRSI) view.getItem(position);
        drawRSIText(point.getRsi(), canvas, view, x, y);
    }

    private void drawRSIText(List<MAIndicator> rsi, Canvas canvas,
                             BaseKLineChartView view, float x, float y) {
        if (KChartListUtils.isEmpty(rsi)) {
            return;
        }
        String text;
        int index = 0;
        for (int i = 0; i < rsi.size(); i++) {
            MAIndicator item = rsi.get(i);
            double calcResult = item.getCalcResult();
            text = KChartConstant.Sub.RSI
                    + item.getValue()
                    + KChartConstant.TEXT_VALUE_SPLICE;
            if (calcResult == 0) {
                text += KChartConstant.TEXT_EMPTY_STATUS;
            } else {
                text += view.formatValue((float) item.getCalcResult());
            }
            String maColor = ColorUtil.getMAColor(index);
            mRSI1Paint.setColor(Color.parseColor(maColor));
            canvas.drawText(text, x, y, mRSI1Paint);
            x += mRSI1Paint.measureText(text) + view.getTopMaOrBullSpaceValue();
            index++;
        }
    }

    @Override
    public float getMaxValue(IRSI point) {
        List<MAIndicator> rsi = point.getRsi();
        if (KChartListUtils.isEmpty(rsi)) {
            return 0f;
        }
        float max = Float.MIN_VALUE;
        for (MAIndicator item : rsi) {
            max = Math.max(max, (float) item.getCalcResult());
        }
        return max;
    }

    @Override
    public float getMinValue(IRSI point) {
        List<MAIndicator> rsi = point.getRsi();
        if (KChartListUtils.isEmpty(rsi)) {
            return 0f;
        }
        float min = Float.MAX_EXPONENT;
        for (MAIndicator item : rsi) {
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
        mRSI1Paint.setTextSize(textSize);
    }
}
