package org.alex73.android.dzietkam.playbook;

import org.alex73.android.dzietkam.imagecache.Dimension;
import org.alex73.android.dzietkam.playbook.textpagesstore.Page;
import org.alex73.android.dzietkam.playbook.textpagesstore.Texts;
import org.alex73.android.dzietkam.util.ImageViewBase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ImageViewBookPage extends ImageViewBase {
    private BookLoader pl;
    private int pageIndex;
    private Paint paintText;
    private Texts texts;

    public ImageViewBookPage(Context context) {
        super(context);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.RED);
    }

    public void setContent(BookLoader pl, int pageIndex) {
        this.pl = pl;
        this.pageIndex = pageIndex;
        this.texts = pl.getTextPages();
        updateContent();
    }

    @Override
    protected void resolveBitmap(final int width, final int height) {
        new Thread() {
            public void run() {
                Bitmap bmp = pl.loadPicture(pageIndex, new Dimension(width, height), pl.getCropCenterMin());
                mBitmap = bmp;
                postInvalidate();
            }
        }.start();
    }

    @Override
    protected void onDrawOver(Canvas canvas) {
        if (texts != null) {
            Page currentPage = texts.pages.get(pageIndex);
        }
    }
}
