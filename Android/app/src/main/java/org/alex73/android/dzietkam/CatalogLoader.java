package org.alex73.android.dzietkam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.FileInfo;
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
        BufferedReader rd = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.catalog), "UTF-8"));
        try {
            return new Gson().fromJson(rd, Catalog.class);
        } finally {
            rd.close();
        }
    }

    public static boolean isItemDownloaded(FileInfo fi) {
        if (fi == null) {
            return true;
        }

        if (fi == null) {
            return true;
        }
        if (fi.name.startsWith("#")) {
            return true;
        }
        File file = new File(dataRoot, fi.name);
        return file.exists() && file.length() == fi.size;
    }

    public static File getItemDownloaded(Item item) {
        if (item.file == null) {
            return null;
        }
        if (item.file.name.startsWith("#")) {
            item = item.parent;
        }
        return new File(dataRoot, item.file.name);
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
}
