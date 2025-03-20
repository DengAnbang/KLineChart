package com.github.fujianlian.klinechart.entity;

public class KLineSelectorEntity {
    private String open;
    private String high;
    private String low;
    private String close;
    private String changeRate;
    private String change;
    private String vol;
    private String time;
    private HistorySignEntity buyOrder;
    private HistorySignEntity sellOrder;
    private HistorySignEntity liqOrder;
    private boolean isUp;
    private String range;
    private String amount;
    private boolean isFutures;

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(String changeRate) {
        this.changeRate = changeRate;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getVol() {
        return vol;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public HistorySignEntity getBuyOrder() {
        return buyOrder;
    }

    public void setBuyOrder(HistorySignEntity buyOrder) {
        this.buyOrder = buyOrder;
    }

    public HistorySignEntity getSellOrder() {
        return sellOrder;
    }

    public void setSellOrder(HistorySignEntity sellOrder) {
        this.sellOrder = sellOrder;
    }

    public HistorySignEntity getLiqOrder() {
        return liqOrder;
    }

    public void setLiqOrder(HistorySignEntity liqOrder) {
        this.liqOrder = liqOrder;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isFutures() {
        return isFutures;
    }

    public void setFutures(boolean futures) {
        isFutures = futures;
    }
}
