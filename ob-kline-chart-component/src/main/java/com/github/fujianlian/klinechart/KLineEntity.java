package com.github.fujianlian.klinechart;

import android.text.TextUtils;

import com.github.fujianlian.klinechart.entity.IKLine;
import com.github.fujianlian.klinechart.entity.MAIndicator;
import com.github.fujianlian.klinechart.utils.DateUtil;
import com.github.fujianlian.klinechart.utils.StringUtils;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * K线实体
 * Created by tifezh on 2016/5/16.
 */
public class KLineEntity implements IKLine, Cloneable {

    public String getDateHHmmss() {
        return timeStamp2Date(date, "HH:mm:ss");
    }

    public String getDateHHmm() {
        return timeStamp2Date(date, "HH:mm");
    }

    public String getDate() {
        return timeStamp2Date(date, "MM-dd HH:mm");
    }

    public String getDateYYMMDD() {
        return timeStamp2Date(date, "yyyy-MM-dd");
    }

    @Override
    public long getLongDate() {
        if (TextUtils.isEmpty(date) || "null".equals(date)) {
            return 0;
        } else {
            try {
                // 尝试解析为毫秒时间戳
                return Long.parseLong(date);
            } catch (NumberFormatException e) {
                try {
                    // 如果解析失败，尝试解析为 "MM-dd HH:mm" 格式
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                    Date parsedDate = sdf.parse(date);
                    return parsedDate.getTime();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return 0;
                }
            }
        }
    }

    @Override
    public float getOpenPrice() {
        if (TextUtils.isEmpty(Open)) {
            return 0;
        } else {
            return Float.parseFloat(Open);
        }
    }

    @Override
    public String getOpenPriceStr() {
        return Open;
    }

    @Override
    public double getOpenPriceD() {
        if (TextUtils.isEmpty(Open)) {
            return 0;
        } else {
            return Double.parseDouble(Open);
        }
    }

    @Override
    public float getHighPrice() {
        if (TextUtils.isEmpty(High)) {
            return 0;
        } else {
            return Float.parseFloat(High);
        }
    }

    @Override
    public String getHighPriceStr() {
        return High;
    }

    @Override
    public double getHighPriceD() {
        if (TextUtils.isEmpty(High)) {
            return 0;
        } else {
            return Double.parseDouble(High);
        }
    }

    @Override
    public float getLowPrice() {
        if (TextUtils.isEmpty(Low)) {
            return 0;
        } else {
            return Float.parseFloat(Low);
        }
    }

    @Override
    public String getLowPriceStr() {
        return Low;
    }

    @Override
    public double getLowPriceD() {
        if (TextUtils.isEmpty(Low)) {
            return 0;
        } else {
            return Double.parseDouble(Low);
        }
    }

    @Override
    public float getClosePrice() {
        if (TextUtils.isEmpty(Close)) {
            return 0;
        } else {
            return Float.parseFloat(Close);
        }
    }

    @Override
    public String getClosePriceStr() {
        return Close;
    }

    @Override
    public double getClosePriceD() {
        if (TextUtils.isEmpty(Close)) {
            return 0;
        } else {
            return Double.parseDouble(Close);
        }
    }

    @Override
    public float getDea() {
        return dea;
    }

    @Override
    public float getDif() {
        return dif;
    }

    @Override
    public float getMacd() {
        return macd;
    }

    @Override
    public String getMacdText() {
        return macdText;
    }

    @Override
    public float getK() {
        return k;
    }

    @Override
    public float getD() {
        return d;
    }

    @Override
    public float getJ() {
        return j;
    }

    @Override
    public String getKDJText() {
        return kdjText;
    }

    @Override
    public float getVolume() {
        if (TextUtils.isEmpty(Volume)) {
            return 0;
        } else {
            return Float.parseFloat(Volume);
        }
    }

    @Override
    public double getAmountD() {
        if (TextUtils.isEmpty(Amount)) {
            return 0;
        }
        return Double.parseDouble(Amount);
    }

    @Override
    public String getVolumeStr() {
        return Volume;
    }

    @Override
    public double getVolumeD() {
        if (TextUtils.isEmpty(Volume)) {
            return 0;
        } else {
            return Double.parseDouble(Volume);
        }
    }

    @Override
    public float getMA5Volume() {
        return MA5Volume;
    }

    @Override
    public float getMA10Volume() {
        return MA10Volume;
    }

    @SerializedName("time")
    public String date;
    @SerializedName("open")
    public String Open;
    @SerializedName("high")
    public String High;
    @SerializedName("low")
    public String Low;
    @SerializedName("close")
    public String Close;
    @SerializedName("vol")
    public String Volume;
    @SerializedName("amount")
    public String Amount;

    public List<MAIndicator> ma;
    public List<MAIndicator> ema;
    public List<MAIndicator> boll;

    public float dea;

    public float dif;

    public float macd;

    public float k;

    public float d;

    public float j;

