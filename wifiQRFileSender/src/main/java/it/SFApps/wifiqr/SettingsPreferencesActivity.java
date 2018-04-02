package it.SFApps.wifiqr;

import com.google.android.gms.analytics.GoogleAnalytics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsPreferencesActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preferences);
		Preference receive_path = super.findPreference("receive_path");
		Preference build_version = findPreference("build_version");
		Preference open_source = findPreference("open_source");

		receive_path
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						Toast.makeText(getBaseContext(),
								getString(R.string.hold_to_set_folder),
								Toast.LENGTH_LONG).show();
						finish();
						return false;
					}
				});

		open_source
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						showDialog(0);
						return false;
					}
				});

		String path = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(
						"receive_path",
						Environment.getExternalStorageDirectory()
								.getAbsolutePath());
		receive_path.setSummary(path);

		PackageInfo pInfo;
		Integer app_version_code = 0;
		String app_version = "";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			app_version_code = pInfo.versionCode;
			app_version = pInfo.versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		build_version.setSummary(getString(R.string.version) + " "
				+ app_version + "(" + app_version_code + ")");

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

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d=null;
		if (id == 0) {
	    	TextView text = new TextView(this);
	    	ScrollView scrollView = new ScrollView(this);
	    	text.setPadding(15, 15, 15, 15);
	    	scrollView.addView(text);
	    	text.setSingleLine(false);
	    	text.setTextSize(17);
	    	text.setTextColor(0xFFFFFFFF);
	    	text.setText(getString(R.string.nanohttpd_license));
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle(getString(R.string.open_source));
	        builder.setView(scrollView);
	        d = builder.create();
		}
		return d;
	}
}
