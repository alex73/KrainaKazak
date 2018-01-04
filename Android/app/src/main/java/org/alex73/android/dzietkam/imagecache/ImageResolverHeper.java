package org.alex73.android.dzietkam.imagecache;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageResolverHeper {

    /**
     * Load bitmap and resize for specific size.
     */
    public static Bitmap resizeBitmap(Resources res, int resId, Dimension bitmapSize, int w, int h,
            boolean center) {
        Rectangle imageOutput = new Rectangle();
        Rectangle widgetOutput = new Rectangle();
        ImageCalc.fixOutRectangles(bitmapSize, new Dimension(w, h), center ? new Dimension() : null,
                imageOutput, widgetOutput);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        Bitmap bmp = BitmapFactory.decodeResource(res, resId, opts);
        bmp.setDensity(0);

        if (imageOutput.x != 0 || imageOutput.y != 0 || bmp.getWidth() != imageOutput.width
                || bmp.getHeight() != imageOutput.height) {
            Bitmap oldbmp=bmp;
            bmp = Bitmap.createBitmap(bmp, imageOutput.x, imageOutput.y, imageOutput.width,
                    imageOutput.height);
            oldbmp.recycle();
            bmp.setDensity(0);
        }

        if (bmp.getWidth() != widgetOutput.width || bmp.getHeight() != widgetOutput.height) {
            Bitmap oldbmp=bmp;
            bmp = Bitmap.createScaledBitmap(bmp, widgetOutput.width, widgetOutput.height, true);
            oldbmp.recycle();
            bmp.setDensity(0);
        }

        return bmp;
    }

}
