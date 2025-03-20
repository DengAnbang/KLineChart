package com.github.fujianlian.klinechart.utils;

import java.util.ArrayList;
import java.util.List;

public class KLineStochRSI {
    private static final int MIN_PERIOD = 1;

    public static List<Double> calculateStoch(List<Double> rsiValues, int stochPeriod) {
        stochPeriod = Math.max(MIN_PERIOD, stochPeriod);
        List<Double> stochValues = new ArrayList<>();

        if (rsiValues.size() >= stochPeriod) {
            for (int i = stochPeriod - 1; i < rsiValues.size(); i++) {
                List<Double> subList = rsiValues.subList(i - stochPeriod + 1, i + 1);
                double minRSI = getMinValue(subList);
                double maxRSI = getMaxValue(subList);
                double stock = (rsiValues.get(i) - minRSI) / (maxRSI - minRSI) * 100;
                stochValues.add(stock);
            }
        }

        return stochValues;
    }

    // 获取列表中的最小值
    private static double getMinValue(List<Double> list) {
        double min = Double.MAX_VALUE;
        for (double value : list) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    // 获取列表中的最大值
    private static double getMaxValue(List<Double> list) {
        double max = Double.MIN_VALUE;
        for (double value : list) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static List<Double> calculateK(List<Double> stochKLine, int kSmooth) {
        kSmooth = Math.max(MIN_PERIOD, kSmooth);
        List<Double> kValues = new ArrayList<>();

        if (stochKLine.size() >= kSmooth) {
            for (int i = kSmooth - 1; i < stochKLine.size(); i++) {
                List<Double> subList = stochKLine.subList(i - kSmooth + 1, i + 1);
                double k = getAverage(subList);
                kValues.add(k);
            }
        }

        return kValues;
    }

    private static double getAverage(List<Double> list) {
        double sum = 0.0;
        for (double value : list) {
            sum += value;
        }
        return sum / list.size();
    }

    public static List<Double> calculateD(List<Double> stochDLine, int dSmooth) {
        dSmooth = Math.max(MIN_PERIOD, dSmooth);
        List<Double> dValues = new ArrayList<>();

        if (stochDLine.size() >= dSmooth) {
            for (int i = dSmooth - 1; i < stochDLine.size(); i++) {
                List<Double> subList = stochDLine.subList(i - dSmooth + 1, i + 1);
                double d = getAverage(subList);
                dValues.add(d);
            }
        }

        return dValues;
    }
}