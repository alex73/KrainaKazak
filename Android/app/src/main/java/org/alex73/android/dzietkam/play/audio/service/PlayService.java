package org.alex73.android.dzietkam.play.audio.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.download.NotificationBuilder;
import org.alex73.android.dzietkam.playbook.BookLoader;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;
import org.alex73.android.dzietkam.util.PackFileWrapper;

import java.io.File;
import java.io.IOException;

public class PlayService extends Service {
    public static final String ACTION_START = "start";
    public static final String ACTION_FINISH = "finish";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_SEEK = "seek";
    public static final String ACTION_VOLUME = "volume";
    public static final String ACTION_FORWARD = "forward";
    public static final String ACTION_BACKWARD = "backward";
    public static final String EXTRA_SEEK = "seek";
    public static final String EXTRA_VOLUME = "volume";

    private static final int NOTIFICATION_ID = 2;

    private static final Logger log = new Logger(PlayService.class);

    PackFileWrapper songFile;
    protected static final PlayStatus currentStatus = new PlayStatus();
    private PackFileWrapper.FileObjectDataSource currentDataSource;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START:
                    AnalyticsApplication application = (AnalyticsApplication) getApplication();
                    currentStatus.itemPath = intent.getData().getPath();
                    currentStatus.item = application.catalog.getItem(currentStatus.itemPath);
                    currentStatus.itemIndex = application.catalog.getItemIndexInParent(currentStatus.item);
                    startSong();
                    break;
                case ACTION_FINISH:
                    stopAll();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_RESUME:
                    play();
                    break;
                case ACTION_SEEK:
                    int position = intent.getIntExtra(EXTRA_SEEK, 0);
                    try {
                        currentStatus.player.seekTo(position);
                    } catch (Exception ex) {
                        log.e("Error seek", ex);
                    }
                    break;
                case ACTION_VOLUME:
                    float volume = intent.getFloatExtra(EXTRA_VOLUME, 1);
                    currentStatus.player.setVolume(volume, volume);
                    currentStatus.volume = volume;
                    break;
                case ACTION_FORWARD:
                    int next = getForwardItemIndex(false);
                    if (next >= 0) {
                        currentStatus.itemIndex = next;
                        currentStatus.item = currentStatus.item.parent.items.get(currentStatus.itemIndex);
                        startSong();
                    }
                    break;
                case ACTION_BACKWARD:
                    int prev = getBackwardItemIndex(false);
                    if (prev >= 0) {
                        currentStatus.itemIndex = prev;
                        currentStatus.item = currentStatus.item.parent.items.get(currentStatus.itemIndex);
                        startSong();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        currentStatus.player = new MediaPlayer();
        currentStatus.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        currentStatus.player.setOnCompletionListener(onCompletionListener);
        currentStatus.player.setOnErrorListener(onErrorListener);
        currentStatus.player.setOnInfoListener(onInfoListener);
        currentStatus.player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        MediaPlayer p = currentStatus.player;
        currentStatus.player = null;
        if (p!=null) {
            p.release();
        }
        if (currentDataSource != null) {
            currentDataSource.close();
            currentDataSource = null;
        }

        super.onDestroy();
    }

