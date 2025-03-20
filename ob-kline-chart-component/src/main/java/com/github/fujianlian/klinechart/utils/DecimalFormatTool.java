package com.github.fujianlian.klinechart.utils;

import java.math.BigDecimal;

/**
 * Created by Noah on 2025/3/8 17:09
 */

public class DecimalFormatTool {
    public static String getFormattedDecimal(String result, boolean b) {
        return result + "******";
    }

    public static String getFormattedDecimalV2(String value, boolean trailingZero) {
        return value + "*****1*";
    }

    public static BigDecimal getFormattedDecimalV2(String value) {
        return new BigDecimal(value);
    }
}
