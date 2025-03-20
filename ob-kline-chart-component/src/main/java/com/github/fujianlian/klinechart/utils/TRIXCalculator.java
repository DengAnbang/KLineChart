package com.github.fujianlian.klinechart.utils;

import java.util.ArrayList;
import java.util.List;

public class TRIXCalculator {

    public static List<Double> calculateTRIX(List<Double> closePrices, int period) {
        List<Double> emaList1 = calculateEMA(closePrices, period);
        List<Double> emaList2 = calculateEMA(emaList1, period);
        List<Double> emaList3 = calculateEMA(emaList2, period);
        List<Double> trixList = new ArrayList<>();
        for (int i = 1; i < emaList3.size(); i++) {
            Double preEma = emaList3.get(i - 1);
            double trix = ((emaList3.get(i) - preEma) / preEma) * 100;
            trixList.add(trix);
        }
        return trixList;
    }

    public static List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> emaList = new ArrayList<>();
        double smoothingConstant = 2.0 / (period + 1);
        double ema = 0;
        for (int i = period - 1; i < prices.size(); i++) {
            if (i == period - 1) {
                ema = calculateInitialEMA(prices.subList(0, period));
                emaList.add(ema);
            } else {
                ema = calculateSubsequentEMA(prices.get(i), ema, smoothingConstant);
                emaList.add(ema);
            }
        }

        return emaList;
    }

    private static double calculateInitialEMA(List<Double> data) {
        double sum = 0;
        for (Double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private static double calculateSubsequentEMA(double currentValue, double previousEMA, double smoothingConstant) {
        return (currentValue - previousEMA) * smoothingConstant + previousEMA;
    }

}