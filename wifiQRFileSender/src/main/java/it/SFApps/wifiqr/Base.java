package it.SFApps.wifiqr;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by stefano on 30/01/15.
 */
public class Base extends Application{

    private static Tracker appTracker;

    public static Tracker getTraker()
    {
        return appTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.app_tracker);
    }
}
