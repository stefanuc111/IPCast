package it.SFApps.wifiqr;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.analytics.GoogleAnalytics;

public class SettingsActivity extends FragmentActivity{
@SuppressLint("NewApi")
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	
    getFragmentManager().beginTransaction()
    .replace(android.R.id.content, new SettingsFragment())
    .commit();

}
@Override
public void onStart() {
  super.onStart();
    GoogleAnalytics.getInstance(this).reportActivityStart(this);

}


@Override
protected void onStop() {
	super.onStop();
    GoogleAnalytics.getInstance(this).reportActivityStop(this);
}

}
