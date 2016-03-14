package com.bkromhout.ruqus.sample;

import android.app.Application;
import android.content.Context;

/**
 * Custom Application Class.
 */
public class SampleApplication extends Application {
    private static SampleApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getAppCtx() {
        return instance.getApplicationContext();
    }
}
