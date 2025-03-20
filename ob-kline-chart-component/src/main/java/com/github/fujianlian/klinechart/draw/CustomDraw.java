package com.github.fujianlian.klinechart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;

import com.github.fujianlian.klinechart.BaseKLineChartView;
import com.github.fujianlian.klinechart.KChartConstant;
import com.github.fujianlian.klinechart.entity.ChartPoint;
import com.github.fujianlian.klinechart.entity.DrawType;
import com.github.fujianlian.klinechart.entity.DrawingBean;
import com.github.fujianlian.klinechart.entity.DrawingPoint;

import java.util.ArrayList;
import java.util.List;

public class CustomDraw {
    private static final int PRICE_TEXT_SIZE = 10;
    private static final float[] FIBONACCI_PARAMS = new float[]{
            0.0F,
            0.236F,
            0.382F,
            0.5F,
            0.618F,
            0.786F,
            1.0F
    };

    private final BaseKLineChartView mParent;
    private final float mArcRadius;
    private final float mArc2Radius;
    private final Paint mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Region mMainRegion = new Region();
    private final RectF mDrawingBeanRectF = new RectF();
    private final float[] mRotatedPoints = new float[8];

    public CustomDraw(BaseKLineChartView view) {
        this.mParent = view;
        mArcRadius = mParent.dip2px(KChartConstant.POINT_ARC_RADIUS);
        mArc2Radius = mParent.dip2px(KChartConstant.POINT_ARC_RADIUS2);

        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setTextSize(mParent.dip2px(PRICE_TEXT_SIZE));

        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
    }

    private void setPointStyle(DrawingBean bean) {
        mPointPaint.setColor(Color.parseColor(bean.getPointColor()));
        mPathPaint.setColor(Color.parseColor(bean.getPointColor()));
        mPathPaint.setStrokeWidth(mParent.dip2px(bean.getLineWidth()));
        ChartPoint.LineStyle style = bean.getLineStyle();
        mPathPaint.setPathEffect(new DashPathEffect(new float[]{
                mParent.dip2px(style.getDashWidth()),
                mParent.dip2px(style.getDashGap())}, 0));
    }

    public void drawCustomLine(Canvas canvas, List<DrawingBean> beans) {
        for (DrawingBean bean : beans) {
            mParent.calculateLocation(bean.getPoints());
            setPointStyle(bean);
            DrawType type = bean.getType();
            switch (type) {
                case TREND_LINE:
                case THREE_WAVES:
                case FIVE_WAVES:
                    drawTrendLine(canvas, bean);
                    break;
                case HORIZONTAL_LINE:
                    drawHorizontalLine(canvas, bean);
                    break;
                case RAY:
                    drawRayLine(canvas, bean);
                    break;
                case VERTICAL_LINE:
                    drawVerticalLine(canvas, bean);
                    break;
                case PRICE_LINE:
                    drawPriceLine(canvas, bean);
                    break;
                case PARALLEL:
                    drawParallel(canvas, bean);
                    break;
                case RECTANGLE:
                    drawRectangle(canvas, bean);
                    break;
                case PARALLELOGRAM:
                    drawParallelogram(canvas, bean);
                    break;
                case FIBONACCI:
                    drawFibonacci(canvas, bean);
                    break;
                default:
                    break;
            }
        }
    }

