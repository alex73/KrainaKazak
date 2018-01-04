package org.alex73.android.dzietkam.playbook;

import org.alex73.android.dzietkam.imagecache.Dimension;
import org.alex73.android.dzietkam.util.ImageViewBase;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageViewBookPage extends ImageViewBase {
    private BookLoader pl;
    private int pageIndex;

    public ImageViewBookPage(Context context) {
        super(context);
    }

    public void setContent(BookLoader pl, int pageIndex) {
        this.pl = pl;
        this.pageIndex = pageIndex;
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
}
