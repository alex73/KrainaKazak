package org.alex73.android.dzietkam.play.youtube;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;
import org.alex73.android.dzietkam.util.IO;


public class PlayYoutubeActivity extends AppCompatActivity {

    private String itemPath;
    private Item item;
    private WebView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_youtube);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();

        itemPath = getIntent().getData().getPath();
        item = application.catalog.getItem(itemPath);

        application.showScreen(item);

        String html;
        InputStream in = getResources().openRawResource(R.raw.youtube);
        try {
            html = IO.read(in, "UTF-8");
            html = html.replace("{ID}", item.id);
        } catch (Exception ex) {
            html = "error";
        } finally {
            try {
                in.close();
            } catch (IOException ex) {}
        }

        youTubeView = (WebView) findViewById(R.id.youtube_view);
        youTubeView.getSettings().setDefaultTextEncodingName("utf-8");
        youTubeView.getSettings().setJavaScriptEnabled(true);
        youTubeView.loadDataWithBaseURL("file:///android_res/raw/", html, "text/html", "UTF-8", null);
    }
}
