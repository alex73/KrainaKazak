package org.alex73.android.dzietkam.playbook;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.ListFiles;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.imagecache.Dimension;
import org.alex73.android.dzietkam.imagecache.ImageCalc;
import org.alex73.android.dzietkam.imagecache.Rectangle;
import org.alex73.android.dzietkam.playbook.textpagesstore.Texts;
import org.alex73.android.dzietkam.util.IO;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.gson.Gson;

public class BookLoader {

    private final Logger log = new Logger(getClass());
    private final Item item;

    private final Map<String, File> files = new TreeMap<>();
    private final Map<Integer, BookPage> pages = new TreeMap<>();
    private final Map<Integer, Dimension> pageSizes = Collections
            .synchronizedMap(new TreeMap<Integer, Dimension>());

    public BookLoader(Item item) {
        this.item = item;

        for (String f : ListFiles.list(item).keySet()) {
            String fn = f.substring(f.lastIndexOf('/')+1);
            files.put(fn, new File(CatalogLoader.getDataRoot(), f));
            int p = fn.indexOf('.');
            if (p < 0) {
                continue;
            }
            int pn;
            try {
                pn = Integer.parseInt(fn.substring(0, p));
            } catch (NumberFormatException ex) {
                continue;
            }
            BookPage page = pages.get(pn);
            if (page == null) {
                page = new BookPage();
                pages.put(pn, page);
            }
            if (fn.endsWith(".mp3")) {
                page.audio = fn;
            } else if (fn.endsWith(".ogg")) {
                page.audio = fn;
            } else if (fn.endsWith(".jpg")) {
                page.jpg = fn;
            } else if (fn.endsWith(".png")) {
                page.png = fn;
            }
        }
    }

    public int getPagesCount() {
        for (int i = 1; i < 1000; i++) {
            if (!pages.containsKey(i)) {
                return i - 1;
            }
        }
        return 0;
    }

    public Dimension getCropCenterMin() {
        int w,h;
        try {
            w = Integer.parseInt(item.settings.get("cropCenterMinWidth"));
        } catch (Exception ex) {
            w = -1;
        }
        try {
            h = Integer.parseInt(item.settings.get("cropCenterMinHeight"));
        } catch (Exception ex) {
            h = -1;
        }
        return w < 0 || h < 0 ? null : new Dimension(w, h);
    }

