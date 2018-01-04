package org.alex73.android.dzietkam.download;

import android.net.Uri;

public class DownloadPart {
    public final Uri uri;
    public final String name;
    public final String file;
    public final long size;

    public DownloadPart(Uri uri, String name, String file, long size) {
        this.uri = uri;
        this.name = name;
        this.file = file;
        this.size = size;
    }
}
