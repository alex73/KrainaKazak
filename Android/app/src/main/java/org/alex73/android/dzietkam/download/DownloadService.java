package org.alex73.android.dzietkam.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alex73.android.dzietkam.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DownloadService extends Service {
    public static final String ACTION_CLOSE = "org.alex73.android.dzietkam.DownloadClose";
    public static final String ACTION_ADD_DOWNLOAD = "org.alex73.android.dzietkam.AddDownload";
    public static final String ACTION_FINISH_DOWNLOAD = "org.alex73.android.dzietkam.FinishDownload";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STORE = "store";
    public static final String EXTRA_SIZE = "size";

    public static final String CHANNEL_ID = "KrainaKazarDownloader";

    private static final int NOTIFICATION_ID = 1;

    private final Logger log = new Logger(getClass());

    NotificationManager mNotificationManager;

    private DownloadThread mDownloadThread;
    private boolean forceStop;
    private List<DownloadPart> downloadQueue = new ArrayList<DownloadPart>();

    private int currentPercent;
    private long downloadedSize, totalSize;

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
        }
    }

    @RequiresApi(26)
    private void createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID,
                "KrainaKazak downloader service", NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNotificationManager.createNotificationChannel(chan);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_ADD_DOWNLOAD.equals(action)) {
                addDownload(new DownloadPart(intent.getData(), intent.getStringExtra(EXTRA_NAME),
                        intent.getStringExtra(EXTRA_STORE), intent.getLongExtra(EXTRA_SIZE, 0)));
                return START_REDELIVER_INTENT;
            } else if (ACTION_CLOSE.equals(action)) {
                stop();
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                log.v("Connection type changed: ");
            }
        }
        return START_NOT_STICKY;
    }

    public static boolean isNotWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            return true;
        }
        return activeNetwork.getType() != ConnectivityManager.TYPE_WIFI;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void stop() {
        log.i("Stop downloads");
        synchronized (DownloadService.this) {
            forceStop = true;
        }
    }

    void addDownload(DownloadPart download) {
        log.i("Add download " + download.uri);
        synchronized (DownloadService.this) {
            forceStop = false;
            totalSize += download.size;
            downloadQueue.add(download);
            if (mDownloadThread == null) {
                currentPercent = -1;
                totalSize = download.size;
                mDownloadThread = new DownloadThread();
                mDownloadThread.start();
                startForeground(NOTIFICATION_ID,
                        NotificationBuilder.createDownloadNotification(DownloadService.this, "", 0));
            }
        }
    }

    void download(final DownloadPart download) throws Exception {
        log.i("Download start: " + download.uri + " to " + download.file);
        new File(download.file).getParentFile().mkdirs();

        new Downloader(download) {
            @Override
            protected boolean needToStop() {
                return forceStop;
            }

            @Override
            protected void downloaded(long count) {
                downloadedSize += count;
                int newPercent = Math.round(100f * downloadedSize / totalSize);
                if (currentPercent != newPercent) {
                    currentPercent = newPercent;
                    mNotificationManager.notify(NOTIFICATION_ID, NotificationBuilder
                            .createDownloadNotification(DownloadService.this, download.name, currentPercent));
                }
            }
        }.process();
    }

    class DownloadThread extends Thread {
        @Override
        public void run() {
            log.v("Service thread started");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            boolean wasError = false;
            while (true) {
                DownloadPart d;
                synchronized (DownloadService.this) {
                    if (forceStop || downloadQueue.isEmpty()) {
                        mDownloadThread = null;
                        break;
                    }
                    d = downloadQueue.remove(0);
                }
                try {
                    download(d);
                } catch (Exception ex) {
                    log.e("Error download", ex);
                    wasError = true;
                    Intent intent = new Intent(ACTION_FINISH_DOWNLOAD);
                    intent.putExtra("error", ex.getMessage());
                    intent.putExtra("filename", d.file);
                    LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
                }
            }
            stopSelf();
            totalSize = 0;

            if (!wasError) {
                Intent intent = new Intent(ACTION_FINISH_DOWNLOAD);
                LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
            }

            log.v("Service thread finished");
        }
    }
}
