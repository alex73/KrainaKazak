package org.alex73.android.dzietkam;

import android.util.Log;

public class Logger {
    private final String tag;

    public Logger(Class source) {
        tag = source.getName();
    }

    public void v(String msg) {
        Log.v(tag, msg);
    }

    public void i(String msg) {
        Log.i(tag, msg);
    }

    public void e(String msg) {
        Log.e(tag, msg);
    }

    public void e(String msg, Throwable ex) {
        Log.e(tag, msg, ex);
    }
}
