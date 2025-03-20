package com.github.fujianlian.klinechart;

import com.github.fujianlian.klinechart.draw.WRDraw;
import com.github.fujianlian.klinechart.entity.Indicators;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.utils.CalcParamsUtil;
import com.github.fujianlian.klinechart.utils.ColorUtil;
import com.github.fujianlian.klinechart.utils.DMICalculator;
import com.github.fujianlian.klinechart.utils.KChartListUtils;
import com.github.fujianlian.klinechart.utils.KLineCCI;
import com.github.fujianlian.klinechart.utils.KLineOBV;
import com.github.fujianlian.klinechart.utils.KLineROC;
import com.github.fujianlian.klinechart.utils.KLineRSI;
import com.github.fujianlian.klinechart.utils.KLineSAR;
import com.github.fujianlian.klinechart.utils.KLineStochRSI;
import com.github.fujianlian.klinechart.utils.TRIXCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据辅助类 计算mac rsi等
 * Created by tifezh on 2016/11/26.
 */
public final class DataHelper {

    private DataHelper() {
    }

    static void calculateROC(List<KLineEntity> dataList) {
        int len = dataList.size();
        double[] close = new double[len];
        double[] high = new double[len];
        double[] low = new double[len];
        double[] vol = new double[len];
        List<Double> closePrices = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            KLineEntity kLine = dataList.get(i);
            close[i] = kLine.getClosePriceD();
            high[i] = kLine.getHighPriceD();
            low[i] = kLine.getLowPriceD();
            vol[i] = kLine.getVolumeD();
            closePrices.add(kLine.getClosePriceD());
        }

        double[] sarAccelerationFactor = CalcParamsUtil.getSARAccelerationFactor();
        int[] rocParams = CalcParamsUtil.getROCPeriod();
        int rocPeriod = rocParams[0];
        int rocMaPeriod = rocParams[1];
        int obvMaPeriod = CalcParamsUtil.getOBVPeriod();
        int cciPeriod = CalcParamsUtil.getCCIPeriod();
        int trixPeriod = CalcParamsUtil.getTRIXPeriod();
        double accelerationFactor = sarAccelerationFactor[0];
        double maxAccelerationFactor = sarAccelerationFactor[1];

        double[][] obv = KLineOBV.obv(close, vol, obvMaPeriod);
        double[][] roc = KLineROC.roc(close, rocPeriod, rocMaPeriod);
        double[] cci = KLineCCI.cci(high, low, close, cciPeriod);
        double[] sar = KLineSAR.sar(high, low, accelerationFactor, maxAccelerationFactor);
        int rocMaStart = rocPeriod + rocMaPeriod;

        //stoch rsi
        int[] stochParams = CalcParamsUtil.getStochRSIParams();
        int rsiPeriod = stochParams[0];
        int stochPeriod = stochParams[1];
        int kSmooth = stochParams[2];
        int dSmooth = stochParams[3];
        List<Double> rsiValues = KLineRSI.calculateRSI(closePrices, rsiPeriod);
        List<Double> stochValues = KLineStochRSI.calculateStoch(rsiValues, stochPeriod);
        List<Double> stochK = KLineStochRSI.calculateK(stochValues, kSmooth);
        List<Double> stockD = KLineStochRSI.calculateD(stochK, dSmooth);
        int kStartIndex = closePrices.size() - stochK.size();
        int dStartIndex = closePrices.size() - stockD.size();

        //trix
        List<Double> trixValues = TRIXCalculator.calculateTRIX(closePrices, trixPeriod);
        int trixStartIndex = closePrices.size() - trixValues.size();

        //dmi
        int[] dmiPeriod = CalcParamsUtil.getDMIPeriod();
        int diPeriod = dmiPeriod[0];
        int adxPeriod = dmiPeriod[1];
        DMICalculator.DMIResult dmiResult = DMICalculator.calcDMI(high, low, close, diPeriod, adxPeriod);
        double[] plusDI = dmiResult.getPlusDI();
        double[] minusDI = dmiResult.getMinusDI();
        double[] adx = dmiResult.getADX();
        double[] adxr = dmiResult.getADXR();
        int adxStartIndex = diPeriod + adxPeriod - 1;
        int adxrStartIndex = diPeriod * 2 + adxPeriod - 2;

        for (int i = 0; i < len; i++) {
            KLineEntity kLine = dataList.get(i);

            //sar models
            MAIndicator sarModel = new MAIndicator(true, "",
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));
            sarModel.setCalcResult(sar[i]);
            kLine.sar = sarModel;

