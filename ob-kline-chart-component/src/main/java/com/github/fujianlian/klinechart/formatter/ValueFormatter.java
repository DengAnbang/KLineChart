package com.github.fujianlian.klinechart.formatter;

import com.github.fujianlian.klinechart.base.IValueFormatter;
import com.github.fujianlian.klinechart.utils.DecimalFormatTool;

import java.util.Locale;

/**
 * Value格式化类
 * Created by tifezh on 2016/6/21.
 */

public class ValueFormatter implements IValueFormatter {
    private int priceMinNumber;
//    private DecimalFormatWrapper decimalFormat;

    public int getPriceMinNumber() {
        return priceMinNumber;
    }

    public void setPriceMinNumber(int priceMinNumber) {
        this.priceMinNumber = priceMinNumber;
    }

//    public void setDecimalFormat(DecimalFormatWrapper decimalFormat) {
//        this.decimalFormat = decimalFormat;
//    }

    @Override
    public String format(float value) {
        return String.format(Locale.ENGLISH,"%.2f",value);

//        if (decimalFormat != null) {
//            return DecimalFormatTool.indentDecimal0(decimalFormat.format(value));
//        }
//        if (priceMinNumber != 0) {
//            return String.format(Locale.ENGLISH, "%." + priceMinNumber + "f", value);
//        } else {
//            return String.format(Locale.ENGLISH, "%.2f", value);
//        }
    }

    @Override
    public String formatD(Double value) {
        return String.format(Locale.ENGLISH,"%.2f",value);

//        if (decimalFormat != null) {
//            return DecimalFormatTool.indentDecimal0(decimalFormat.format(value));
//        }
//
//        if (priceMinNumber != 0) {
//            return String.format(Locale.ENGLISH, "%." + priceMinNumber + "f", value);
//        } else {
//            return String.format(Locale.ENGLISH, "%.2f", value);
//        }
    }

    @Override
    public String formatStr(String value, boolean trailingZero) {
        return DecimalFormatTool.getFormattedDecimalV2(value, trailingZero);
    }

}
