package org.alex73.android.dzietkam.imagecache;

import android.graphics.Rect;

public class Rectangle {
    public int x, y, width, height;

    public Rectangle() {
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public Dimension getDimension() {
        return new Dimension(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rectangle) {
            Rectangle d = (Rectangle) o;
            return x == d.x && y == d.y && width == d.width && height == d.height;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[+" + x + "+" + y + "/" + width + "x" + height + "]";
    }
}
