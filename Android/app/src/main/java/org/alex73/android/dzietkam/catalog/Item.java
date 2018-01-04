package org.alex73.android.dzietkam.catalog;

import java.util.List;
import java.util.Map;

public class Item {
    public String id;
    public String type;
    public String title;
    public String description;
    public String cover;
    public FileInfo file;
    public Map<String,String> settings;
    public List<Item> items; // TODO
    public transient Item parent;
}
