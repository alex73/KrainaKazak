package org.alex73.android.dzietkam.playbook.textpagesstore;

import java.util.ArrayList;
import java.util.List;



public class Texts {
    public List<Style> styles;
    public List<Page> pages;

    public Texts() {
        styles = new ArrayList<>();
        pages = new ArrayList<>();
    }

    public List<Style> getStyles() {
        return styles;
    }

    public List<Page> getPages() {
        return pages;
    }
}
