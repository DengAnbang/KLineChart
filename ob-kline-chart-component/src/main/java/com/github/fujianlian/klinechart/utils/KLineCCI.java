package com.github.fujianlian.klinechart.utils;

public class KLineCCI {

    public static double[] cci(double[] high, double[] low, double[] close, int period) {
        int length = close.length;
        double[] typicalPrice = new double[length];
        double[] smaTypicalPrice = new double[length];
        double[] meanDeviation = new double[length];
        double[] cci = new double[length];

        for (int i = 0; i < length; i++) {
            typicalPrice[i] = (high[i] + low[i] + close[i]) / 3.0;
        }

        for (int i = period - 1; i < length; i++) {
            double sumTypicalPrice = 0.0;

            for (int j = i - period + 1; j <= i; j++) {
                sumTypicalPrice += typicalPrice[j];
            }

            smaTypicalPrice[i] = sumTypicalPrice / period;
        }

        for (int i = period - 1; i < length; i++) {
            double sumMeanDeviation = 0.0;

            for (int j = i - period + 1; j <= i; j++) {
                sumMeanDeviation += Math.abs(typicalPrice[j] - smaTypicalPrice[i]);
            }

            meanDeviation[i] = sumMeanDeviation / period;
        }

        for (int i = period - 1; i < length; i++) {
            cci[i] = (typicalPrice[i] - smaTypicalPrice[i]) / (0.015 * meanDeviation[i]);
        }

        return cci;
    }
}