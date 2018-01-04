package org.alex73.android.dzietkam.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.play.youtube.PlayYoutubeActivity;

import java.io.File;
import java.util.List;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        ListPreference list = (ListPreference) findPreference("pref_storage");
        List<File> dirs = CatalogLoader.getDataRoots();
        String[] dir_values = new String[dirs.size()];
        String[] dir_names = new String[dirs.size()];
        for (int i = 0; i < dir_values.length; i++) {
            dir_values[i] = dirs.get(i).getPath();
        }
        for (int i = 0; i < dir_names.length; i++) {
            int freeMB = Math.round(dirs.get(i).getFreeSpace() / 1024f / 1024f);
            dir_names[i] = forDisplay(dir_values[i]) + " (" + freeMB + " МіБ)";
        }
        list.setEntries(dir_names);
        list.setEntryValues(dir_values);
        list.setDefaultValue(dir_names[0]);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        updateSummary(CatalogLoader.getDataRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals("pref_storage")) {
            String storage = sharedPreferences.getString("pref_storage", "");
            if (!storage.equals(CatalogLoader.getDataRoot())) {
                new AlertDialog.Builder(this).setTitle("Сховішча")
                        .setMessage("Выдаліць файлы з папярэдняга сховішча (" + forDisplay(CatalogLoader.getDataRoot()) + ") ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                CatalogLoader.removeAll();
                                CatalogLoader.initDataRoot(SettingsActivity.this);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                CatalogLoader.initDataRoot(SettingsActivity.this);
                            }
                        }).show();
            }
            updateSummary(storage);
        }
    }

    private void updateSummary(String place) {
        Preference storagePref = findPreference("pref_storage");
        storagePref.setSummary(forDisplay(place));
    }

    private String forDisplay(String path) {
        int p = path.indexOf("/Android/data/org.alex73.android.dzietkam");
        if (p < 0) {
            p = path.indexOf("org.alex73.android.dzietkam");
        }
        if (p < 0) {
            p = path.length();
        }
        return path.substring(0, p);
    }

    int getCommonLatestCount(String[] names) {
        int i;
        m:
        for (i = 1; ; i++) {
            for (int j = 0; j < names.length; j++) {
                if (names[j].length() < i) {
                    break m;
                }
                char n0 = names[0].charAt(names[0].length() - i);
                char n1 = names[j].charAt(names[j].length() - i);
                if (n0 != n1) {
                    break m;
                }
            }
        }
        for (int k = i - 1; k >= 0; k--) {
            if (names[0].charAt(names[0].length() - k) == '/') {
                return k;
            }
        }
        return 0;
    }
}
