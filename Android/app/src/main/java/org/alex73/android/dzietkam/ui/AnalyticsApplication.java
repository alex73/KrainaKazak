package org.alex73.android.dzietkam.ui;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class AnalyticsApplication extends Application {
    public static final String ga_trackingId = "UA-68500274-2";
    static int memorymb;

    private AnalyticsInstance analyticsInstance;
    public Catalog catalog;

    public synchronized AnalyticsInstance analytics() {
        if (analyticsInstance == null) {
            memorymb = Math.round(Runtime.getRuntime().maxMemory() / 1024f / 1024f);
            analyticsInstance = new AnalyticsInstance(this);

            UncaughtExceptionHandler myHandler = new ExceptionReporter(analyticsInstance.mTracker,
                    Thread.getDefaultUncaughtExceptionHandler(), this);
            Thread.setDefaultUncaughtExceptionHandler(myHandler);
        }
        return analyticsInstance;
    }

    public String getVersion() {
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            return info.versionName;
        } catch (Exception ex) {
            return null;
        }
    }

    public void crash(Exception ex) {
        analyticsInstance.mTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription("ver:" + Build.VERSION.RELEASE + " mem:" + memorymb + " err:"
                        + new StandardExceptionParser(this, null)
                        .getDescription(Thread.currentThread().getName(), ex))
                .setFatal(false).build());
    }

    public static class AnalyticsInstance {
        GoogleAnalytics analytics;
        final private Tracker mTracker;

        public AnalyticsInstance(AnalyticsApplication app) {
            analytics = GoogleAnalytics.getInstance(app);
            mTracker = analytics.newTracker(ga_trackingId);

            sendEvent("Info", "JVM memory, mb", Integer.toString(memorymb));
        }

        public void showScreen(Item item) {
            mTracker.setScreenName(Catalog.getTitlePath(item));
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        public void sendEvent(String category, String action, String label) {
            mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action)
                    .setLabel(label).build());
        }
    }
}
