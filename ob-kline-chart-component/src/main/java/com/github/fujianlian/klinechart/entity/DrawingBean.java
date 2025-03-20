package com.github.fujianlian.klinechart.entity;

import android.graphics.Path;
import android.graphics.Region;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class DrawingBean implements Cloneable {
    //绘图的类型
    @Expose
    private DrawType type;

    //绘图的点
    @Expose
    private List<DrawingPoint> points;

    //是否选中；新创建的，默认选中
    @Expose
    private boolean isSelected = true;

    @Expose
    private boolean isLocked = false;

    @Expose(serialize = false, deserialize = false)
    private Region region;

    @Expose(serialize = false, deserialize = false)
    private List<Region> regions;

    @Expose(serialize = false, deserialize = false)
    private Path path;

    @Expose(serialize = false, deserialize = false)
    private Path clickPath;

    @Expose
    private String pointColor;

    @Expose
    private float lineWidth;

    @Expose
    private ChartPoint.LineStyle lineStyle;

    public DrawingBean(@NonNull DrawType type) {
        this.type = type;
    }

    public DrawingBean(DrawType type, List<DrawingPoint> points, boolean isSelected,
                       boolean isLocked, String pointColor, float lineWidth,
                       ChartPoint.LineStyle lineStyle) {
        this.type = type;
        this.points = points;
        this.isSelected = isSelected;
        this.isLocked = isLocked;
        this.pointColor = pointColor;
        this.lineWidth = lineWidth;
        this.lineStyle = lineStyle;
    }

    public DrawType getType() {
        return type;
    }

    public void setType(DrawType type) {
        this.type = type;
    }

    @NonNull
    public List<DrawingPoint> getPoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return points;
    }

    public void addPoint(DrawingPoint point) {
        getPoints().add(point);
    }

    public int getNeedPointSize() {
        return type != null ? type.getPointSize() : 0;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @NonNull
    public Region getRegion() {
        if (region == null) {
            region = new Region();
        }
        return region;
    }

    @NonNull
    public Region getRegion(int i) {
        if (regions == null) {
            regions = new ArrayList<>();
        }
        if (!regions.isEmpty() && i < regions.size()) {
            return regions.get(i);
        }
        Region r = new Region();
        regions.add(r);
        return r;
    }

    @NonNull
    public Path getPath() {
        if (path == null) {
            path = new Path();
        }
        return path;
    }

    @NonNull
    public Path getClickPath() {
        if (clickPath == null) {
            clickPath = new Path();
        }
        return clickPath;
    }

    public boolean isClickPath(float x, float y) {
        if (regions == null || regions.isEmpty()) {
            return getRegion().contains((int) x, (int) y);
        }
        for (Region r : regions) {
            if (r.contains((int) x, (int) y)) {
                return true;
            }
        }
        return false;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPointColor(String color) {
        this.pointColor = color;
    }

    @NonNull
    public String getPointColor() {
        if (TextUtils.isEmpty(pointColor)) {
            pointColor = ChartPoint.Color.COLOR_1.getColor();
        }
        return pointColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @NonNull
    public ChartPoint.LineStyle getLineStyle() {
        if (lineStyle == null) {
            lineStyle = ChartPoint.LineStyle.STYLE_3;
        }
        return lineStyle;
    }

    public void setLineStyle(ChartPoint.LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public DrawingBean clone() {
        List<DrawingPoint> list = new ArrayList<>();
        if (points != null) {
            for (DrawingPoint p : points) {
                list.add(p.clone());
            }
        }
        return new DrawingBean(type, list, false, isLocked, pointColor, lineWidth, lineStyle);
    }
}
