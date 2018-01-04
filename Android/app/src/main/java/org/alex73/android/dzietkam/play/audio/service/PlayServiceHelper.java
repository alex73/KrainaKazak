package org.alex73.android.dzietkam.play.audio.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;

public class PlayServiceHelper {
    public static void startPlay(Context context, Item item) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_START);
        intent.setData(Uri.parse(Catalog.getPath(item)));
        context.startService(intent);
    }

    public static void finish(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_FINISH);
        context.startService(intent);
    }

    public static void pause(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_PAUSE);
        context.startService(intent);
    }

    public static void resume(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_RESUME);
        context.startService(intent);
    }

    public static void seekTo(Context context, int position) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_SEEK);
        intent.putExtra(PlayService.EXTRA_SEEK, position);
        context.startService(intent);
    }

    public static void setVolume(Context context, float volume) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_VOLUME);
        intent.putExtra(PlayService.EXTRA_VOLUME, volume);
        context.startService(intent);
    }

    public static void playForward(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_FORWARD);
        context.startService(intent);
    }

    public static void playBackward(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(PlayService.ACTION_BACKWARD);
        context.startService(intent);
    }

    public static PlayStatus getStatus() {
        return PlayService.currentStatus;
    }
}
