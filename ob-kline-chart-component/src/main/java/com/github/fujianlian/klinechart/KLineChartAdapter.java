package com.github.fujianlian.klinechart;

import android.text.TextUtils;

import com.github.fujianlian.klinechart.entity.TimeQuantum;
import com.github.fujianlian.klinechart.utils.KChartListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据适配器
 * Created by tifezh on 2016/6/18.
 */
public class KLineChartAdapter extends BaseKLineChartAdapter {
    private static final int INDEX_NOT_FOUND = -1;

    private List<KLineEntity> datas = new ArrayList<>();
    private List<Long> keys = new ArrayList<>();

    /**
     * 在子线程计算数据的过程中，数据集合是否发生了改变.
     */
    private boolean mDataChanged = false;

    public KLineChartAdapter() {

    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < datas.size()) {
            return datas.get(position);
        }
        return new KLineEntity();
    }

    @Override
    public int getKeyPosition(long key) {
        if (keys.isEmpty()) {
            return INDEX_NOT_FOUND;
        }
        int index = keys.indexOf(key);
        if (index == INDEX_NOT_FOUND) {
            index = findKeyIndex(key);
        }
        return index;
    }

    public int findKeyIndex(long time) {
        if (keys.isEmpty()) {
            return INDEX_NOT_FOUND;
        }
        if (time < keys.get(0) || time > keys.get(keys.size() - 1)) {
            return INDEX_NOT_FOUND;
        }
        for (int i = keys.size() - 1; i > 0; i--) {
            long end = keys.get(i);
            long start = end - (end - keys.get(i - 1));
            if (time >= start && time <= end) {
                return i - 1;
            }
        }
        return INDEX_NOT_FOUND;
    }

    @Override
    public long getKey(int position) {
        if (position >= 0 && position < keys.size()) {
            return keys.get(position);
        }
        return 0;
    }

    @Override
    public String getDate(int position, String interval) {
        if (datas == null || datas.isEmpty()) {
            return "";
        }
        if (position < 0 || position >= datas.size()) {
            return "";
        }
        if (TextUtils.isEmpty(interval)) {
            return datas.get(position).getDate();
        }
        if (TimeQuantum.SECOND1.getInterval().equals(interval)) {
            return datas.get(position).getDateHHmmss();
        } else if (isDisplayHHmm(interval)) {
            return datas.get(position).getDateHHmm();
        } else if (isDisplayYYMMDD(interval)) {
            return datas.get(position).getDateYYMMDD();
        }
        return datas.get(position).getDate();
    }

    /**
     * 显示时分的周期
     *
     * @param interval
     * @return
     */
    private boolean isDisplayHHmm(String interval) {
        return TimeQuantum.MIN.getInterval().equals(interval)
                || TimeQuantum.MIN1.getInterval().equals(interval);
    }

    /**
     * 显示年月日的周期
     *
     * @param interval
     * @return
     */
    private boolean isDisplayYYMMDD(String interval) {
        return TimeQuantum.DAY1.getInterval().equals(interval)
                || TimeQuantum.DAY2.getInterval().equals(interval)
                || TimeQuantum.DAY3.getInterval().equals(interval)
                || TimeQuantum.DAY5.getInterval().equals(interval)
                || TimeQuantum.WEEK1.getInterval().equals(interval)
                || TimeQuantum.MONTH1.getInterval().equals(interval);
    }

    /**
     * 向尾部添加数据
     */
    public void addFooterData(List<KLineEntity> data) {
        if (data != null) {
            datas.clear();
            datas.addAll(0, data);
            addKeys();
            mDataChanged = true;
        }
    }

    private void addKeys() {
        keys.clear();
        for (KLineEntity entity : datas) {
            keys.add(entity.getLongDate());
        }
    }

    /**
     * 数据清除
     */
    public void clearData() {
        datas.clear();
        keys.clear();
        notifyDataSetChanged();
        mDataChanged = true;
    }

    public List<KLineEntity> getDatas() {
        return datas;
    }

    /**
     * 该方法用于提供子线程计算时所需的数据，请谨慎使用.
     */
    public List<KLineEntity> getDataWithState() {
        mDataChanged = false;
        return datas;
    }

    public void updateAll(List<KLineEntity> data) {
        addFooterData(data);
        DataHelper.calculate(datas);
        notifyDataSetChanged();
        mDataChanged = true;
    }

    /**
     * 此处一般接收从子线程返回的已经计算好的数据，如果在子线程计算期间数据集合没有发生改变，则将用新的数据替换旧的数据.
     */
    public void updateAllWithoutCalculate(List<KLineEntity> data) {
        if (mDataChanged || KChartListUtils.isEmpty(data)) {
            return;
        }
        addFooterData(data);
        notifyDataSetChanged();
    }

    public void replaceData(List<KLineEntity> data) {
        addFooterData(data);
        notifyDataSetChanged();
    }

    public void updateLast(KLineEntity entity) {
        if (datas.isEmpty()) {
            datas.add(entity);
            keys.add(entity.getLongDate());
        } else {
            int lastIndex = datas.size() - 1;
            KLineEntity lastEntity = datas.get(lastIndex);
            if (lastEntity.getLongDate() == entity.getLongDate()) {
                datas.set(lastIndex, entity);
            } else if (entity.getLongDate() > lastEntity.getLongDate()) {
                datas.add(entity);
                keys.add(entity.getLongDate());
            }
        }

        DataHelper.calculate(datas);
        notifyDataSetChanged();
        mDataChanged = true;
    }

    public void addLoadMoreData(List<KLineEntity> entities) {
        if (KChartListUtils.isEmpty(entities)) {
            return;
        }
        // 将获取到的更多数据放到集合最前面
        datas.addAll(0, entities);
        addKeys();
        DataHelper.calculate(datas);
        notifyDataSetChanged();
        mDataChanged = true;
    }

}
