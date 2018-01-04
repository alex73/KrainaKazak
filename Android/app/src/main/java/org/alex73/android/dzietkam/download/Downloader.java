package org.alex73.android.dzietkam.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alex73.android.dzietkam.Logger;

public abstract class Downloader {
    private final static Pattern RE_CONTENT_RANGE = Pattern.compile("bytes ([0-9]+)\\-([0-9]+)/([0-9]+)");

    private final Logger log = new Logger(getClass());

    private final DownloadPart d;

    public Downloader(DownloadPart download) {
        this.d = download;
    }

    abstract protected boolean needToStop();

    abstract protected void downloaded(long count);

    public void process() throws Exception {
        log.i("Request download " + d.uri);
        HttpURLConnection connection = (HttpURLConnection) new URL(d.uri.toString()).openConnection();
        try {
            File fout = new File(d.file);
            File dir = fout.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IOException("Error create parent dir");
            }

            long exist = fout.exists() ? fout.length() : -1;
            if (exist == d.size) {
                return; // finish
            }
            if (exist > 0) {
                connection.setRequestProperty("Range", "bytes=" + exist + "-");
            }

            boolean append;

            connection.connect();

            String contentLengthStr = connection.getHeaderField("Content-Length");

            switch (connection.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                if (Long.parseLong(contentLengthStr) != d.size) {
                    log.e("Wrong Content-Length : " + contentLengthStr);
                    throw new Exception();
                }
                append = false;
                log.v("Download from start");
                break;
            case HttpURLConnection.HTTP_PARTIAL:
                if (Long.parseLong(contentLengthStr) != d.size - exist) {
                    log.e("Wrong Content-Length : " + contentLengthStr);
                    throw new Exception("Несупадзеньне файла ў каталозе і на сэрверы");
                }
                String contentRangeStr = connection.getHeaderField("Content-Range");
                Matcher m = RE_CONTENT_RANGE.matcher(contentRangeStr);
                if (!m.matches()) {
                    log.e("Wrong Content-Range (not matched) : " + contentRangeStr);
                    throw new Exception("Няправільны памер у адказе сэрвера");
                }
                if (Long.parseLong(m.group(3)) != d.size) {
                    log.e("Wrong Content-Range (size is differ) : " + contentRangeStr);
                    throw new Exception("Несупадзеньне файла ў каталозе і на сэрверы");
                }
                if (Long.parseLong(m.group(2)) + 1 != Long.parseLong(m.group(3))) {
                    log.e("Wrong Content-Range (wrong end) : " + contentRangeStr);
                    throw new Exception("Несупадзеньне файла ў каталозе і на сэрверы");
                }
                if (Long.parseLong(m.group(1)) != exist) {
                    log.e("Wrong Content-Range (wrong start) : " + contentRangeStr);
                    throw new Exception("Несупадзеньне файла ў каталозе і на сэрверы");
                }
                append = true;
                log.v("Download from " + exist);
                break;
            default:
                log.e("Wrong status : " + connection.getResponseCode() + " "
                        + connection.getResponseMessage());
                throw new IOException("Памылка сэрвера " + connection.getResponseCode() + " "
                        + connection.getResponseMessage());
            }

            downloaded(append ? exist : 0);

            InputStream input = connection.getInputStream();
            try {
                OutputStream output = new FileOutputStream(fout, append); // TODO error handling
                try {
                    int count;
                    byte[] buffer = new byte[64 * 1024];
                    while ((count = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, count);
                        downloaded(count);
                        if (needToStop()) {
                            return;
                        }
                    }
                } finally {
                    try {
                        output.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}
