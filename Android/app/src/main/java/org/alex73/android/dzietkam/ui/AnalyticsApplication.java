package org.alex73.android.dzietkam.ui;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;

import java.lang.Thread.UncaughtExceptionHandler;

public class AnalyticsApplication extends Application {
    static int memorymb;

    public Catalog catalog;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        memorymb = Math.round(Runtime.getRuntime().maxMemory() / 1024f / 1024f);

        UncaughtExceptionHandler myHandler = (Thread t, Throwable e) -> sendEvent("Exception", e.getClass().getName(), e.getMessage());
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        sendEvent("LowMemory", "LowMemory", "LowMemory");
    }

    public void crash(Exception ex) {
        sendEvent("Crash", ex.getClass().getName(), ex.getMessage());
    }


    public void showScreen(Item item) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Catalog.getPath(item));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Catalog.getTitlePath(item));
        mFirebaseAnalytics.logEvent("show_screen", bundle);
    }

    public void sendEvent(String action, String category, String label) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, action);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        mFirebaseAnalytics.logEvent(category, bundle);
    }
}
