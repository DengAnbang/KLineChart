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
import com.github.fujianlian.klinechart.entity.IDMI;
import com.github.fujianlian.klinechart.entity.IndicatorLine;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.formatter.ValueFormatter;
import com.github.fujianlian.klinechart.utils.CalcParamsUtil;
import com.github.fujianlian.klinechart.utils.KChartListUtils;
import com.github.fujianlian.klinechart.utils.ThemeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DMI实现类
 * Created by tifezh on 2016/6/19.
 */
public class DMIDraw implements IChartDraw<IDMI> {

    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, IndicatorLine> mIndicatorLines = new HashMap<>();
    private final int mCalcParamsTextColor;
    private final float VALUE_LEFT_MARGIN;

    public DMIDraw(BaseKLineChartView view) {
        mCalcParamsTextColor = ThemeUtils.getThemeColor(view.getContext(), R.attr.textSecondary);
        VALUE_LEFT_MARGIN = view.getContext().getResources().getDimension(R.dimen.dp_4);
    }

    @Override
    public void setTypeface(Typeface customTypeface) {
        if (customTypeface != null) {
            mTextPaint.setTypeface(customTypeface);
        }
    }

    @Override
    public void drawTranslated(@Nullable IDMI lastPoint, @NonNull IDMI curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice) {
        // do nothing.
    }

    @Override
    public void drawIndicatorLine(@NonNull IDMI point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast) {
        if (isFirst) {
            for (Map.Entry<String, IndicatorLine> line : mIndicatorLines.entrySet()) {
                line.getValue().setFirst(true);
                line.getValue().getPath().reset();
            }
        }
        List<MAIndicator> items = point.getDMI();
        if (KChartListUtils.isEmpty(items)) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            MAIndicator ma = items.get(i);
            if (ma.isNaValue()) {
                continue;
            }
            IndicatorLine line = getCacheIndicatorLine(i, KChartConstant.Sub.DMI);
            float y = view.getChildY(KChartConstant.Sub.DMI, (float) ma.getCalcResult());
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
        IDMI point = (IDMI) view.getItem(position);
        drawValueText(point.getDMI(), canvas, view, x, y);
    }

    private void drawValueText(
            List<MAIndicator> items,
            Canvas canvas,
            BaseKLineChartView view,
            float x,
            float y
    ) {
        int[] period = CalcParamsUtil.getDMIPeriod();
        String displayText = CalcParamsUtil.getCalcParamsDisplayText(
                KChartConstant.Sub.DMI,
                String.valueOf(period[0]),
                String.valueOf(period[1])
        );
        mTextPaint.setColor(mCalcParamsTextColor);
        canvas.drawText(displayText, x, y, mTextPaint);
        x += mTextPaint.measureText(displayText) + view.getTopMaOrBullSpaceValue();

        if (KChartListUtils.isEmpty(items)) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            MAIndicator item = items.get(i);

            mTextPaint.setColor(Color.parseColor(item.getColor()));

            String text = getValueText(view, builder, item);
            canvas.drawText(text, x, y, mTextPaint);
            x += mTextPaint.measureText(text) + view.getTopMaOrBullSpaceValue();

            //检查下一个文本的宽度是否超过屏幕宽度，超过就需要换行
            if (i + 1 < items.size()) {
                int width = view.getChartWidth();
                String nextText = getValueText(view, builder, items.get(i + 1));
                if (x + mTextPaint.measureText(nextText) > width) {
                    x = VALUE_LEFT_MARGIN;
                    y += view.getValueTextHeight(mTextPaint);
                }
            }
        }
    }

    private String getValueText(BaseKLineChartView view, StringBuilder builder, MAIndicator item) {
        builder.delete(0, builder.length());
        builder.append(item.getValue())
                .append(KChartConstant.TEXT_VALUE_SPLICE);
        if (item.isNaValue()) {
            builder.append(KChartConstant.TEXT_EMPTY_STATUS);
        } else {
            builder.append(view.formatValue((float) item.getCalcResult()));
        }
        return builder.toString();
    }

    @Override
    public float getMaxValue(IDMI point) {
        float max = Float.MIN_VALUE;
        List<MAIndicator> items = point.getDMI();
        if (KChartListUtils.isEmpty(items)) {
            return max;
        }
        for (MAIndicator item : items) {
            max = Math.max(max, (float) item.getCalcResult());
        }
        return max;
    }

    @Override
    public float getMinValue(IDMI point) {
        float min = Float.MAX_VALUE;
        List<MAIndicator> items = point.getDMI();
        if (KChartListUtils.isEmpty(items)) {
            return min;
        }
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
