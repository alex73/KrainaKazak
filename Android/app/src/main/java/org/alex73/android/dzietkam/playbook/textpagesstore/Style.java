package org.alex73.android.dzietkam.playbook.textpagesstore;

public class Style {
    public enum ALIGN_HORIZONTAL {
        LEFT, CENTER, RIGHT
    };

    public enum ALIGN_VERTICAL {
        TOP, CENTER, BOTTOM
    };

    public String name;
    public String font = "sans-serif";
    public boolean bold = false;
    public boolean italic = false;
    public ALIGN_HORIZONTAL alignHorizontal = ALIGN_HORIZONTAL.LEFT;
    public ALIGN_VERTICAL alignVertical = ALIGN_VERTICAL.CENTER;
    public String foreground = "#FFFFFF";
    public String background = "#000000";
    public StyleInsets insets;
}
