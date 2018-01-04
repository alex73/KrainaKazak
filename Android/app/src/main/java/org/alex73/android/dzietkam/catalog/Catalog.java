package org.alex73.android.dzietkam.catalog;

public class Catalog {
    public String baseUrl;
    public Item items;

    public Item getItem(String path) {
        String[] ps = path.split("/");
        if (!ps[0].isEmpty()) {
            throw new RuntimeException("Wrong path: " + path);
        }
        Item it = items;
        for (int i = 1; i < ps.length && it != null; i++) {
            it = findChild(it, ps[i]);
        }
        if (it == null) {
            throw new RuntimeException("Wrong path: " + path);
        }
        return it;
    }

    private Item findChild(Item item, String id) {
        for (Item it : item.items) {
            if (id.equals(it.id)) {
                return it;
            }
        }
        return null;
    }

    public static String getPath(Item item) {
        String p = item.id;
        Item it = item.parent;
        while (it != null) {
            p = it.id + '/' + p;
            it = it.parent;
        }
        return p;
    }

    public static String getTitlePath(Item item) {
        String p = item.id;
        Item it = item.parent;
        while (it != null) {
            p = (it.title != null ? it.title : it.id) + '/' + p;
            it = it.parent;
        }
        return p;
    }

    public void setupParents() {
        setupParents(items);
    }

    private void setupParents(Item item) {
        if (item.items == null) {
            return;
        }
        for (Item it : item.items) {
            it.parent = item;
            setupParents(it);
        }
    }

    public int getItemIndexInParent(Item item) {
        for (int i = 0; i < item.parent.items.size(); i++) {
            if (item == item.parent.items.get(i)) {
                return i;
            }
        }
        return -1;
    }
}
