package com.github.fujianlian.klinechart.utils;

public class KLineOBV {

    public static double[][] obv(double[] close, double[] volume, int m) {
        int len = close.length;
        double[] obv = new double[len];
        double[] maobv = new double[len];
        double sum = 0;
        for (int i = 1; i < len; i++) {
            if (close[i] > close[i - 1]) {
                obv[i] = obv[i - 1] + volume[i];
            } else if (close[i] < close[i - 1]) {
                obv[i] = obv[i - 1] - volume[i];
            } else {
                obv[i] = obv[i - 1];
            }
            sum += obv[i];
            if (i >= m) {
                maobv[i] = sum / m;
                sum -= obv[i - m];
            }
        }
        return new double[][]{obv, maobv};
    }

    public static double[] maobv(double[] obv, int n) {
        int len = obv.length;
        double[] maobv = new double[len];
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum += obv[i];

            if (i > n - 1) {
                maobv[i] = sum / n;
                sum -= obv[i - n];
            }
        }
        return maobv;
    }

}