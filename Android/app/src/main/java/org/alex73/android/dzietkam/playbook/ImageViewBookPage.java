package org.alex73.android.dzietkam.playbook;

import org.alex73.android.dzietkam.imagecache.Dimension;
import org.alex73.android.dzietkam.imagecache.Rectangle;
import org.alex73.android.dzietkam.playbook.textpagesstore.Page;
import org.alex73.android.dzietkam.playbook.textpagesstore.Style;
import org.alex73.android.dzietkam.playbook.textpagesstore.StyleInsets;
import org.alex73.android.dzietkam.playbook.textpagesstore.Text;
import org.alex73.android.dzietkam.playbook.textpagesstore.Texts;
import org.alex73.android.dzietkam.util.ImageViewBase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

public class ImageViewBookPage extends ImageViewBase {
    private BookLoader pl;
    private int pageIndex;
    private Paint paintText;
    private Texts texts;

    public ImageViewBookPage(Context context) {
        super(context);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.RED);
    }

    public void setContent(BookLoader pl, int pageIndex) {
        this.pl = pl;
        this.pageIndex = pageIndex;
        this.texts = pl.getTextPages();
        updateContent();
    }

    @Override
    protected void resolveBitmap(final int width, final int height) {
        new Thread() {
            public void run() {
                Bitmap bmp = pl.loadPicture(pageIndex, new Dimension(width, height), pl.getCropCenterMin());
                mBitmap = bmp;
                postInvalidate();
            }
        }.start();
    }

    @Override
    protected void onDrawOver(Canvas canvas) {
        if (texts != null) {
            Paint pp=new Paint();
            pp.setTextSize(40);
            pp.setTypeface(Typeface.DEFAULT);
            canvas.drawText("testzzzzzzzzzzzzzzzzzzzzzz", 20, 100, pp);
            Page currentPage = texts.pages.get(pageIndex);

            for (Text t : currentPage.texts) {
                Style style = getStyle(t.style);
                OutputPlace place = new OutputPlace(currentPage, t, style);

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                int fontSize = measureStyle(style, paint);
                place.output(canvas, paint, fontSize);
            }
        }
    }
    Style getStyle(String name) {
        for (Style s : texts.styles) {
            if (name.equals(s.name)) {
                return s;
            }
        }
        return null;
    }

    double getScale(Page p) {
        return p.width * 1.0 / getWidth();
    }

    int measureStyle(Style style, Paint paint) {
        List<Measure> measures = new ArrayList<>();
        for (Page p : texts.pages) {
            for (Text t : p.texts) {
                if (style.name.equals(t.style)) {
                    measures.add(new Measure(t, p, style));
                }
            }
        }
        if (measures.isEmpty()) {
            return 0;
        }
        for (int fontSize = 1;; fontSize++) {
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(fontSize);
            Paint.FontMetrics metrics = paint.getFontMetrics();
            for (Measure m : measures) {
                List<Block> blocks = new ArrayList<>();
                if (m.isBigger(paint, getScale(m.page), blocks)) {
                    return fontSize - 1;
                }
            }
        }
    }

    class OutputPlace {
        final Page page;
        final Text text;
        final Style style;
        final double scale;
        final Rectangle area;
        final StyleInsets insets;

        public OutputPlace(Page page, Text text, Style style) {
            this.page = page;
            this.text = text;
            this.style = style;
            this.scale = getScale(page);
            area = new Rectangle();
            area.x = (int) Math.round(text.xMin / scale);
            area.y = (int) Math.round(text.yMin / scale);
            area.width = (int) Math.round((text.xMax - text.xMin + 1) / scale);
            area.height = (int) Math.round((text.yMax - text.yMin + 1) / scale);
            insets = new StyleInsets();
            if (style.insets != null) {
                insets.top = (int) Math.round(style.insets.top / scale);
                        insets.left = (int) Math.round(style.insets.left / scale);
                        insets.bottom= (int) Math.round(style.insets.bottom / scale);
                        insets.right=(int) Math.round(style.insets.right / scale);
            }
        }

        int getTextMaxWidth() {
            return area.width - insets.left - insets.right;
        }

        int getTextMaxHeight() {
            return area.height - insets.top - insets.bottom;
        }

        public void output(Canvas canvas, Paint paint, int fontSize) {
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(fontSize);
            paint.setColor(Integer.decode(style.background.replaceAll("^#", "0x")));
            canvas.drawRect(area.x, area.y, area.width, area.height, paint);

            paint.setColor(Integer.decode(style.foreground.replaceAll("^#", "0x")));

            Paint.FontMetrics metrics = paint.getFontMetrics();

            Measure m = new Measure(text, page, style);
            List<Block> blocks = new ArrayList<>();
            m.getHeightForWidth(getTextMaxWidth(), paint, scale, blocks);

            int y = area.y + insets.top;
            int leaveY = getTextMaxHeight() - blocks.size() * Math.round(paint.getTextSize());
            switch (style.alignVertical) {
                case TOP:
                    break;
                case CENTER:
                    y += leaveY / 2;
                    break;
                case BOTTOM:
                    y += leaveY;
                    break;
            }
            y += metrics.ascent;
            for (Block b : blocks) {
                int x = area.x + insets.left;
                int leaveX = getTextMaxWidth() - b.getRect(paint).width;
                switch (style.alignHorizontal) {
                    case LEFT:
                        break;
                    case CENTER:
                        x += leaveX / 2;
                        break;
                    case RIGHT:
                        x += leaveX;
                        break;
                }
                canvas.drawText(b.line.substring(b.begin, b.end), x, y, paint);
                y += Math.round(paint.getTextSize());
            }
        }
    }

    class Measure {
        OutputPlace place;
        Text text;
        Page page;
        String[] lines;

        public Measure(Text text, Page page, Style style) {
            place = new OutputPlace(page, text, style);
            this.text = text;
            this.page = page;
            lines = text.text.trim().split("\n");
        }

        public boolean isBigger(Paint paint, double scale, List<Block> blocks) {
            int realHeight = getHeightForWidth(place.getTextMaxWidth(), paint, scale, blocks);
            return realHeight > place.getTextMaxHeight();
        }

        public int getHeightForWidth(int width, Paint paint, double scale, List<Block> blocks) {
            for (String line : lines) {
                blocks.add(new Block(line, 0, line.length()));
            }
            int height = 0;
            for (int i = 0; i < blocks.size(); i++) {
                Block b = blocks.get(i);
                Dimension d;
                while (true) {
                    d = b.getRect(paint);
                    if (d.width <= width) {
                        break;
                    }
                    int prevSpace = b.line.lastIndexOf(' ', b.end - 1);
                    if (prevSpace < 0 || prevSpace <= b.begin) {
                        return Integer.MAX_VALUE;
                    }
                    b.end = prevSpace;
                    while (b.end > b.begin && b.line.charAt(b.end - 1) == ' ')
                        b.end--;
                }
                height += d.height;
                if (b.end < b.line.length()) {
                    Block nb = new Block(b.line, b.end, b.line.length());
                    while (nb.begin < nb.end && nb.line.charAt(nb.begin) == ' ')
                        nb.begin++;
                    blocks.add(i + 1, nb);
                }
            }
            return height;
        }
    }

    static class Block {
        String line;
        int begin;
        int end;

        public Block(String line, int begin, int end) {
            this.line = line;
            this.begin = begin;
            this.end = end;
        }

        Dimension getRect(Paint paint) {
            int width = (int) Math.round(paint.measureText(line.substring(begin, end)));
            int height = (int) Math.round(paint.getTextSize());
            return new Dimension(width, height);
        }
    }
}
