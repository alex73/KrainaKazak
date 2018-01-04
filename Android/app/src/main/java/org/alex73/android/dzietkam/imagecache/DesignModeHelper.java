package org.alex73.android.dzietkam.imagecache;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DesignModeHelper {

    public static Dimension getSize(Resources res, int resId) {
        Bitmap bmp = BitmapFactory.decodeResource(res, resId);
        bmp.setDensity(0);
        return new Dimension(bmp.getWidth(), bmp.getHeight());
    }
}