    private void drawFibonacci(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.size() == 1) {
            drawPoint(canvas, bean.isSelected(), points.get(0));
        } else if (points.size() == DrawType.FIBONACCI.getPointSize()) {
            DrawingPoint pointHigh = points.get(1);
            DrawingPoint pointLow = points.get(0);
            float priceHigh = mParent.getDrawMainYToPrice(pointHigh.getY());
            float priceLow = mParent.getDrawMainYToPrice(pointLow.getY());

            float chang = priceHigh - priceLow;
            float[] price = new float[FIBONACCI_PARAMS.length];
            for (int i = 0; i < FIBONACCI_PARAMS.length; i++) {
                if (i == 0) {
                    price[i] = priceHigh;
                } else if (i == FIBONACCI_PARAMS.length - 1) {
                    price[i] = priceLow;
                } else {
                    price[i] = priceHigh - (FIBONACCI_PARAMS[i] * chang);
                }
            }

            float[] y = new float[FIBONACCI_PARAMS.length];
            for (int i = 0; i < FIBONACCI_PARAMS.length; i++) {
                if (i == 0) {
                    y[i] = pointHigh.getY();
                } else if (i == FIBONACCI_PARAMS.length - 1) {
                    y[i] = pointLow.getY();
                } else {
                    y[i] = mParent.getDrawMainY(price[i]);
                }
            }

            Path clickPath = bean.getClickPath();
            clickPath.reset();
            clickPath.moveTo(pointHigh.getX(), pointHigh.getY());
            clickPath.lineTo(pointLow.getX(), pointHigh.getY());
            clickPath.lineTo(pointLow.getX(), pointLow.getY());
            clickPath.lineTo(pointHigh.getX(), pointLow.getY());
            clickPath.close();

            bean.getRegion().setPath(clickPath, mMainRegion);
            mPointPaint.setAlpha(26);
            canvas.drawPath(clickPath, mPointPaint);

            float textEndX = Math.min(pointHigh.getX(), pointLow.getX()) - mParent.dip2px(5);
            for (int i = 0; i < y.length; i++) {
                canvas.drawLine(pointHigh.getX(), y[i], pointLow.getX(), y[i], mPathPaint);
                String text = FIBONACCI_PARAMS[i] + "(" + mParent.formatValue(price[i]) + ")";
                float textStartX = textEndX - mPointPaint.measureText(text);
                mPointPaint.setAlpha(255);
                canvas.drawText(text, textStartX, mParent.fixTextY1(y[i]), mPointPaint);
            }

            drawPoint(canvas, bean.isSelected(), points.get(0));
            drawPoint(canvas, bean.isSelected(), points.get(1));
        }
    }

    private void drawParallelogram(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }
        Path path = bean.getPath();
        path.reset();
        for (int i = 0; i < points.size(); i++) {
            DrawingPoint point = points.get(i);
            if (i == 0) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
            if (i == 2) {
                //ax + (cx - bx)
                float targetX = points.get(0).getX() + (points.get(2).getX() - points.get(1).getX());
                //ay + (cy - by)
                float targetY = points.get(0).getY() + (points.get(2).getY() - points.get(1).getY());
                path.lineTo(targetX, targetY);
                path.close();
                bean.getRegion().setPath(path, mMainRegion);
            }
        }
        canvas.drawPath(path, mPathPaint);
        for (DrawingPoint point : points) {
            drawPoint(canvas, bean.isSelected(), point);
        }
    }

    private void drawRectangle(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }

        Path path = bean.getPath();
        path.reset();
        for (int i = 0; i < points.size(); i++) {
            DrawingPoint point = points.get(i);
            float x = point.getX();
            float y = point.getY();
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
                path.close();
            }
        }

        path.computeBounds(mDrawingBeanRectF, true);

        Path clickPath = bean.getClickPath();
        clickPath.reset();
        clickPath.moveTo(mDrawingBeanRectF.left, mDrawingBeanRectF.top);
        clickPath.lineTo(mDrawingBeanRectF.left, mDrawingBeanRectF.bottom);
        clickPath.lineTo(mDrawingBeanRectF.right, mDrawingBeanRectF.bottom);
        clickPath.lineTo(mDrawingBeanRectF.right, mDrawingBeanRectF.top);
        clickPath.close();
        canvas.drawPath(clickPath, mPathPaint);
        bean.getRegion().setPath(clickPath, mMainRegion);

        for (int i = 0; i < points.size(); i++) {
            DrawingPoint point = points.get(i);
            drawPoint(canvas, bean.isSelected(), point);
        }
    }

    private void drawParallel(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }
        float[] x = new float[points.size()];
        float[] y = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            DrawingPoint p = points.get(i);
            x[i] = p.getX();
            y[i] = p.getY();
        }

        Path path = bean.getPath();
        path.reset();
        float[] clickPathX = new float[4];
        float[] clickPathY = new float[4];
        for (int i = 0; i < x.length; i++) {
            DrawingPoint point = points.get(i);
            if (i == 0 || i == 1) {
                clickPathX[i] = x[i];
                clickPathY[i] = y[i];
                if (i == 0) {
                    path.moveTo(x[i], y[i]);
                } else {
                    path.lineTo(x[i], y[i]);
                    canvas.drawPath(path, mPathPaint);
                }
                drawPoint(canvas, bean.isSelected(), point);
            }
            if (i == 2) {
                path.computeBounds(mDrawingBeanRectF, true);
                float centerX = mDrawingBeanRectF.centerX();
                float centerY = mDrawingBeanRectF.centerY();
                if (point.getOffsetY() == 0) {
                    float distance0 = PointUtil.findDistance(new PointF(x[0], y[0]), new PointF(x[2], y[2]));
                    float distance1 = PointUtil.findDistance(new PointF(x[1], y[1]), new PointF(x[2], y[2]));
                    float distance2 = PointUtil.findDistance(new PointF(centerX, centerY), new PointF(x[2], y[2]));
                    float shortLen = Math.min(distance0, Math.min(distance1, distance2));
                    if (y[2] < centerY) {
                        shortLen = -shortLen;
                    }
                    point.setOffsetY(shortLen);
                    canvas.drawPath(path, mPathPaint);
                }

                float offsetY = point.getOffsetY();
                path.reset();
                path.moveTo(x[0], y[0] + offsetY);
                path.lineTo(x[1], y[1] + offsetY);
                canvas.drawPath(path, mPathPaint);

                path.computeBounds(mDrawingBeanRectF, true);
                centerX = mDrawingBeanRectF.centerX();
                centerY = mDrawingBeanRectF.centerY();
                point.setX(centerX);
                point.setY(centerY);
                drawPoint(canvas, bean.isSelected(), point);

                path.reset();
                path.moveTo(x[0], y[0] + offsetY / 2);
                path.lineTo(x[1], y[1] + offsetY / 2);
                PathEffect pathEffect = mPathPaint.getPathEffect();
                mPathPaint.setPathEffect(new DashPathEffect(new float[]{
                        mParent.dip2px(3),
                        mParent.dip2px(3)}, 0));
                canvas.drawPath(path, mPathPaint);
                mPathPaint.setPathEffect(pathEffect);

                clickPathX[2] = x[1];
                clickPathY[2] = y[1] + offsetY;

                clickPathX[3] = x[0];
                clickPathY[3] = y[0] + offsetY;
            }
        }

        if (points.size() >= DrawType.PARALLEL.getPointSize()) {
            Path clickPath = bean.getClickPath();
            clickPath.reset();
            for (int i = 0; i < clickPathX.length; i++) {
                if (i == 0) {
                    clickPath.moveTo(clickPathX[i], clickPathY[i]);
                } else {
                    clickPath.lineTo(clickPathX[i], clickPathY[i]);
                }
            }
            clickPath.close();
            bean.getRegion().setPath(clickPath, mMainRegion);
            mPointPaint.setAlpha(26);
            canvas.drawPath(clickPath, mPointPaint);
        }
    }

    private void drawPriceLine(Canvas canvas, DrawingBean bean) {
        drawHorizontalLine(canvas, bean);
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }
        float x = points.get(0).getX();
        float y = points.get(0).getY();
        String price = mParent.formatValue(mParent.getMainYToPrice(y));
        canvas.drawText(price, x - mParent.dip2px(2), y - mParent.dip2px(4), mPointPaint);
    }

    private void drawRayLine(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }

        if (points.size() <= 1) {
            drawPoint(canvas, bean.isSelected(), points.get(0));
            return;
        }

        DrawingPoint point0 = points.get(0);
        DrawingPoint point1 = points.get(1);
        float x0 = point0.getX();
        float y0 = point0.getY();
        float x1 = point1.getX();
        float y1 = point1.getY();

        double length = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
        double targetX = x1 + (x1 - x0) / length * (mParent.getWidth() * 2);
        double targetY = y1 + (y1 - y0) / length * (mParent.getWidth() * 2);

        Path path = bean.getPath();
        path.reset();
        path.moveTo(x0, y0);
        path.lineTo((float) targetX, (float) targetY);
        canvas.drawPath(path, mPathPaint);

        RectF rectF2 = point1.getRectF2();
        rectF2.set(
                (float) targetX - mArc2Radius,
                (float) targetY - mArc2Radius,
                (float) targetX + mArc2Radius,
                (float) targetY + mArc2Radius
        );

        List<RectF> pointRectFs = new ArrayList<>();
        pointRectFs.add(point0.getRectF2());
        pointRectFs.add(rectF2);
        connectClickPath(bean, pointRectFs);

        for (int i = 0; i < points.size(); i++) {
            DrawingPoint point = points.get(i);
            drawPoint(canvas, bean.isSelected(), point);
        }
    }

    private void drawVerticalLine(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        float x = 0;
        for (int i = 0; i < points.size(); i++) {
            DrawingPoint p = points.get(i);
            if (i == 0) {
                x = p.getX();
            } else {
                p.setX(x);
            }
        }
        drawTrendLine(canvas, bean);
    }

    private void drawHorizontalLine(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        if (points.isEmpty()) {
            return;
        }
        DrawingPoint point = points.get(0);
        float x = point.getX();
        float y = point.getY();
        if (DrawType.HORIZONTAL_LINE == bean.getType()) {
            x = 0;
        }
        Path path = bean.getPath();
        path.reset();
        path.moveTo(x, y);
        path.lineTo(mParent.getWidth(), y);
        canvas.drawPath(path, mPathPaint);

        drawPoint(canvas, bean.isSelected(), point);

        Path clickPath = bean.getClickPath();
        clickPath.reset();
        clickPath.moveTo(x, y - mArc2Radius);
        clickPath.lineTo(x, y + mArc2Radius);
        clickPath.lineTo(mParent.getWidth(), y + mArc2Radius);
        clickPath.lineTo(mParent.getWidth(), y - mArc2Radius);
        clickPath.close();
        bean.getRegion().setPath(clickPath, mMainRegion);
    }

    private void drawTrendLine(Canvas canvas, DrawingBean bean) {
        List<DrawingPoint> points = bean.getPoints();
        Path path = bean.getPath();
        path.reset();
        for (int i = 0; i < points.size(); i++) {
            DrawingPoint point = points.get(i);
            float x = point.getX();
            float y = point.getY();
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, mPathPaint);

        List<RectF> pointRectFs = new ArrayList<>();
        for (DrawingPoint point : points) {
            drawPoint(canvas, bean.isSelected(), point);
            pointRectFs.add(point.getRectF2());
        }
        connectClickPath(bean, pointRectFs);
    }

    private void connectClickPath(DrawingBean bean, List<RectF> pointRectFs) {
        if (pointRectFs.size() < 2) {
            return;
        }

        for (int i = 0; i < pointRectFs.size() - 1; i++) {
            Path path = createPathRectF(bean.getClickPath(), pointRectFs.get(i), pointRectFs.get(i + 1));
            bean.getRegion(i).setPath(path, mMainRegion);
        }
    }

    private Path createPathRectF(Path path, RectF rf0, RectF rf1) {
        float circleAX = rf0.centerX();
        float circleAY = rf0.centerY();
        float circleBX = rf1.centerX();
        float circleBY = rf1.centerY();

        double angle = Math.atan2(circleBY - circleAY, circleBX - circleAX);
        float width = (float) Math.sqrt(Math.pow(circleBX - circleAX, 2) + Math.pow(circleBY - circleAY, 2));
        float height = mArcRadius;  // 自定义矩形高度

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        mRotatedPoints[0] = circleAX;
        mRotatedPoints[1] = circleAY - height / 2;
        mRotatedPoints[2] = circleAX + width;
        mRotatedPoints[3] = circleAY - height / 2;
        mRotatedPoints[4] = circleAX + width;
        mRotatedPoints[5] = circleAY + height / 2;
        mRotatedPoints[6] = circleAX;
        mRotatedPoints[7] = circleAY + height / 2;

        for (int i = 0; i < mRotatedPoints.length; i += 2) {
            float rotatedX = circleAX + (mRotatedPoints[i] - circleAX) * cos - (mRotatedPoints[i + 1] - circleAY) * sin;
            float rotatedY = circleAY + (mRotatedPoints[i] - circleAX) * sin + (mRotatedPoints[i + 1] - circleAY) * cos;
            mRotatedPoints[i] = rotatedX;
            mRotatedPoints[i + 1] = rotatedY;
        }

        path.reset();
        path.moveTo(mRotatedPoints[0], mRotatedPoints[1]);
        path.lineTo(mRotatedPoints[2], mRotatedPoints[3]);
        path.lineTo(mRotatedPoints[4], mRotatedPoints[5]);
        path.lineTo(mRotatedPoints[6], mRotatedPoints[7]);
        path.close();

        return path;
    }

    public void setMainRegion(int left, int top, int right, int bottom) {
        mMainRegion.set(left, top, right, bottom);
    }

    private void drawPoint(Canvas canvas, boolean isSelected, DrawingPoint point) {
        float x = point.getX();
        float y = point.getY();
        RectF rectF = point.getRectF();
        rectF.set(
                x - mArcRadius,
                y - mArcRadius,
                x + mArcRadius,
                y + mArcRadius
        );
        RectF rectF2 = point.getRectF2();
        rectF2.set(
                x - mArc2Radius,
                y - mArc2Radius,
                x + mArc2Radius,
                y + mArc2Radius
        );
        if (isSelected) {
            drawPoint2(canvas, point);
        }
    }

    private void drawPoint2(Canvas canvas, DrawingPoint point) {
        mPointPaint.setAlpha(41);
        canvas.drawArc(point.getRectF(), 0, 360, true, mPointPaint);

        mPointPaint.setAlpha(255);
        canvas.drawArc(point.getRectF2(), 0, 360, true, mPointPaint);
    }

    public void setTypeface(Typeface typeface) {
        mPointPaint.setTypeface(typeface);
    }
}
