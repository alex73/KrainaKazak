package org.alex73.android.dzietkam.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.download.DownloadService;
import org.alex73.android.dzietkam.play.audio.service.PlayServiceHelper;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final Logger log = new Logger(getClass());

    AnalyticsApplication application;

    @Override
    public void onCreate(Bundle icicle) {
        log.v(">> onCreate");
        super.onCreate(icicle);

        setContentView(getMainScreen());

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_FINISH_DOWNLOAD);
        bManager.registerReceiver(downloadFinished, intentFilter);

        application = (AnalyticsApplication) getApplication();
        application.analytics();

        application.catalog = loadCatalog();

        findViewById(R.id.btnKnizki).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/knizki"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnKalychanki).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/kalychanki"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnPiesienki).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/piesienki"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnAudyjokazki).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/audyjokazki"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnAdukacyja).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/adukacyja"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnTexty).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                intent.setData(Uri.parse("/teksty"));
                startActivity(intent);
            }
        });
        findViewById(R.id.btnMulty).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean allowed = sharedPref.getBoolean("pref_show_multy",true);
                if (allowed) {
                    Intent intent = new Intent(MainActivity.this, ItemsListActivity.class);
                    intent.setData(Uri.parse("/multfilmy"));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Забаронена", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btnSettings).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        for (int id : new int[]{R.id.btnKnizki, R.id.btnKalychanki, R.id.btnPiesienki, R.id.btnAudyjokazki, R.id.btnAdukacyja, R.id.btnTexty, R.id.btnMulty, R.id.btnSettings}) {
            findViewById(id).setFocusable(true);
            findViewById(id).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    view.setBackgroundColor(b ? 0x20000000 : 0x00000000);
                }
            });
        }
        findViewById(R.id.btnKnizki).requestFocusFromTouch();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PlayServiceHelper.finish(this);
    }

    private BroadcastReceiver downloadFinished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_FINISH_DOWNLOAD)) {
                String error = intent.getStringExtra("error");
                if (error == null) {
                    Toast.makeText(MainActivity.this, R.string.download_finish, Toast.LENGTH_SHORT).show();
                } else {
                    application.analytics().sendEvent("Download", "Error", error);
                    Toast.makeText(MainActivity.this, "Памылка спампоўваньня: " + error, Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    };

    Catalog loadCatalog() {
        try {
            CatalogLoader.initDataRoot(application);
            Catalog c = CatalogLoader.load(getResources());
            c.setupParents();
            return c;
        } catch (Exception ex) {
            log.e("Error catalog read", ex);
            Toast.makeText(this, "Памылка чытаньня каталогу: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            application.crash(ex);
            return null;
        }
    }

    /**
     * screen depends on month
     */
    int getMainScreen() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH);
        if (m >= Calendar.MARCH && m <= Calendar.OCTOBER) {
            return R.layout.main_screen_summer;
        } else {
            return R.layout.main_screen_winter;
        }
    }
}
