package org.alex73.android.dzietkam.playbook.textpagesstore;

import java.util.ArrayList;
import java.util.List;

public class Page {
    public int width, height;
    public List<Text> texts;

    public Page() {
        texts = new ArrayList<>();
    }

    public List<Text> getTexts() {
        return texts;
    }
}
