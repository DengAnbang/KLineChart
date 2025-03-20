package com.github.fujianlian.klinechart.entity;

import java.util.List;

/**
 * 蜡烛图实体接口
 * Created by tifezh on 2016/6/9.
 */
public interface ICandle {

    /**
     * 开盘价
     */
    float getOpenPrice();

    String getOpenPriceStr();

    double getOpenPriceD();

    /**
     * 最高价
     */
    float getHighPrice();

    String getHighPriceStr();

    double getHighPriceD();

    /**
     * 最低价
     */
    float getLowPrice();

    String getLowPriceStr();

    double getLowPriceD();

    /**
     * 收盘价
     */
    float getClosePrice();

    String getClosePriceStr();

    double getClosePriceD();

    /**
     * 成交量
     */
    float getVolume();

    String getVolumeStr();

    double getVolumeD();

    double getAmountD();

    /**
     * 时间戳
     */
    long getLongDate();

    List<MAIndicator> getMa();

    List<MAIndicator> getEma();

    List<MAIndicator> getBoll();
    MAIndicator getSar();

}

