package org.alex73.android.dzietkam.catalog;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class Item {
    public String id;
    public String type;
    public String title;
    public String description;
    public String cover;
    public FileInfo file;
    public Map<String, String> settings;
    public List<Item> items; // TODO
    public transient Item parent;
    public transient File f, coverFile;
    public transient List<String> settingsOrder;

    public long getSize() {
        if (f.isDirectory()) {
            long sz = 0;
            for (File ff : FileUtils.listFiles(f, null, true)) {
                sz += ff.length();
            }
            return sz;
        } else {
            return f.length();
        }
    }

    public int count(String expectedType) {
        int c = expectedType.equals(type) ? 1 : 0;
        if (items != null) {
            for (Item sub : items) {
                c += sub.count(expectedType);
            }
        }
        return c;
    }
}
