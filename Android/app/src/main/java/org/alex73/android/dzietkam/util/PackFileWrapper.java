package org.alex73.android.dzietkam.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import android.media.MediaPlayer;

import static org.alex73.android.dzietkam.util.IO.*;

public class PackFileWrapper {
    static final Charset UTF8 = Charset.forName("UTF-8");

    private final File file;
    private final Map<String, FileObject> filesMap;
    private final Settings settings;

    public PackFileWrapper(File f) throws IOException {
        this.file = f;
        Map<String, FileObject> map = new HashMap<String, FileObject>();

        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            int filesCount = in.readInt();
            for (int i = 0; i < filesCount; i++) {
                long start = in.readLong();
                long size = in.readLong();
                int nlen = in.readInt();
                byte[] nbytes = new byte[nlen];
                in.readFully(nbytes);
                String name = new String(nbytes, UTF8);
                FileObject fo = new FileObject(start, size);
                map.put(name, fo);
            }
        } finally {
             in.close();
        }

        filesMap = Collections.unmodifiableMap(map);

        settings = new Settings();
    }

    public Set<String> list() {
        return Collections.unmodifiableSet(filesMap.keySet());
    }

    public Settings getSettings() {
        return settings;
    }

    public FileObject getFileObject(String name) {
        return filesMap.get(name);
    }

    public boolean isFileExist(String name) {
        return filesMap.containsKey(name);
    }

    public String getOneFileNameByExtension(String... extensions) {
        List<String> result = new ArrayList<>();
        for (String ext : extensions) {
            ext = '.' + ext;
            for (String fn : filesMap.keySet()) {
                if (fn.endsWith(ext)) {
                    result.add(fn);
                }
            }
        }
        return result.size() == 1 ? result.get(0) : null;
    }

    public FileObjectInputStream createStream(String name) throws IOException {
        FileObject fo = filesMap.get(name);
        return fo != null ? new FileObjectInputStream(fo) : null;
    }

    public FileObjectDataSource createDataSource(String name) throws IOException {
        FileObject fo = filesMap.get(name);
        return fo != null ? new FileObjectDataSource(fo) : null;
    }

    public String readText(String name) throws IOException {
        FileObject fo = filesMap.get(name);
        if (fo == null) {
            return null;
        }
        InputStream in = new FileObjectInputStream(fo);
        try {
            return IO.read(in, "UTF-8");
        } finally {
            in.close();
        }
    }

    public class FileObject {
        final public long start, size;

        public FileObject(long start, long size) {
            this.start = start;
            this.size = size;
        }
    }

    public class FileObjectInputStream extends InputStream {
        private RandomAccessFile raFile;
        private long leave;

        public FileObjectInputStream(FileObject fo) throws IOException {
            raFile = new RandomAccessFile(file, "r");
            raFile.seek(fo.start);
            leave = fo.size;
        }

        @Override
        public void close() {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (IOException ex) {
                }
                raFile = null;
            }
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            if (leave == 0) {
                return -1;
            }
            if (byteCount > leave) {
                byteCount = (int) leave;
            }
            int res = raFile.read(buffer, byteOffset, byteCount);
            leave -= res;
            return res;
        }

        @Override
        public int read() throws IOException {
            if (leave == 0) {
                return -1;
            }
            leave--;
            return raFile.read();
        }
    }

    public class FileObjectDataSource implements Closeable {
        private final FileObject fo;
        private RandomAccessFile raFile;

        public FileObjectDataSource(FileObject fo) throws IOException {
            this.fo = fo;
            raFile = new RandomAccessFile(file, "r");
        }

        @Override
        public void close() {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (IOException ex) {
                }
                raFile = null;
            }
        }

        public void apply(MediaPlayer player) throws IOException {
            player.setDataSource(raFile.getFD(), fo.start, fo.size);
        }
    }

    public class Settings {
        private final Properties props = new Properties();

        public Settings() throws IOException {

            InputStream ins = createStream("settings.properties");
            if (ins != null) {
                try {
                    props.load(new BufferedReader(new InputStreamReader(ins, "UTF-8")));
                } finally {
                    ins.close();
                }
            }
        }

        public int getInt(String name, int defaultValue) {
            String v = props.getProperty(name);
            return v == null ? defaultValue : Integer.parseInt(v);
        }

        public String getString(String name) {
            return props.getProperty(name);
        }
    }
}
