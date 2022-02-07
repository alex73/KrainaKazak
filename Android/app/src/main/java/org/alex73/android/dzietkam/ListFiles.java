package org.alex73.android.dzietkam;

import android.content.res.Resources;

import org.alex73.android.dzietkam.catalog.Item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ListFiles {
    static String baseUrl;
    static Map<String, Long> files;

    public static void load(Resources resources, String bu) throws IOException {
        baseUrl = bu;
        files = new HashMap<>();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.files), "UTF-8"))) {
            while (true) {
                String s = rd.readLine();
                if (s == null) {
                    break;
                }
                int p = s.lastIndexOf(':');
                if (p<0) {
                    throw new RuntimeException("Wrong files list: "+s);
                }
                try {
                    files.put(s.substring(0,p), Long.parseLong(s.substring(p+1)));
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static Map<String, Long> list(Item it) {
        Map<String, Long> r;
        if (it.type == null) {
            r = new TreeMap<>();
            for (Item ch : it.items) {
                r.putAll(list(ch));
            }
            return r;
        }
        switch (it.type) {
            case "audio":
                r = listAudio(it.getPath());
                break;
            case "text":
                r = listText(it.getPath());
                break;
            case "book":
                r = listBook(it.getPath());
                break;
            case "youtube":
                return new TreeMap<>(); // do not retrieve cover
            default:
                throw new RuntimeException("Unknown type: " + it.type);
        }
        addCover(it, r);
        if (it.parent != null) {
            addCover(it.parent, r);
            if (it.parent.parent != null) {
                addCover(it.parent.parent, r);
            }
        }
        return r;
    }

    private static void addCover(Item it, Map<String, Long> r) {
        if (it.cover != null) {
            String coverPath = it.getPath()+'/'+it.cover;
            coverPath = coverPath.replaceAll("/[^/]+/\\.\\./", "/").replaceAll("/{2,}", "/");
            Long sz = files.get(coverPath);
            if (sz==null) {
                throw new RuntimeException("No cover: "+coverPath);
            }
            r.put(coverPath, sz);
        }
    }

    private static Map<String, Long> listAudio(String id) {
        Map<String, Long> r = new TreeMap<>();
        String prefix = id + '.';
        for (String p : files.keySet()) {
            if (p.startsWith(prefix)) {
                r.put(p, files.get(p));
            }
        }
        return r;
    }

    private static Map<String, Long> listText(String id) {
        Map<String, Long> r = new TreeMap<>();
        String fn = id + ".text";
        if (files.containsKey(fn)) {
            r.put(fn, files.get(fn));
        }
        return r;
    }

    private static Map<String, Long> listBook(String id) {
        Map<String, Long> r = new TreeMap<>();
        String prefix = id + '/';
        for (String p : files.keySet()) {
            if (p.startsWith(prefix)) {
                r.put(p, files.get(p));
            }
        }
        return r;
    }
}
