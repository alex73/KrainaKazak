package org.alex73.android.dzietkam.download;

import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.play.audio.PlayAudioActivity;
import org.alex73.android.dzietkam.play.audio.service.PlayService;
import org.alex73.android.dzietkam.play.audio.service.PlayStatus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.widget.AppCompatDrawableManager;
import android.widget.RemoteViews;

public class NotificationBuilder {

    @SuppressLint("NewApi")
    public static Notification createDownloadNotification(Service caller, String title, int progress) {
        RemoteViews views = new RemoteViews(caller.getPackageName(), R.layout.notification);

        if (Build.VERSION.SDK_INT < 21) {
            // vector drawable for old versions
            Drawable d = AppCompatDrawableManager.get().getDrawable(caller, R.drawable.notification_close);
            Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, c.getWidth(), c.getHeight());
            d.draw(c);
            views.setImageViewBitmap(R.id.notificationClose, b);
        }

        ComponentName service = new ComponentName(caller, DownloadService.class);

        Intent close = new Intent(DownloadService.ACTION_CLOSE);
        close.setComponent(service);
        views.setOnClickPendingIntent(R.id.notificationClose, PendingIntent.getService(caller, 0, close, PendingIntent.FLAG_IMMUTABLE));

        views.setTextViewText(R.id.notificationTitle, title);
        views.setProgressBar(R.id.notificationProgressBar, 100, progress, false);

        Intent intent = new Intent(caller, caller.getClass());
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent mNotificationAction = PendingIntent.getActivity(caller, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(caller, DownloadService.CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(progress + " %");
        //mBuilder.setContent(views);
        mBuilder.setContentIntent(mNotificationAction);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // notification.flags |= Notification.FLAG_ONGOING_EVENT;
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // notification.visibility = Notification.VISIBILITY_PUBLIC;
        // }

        // if (false) {// very verbose notification
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        // notification.priority = Notification.PRIORITY_MAX;
        // notification.vibrate = new long[0]; // needed to get headsup
        // } else {
        // // notification.tickerText = song.title + " - " + Playlist.album;
        // }
        // }

        return mBuilder.build();
    }

    @SuppressLint("NewApi")
    public static Notification createPlayaudioNotification(Service caller, PlayStatus song) {
        Intent intent = new Intent(caller, PlayAudioActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent mNotificationAction = PendingIntent.getActivity(caller, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(caller, PlayService.CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle(song.item.parent.title);
        mBuilder.setContentText(song.item.title);
        mBuilder.setContentIntent(mNotificationAction);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return mBuilder.build();
    }
}
