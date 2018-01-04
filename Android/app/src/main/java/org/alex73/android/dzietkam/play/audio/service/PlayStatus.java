package org.alex73.android.dzietkam.play.audio.service;

import android.media.MediaPlayer;

import org.alex73.android.dzietkam.catalog.Item;

public class PlayStatus {
    protected MediaPlayer player;

    public Item item;
    public String itemPath;
    public int itemIndex;

    public int duration;
    public float volume = 1f;

    public Object cover;
    public String text;

    public boolean paused;
    public boolean backwardEnabled, forwardEnabled, playContinue, playList;

    public int getPosition() {
        MediaPlayer p=player;
        return p!=null?p.getCurrentPosition():0;
    }
}
