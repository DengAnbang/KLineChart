package com.github.fujianlian.klinechart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.Magnifier;

import androidx.annotation.RequiresApi;

import com.github.fujianlian.klinechart.draw.CustomDraw;
import com.github.fujianlian.klinechart.entity.ChartPoint;
import com.github.fujianlian.klinechart.entity.DrawType;
import com.github.fujianlian.klinechart.entity.DrawingBean;
import com.github.fujianlian.klinechart.entity.DrawingPoint;
import com.github.fujianlian.klinechart.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomDrawChart {
    private static final String TAG = "CustomDrawChart";
    private static final int MAGNIFIER_SIZE = 90;

    //是否开启绘图
    private boolean isOpenDraw = false;

    //是否正在绘图
    private boolean isDrawing = false;

    //是否显示已绘制的图形
    private boolean isShowDraw = true;

    //是否连续画线
    private boolean isContinuousDraw = false;

    //需要绘制的视图
    private final List<DrawingBean> mDrawingBeans = new ArrayList<>();

    //正在绘制中的视图
    private DrawingBean mCurDrawingBean;

    //正在编辑的点
    private final List<DrawingPoint> mEditPoints = new ArrayList<>();

    //画笔的样式
    private String mCurDrawPointColor = ChartPoint.Color.COLOR_1.getColor();
    private float mCurDrawPointStrokeWidth = ChartPoint.LineWidth.WIDTH_1.getWidth();
    private ChartPoint.LineStyle mCurDrawPointStyle = ChartPoint.LineStyle.STYLE_3;

    //绘制区域
    private final RectF mMainDrawRectF = new RectF();

    private final BaseKLineChartView mParent;
    private final CustomDraw mCustomDraw;
    private DrawingEventListener mDrawingEventListener;
    private Magnifier mMagnifier;

    public CustomDrawChart(BaseKLineChartView parent) {
        mParent = parent;
        mCustomDraw = new CustomDraw(parent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMagnifier = new Magnifier.Builder(parent)
                    .setSize(parent.dip2px(MAGNIFIER_SIZE), parent.dip2px(MAGNIFIER_SIZE))
                    .setCornerRadius(parent.dip2px(MAGNIFIER_SIZE))
                    .setOverlay(getMagnifierOverlay())
                    .setInitialZoom(2.0F)
                    .build();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showMagnifier() {
        if (mEditPoints.isEmpty()) {
            return;
        }
        float moveY = mParent.getMoveY();
        float moveX = mParent.getMoveX();
        float magnifierCenterX = mParent.getWidth() - mMagnifier.getWidth() / 2F;
        float magnifierCenterY = mMagnifier.getHeight() / 2F;
        if (moveX > mParent.getWidth() / 2F) {
            magnifierCenterX = mMagnifier.getWidth() / 2F;
        }
        mMagnifier.show(moveX, moveY, magnifierCenterX, magnifierCenterY);
    }

    public boolean isOpenDraw() {
        return isOpenDraw;
    }

    public void setOpenDraw(boolean openDraw) {
        isOpenDraw = openDraw;
    }

    public void setContinuousDraw(boolean continuousDraw) {
        isContinuousDraw = continuousDraw;
    }

    private GradientDrawable getMagnifierOverlay() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setCornerRadius(mParent.dip2px(MAGNIFIER_SIZE));
        gd.setStroke(mParent.dip2px(1), ThemeUtils.getThemeColor(mParent.getContext(), R.attr.textTertiary));
        return gd;
    }

    public void drawCustomBeans(Canvas canvas) {
        if (isShowDraw && !mDrawingBeans.isEmpty()) {
            mCustomDraw.drawCustomLine(canvas, mDrawingBeans);
        }
    }

    private void updateCurDrawBean() {
        if (mCurDrawingBean == null) {
            return;
        }

        //当前保存的点小于需要的点，表示还未完成绘制
        if (mCurDrawingBean.getPoints().size() < mCurDrawingBean.getNeedPointSize()) {
            float y = checkDrawingPointRegion();
            float lastClickX = mParent.getLastClickX();
            mCurDrawingBean.addPoint(new DrawingPoint(lastClickX, y));
            if (mDrawingEventListener != null) {
                mDrawingEventListener.onDrawProgress(mCurDrawingBean.getPoints().size());
            }
        }
    }

    private float checkDrawingPointRegion() {
        boolean contains = mMainDrawRectF.contains((int) mParent.getLastClickX(), (int) mParent.getLastClickY());
        if (contains) {
            return mParent.getLastClickY();
        }
        float y = mParent.getLastClickY();
        if (y > mMainDrawRectF.bottom) {
            y = mMainDrawRectF.bottom;
        }
        if (mDrawingEventListener != null) {
            mDrawingEventListener.onRegionOut();
        }
        return y;
    }

    private void checkClickDrawBean() {
        boolean isSelected = false;
        for (DrawingBean bean : mDrawingBeans) {
            bean.setSelected(false);
            if (isSelected) {
                continue;
            }
            if (isSelectedDrawBean(bean)) {
                isSelected = true;
                bean.setSelected(true);
                mCurDrawingBean = bean;
            }
        }
        //绘图模式下，没有选中绘制的视图，k线才可以滑动
        mParent.setEditDrawBean(isSelected);
        if (mDrawingEventListener != null) {
            mDrawingEventListener.onSelectedChange(isSelected, mCurDrawingBean);
        }
    }

    private boolean isSelectedDrawBean(DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        for (DrawingPoint p : points) {
            if (p.getRectF().contains(mParent.getLastClickX(), mParent.getLastClickY())) {
                return true;
            }
        }

        return bean.isClickPath(mParent.getLastClickX(), mParent.getLastClickY());
    }

    public void setEnableDrawing(boolean enable) {
        this.isOpenDraw = enable;
        if (!isOpenDraw) {
            exitEditDraw();
        }
    }

    public void setDrawType(DrawType type) {
        if (isDrawing) {
            if (mCurDrawingBean != null) {
                mDrawingBeans.remove(mCurDrawingBean);
                mParent.invalidate();
            }
        } else {
            if (mCurDrawingBean != null) {
                mCurDrawingBean.setSelected(false);
                mParent.invalidate();
            }
        }

        this.mCurDrawingBean = new DrawingBean(type);
        this.mCurDrawingBean.setLineStyle(mCurDrawPointStyle);
        this.mCurDrawingBean.setLineWidth(mCurDrawPointStrokeWidth);
        this.mCurDrawingBean.setPointColor(mCurDrawPointColor);
        this.mDrawingBeans.add(mCurDrawingBean);
        this.isDrawing = true;
        mParent.setEditDrawBean(true);
        if (mDrawingEventListener != null) {
            mDrawingEventListener.onSelectedChange(true, mCurDrawingBean);
        }
    }

    public void setDrawVisible(boolean isShowDraw) {
        this.isShowDraw = isShowDraw;
        mParent.invalidate();
    }

    public void deleteAllDraw() {
        if (isDrawing) {
            cancelCurDrawingBean();
        }
        this.mDrawingBeans.clear();
        mParent.invalidate();
    }

    public void deleteCurDraw() {
        if (isDrawing) {
            cancelCurDrawingBean();
        } else {
            this.mDrawingBeans.remove(mCurDrawingBean);
        }
        if (mCurDrawingBean != null
                && !mCurDrawingBean.getPoints().isEmpty()
                && mDrawingEventListener != null) {
            mDrawingEventListener.onDelCurDraw();
        }
        mParent.invalidate();
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawPointColor(String color) {
        this.mCurDrawPointColor = color;
        if (mCurDrawingBean != null) {
            this.mCurDrawingBean.setPointColor(color);
            mParent.invalidate();
        }
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawPointStrokeWidth(float width) {
        this.mCurDrawPointStrokeWidth = width;
        if (mCurDrawingBean != null) {
            this.mCurDrawingBean.setLineWidth(width);
            mParent.invalidate();
        }
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawPointStyle(ChartPoint.LineStyle lineStyle) {
        this.mCurDrawPointStyle = lineStyle;
        if (mCurDrawingBean != null) {
            this.mCurDrawingBean.setLineStyle(lineStyle);
            mParent.invalidate();
        }
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawLockState(boolean isLocked) {
        if (mCurDrawingBean != null) {
            mCurDrawingBean.setLocked(isLocked);
        }
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawingBeans(List<DrawingBean> drawingBeans) {
        mDrawingBeans.clear();
        if (drawingBeans != null && drawingBeans.size() > 0) {
            mDrawingBeans.addAll(drawingBeans);
        }
        mParent.invalidate();
    }

    private void notifyDrawingBeansUpdateListener() {
        Log.d(TAG, "notifyDrawingBeansUpdateListener...");
        if (mDrawingEventListener != null) {
            List<DrawingBean> clones = new ArrayList<>();
            for (DrawingBean bean : mDrawingBeans) {
                if (bean.getPoints().size() == bean.getType().getPointSize()) {
                    DrawingBean clone = bean.clone();
                    clones.add(clone);
                }
            }
            mDrawingEventListener.onSave(clones);
        }
    }

    public void exitEditDraw() {
        if (isDrawing) {
            cancelCurDrawingBean();
        }
        isDrawing = false;
        mParent.resetSelectedPoint();
        mParent.setEditDrawBean(false);
        for (DrawingBean bean : mDrawingBeans) {
            bean.setSelected(false);
        }
        notifyDrawingBeansUpdateListener();
    }

    public void setDrawingEventListener(DrawingEventListener drawingEventListener) {
        this.mDrawingEventListener = drawingEventListener;
    }

    public void onTouchClick() {
        if (isOpenDraw && !isDrawing) {
            checkClickDrawBean();
        }
    }

    public void onTouchDown() {
        if (isDrawing) {
            updateCurDrawBean();
        }
    }

    public void onTouchMove() {
        Log.d(TAG, "onTouchMove");
        if (mParent.isEditDrawBean() && mCurDrawingBean != null) {
            if (mEditPoints.isEmpty()) {
                getEditPoints();
            }
            switchUpdatePointLocation();
            mParent.invalidate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                showMagnifier();
            }
        }
    }

    public void onTouchUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMagnifier.dismiss();
        }

        //当前图形绘制完成
        if (isDrawing && mCurDrawingBean != null
                && mCurDrawingBean.getPoints().size() >= mCurDrawingBean.getNeedPointSize()) {
            isDrawing = false;
            mEditPoints.addAll(mCurDrawingBean.getPoints());
            if (mDrawingEventListener != null) {
                mDrawingEventListener.onDrawOver(isContinuousDraw, mCurDrawingBean.getType(), false);
            }
            if (isContinuousDraw) {
                setDrawType(mCurDrawingBean.getType());
            }
        }

        //将x和y坐标转换成k线的时间和价格
        bindKLineTimeAndPrice(mEditPoints);
        mEditPoints.clear();

        if (isOpenDraw) {
            notifyDrawingBeansUpdateListener();
        }
    }

    private void getEditPoints() {
        mEditPoints.clear();
        if (mCurDrawingBean.isLocked()) {
            return;
        }
        List<DrawingPoint> points = mCurDrawingBean.getPoints();
        for (DrawingPoint p : points) {
            if (p.getRectF().contains(mParent.getLastClickX(), mParent.getLastClickY())) {
                mEditPoints.add(p);
                return;
            }
        }
        if (mCurDrawingBean.isClickPath(mParent.getLastClickX(), mParent.getLastClickY())) {
            if (mCurDrawingBean.getType() == DrawType.PARALLEL) {
                mEditPoints.add(points.get(0));
                mEditPoints.add(points.get(1));
            } else {
                mEditPoints.addAll(points);
            }
        }
        Log.d(TAG, "getEditPoints:" + mEditPoints);
    }

    private void switchUpdatePointLocation() {
        if (mCurDrawingBean.getType() == DrawType.VERTICAL_LINE) {
            updateVerticalLineLocation(mCurDrawingBean.getPoints().size() == 1);
        } else {
            for (DrawingPoint p : mEditPoints) {
                updatePointLocation(p);
            }
        }
    }

    private void updateVerticalLineLocation(boolean isChangeX) {
        if (mEditPoints.size() == 1) {
            updatePointLocation(mEditPoints.get(0), isChangeX, true);
        } else {
            for (DrawingPoint p : mEditPoints) {
                updatePointLocation(p);
            }
        }
    }

    private void updatePointLocation(DrawingPoint p) {
        updatePointLocation(p, true, true);
    }

    private void updatePointLocation(DrawingPoint p, boolean isChangeX, boolean isChangeY) {
        float x;
        float y;
        if (p.getX() != 0) {
            x = p.getX();
            y = p.getY();
        } else {
            x = mParent.pointTimeToX(p.getTime());
            y = mParent.getDrawMainY(p.getPrice());
        }

        if (isChangeX) {
            x = x + mParent.getOffsetX();
        }
        if (isChangeY) {
            y = y + mParent.getOffsetY();
        }

        p.setX(x);
        p.setY(y);
        p.setOffsetY(p.getOffsetY() + mParent.getOffsetY());
        p.setUseRawX(true);
    }

    private void bindKLineTimeAndPrice(List<DrawingPoint> points) {
        for (DrawingPoint p : points) {
            long time = mParent.pointXToTime(p.getX());
            float price = mParent.getDrawMainYToPrice(p.getY());
            p.setTime(time);
            p.setPrice(price);
            p.setUseRawX(false);
        }
    }

    public void setMainRegion(int left, int top, int right, int bottom) {
        mCustomDraw.setMainRegion(left, top, right, bottom);
    }

    public void setMainDrawRectF(int left, int top, int right, int bottom) {
        mMainDrawRectF.set(left, top, right, bottom);
    }

    public void onChangeDrawBeansVisible(boolean showDraw) {
        if (showDraw) {
            if (isDrawing && mCurDrawingBean != null) {
                if (mDrawingEventListener != null) {
                    mDrawingEventListener.onDrawResume();
                }
            }
        } else {
            if (mCurDrawingBean == null) {
                return;
            }
            if (mCurDrawingBean.getPoints().size() >= mCurDrawingBean.getType().getPointSize()) {
                mCurDrawingBean.setSelected(false);
            }
        }
    }

    private void cancelCurDrawingBean() {
        if (mCurDrawingBean == null) {
            return;
        }
        mDrawingBeans.remove(mCurDrawingBean);
        if (mDrawingEventListener != null) {
            mDrawingEventListener.onDrawOver(isContinuousDraw, mCurDrawingBean.getType(), true);
        }
    }

    public void setTypeface(Typeface typeface) {
        mCustomDraw.setTypeface(typeface);
    }

    public interface DrawingEventListener {
        void onDrawOver(boolean isContinuousDraw, DrawType drawType, boolean isCancel);

        void onDrawProgress(int pointSize);

        void onSelectedChange(boolean isSelected, DrawingBean bean);

        void onSave(List<DrawingBean> beans);

        void onRegionOut();

        void onDrawResume();

        void onDelCurDraw();
    }
}
