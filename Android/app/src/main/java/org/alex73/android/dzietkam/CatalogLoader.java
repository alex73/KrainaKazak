package org.alex73.android.dzietkam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

public class CatalogLoader {
    private static File dataRoot;
    private static List<File> dataRoots;

    public static void initDataRoot(Context context) {
        dataRoot = null;
        dataRoots = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 19) {
            File[] dirs = context.getExternalFilesDirs("Media");
            if (dirs != null) {
                for (File d : dirs) {
                    if (d != null) {
                        dataRoots.add(d);
                    }
                }
            }
        } else {
            File dir = context.getExternalFilesDir("Media");
            if (dir != null) {
                dataRoots.add(dir);
            }
        }
        if (dataRoots.isEmpty()) {
            File dir = context.getFilesDir();
            if (dir != null) {
                dataRoots.add(dir);
            }
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String storage = sharedPref.getString("pref_storage",null);
        for(File d:dataRoots) {
            if (d.getPath().equals(storage)) {
                dataRoot = d;
                break;
            }
        }
        if (dataRoot==null) {
            dataRoot = dataRoots.get(0);
        }
    }

    public static String getDataRoot() {
        return dataRoot.getPath();
    }

    public static void removeAll() {
        if (dataRoot==null) {
            return;
        }
        removeIn(dataRoot);
    }

    private static void removeIn(File dir) {
        File[] ls=dir.listFiles();
        if (ls==null) {
            return;
        }
        for(File f:ls) {
            if (f.isDirectory()) {
                removeIn(f);
            }
            f.delete();
        }
    }

    public static List<File> getDataRoots() {
        return Collections.unmodifiableList(dataRoots);
    }

    public static Catalog load(Resources resources) throws IOException {
        return load(resources.openRawResource(R.raw.catalog));
    }

    public static Catalog load(InputStream in) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            return new Gson().fromJson(rd, Catalog.class);
        }
    }

    public static long getNotExistSize(Item it) {
        Map<String, Long> files = ListFiles.list(it);
        long r = 0;
        for(Map.Entry<String,Long> en:files.entrySet()) {
            File f = new File(dataRoot, en.getKey());
            if (f.exists()) {
               long delta = en.getValue()-f.length();
               r+=delta>=0?delta:en.getValue();
            } else {
                r+=en.getValue();
            }
        }
        return r;
    }

    public static boolean isItemDownloaded(Item it) {
        return getNotExistSize(it)==0;
    }
    public static void removeDownloaded(Item it) {
        Map<String, Long> files = ListFiles.list(it);
        for(String fn:files.keySet()) {
            new File(dataRoot, fn).delete();
        }
    }

    public static boolean isItemViewed(Context context, Item item) {
        String key = "viewed_"+item.parent.id+"/"+item.id;
        SharedPreferences pref = context.getSharedPreferences("viewed", Context.MODE_PRIVATE);
        return pref.getBoolean(key,false);
    }
    public static void setItemViewed(Context context, Item item, boolean value) {
        String key = "viewed_"+item.parent.id+"/"+item.id;
        SharedPreferences pref = context.getSharedPreferences("viewed", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static File getOneFileNameByExtension(String path, String... extensions) {
        File dir = new File(getDataRoot(), path);
        File[] ls = dir.listFiles();
        if (ls != null) {
            for (String ext : extensions) {
                for (File f : ls) {
                    if (f.isFile() && f.getName().endsWith("." + ext)) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
}
