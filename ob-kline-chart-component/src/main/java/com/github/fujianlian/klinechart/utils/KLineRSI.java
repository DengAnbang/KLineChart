package com.github.fujianlian.klinechart.utils;

import java.util.ArrayList;
import java.util.List;

public class KLineRSI {
    private static final int MIN_PERIOD = 1;

    public static List<Double> calculateRSI(List<Double> prices, int period) {
        period = Math.max(MIN_PERIOD, period);
        List<Double> rsiValues = new ArrayList<>();

        if (prices.size() <= period) {
            return rsiValues;
        }

        // 计算价格变动
        List<Double> priceChanges = calculatePriceChanges(prices);

        // 计算平均涨幅和平均跌幅
        double avgGain = calculateAverageGain(priceChanges, period);
        double avgLoss = calculateAverageLoss(priceChanges, period);

        // 计算初始RS和RSI值
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));
        rsiValues.add(rsi);

        // 根据前一周期的RSI值和当前价格变动，计算后续的RSI值
        for (int i = period; i < priceChanges.size(); i++) {
            double priceChange = priceChanges.get(i);
            double gain = Math.max(priceChange, 0);
            double loss = Math.abs(Math.min(priceChange, 0));

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;

            rs = avgGain / avgLoss;
            rsi = 100 - (100 / (1 + rs));
            rsiValues.add(rsi);
        }

        return rsiValues;
    }

    private static List<Double> calculatePriceChanges(List<Double> prices) {
        List<Double> priceChanges = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double priceChange = prices.get(i) - prices.get(i - 1);
            priceChanges.add(priceChange);
        }
        return priceChanges;
    }

    private static double calculateAverageGain(List<Double> priceChanges, int period) {
        double sum = 0;
        for (int i = 0; i < period; i++) {
            double priceChange = priceChanges.get(i);
            if (priceChange > 0) {
                sum += priceChange;
            }
        }
        return sum / period;
    }

    private static double calculateAverageLoss(List<Double> priceChanges, int period) {
        double sum = 0;
        for (int i = 0; i < period; i++) {
            double priceChange = priceChanges.get(i);
            if (priceChange < 0) {
                sum += Math.abs(priceChange);
            }
        }
        return sum / period;
    }
}