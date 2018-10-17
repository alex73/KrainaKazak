package org.alex73.android.dzietkam.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.util.IO;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutActivity extends AppCompatActivity {
    private final Logger log = new Logger(getClass());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.v(">> onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        String version;
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = info.versionName;
        } catch (Exception ex) {
            version = "<невядомая>";
        }
        String systemVersion = Build.VERSION.RELEASE;
        int memorymb = Math.round(Runtime.getRuntime().maxMemory() / 1024f / 1024f);
        String systemMemory = memorymb + " MiB";

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        String systemScreen = metrics.widthPixels + "x" + metrics.heightPixels;

        String html;
        InputStream in = getResources().openRawResource(R.raw.about);
        try {
            html = IO.read(in, "UTF-8");
            html = html.replace("{VER}", version);
            html = html.replace("{SYSTEM_VERSION}", systemVersion);
            html = html.replace("{SYSTEM_MEMORY}", systemMemory);
            html = html.replace("{SYSTEM_SCREEN}", systemScreen);
          //  html = URLEncoder.encode(html, "UTF-8").replaceAll("\\+", " ");
        } catch (Exception ex) {
            html = "error";
        } finally {
            try {
                in.close();
            } catch (IOException ex) {}
        }

        WebView wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setDefaultTextEncodingName("utf-8");
        wv.loadDataWithBaseURL("file:///android_res/raw/", html, "text/html", "UTF-8", null);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView  view, String  url){
                if ("http://settings/".equals(url)) {
                    Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }else {
                    return false;
                }
            }
        });
    }
}
