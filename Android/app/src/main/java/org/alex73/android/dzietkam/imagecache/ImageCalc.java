package org.alex73.android.dzietkam.imagecache;

import org.alex73.android.dzietkam.Logger;

public class ImageCalc {
    private static final Logger log = new Logger(ImageCalc.class);
    /**
     * Calculates bitmap size and position output to screen.
     * 
     * @param imageSize
     *            source image size
     * @param outputSize
     *            output widget size
     * @param centerMin
     *            center min. Will be null if full image should be displayed. width and height can be zero if
     *            not important.
     * @param image
     *            output image part
     * @param output
     *            output place
     */
    public static void fixOutRectangles(Dimension imageSize, Dimension outputSize, Dimension centerMin,
            Rectangle image, Rectangle output) {

        if (centerMin == null) {
            // full image output
            image.x = 0;
            image.y = 0;
            image.width = imageSize.width;
            image.height = imageSize.height;
        } else {
            // center only
            float aspect = 1.0f * outputSize.width / outputSize.height;
            int cropW = imageSize.width;
            int cropH = Math.round(cropW / aspect);
            if (cropH > imageSize.height) {
                cropH = imageSize.height;
                cropW = Math.round(cropH * aspect);
            }
            if (cropW < centerMin.width) {
                cropW = centerMin.width;
                cropH = Math.round(cropW / aspect);
            }
            if (cropH < centerMin.height) {
                cropH = centerMin.height;
                cropW = Math.round(cropH * aspect);
            }
            image.x = Math.max(0, (imageSize.width - cropW) / 2);
            image.y = Math.max(0, (imageSize.height - cropH) / 2);
            image.width = Math.min(imageSize.width, cropW);
            image.height = Math.min(imageSize.height, cropH);
        }

        float aspect = 1.0f * image.width / image.height;

        int newW = outputSize.width;
        int newH = Math.round(newW / aspect);
        if (newH > outputSize.height) {
            newH = outputSize.height;
            newW = Math.round(newH * aspect);
        }
        output.width = newW;
        output.height = newH;
        output.x = (outputSize.width - newW) / 2;
        output.y = (outputSize.height - newH) / 2;
        //log.i("Calculated rect source " + imageSize + " for output " + outputSize + " center " + centerMin
          //      + ": image " + image + " -> output " + output);
    }
}
