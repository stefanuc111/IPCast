package it.SFApps.wifiqr;

import it.SFApps.wifiqr.tool.DebugActivity;
import it.SFApps.wifiqr.tool.LicenseDialog;
import it.SFApps.wifiqr.tool.PlayerActivity;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

@TargetApi(11)
public class SettingsFragment extends PreferenceFragment {
    boolean byclick=false;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        Preference receive_path = this.findPreference("receive_path");
        Preference build_version = findPreference("build_version");
        Preference open_source = findPreference("open_source");


        boolean isDebuggable =  ( 0 != ( getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        if(isDebuggable)
        {
        SwitchPreference debug  = new SwitchPreference(getActivity());
        debug.setKey("debug");
        debug.setTitle("Debug");
        debug.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				if(byclick)
				{
					byclick = false;
					return false;
				}
				return true;
			}});
        
        debug.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				byclick = true;
				startActivity(new Intent(getActivity(), DebugActivity.class));
				
				return false;
			}});
        this.getPreferenceScreen().addPreference(debug);
        }
        
        
        

        
        receive_path.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Toast.makeText(getActivity(), getString(R.string.hold_to_set_folder), Toast.LENGTH_LONG).show();
				Intent i = new Intent(getActivity(),MainActivity.class);
				i.putExtra("position", 0);
				startActivity(i);				
				return true;
			}});
    
        open_source.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				DialogFragment newFragment = new LicenseDialog();
			    newFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "missiles");
				return true;
			}});
        
        String path = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("receive_path", Environment.getExternalStorageDirectory().getAbsolutePath());
        receive_path.setSummary(path);
        
    	PackageInfo pInfo;
    	Integer app_version_code=0;
    	String app_version="";
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			app_version_code = pInfo.versionCode;
			app_version = pInfo.versionName;
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        build_version.setSummary(getString(R.string.version)+" "+app_version+"("+app_version_code+")");
	
        
	}
}