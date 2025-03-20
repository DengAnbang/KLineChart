package com.github.fujianlian.klinechart.utils;

import com.github.fujianlian.klinechart.KChartConstant;

public class ColorUtil {
    public static final String COLOR_TRANSPARENT = "#00000000";

    public static String getIndicatorColor(String indicator, int index) {
        String color;
        switch (indicator) {
            case KChartConstant.Main.MA:
            case KChartConstant.Main.EMA:
            case KChartConstant.Sub.RSI:
            case KChartConstant.Sub.WR:
            case KChartConstant.Sub.OBV:
            case KChartConstant.Sub.ROC:
            case KChartConstant.Sub.CCI:
            case KChartConstant.Sub.TRIX:
                color = getMAColor(index);
                break;
            default:
                color = COLOR_TRANSPARENT;
                break;
        }
        return color;
    }

    public static String getMAColor(int index) {
        String color;
        switch (index) {
            case 0:
                color = "#FCA6FF";
                break;
            case 1:
                color = "#70C2FC";
                break;
            case 2:
                color = "#FCCE70";
                break;
            case 3:
                color = "#9685FF";
                break;
            default:
                color = COLOR_TRANSPARENT;
                break;
        }
        return color;
    }

}
