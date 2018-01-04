package org.alex73.android.dzietkam.imagecache;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImageCalcTest {
    @Test
    public void landscapeInSquare() {
        Dimension imageSize = new Dimension(40, 20);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        ImageCalc.fixOutRectangles(imageSize, outputSize, null, imageRect, outputRect);
        assertEquals(new Rectangle(0, 0, 40, 20), imageRect);
        assertEquals(new Rectangle(0, 5, 20, 10), outputRect);
    }

    @Test
    public void portraitInSquare() {
        Dimension imageSize = new Dimension(20, 40);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        ImageCalc.fixOutRectangles(imageSize, outputSize, null, imageRect, outputRect);
        assertEquals(new Rectangle(0, 0, 20, 40), imageRect);
        assertEquals(new Rectangle(5, 0, 10, 20), outputRect);
    }

    @Test
    public void squareInSquare() {
        Dimension imageSize = new Dimension(40, 40);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        ImageCalc.fixOutRectangles(imageSize, outputSize, null, imageRect, outputRect);
        assertEquals(new Rectangle(0, 0, 40, 40), imageRect);
        assertEquals(new Rectangle(0, 0, 20, 20), outputRect);
    }

    @Test
    public void landscapeCenterInSquare() {
        Dimension imageSize = new Dimension(40, 20);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        ImageCalc.fixOutRectangles(imageSize, outputSize, new Dimension(), imageRect, outputRect);
        assertEquals(new Rectangle(10, 0, 20, 20), imageRect);
        assertEquals(new Rectangle(0, 0, 20, 20), outputRect);
    }

    @Test
    public void portraitCenterInSquare() {
        Dimension imageSize = new Dimension(20, 40);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        ImageCalc.fixOutRectangles(imageSize, outputSize, new Dimension(), imageRect, outputRect);
        assertEquals(new Rectangle(0, 10, 20, 20), imageRect);
        assertEquals(new Rectangle(0, 0, 20, 20), outputRect);
    }

    @Test
    public void landscapeCenterMinInSquare() {
        Dimension imageSize = new Dimension(800, 100);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        Dimension centerMin = new Dimension(400, 100);
        ImageCalc.fixOutRectangles(imageSize, outputSize, centerMin, imageRect, outputRect);
        assertEquals(new Rectangle(200, 0, 400, 100), imageRect);
        assertEquals(new Rectangle(0, 7, 20, 5), outputRect);
    }

    @Test
    public void portraitCenterMinInSquare() {
        Dimension imageSize = new Dimension(100, 800);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        Dimension centerMin = new Dimension(100, 400);
        ImageCalc.fixOutRectangles(imageSize, outputSize, centerMin, imageRect, outputRect);
        assertEquals(new Rectangle(0, 200, 100, 400), imageRect);
        assertEquals(new Rectangle(7, 0, 5, 20), outputRect);
    }

    @Test
    public void landscapeBigCenterMinInSquare() {
        Dimension imageSize = new Dimension(400, 100);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        Dimension centerMin = new Dimension(100, 200);
        ImageCalc.fixOutRectangles(imageSize, outputSize, centerMin, imageRect, outputRect);
        assertEquals(new Rectangle(100, 0, 200, 100), imageRect);
        assertEquals(new Rectangle(0, 5, 20, 10), outputRect);
    }

    @Test
    public void portraitBigCenterMinInSquare() {
        Dimension imageSize = new Dimension(100, 400);
        Dimension outputSize = new Dimension(20, 20);
        Rectangle imageRect = new Rectangle();
        Rectangle outputRect = new Rectangle();
        Dimension centerMin = new Dimension(200, 100);
        ImageCalc.fixOutRectangles(imageSize, outputSize, centerMin, imageRect, outputRect);
        assertEquals(new Rectangle(0, 100, 100, 200), imageRect);
        assertEquals(new Rectangle(5, 0, 10, 20), outputRect);
    }

}