    public List<MAIndicator> rsi;
    public List<MAIndicator> wr;
    public List<MAIndicator> obv;
    public List<MAIndicator> roc;
    public List<MAIndicator> cci;
    public List<MAIndicator> stochRSI;
    public List<MAIndicator> trix;
    public List<MAIndicator> dmi;
    public MAIndicator sar;

    public float MA5Volume;

    public float MA10Volume;

    public String macdText;
    public String kdjText;

    //成交时间
    private long tradeTime;

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param departure 精确到毫秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String departure, String format) {
        if (departure == null || departure.isEmpty() || "null".equals(departure)) {
            return "";
        }
        if (format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = DateUtil.getSimpleDateFormat(format);
        try {
            return StringUtils.forceLtr(sdf.format(new Date(Long.parseLong(departure))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setOpen(String open) {
        Open = open;
    }

    public void setHigh(String high) {
        High = high;
    }

    public void setLow(String low) {
        Low = low;
    }

    public void setClose(String close) {
        Close = close;
    }

    public void setVolume(String volume) {
        Volume = volume;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public void addVolume(double volume) {
        Volume = new BigDecimal(Volume).add(new BigDecimal(volume)).toPlainString();
    }

    public void addAmount(double amount) {
        Amount = new BigDecimal(Amount).add(new BigDecimal(amount)).toPlainString();
    }

    public List<MAIndicator> getMa() {
        return ma;
    }

    public void setMa(List<MAIndicator> ma) {
        this.ma = ma;
    }

    public List<MAIndicator> getEma() {
        return ema;
    }

    public void setEma(List<MAIndicator> ema) {
        this.ema = ema;
    }

    public void setBoll(List<MAIndicator> boll) {
        this.boll = boll;
    }

    @Override
    public List<MAIndicator> getBoll() {
        return boll;
    }

    @Override
    public List<MAIndicator> getRsi() {
        return rsi;
    }

    public void setRsi(List<MAIndicator> rsi) {
        this.rsi = rsi;
    }

    @Override
    public List<MAIndicator> getWr() {
        return wr;
    }

    public void setWr(List<MAIndicator> wr) {
        this.wr = wr;
    }

    @Override
    public List<MAIndicator> getOBV() {
        return obv;
    }

    public void setObv(List<MAIndicator> obv) {
        this.obv = obv;
    }

    @Override
    public List<MAIndicator> getROC() {
        return roc;
    }

    @Override
    public List<MAIndicator> getCCI() {
        return cci;
    }

    @Override
    public MAIndicator getSar() {
        return sar;
    }

    @Override
    public List<MAIndicator> getStochRSI() {
        return stochRSI;
    }

    @Override
    public List<MAIndicator> getDMI() {
        return dmi;
    }

    @Override
    public List<MAIndicator> getTRIX() {
        return trix;
    }

    public void setTradeTime(long tradeTime) {
        this.tradeTime = tradeTime;
    }

    public long getTradeTime() {
        return tradeTime;
    }

    @Override
    public KLineEntity clone() {
        try {
            KLineEntity clone = (KLineEntity) super.clone();
            if (ma != null) {
                List<MAIndicator> cloneMa = new ArrayList<>();
                for (MAIndicator item : ma) {
                    cloneMa.add(item.clone());
                }
                clone.ma = cloneMa;
            }
            if (ema != null) {
                List<MAIndicator> cloneEma = new ArrayList<>();
                for (MAIndicator item : ema) {
                    cloneEma.add(item.clone());
                }
                clone.ema = cloneEma;
            }
            if (boll != null) {
                List<MAIndicator> cloneBoll = new ArrayList<>();
                for (MAIndicator item : boll) {
                    cloneBoll.add(item.clone());
                }
                clone.boll = cloneBoll;
            }
            if (rsi != null) {
                List<MAIndicator> cloneRsi = new ArrayList<>();
                for (MAIndicator item : rsi) {
                    cloneRsi.add(item.clone());
                }
                clone.rsi = cloneRsi;
            }
            if (wr != null) {
                List<MAIndicator> cloneWr = new ArrayList<>();
                for (MAIndicator item : wr) {
                    cloneWr.add(item.clone());
                }
                clone.wr = cloneWr;
            }
            if (obv != null) {
                List<MAIndicator> cloneObv = new ArrayList<>();
                for (MAIndicator item : obv) {
                    cloneObv.add(item.clone());
                }
                clone.obv = cloneObv;
            }
            if (roc != null) {
                List<MAIndicator> cloneRoc = new ArrayList<>();
                for (MAIndicator item : roc) {
                    cloneRoc.add(item.clone());
                }
                clone.roc = cloneRoc;
            }
            if (cci != null) {
                List<MAIndicator> cloneCci = new ArrayList<>();
                for (MAIndicator item : cci) {
                    cloneCci.add(item.clone());
                }
                clone.cci = cloneCci;
            }
            if (sar != null) {
                clone.sar = sar.clone();
            }
            if (stochRSI != null) {
                List<MAIndicator> cloneStochRSI = new ArrayList<>();
                for (MAIndicator item : stochRSI) {
                    cloneStochRSI.add(item.clone());
                }
                clone.stochRSI = cloneStochRSI;
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
