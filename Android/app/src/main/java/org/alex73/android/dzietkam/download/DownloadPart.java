package org.alex73.android.dzietkam.download;

public class DownloadPart {
    public final String path;
    public final String name;
    public final long size;

    public DownloadPart(String path, String name, long size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }
}