    /**
     * MediaPlayer event listener.
     */
    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (currentStatus.playList) {
                int nextItemIndex = getForwardItemIndex(currentStatus.playContinue);
                if (nextItemIndex >= 0) {
                    currentStatus.itemIndex = nextItemIndex;
                    currentStatus.item = currentStatus.item.parent.items.get(nextItemIndex);
                    startSong();
                } else {
                    currentStatus.paused = true;
                    pause();
                }
            } else if (currentStatus.playContinue) {
                play();
            } else {
                currentStatus.paused = true;
                pause();
            }
        }
    };

    /**
     * MediaPlayer event listener.
     */
    private OnErrorListener onErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            log.e("MediaPlayer error: " + what + ' ' + extra);
            return true;
        }
    };
    private OnInfoListener onInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            log.i("MediaPlayer info: " + what + "/" + extra);
            return true;
        }
    };

    int getBackwardItemIndex(boolean allowFromEnd) {
        int nextItemIndex = -1;
        for (int i = currentStatus.itemIndex - 1; i >= 0; i--) {
            if (CatalogLoader.isItemDownloaded(currentStatus.item.parent.items.get(i).file)) {
                nextItemIndex = i;
                break;
            }
        }
        if (nextItemIndex < 0 && allowFromEnd) {
            for (int i = currentStatus.item.parent.items.size() - 1; i > currentStatus.itemIndex; i--) {
                if (CatalogLoader.isItemDownloaded(currentStatus.item.parent.items.get(i).file)) {
                    nextItemIndex = i;
                    break;
                }
            }
        }
        return nextItemIndex;
    }

    int getForwardItemIndex(boolean allowFromStart) {
        int nextItemIndex = -1;
        for (int i = currentStatus.itemIndex + 1; i < currentStatus.item.parent.items.size(); i++) {
            if (CatalogLoader.isItemDownloaded(currentStatus.item.parent.items.get(i).file)) {
                nextItemIndex = i;
                break;
            }
        }
        if (nextItemIndex < 0 && allowFromStart) {
            for (int i = 0; i < currentStatus.itemIndex; i++) {
                if (CatalogLoader.isItemDownloaded(currentStatus.item.parent.items.get(i).file)) {
                    nextItemIndex = i;
                    break;
                }
            }
        }
        return nextItemIndex;
    }

    private void startSong() {
        log.v(">> startSong");

        if (currentStatus.player.isPlaying()) {
            currentStatus.player.stop();
        }
        currentStatus.player.reset();

        currentStatus.backwardEnabled = getBackwardItemIndex(false) >= 0;
        currentStatus.forwardEnabled = getForwardItemIndex(false) >= 0;

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        application.analytics().showScreen(currentStatus.item);

        File file = CatalogLoader.getItemDownloaded(currentStatus.item);
        try {
            songFile = new PackFileWrapper(file);
            getCover(currentStatus.item);
            currentStatus.text = songFile.readText("text.txt");
            String name = songFile.getOneFileNameByExtension("ogg", "mp3");
            if (name == null) {
                name = "file";
            }
            if (currentDataSource != null) {
                currentDataSource.close();
                currentDataSource = null;
            }
            currentDataSource = songFile.createDataSource(name);
            currentDataSource.apply(currentStatus.player);
            currentStatus.player.prepare();//TODO
            currentStatus.duration = currentStatus.player.getDuration();
        } catch (Exception ex) {
            log.e("MediaPlayer error start song: " + ex.getMessage(), ex);
            return;
        }

        CatalogLoader.setItemViewed(this, currentStatus.item, true);

        play();

        log.v("<< startSong");
    }

    private void getCover(Item item) throws IOException {
        if (songFile.isFileExist("cover.jpg")) {
            currentStatus.cover = BookLoader.loadBitmap(songFile, "cover.jpg");
        } else if (songFile.isFileExist("cover.png")) {
            currentStatus.cover = BookLoader.loadBitmap(songFile, "cover.png");
        } else {
            String cover = item.cover;
            if (cover == null) {
                cover = item.parent.cover;
            }
            if (cover != null) {
                int resourceId = this.getResources().getIdentifier(
                        cover.replaceAll("^cover_", "orig_"), "drawable", getPackageName());
                if (resourceId == 0) {
                    resourceId = this.getResources().getIdentifier(cover, "drawable", getPackageName());
                }
                currentStatus.cover = BookLoader.loadBitmap(getResources(), resourceId);
            } else {
                currentStatus.cover = new ColorDrawable();
            }
        }
    }

    private void stopAll() {
        if (currentStatus.player.isPlaying()) {
            currentStatus.player.stop();
        }
        if (currentDataSource != null) {
            currentDataSource.close();
            ;
            currentDataSource = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private void play() {
        currentStatus.player.start();
        startForeground(NOTIFICATION_ID, NotificationBuilder.createPlayaudioNotification(this, currentStatus));
        currentStatus.paused = false;
    }

    private void pause() {
        if (currentStatus.player.isPlaying()) {
            currentStatus.player.pause();
        }
        currentStatus.paused = true;
        stopForeground(true);
    }
}
