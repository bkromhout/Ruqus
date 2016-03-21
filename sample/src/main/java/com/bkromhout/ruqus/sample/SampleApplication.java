package com.bkromhout.ruqus.sample;

import android.app.Application;
import android.content.Context;
import com.bkromhout.ruqus.Ruqus;

/**
 * Custom Application Class.
 */
public class SampleApplication extends Application {
    private static SampleApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Ruqus.init(this);
    }

    public static Context getAppCtx() {
        return instance.getApplicationContext();
    }
}
