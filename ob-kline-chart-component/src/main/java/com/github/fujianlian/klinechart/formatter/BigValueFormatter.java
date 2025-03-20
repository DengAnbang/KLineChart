package com.github.fujianlian.klinechart.formatter;

import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.utils.DecimalFormatTool;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 对较大数据进行格式化
 * Created by tifezh on 2017/12/13.
 */

public class BigValueFormatter implements IValueFormatter {
    private static final int SCALE = 3;

    //必须是排好序的
    private long[] values = {1000, 1000000, 1000000000, 1000000000000L};
    private String[] units = {"K", "M", "B", "T"};

    @Override
    public String format(float value) {
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
        String result = new BigDecimal(value).setScale(SCALE, RoundingMode.DOWN).toPlainString();
        return DecimalFormatTool.getFormattedDecimal(result, true) + unit;
    }

    @Override
    public String formatD(Double value) {
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
        String result = new BigDecimal(value).setScale(SCALE, RoundingMode.DOWN).toPlainString();
        return DecimalFormatTool.getFormattedDecimal(result, true) + unit;
    }

    @Override
    public String formatStr(String value, boolean trailingZero) {
        try {
            return formatD(Double.parseDouble(value));
        } catch (Exception e) {
            return value;
        }
    }
}
