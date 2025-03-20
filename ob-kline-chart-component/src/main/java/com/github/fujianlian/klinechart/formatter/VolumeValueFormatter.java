package com.github.fujianlian.klinechart.formatter;

import android.text.TextUtils;


import com.github.fujianlian.klinechart.utils.DecimalFormatTool;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VolumeValueFormatter extends ValueFormatter {

    //必须是排好序的
    private long[] values = {1000, 1000000, 1000000000, 1000000000000L};
    private String[] units = {"K", "M", "B", "T"};

    /**
     * 默认保留三位小数
     */
    private static final int DEFAULT_DECIMAL_VALUE = 3;

    /**
     * 设置保留几位小数
     */

    private int mScale;

    public String formatVolume(float value) {
        String unit = "";
        int i = values.length - 1;
        while (i >= 0) {
            if (Math.abs(value) >= values[i]) {
                value /= values[i];
                unit = units[i];
                break;
            }
            i--;
        }
        BigDecimal bigDecimal = new BigDecimal(value);
        return getFormatStr(bigDecimal, unit);
    }

    public String formatVolumeD(Double value) {
        String unit = "";
        int i = values.length - 1;
        while (i >= 0) {
            if (Math.abs(value) >= values[i]) {
                value /= values[i];
                unit = units[i];
                break;
            }
            i--;
        }
        BigDecimal bigDecimal = new BigDecimal(value);
        return getFormatStr(bigDecimal, unit);
    }

    public int getScale() {
        return mScale;
    }

    public void setScale(int scale) {
        this.mScale = scale;
    }

    private String getFormatStr(BigDecimal bigDecimal, String unit) {
        if (!TextUtils.isEmpty(unit) || getScale() <= 0) {
            return DecimalFormatTool.getFormattedDecimalV2(bigDecimal.setScale(DEFAULT_DECIMAL_VALUE, RoundingMode.DOWN).toPlainString()) + unit;
        }
        return  DecimalFormatTool.getFormattedDecimalV2(bigDecimal.setScale(getScale(), RoundingMode.DOWN).toPlainString()) + unit;
    }

}
