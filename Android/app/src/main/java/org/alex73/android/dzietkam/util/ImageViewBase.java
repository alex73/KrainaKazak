package org.alex73.android.dzietkam.util;

import org.alex73.android.dzietkam.imagecache.Dimension;
import org.alex73.android.dzietkam.imagecache.ImageCalc;
import org.alex73.android.dzietkam.imagecache.Rectangle;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public abstract class ImageViewBase extends View {
    protected Context mContext;
    protected boolean showCenter;

    protected int resId;
    protected Bitmap mBitmap;

    public ImageViewBase(Context context) {
        super(context);
        this.mContext = context;
        init(null);
    }

    public ImageViewBase(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs);
    }

    public ImageViewBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    @TargetApi(21)
    public ImageViewBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        init(attrs);
    }

    protected void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // System.out.println("onMeasure: " + Long.toHexString(resId) + " " +
        // debugShowSizeMode(widthMeasureSpec)
        // + " " + debugShowSizeMode(heightMeasureSpec));
        // see http://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean needResize = widthMode != View.MeasureSpec.EXACTLY || heightMode != View.MeasureSpec.EXACTLY;

        if (needResize) {
            throw new RuntimeException(getClass() + " shouldn't be resized");
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private String debugShowSizeMode(int measureSpec) {
        String m = MeasureSpec.getSize(measureSpec) + "/";
        switch (MeasureSpec.getMode(measureSpec)) {
        case View.MeasureSpec.AT_MOST:
            m += "AT_MOST/";
            break;
        case View.MeasureSpec.EXACTLY:
            m += "EXACTLY/";
            break;
        case View.MeasureSpec.UNSPECIFIED:
            m += "UNSPECIFIED/";
            break;
        default:
            m += MeasureSpec.getMode(measureSpec);
            break;
        }
        return m;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // System.out.println("onLayout " + changed + " " + left + " " + top + " " + right + " " + bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // System.out.println("onSizeChanged " + Long.toHexString(resId) + " " + w + "x" + h + " from " + oldw
        // + "x" + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        updateContent();
    }

    public void setImageResource(@DrawableRes int resId) {
        this.resId = resId;
        updateContent();
        invalidate();
    }

    protected void updateContent() {
        int w = getWidth();
        int h = getHeight();

        if (w > 1 && h > 1) {
            if (isInEditMode()) {
                // just load bitmap for preview
                mBitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
            } else {
                resolveBitmap(w, h);
            }
        }
    }

    protected abstract void resolveBitmap(int width, int height);

    Rect src = new Rect();
    Rect dst = new Rect();
    Paint p = new Paint();
    Dimension imageSize = new Dimension();
    Dimension outputSize = new Dimension();
    Dimension center = new Dimension();
    Rectangle imageOutput = new Rectangle();
    Rectangle widgetOutput = new Rectangle();

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap dr = mBitmap;
        if (dr == null) {
            return;
        }

        int w = getWidth();
        int h = getHeight();

        if (isInEditMode()) {
            imageSize.width = dr.getWidth();
            imageSize.height = dr.getHeight();
            outputSize.width = w;
            outputSize.height = h;
            ImageCalc.fixOutRectangles(imageSize, outputSize, showCenter ? center : null, imageOutput,
                    widgetOutput);
            src.left = imageOutput.x;
            src.top = imageOutput.y;
            src.right = imageOutput.x + imageOutput.width;
            src.bottom = imageOutput.y + imageOutput.height;

            dst.left = widgetOutput.x;
            dst.top = widgetOutput.y;
            dst.right = widgetOutput.x + widgetOutput.width;
            dst.bottom = widgetOutput.y + widgetOutput.height;
        } else {
            src.left = 0;
            src.top = 0;
            src.right = dr.getWidth();
            src.bottom = dr.getHeight();

            dst.left = (w - dr.getWidth()) / 2;
            dst.top = (h - dr.getHeight()) / 2;
            dst.right = dst.left + dr.getWidth();
            dst.bottom = dst.top + dr.getHeight();
        }

        canvas.drawBitmap(dr, src, dst, p);
    }
}