    @SuppressLint("NewApi")
    public Bitmap loadPicture(int pageNum, Dimension outputSize, Dimension centerMin) {
        log.v("read bitmaps for page #" + pageNum + " for size " + outputSize);

        BookPage page = pages.get(pageNum);

        if (!files.containsKey(page.jpg)) {
            return null;
        }

        Bitmap result = null;
        try {
            synchronized (BookLoader.class) {
                Dimension fullImageSize = getPageSize(pageNum);
                Rectangle imageRect = new Rectangle();
                Rectangle outputRect = new Rectangle();
                ImageCalc.fixOutRectangles(fullImageSize, outputSize, centerMin, imageRect, outputRect);

                result = Bitmap.createBitmap(outputRect.width, outputRect.height, Bitmap.Config.ARGB_8888);
                result.setDensity(0);

                Canvas canvas = new Canvas(result);

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);

                Bitmap bmp = loadBitmap(files.get(page.jpg));
                int scale = Math.round(1.0f * fullImageSize.width / bmp.getWidth());
                Dimension imageSize = new Dimension(bmp.getWidth(), bmp.getHeight());
                outputRect = new Rectangle();
                Dimension centerMinImage = centerMin == null ? null
                        : new Dimension(centerMin.width / scale, centerMin.height / scale);
                ImageCalc.fixOutRectangles(imageSize, outputSize, centerMinImage, imageRect, outputRect);
                //log.i("draw bitmap " + imageRect.getRect() + " -> " + outputRect.getRect());
                outputRect.x = 0;
                outputRect.y = 0;
                canvas.drawBitmap(bmp, imageRect.getRect(), outputRect.getRect(), paint);
                bmp.recycle();
                bmp = null;

                if (files.containsKey(page.png)) {
                    bmp = loadBitmap(files.get(page.png));
                    scale = Math.round(1.0f * fullImageSize.width / bmp.getWidth());
                    imageSize = new Dimension(bmp.getWidth(), bmp.getHeight());
                    outputRect = new Rectangle();
                    centerMinImage = centerMin == null ? null
                            : new Dimension(centerMin.width / scale, centerMin.height / scale);
                    ImageCalc.fixOutRectangles(imageSize, outputSize, centerMinImage, imageRect, outputRect);
                    //log.i("draw bitmap " + imageRect.getRect() + " -> " + outputRect.getRect());
                    outputRect.x = 0;
                    outputRect.y = 0;
                    canvas.drawBitmap(bmp, imageRect.getRect(), outputRect.getRect(), paint);
                    bmp.recycle();
                    bmp = null;
                }
            }
        } catch (Exception ex) {
            log.e("Error load page", ex);
        }
        return result;
    }

    // @SuppressLint("NewApi")
    // public Bitmap loadPictureo(int pageNum, Dimension outputSize, Dimension centerMin) {
    // log.v("read Bitmap " + pageNum + " for size " + outputSize);
    //
    // BookPage page = pages.get(pageNum);
    //
    // if (!file.isFileExist(page.jpg)) {
    // return null;
    // }
    // Bitmap bmp = null;
    // try {
    // Dimension imageSize = getPageSize(pageNum);
    // Rectangle imageRect = new Rectangle();
    // Rectangle outputRect = new Rectangle();
    // ImageCalc.fixOutRectangles(imageSize, outputSize, centerMin, imageRect, outputRect);
    //
    // synchronized (BookLoader.class) {
    // bmp = loadBitmap(file, page.jpg, imageRect);
    // Bitmap oldbmp = bmp;
    // bmp = Bitmap.createScaledBitmap(bmp, outputRect.width, outputRect.height, true);
    // oldbmp.recycle();
    // bmp.setDensity(0);
    //
    // Canvas canvas = new Canvas(bmp);
    //
    // Paint paint = new Paint();
    // paint.setAntiAlias(true);
    // paint.setFilterBitmap(true);
    // paint.setDither(true);
    //
    // if (file.isFileExist(page.png)) {
    // Bitmap bitmap = loadBitmap(file, page.png, imageRect);
    // Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Rect dest = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
    // canvas.drawBitmap(bitmap, src, dest, paint);
    // bitmap.recycle();
    // }
    // }
    // } catch (Exception ex) {
    // log.e("Error load page", ex);
    // }
    // return bmp;
    // }

    private Dimension getPageSize(int pageNum) throws IOException {
        Dimension size = pageSizes.get(pageNum);
        if (size == null) {
            BookPage page = pages.get(pageNum);

            if (!files.containsKey(page.jpg)) {
                return null;
            }
            size = loadBitmapSize(files.get(page.jpg));
            pageSizes.put(pageNum, size);
        }
        return size;
    }

    static Dimension loadBitmapSize(File file) throws IOException {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        InputStream in2 = new BufferedInputStream(new FileInputStream(file));
        try {
            BitmapFactory.decodeStream(in2, null, opts);
        } finally {
            in2.close();
        }
        return new Dimension(opts.outWidth, opts.outHeight);
    }

    public static Bitmap loadBitmap(File file) throws IOException {
        for (int i = 1; i <= 10; i++) {
            InputStream in2 = new BufferedInputStream(new FileInputStream(file));
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = i;
                opts.inScaled = false;
                Bitmap bmp = BitmapFactory.decodeStream(in2, null, opts);
                bmp.setDensity(0);
                return bmp;
            } catch (OutOfMemoryError ex) {
            } finally {
                in2.close();
            }
        }
        return null;
    }

    public static Bitmap loadBitmap(Resources res, int resourceId) throws IOException {
        for (int i = 1; i <= 10; i++) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = i;
                opts.inScaled = false;
                Bitmap bmp = BitmapFactory.decodeResource(res, resourceId, opts);
                bmp.setDensity(0);
                return bmp;
            } catch (OutOfMemoryError ex) {
            }
        }
        return null;
    }

    public File getAudioFile(int pageNum) throws IOException {
        BookPage page = pages.get(pageNum);
        if (page.audio == null) {
            return null;
        }
        return files.get(page.audio);
    }

    public Texts getTextPages() {
        if (!files.containsKey("pages.json")) {
            return null;
        }
        try {
            String s = IO.readText(files.get("pages.json"));
            return new Gson().fromJson(s, Texts.class);
        } catch (Exception ex) {
            log.e("Error load page", ex);
            return null;
        }
    }

    static class BookPage {
        String jpg;
        String png;
        String audio;
    }
}