            //cci models
            List<MAIndicator> cciList = new ArrayList<>();
            MAIndicator cciModel = new MAIndicator(true, KChartConstant.Sub.CCI,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));
            cciModel.setCalcResult(cci[i]);
            cciModel.setNaValue(i < cciPeriod);
            cciList.add(cciModel);
            kLine.cci = cciList;

            //obv models
            List<MAIndicator> obvList = new ArrayList<>();
            MAIndicator obvModel = new MAIndicator(true, KChartConstant.Sub.OBV,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));
            obvModel.setCalcResult(obv[0][i]);
            obvList.add(obvModel);

            MAIndicator maobvModel = new MAIndicator(true, KChartConstant.Sub.MAOBV,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(1));
            maobvModel.setCalcResult(obv[1][i]);
            maobvModel.setNaValue(i < obvMaPeriod);
            obvList.add(maobvModel);

            kLine.obv = obvList;

            //roc models
            List<MAIndicator> rocList = new ArrayList<>();
            MAIndicator rocModel = new MAIndicator(true, KChartConstant.Sub.ROC,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));
            rocModel.setCalcResult(roc[0][i]);
            rocModel.setNaValue(i < rocPeriod);
            rocList.add(rocModel);

            MAIndicator marocModel = new MAIndicator(true, KChartConstant.Sub.MAROC,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(1));
            marocModel.setCalcResult(roc[1][i]);
            marocModel.setNaValue(i < rocMaStart);
            rocList.add(marocModel);

            kLine.roc = rocList;

            //stoch rsi
            List<MAIndicator> stochList = new ArrayList<>();
            MAIndicator stochKModel = new MAIndicator(
                    true,
                    KChartConstant.Sub.STOCHRSI.toUpperCase(),
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(0)
            );
            if (i >= kStartIndex) {
                stochKModel.setCalcResult(stochK.get(i - kStartIndex));
            }
            stochKModel.setNaValue(i < kStartIndex);
            stochList.add(stochKModel);

            MAIndicator stochDModel = new MAIndicator(
                    true,
                    KChartConstant.Sub.MASTOCHRSI.toUpperCase(),
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(1)
            );
            if (i >= dStartIndex) {
                stochDModel.setCalcResult(stockD.get(i - dStartIndex));
            }
            stochDModel.setNaValue(i < dStartIndex);
            stochList.add(stochDModel);

            kLine.stochRSI = stochList;

            //trix
            List<MAIndicator> trixList = new ArrayList<>();
            MAIndicator trixModel = new MAIndicator(true, KChartConstant.Sub.TRIX,
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));
            if (i >= trixStartIndex) {
                trixModel.setCalcResult(trixValues.get(i - trixStartIndex));
            }
            trixModel.setNaValue(i < trixStartIndex);
            trixList.add(trixModel);
            kLine.trix = trixList;

            //dmi
            List<MAIndicator> dmiList = new ArrayList<>();

            MAIndicator pdi = new MAIndicator(
                    true,
                    KChartConstant.DMIIndicatorsName.PLUS_DI,
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(0)
            );
            pdi.setCalcResult(plusDI[i]);
            pdi.setNaValue(i < diPeriod);

            MAIndicator mdi = new MAIndicator(
                    true,
                    KChartConstant.DMIIndicatorsName.MINUS_DI,
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(1)
            );
            mdi.setCalcResult(minusDI[(i)]);
            mdi.setNaValue(i < diPeriod);

            MAIndicator adxModel = new MAIndicator(
                    true,
                    KChartConstant.DMIIndicatorsName.ADX,
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(2)
            );
            adxModel.setCalcResult(adx[i]);
            adxModel.setNaValue(i < adxStartIndex);

            MAIndicator adxrModel = new MAIndicator(
                    true,
                    KChartConstant.DMIIndicatorsName.ADXR,
                    MAIndicator.Width.NORMAL,
                    ColorUtil.getMAColor(3)
            );
            adxrModel.setCalcResult(adxr[i]);
            adxrModel.setNaValue(i < adxrStartIndex);

            dmiList.add(pdi);
            dmiList.add(mdi);
            dmiList.add(adxModel);
            dmiList.add(adxrModel);
            kLine.dmi = dmiList;
        }
    }

    static void calculateRSI(List<KLineEntity> dataList) {
        String rsiKey = KChartConstant.Sub.RSI;
        Indicators indicators = ConfigController.getIndicators(rsiKey);
        if (indicators == null) {
            return;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.RSI) {
            return;
        }

        int c1 = calcParams.get(0);
        int c2 = calcParams.get(1);
        int c3 = calcParams.get(2);

        float rsi = 0f;
        float rsi2 = 0f;
        float rsi3 = 0f;

        float rsiABSEma = 0;
        float rsiMaxEma = 0;

        float rsi2ABSEma = 0;
        float rsi2MaxEma = 0;

        float rsi3ABSEma = 0;
        float rsi3MaxEma = 0;
        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            final float closePrice = point.getClosePrice();
            if (i > 0) {
                float rMax = Math.max(0, closePrice - dataList.get(i - 1).getClosePrice());
                float rAbs = Math.abs(closePrice - dataList.get(i - 1).getClosePrice());

                rsiMaxEma = (rMax + (c1 - 1) * rsiMaxEma) / c1;
                rsiABSEma = (rAbs + (c1 - 1) * rsiABSEma) / c1;
                rsi = (rsiMaxEma / rsiABSEma) * 100;

                rsi2MaxEma = (rMax + (c2 - 1) * rsi2MaxEma) / c2;
                rsi2ABSEma = (rAbs + (c2 - 1) * rsi2ABSEma) / c2;
                rsi2 = (rsi2MaxEma / rsi2ABSEma) * 100;

                rsi3MaxEma = (rMax + (c3 - 1) * rsi3MaxEma) / c3;
                rsi3ABSEma = (rAbs + (c3 - 1) * rsi3ABSEma) / c3;
                rsi3 = (rsi3MaxEma / rsi3ABSEma) * 100;
            }

            if (i < c1 - 1) {
                rsi = 0f;
            }
            if (i < c2 - 1) {
                rsi2 = 0f;
            }
            if (i < c3 - 1) {
                rsi3 = 0f;
            }

            List<MAIndicator> rsiList = new ArrayList<>();

            MAIndicator rsiItem1 = new MAIndicator(true, String.valueOf(c1),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));

            MAIndicator rsiItem2 = new MAIndicator(true, String.valueOf(c2),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(1));

            MAIndicator rsiItem3 = new MAIndicator(true, String.valueOf(c3),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(2));

            rsiItem1.setCalcResult(rsi);
            rsiItem2.setCalcResult(rsi2);
            rsiItem3.setCalcResult(rsi3);

            rsiList.add(rsiItem1);
            rsiList.add(rsiItem2);
            rsiList.add(rsiItem3);
            point.rsi = rsiList;
        }
    }

    static void calculateKDJ(List<KLineEntity> dataList) {
        String kdjKey = KChartConstant.Sub.KDJ;
        Indicators indicators = ConfigController.getIndicators(kdjKey);
        if (indicators == null) {
            return;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.KDJ) {
            return;
        }

        int c1 = calcParams.get(0);
        int c2 = calcParams.get(1);
        int c3 = calcParams.get(2);
        String kdjText = new StringBuilder()
                .append(kdjKey)
                .append("(")
                .append(c1)
                .append(",")
                .append(c2)
                .append(",")
                .append(c3)
                .append(")")
                .toString();

        float k = 50;
        float d = 50;
        for (int i = c1 - 1; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            point.kdjText = kdjText;
            final float closePrice = point.getClosePrice();
            int startIndex = i - (c1 - 1);
            if (startIndex < 0) {
                startIndex = 0;
            }
            float max14 = Float.MIN_VALUE;
            float min14 = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                max14 = Math.max(max14, dataList.get(index).getHighPrice());
                min14 = Math.min(min14, dataList.get(index).getLowPrice());
            }
            Float rsv = 100f * (closePrice - min14) / (max14 - min14);
            if (rsv.isNaN()) {
                rsv = 0f;
            }
            k = (rsv + (c2 - 1) * k) / c2;
            d = (k + (c3 - 1) * d) / c3;
            point.k = k;
            point.d = d;
            point.j = 3f * k - 2 * d;
        }
    }

    static void calculateWR(List<KLineEntity> dataList) {
        String wrKey = KChartConstant.Sub.WR;
        Indicators indicators = ConfigController.getIndicators(wrKey);
        if (indicators == null) {
            return;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.WR) {
            return;
        }

        int c1 = calcParams.get(0);
        int c2 = calcParams.get(1);
        int c3 = calcParams.get(2);
        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            float wr = getWR(dataList, i, c1);
            float wr2 = getWR(dataList, i, c2);
            float wr3 = getWR(dataList, i, c3);

            List<MAIndicator> wrList = new ArrayList<>();

            MAIndicator wrItem1 = new MAIndicator(true, String.valueOf(c1),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(0));

            MAIndicator wrItem2 = new MAIndicator(true, String.valueOf(c2),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(1));

            MAIndicator wrItem3 = new MAIndicator(true, String.valueOf(c3),
                    MAIndicator.Width.NORMAL, ColorUtil.getMAColor(2));

            wrItem1.setCalcResult(wr);
            wrItem2.setCalcResult(wr2);
            wrItem3.setCalcResult(wr3);

            wrList.add(wrItem1);
            wrList.add(wrItem2);
            wrList.add(wrItem3);
            point.wr = wrList;
        }
    }

    static float getWR(List<KLineEntity> dataList, int i, int c) {
        Float r;
        int startIndex = i - (c - 1);
        if (startIndex < 0) {
            startIndex = 0;
        }
        float max14 = Float.MIN_VALUE;
        float min14 = Float.MAX_VALUE;
        for (int index = startIndex; index <= i; index++) {
            max14 = Math.max(max14, dataList.get(index).getHighPrice());
            min14 = Math.min(min14, dataList.get(index).getLowPrice());
        }
        if (i < c - 1) {
            return WRDraw.EMPTY_VALUE;
        } else {
            r = 100 * (max14 - dataList.get(i).getClosePrice()) / (max14 - min14);
            if (r.isNaN()) {
                return 0F;
            }
            return r;
        }
    }

    static void calculateMACD(List<KLineEntity> dataList) {
        String macdKey = KChartConstant.Sub.MACD;
        Indicators indicators = ConfigController.getIndicators(macdKey);
        if (indicators == null) {
            return;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.MACD) {
            return;
        }

        int shortPeriod = calcParams.get(0);
        int longPeriod = calcParams.get(1);
        int maPeriod = calcParams.get(2);

        String macdText = new StringBuilder()
                .append(macdKey)
                .append("(")
                .append(shortPeriod)
                .append(",")
                .append(longPeriod)
                .append(",")
                .append(maPeriod)
                .append(")")
                .toString();

        double ema12 = 0;
        double ema26 = 0;
        shortPeriod = shortPeriod + 1;
        longPeriod = longPeriod + 1;
        maPeriod = maPeriod + 1;

        double dif = 0;
        double dea = 0;
        double macd = 0;
        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            final float closePrice = point.getClosePrice();
            if (i == 0) {
                ema12 = closePrice;
                ema26 = closePrice;
            }
            // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
            ema12 = ema12 * (shortPeriod - 2) / shortPeriod + closePrice * 2f / shortPeriod;
            // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
            ema26 = ema26 * (longPeriod - 2) / longPeriod + closePrice * 2f / longPeriod;

            // DIF = EMA（12） - EMA（26） 。
            // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            // 用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26;
            dea = dea * (maPeriod - 2) / maPeriod + dif * 2f / maPeriod;
            macd = (dif - dea) * 2f;
            point.dif = (float) dif;
            point.dea = (float) dea;
            point.macd = (float) macd;
            point.macdText = macdText;
        }
    }

    static void calculateBOLL(List<KLineEntity> dataList) {
        String bollText = KChartConstant.Main.BOLL;
        Indicators indicators = ConfigController.getIndicators(bollText);
        if (indicators == null) {
            return;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.BOLL) {
            return;
        }
        int c1 = calcParams.get(0);
        int p = c1 - 1;
        double closeSum = 0;
        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            float closePrice = point.getClosePrice();
            closeSum += closePrice;
            if (i >= p) {
                float mid = (float) (closeSum / c1);
                float md = getBollMd(dataList.subList(i - p, i + 1), mid);
                float up = mid + 2 * md;
                float dn = mid - 2 * md;
                closeSum -= dataList.get(i - p).getClosePrice();

                List<MAIndicator> boll = new ArrayList<>();
                MAIndicator mbItem = new MAIndicator(true, "", "", ColorUtil.getMAColor(1));
                mbItem.setCalcResult(mid);
                mbItem.setPrefix(KChartConstant.Boll.MID);

                MAIndicator upItem = new MAIndicator(true, "", "", ColorUtil.getMAColor(0));
                upItem.setCalcResult(up);
                upItem.setPrefix(KChartConstant.Boll.UP);

                MAIndicator dnItem = new MAIndicator(true, "", "", ColorUtil.getMAColor(2));
                dnItem.setCalcResult(dn);
                dnItem.setPrefix(KChartConstant.Boll.DN);

                boll.add(mbItem);
                boll.add(upItem);
                boll.add(dnItem);
                point.setBoll(boll);
            }
        }
    }

    static float getBollMd(List<KLineEntity> dataList, float ma) {
        int dataSize = dataList.size();
        double sum = 0;
        for (int i = 0; i < dataSize; i++) {
            KLineEntity kLineEntity = dataList.get(i);
            float closePrice = kLineEntity.getClosePrice();
            float closeMa = closePrice - ma;
            sum += closeMa * closeMa;
        }
        boolean b = sum > 0;
        sum = Math.abs(sum);
        float md = (float) Math.sqrt(sum / dataSize);
        return b ? md : -1 * md;
    }

    static void calculateMA(List<KLineEntity> dataList) {
        String maText = KChartConstant.Main.MA;
        Indicators indicators = ConfigController.getIndicators(maText);
        if (indicators == null) {
            return;
        }
        List<MAIndicator> items = indicators.getItems();
        if (KChartListUtils.isEmpty(items)) {
            return;
        }

        double[] closeSum = new double[items.size()];
        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            final float closePrice = point.getClosePrice();
            List<MAIndicator> calcItems = new ArrayList<>();
            for (int index = 0; index < items.size(); index++) {
                closeSum[index] = closeSum[index] + closePrice;
                int day = parseInt(items.get(index).getValue());
                double ma = 0;
                if (i == day - 1) {
                    ma = closeSum[index] / day;
                } else if (i >= day) {
                    closeSum[index] = closeSum[index] - dataList.get(i - day).getClosePrice();
                    ma = closeSum[index] / day;
                }
                MAIndicator cloneItem = items.get(index).clone();
                cloneItem.setCalcResult(ma);
                calcItems.add(cloneItem);
            }
            point.setMa(calcItems);
        }
    }

    static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static void calculateEMA(List<KLineEntity> dataList) {
        String emaText = KChartConstant.Main.EMA;
        Indicators indicators = ConfigController.getIndicators(emaText);
        if (indicators == null) {
            return;
        }
        List<MAIndicator> items = indicators.getItems();
        if (KChartListUtils.isEmpty(items)) {
            return;
        }

        double[] closeSum = new double[items.size()];
        double[][] tempEma = new double[items.size()][dataList.size()];
        float[] factor1 = new float[items.size()];
        float[] factor2 = new float[items.size()];

        for (int i = 0; i < items.size(); i++) {
            int n = parseInt(items.get(i).getValue());
            factor1[i] = 2f / (n + 1);
            factor2[i] = (n - 1) * 1.0f / (n + 1);
        }

        for (int i = 0; i < dataList.size(); i++) {
            KLineEntity point = dataList.get(i);
            final float closePrice = point.getClosePrice();
            List<MAIndicator> calcItems = new ArrayList<>();
            for (int index = 0; index < items.size(); index++) {
                closeSum[index] = closeSum[index] + closePrice;
                int day = parseInt(items.get(index).getValue());
                double ema = 0f;
                if (i >= day - 1) {
                    int preEmaIndex = Math.max(0, i - 1);
                    if (i == day - 1) {
                        tempEma[index][preEmaIndex] = closeSum[index] / day;
                    }
                    ema = factor1[index] * closePrice + factor2[index] * tempEma[index][preEmaIndex];
                    tempEma[index][i] = ema;
                }
                MAIndicator cloneItem = items.get(index).clone();
                cloneItem.setCalcResult(ema);
                calcItems.add(cloneItem);
            }
            point.setEma(calcItems);
        }
    }

    public static void calculate(List<KLineEntity> dataList) {
        calculateMA(dataList);
        calculateEMA(dataList);
        calculateMACD(dataList);
        calculateBOLL(dataList);
        calculateRSI(dataList);
        calculateKDJ(dataList);
        calculateWR(dataList);
        calculateVolumeMA(dataList);
        calculateROC(dataList);
    }

    private static void calculateVolumeMA(List<KLineEntity> entries) {
        double volumeMa5 = 0;
        double volumeMa10 = 0;

        for (int i = 0; i < entries.size(); i++) {
            KLineEntity entry = entries.get(i);

            volumeMa5 += entry.getVolume();
            volumeMa10 += entry.getVolume();

            if (i == 4) {
                entry.MA5Volume = (float) (volumeMa5 / 5f);
            } else if (i > 4) {
                volumeMa5 -= entries.get(i - 5).getVolume();
                entry.MA5Volume = (float) (volumeMa5 / 5f);
            } else {
                entry.MA5Volume = 0f;
            }

            if (i == 9) {
                entry.MA10Volume = (float) (volumeMa10 / 10f);
            } else if (i > 9) {
                volumeMa10 -= entries.get(i - 10).getVolume();
                entry.MA10Volume = (float) (volumeMa10 / 10f);
            } else {
                entry.MA10Volume = 0f;
            }
        }
    }

}
