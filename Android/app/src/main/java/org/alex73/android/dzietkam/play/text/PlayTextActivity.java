package org.alex73.android.dzietkam.play.text;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;
import org.alex73.android.dzietkam.util.IO;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;

public class PlayTextActivity extends AppCompatActivity {

    private final Logger log = new Logger(getClass());
    AnalyticsApplication application;
    private String itemPath;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.v(">> onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_text);

        application = (AnalyticsApplication) getApplication();
        itemPath = getIntent().getData().getPath();
        item = application.catalog.getItem(itemPath);

        application.showScreen(item);
        CatalogLoader.setItemViewed(this, item, true);

        File fn = new File(CatalogLoader.getDataRoot(), item.getPath()+".text");
        String html = "";
        try {
            html = text2html(item.title, item.description, IO.readText(fn));
            html = URLEncoder.encode(html, "UTF-8").replaceAll("\\+", " ");
        } catch (Exception ex) {
            log.e("Error read pack file", ex);
        }

        WebView wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setDefaultTextEncodingName("utf-8");
        wv.loadData(html, "text/html; charset=UTF-8", "UTF-8");
    }

    String text2html(String title, String description, String text) {
        StringBuilder o = new StringBuilder();
        o.append("<!DOCTYPE HTML><html><head><meta charset='UTF-8'><style type='text/css'>");
        o.append(" body {  margin: 0.5em; } ");
        o.append(" p {  margin: 0;  text-align: justify;  text-indent: 1.5em;  }");
        o.append(" .POETRY {  margin: 0 auto;  width: 80%;  }");
        o.append(" .POETRY p {  text-align: left;  text-indent: 0; }");
        o.append("</style></head><body>\n");
        if (title != null) {
            o.append("<h1>" + title + "</h1>\n");
        }
        if (description != null) {
            o.append("<h2>" + description + "</h2>\n");
        }
        for (String s : text.split("\n")) {
            s = s.trim();
            switch (s) {
                case "##Poetry:begin":
                    o.append("<div class='POETRY'>\n");
                    break;
                case "##Poetry:end":
                    o.append("</div>\n");
                    break;
                case "":
                    o.append("<p>&nbsp;</p>\n");
                default:
                    o.append("<p>").append(s).append("</p>\n");
            }
        }
        o.append("</body></html>\n");

        return o.toString();
    }
}
