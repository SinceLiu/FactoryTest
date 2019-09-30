package com.dinghmcn.android.wificonnectclient.scanCode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dinghmcn.android.wificonnectclient.R;

import cn.szx.simplescanner.base.IViewFinder;

/**
 * 覆盖在相机预览上的view，包含扫码框、扫描线、扫码框周围的阴影遮罩等
 */
public class ViewFinderView extends RelativeLayout implements IViewFinder {
    private Rect framingRect;//扫码框所占区域
    private float widthRatio = 0.6f;//扫码框宽度占view总宽度的比例
    private float heightWidthRatio = 1.0f;//扫码框的高宽比
    private int leftOffset = -1;//扫码框相对于左边的偏移量，若为负值，则扫码框会水平居中
    private int topOffset = -1;//扫码框相对于顶部的偏移量，若为负值，则扫码框会竖直居中

    private boolean isLaserEnabled = true;//是否显示扫描线
    private int laserAlphaIndex;
    private int[] laserActiveDistance;      //扫描线的位置数组
    private static final int LASER_SPEED = 22;        //扫描线的速度
    private int middle = 0;
    private static final long animationDelay = 100l;
    private final int laserColor = Color.parseColor("#FFFFFF"); //扫描线颜色

    private final int maskColor = Color.parseColor("#60000000");
    private final int borderColor = Color.parseColor("#FFFFFF");        //角的颜色
    private final int borderStrokeWidth = 6;
    protected int borderLineLength = 100;

    protected Paint laserPaint;
    protected Paint maskPaint;
    protected Paint borderPaint;

    private TextView mTextView;

    public ViewFinderView(Context context) {
        super(context);
        initDraw();
        initLayout();
    }

    private void initDraw() {
        setWillNotDraw(false);//需要进行绘制

        //扫描线画笔
        laserPaint = new Paint();
        laserPaint.setColor(laserColor);
        laserPaint.setStyle(Paint.Style.FILL);
        laserPaint.setAlpha(200);

        //阴影遮罩画笔
        maskPaint = new Paint();
        maskPaint.setColor(maskColor);

        //边框画笔
        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderStrokeWidth);
        borderPaint.setAntiAlias(true);
    }

    private void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_view_finder, this, true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getFramingRect() == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);

        if (isLaserEnabled) {
            drawLaser(canvas);
        }
    }

    /**
     * 绘制扫码框四周的阴影遮罩
     */
    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Rect framingRect = getFramingRect();

        canvas.drawRect(0, 0, width, framingRect.top, maskPaint);//扫码框顶部阴影
        canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom, maskPaint);//扫码框左边阴影
        canvas.drawRect(framingRect.right, framingRect.top, width, framingRect.bottom, maskPaint);//扫码框右边阴影
        canvas.drawRect(0, framingRect.bottom, width, height, maskPaint);//扫码框底部阴影
    }

    /**
     * 绘制扫码框的边框
     */
    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // Top-left corner
        Path path = new Path();
        path.moveTo(framingRect.left, framingRect.top + borderLineLength);
        path.lineTo(framingRect.left, framingRect.top);
        path.lineTo(framingRect.left + borderLineLength, framingRect.top);
        canvas.drawPath(path, borderPaint);

        // Top-right corner
        path.moveTo(framingRect.right, framingRect.top + borderLineLength);
        path.lineTo(framingRect.right, framingRect.top);
        path.lineTo(framingRect.right - borderLineLength, framingRect.top);
        canvas.drawPath(path, borderPaint);

        // Bottom-right corner
        path.moveTo(framingRect.right, framingRect.bottom - borderLineLength);
        path.lineTo(framingRect.right, framingRect.bottom);
        path.lineTo(framingRect.right - borderLineLength, framingRect.bottom);
        canvas.drawPath(path, borderPaint);

        // Bottom-left corner
        path.moveTo(framingRect.left, framingRect.bottom - borderLineLength);
        path.lineTo(framingRect.left, framingRect.bottom);
        path.lineTo(framingRect.left + borderLineLength, framingRect.bottom);
        canvas.drawPath(path, borderPaint);
    }

    /**
     * 绘制扫描线
     */
    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();
        if (null == laserActiveDistance) {  //若为null,则加入扫描位置信息数据
            laserActiveDistance = new int[(framingRect.bottom - framingRect.top) / LASER_SPEED];
            for (int i = 0; i < laserActiveDistance.length; i++) {
                laserActiveDistance[i] = framingRect.top + (i + 1) * LASER_SPEED;
            }
        }
        laserAlphaIndex = (laserAlphaIndex + 1) % laserActiveDistance.length;
        middle = laserActiveDistance[laserAlphaIndex];
        canvas.drawRect(framingRect.left + LASER_SPEED, middle - 2, framingRect.right - LASER_SPEED, middle + 2, laserPaint);
        //区域刷新
        postInvalidateDelayed(animationDelay,
                framingRect.left,
                framingRect.top,
                framingRect.right,
                framingRect.bottom);
    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    /**
     * 设置framingRect的值（扫码框所占的区域）
     */
    public synchronized void updateFramingRect() {
        Point viewSize = new Point(getWidth(), getHeight());
        int width, height;
        width = (int) (getWidth() * widthRatio);
        height = (int) (heightWidthRatio * width);

        int left, top;
        if (leftOffset < 0) {
            left = (viewSize.x - width) / 2;//水平居中
        } else {
            left = leftOffset;
        }
        if (topOffset < 0) {
            top = (viewSize.y - height) / 2;//竖直居中
        } else {
            top = topOffset;
        }
        framingRect = new Rect(left, top, left + width, top + height);
    }

    public Rect getFramingRect() {
        return framingRect;
    }
}