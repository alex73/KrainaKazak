package org.alex73.android.dzietkam.imagecache;

public class Dimension {
    public int width, height;

    public Dimension() {
    }

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Dimension) {
            Dimension d = (Dimension) o;
            return width == d.width && height == d.height;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + width + "x" + height + "]";
    }
}
