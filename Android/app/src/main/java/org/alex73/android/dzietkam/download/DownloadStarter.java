package org.alex73.android.dzietkam.download;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.ListFiles;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class DownloadStarter {
    public static void start(final Context activityContext, final Context applicationContext,
            final AnalyticsApplication application, final Item it) {
        long size = CatalogLoader.getNotExistSize(it);
        final int sizemb = Math.round(size / 1024f / 1024f);
        final Resources res = activityContext.getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activityContext);
        boolean ask = sharedPref.getBoolean("pref_ask_wifi",true);
        if (sizemb > 0 && DownloadService.isNotWiFi(applicationContext) && ask) {
            new AlertDialog.Builder(activityContext).setTitle(R.string.download_nowifi_title)
                    .setMessage(res.getString(R.string.download_nowifi_message, sizemb))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                                startDownload(activityContext, application, it, size);
                                Toast.makeText(activityContext, R.string.download_start, Toast.LENGTH_SHORT)
                                        .show();
                        }
                    }).setNegativeButton(R.string.no, null).show();
        } else {
            startDownload(activityContext, application, it, size);
            Toast.makeText(activityContext, R.string.download_start, Toast.LENGTH_SHORT).show();
        }
    }

    static void startDownload(final Context activityContext, AnalyticsApplication application,
            final Item it, final long size) {
            application.sendEvent("Download", "Start", it.getPath());

            Intent intent = new Intent(activityContext, DownloadService.class);
            intent.setAction(DownloadService.ACTION_ADD_DOWNLOAD);
            intent.putExtra(DownloadService.EXTRA_NAME, it.title);
            intent.putExtra(DownloadService.EXTRA_ITEM_PATH, it.getPath());
            intent.putExtra(DownloadService.EXTRA_SIZE, size);
            activityContext.startService(intent);
    }
}
