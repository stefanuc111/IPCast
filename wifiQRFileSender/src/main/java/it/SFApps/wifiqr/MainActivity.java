package it.SFApps.wifiqr;

import it.SFApps.wifiqr.app_explorer.AppsExplorerFragment;
import it.SFApps.wifiqr.file_explorer.FileExplorerFragment;
import it.SFApps.wifiqr.music_explorer.MusicExplorerTabFragment;
import it.SFApps.wifiqr.photo_explorer.PhotoExplorerFragment;
import it.SFApps.wifiqr.tool.PlayerActivity;
import it.SFApps.wifiqr.tool.ServerService;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;

public class MainActivity extends PlayerActivity  {


	private ListView menu_list;	
	private  String DrawerNames[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menu_list = (ListView) findViewById(R.id.left_drawer);
		DrawerNames = getResources().getStringArray(R.array.drawer_menu);

		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		startService(new Intent(this, ServerService.class));

	

		if (savedInstanceState == null) {
			loadFragment(0);
			menu_list.setItemChecked(0, true);
		}
		
		menu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				loadFragment(position);
				closeDrawer();

				}
			

		});

		
		
		
		if(savedInstanceState==null)
		{
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;

			if(version.endsWith("b"))
			{
		        new AlertDialog.Builder(this).setTitle(getString(R.string.nome_app)+" BETA")
		        .setMessage(R.string.beta_message).setPositiveButton(getString(R.string.beta_message_ok), null).show();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if(sharedPref.getBoolean("firstLaunch", true))
		{
	        new AlertDialog.Builder(this).setTitle(R.string.changelog_title)
	        .setMessage(R.string.changelog_message).setPositiveButton(R.string.changelog_Ok_button, new OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					sharedPref.edit().putBoolean("firstLaunch", false).commit();

				}}).show();
	
		}
		
	}
	
	
	private void loadFragment(int position)
	{
		Fragment Actual = getSupportFragmentManager().findFragmentById(
				R.id.content_frame);
		Fragment n = null;
		String tag = null;
		setTitle(DrawerNames[position]);
		switch (position) {
		case 0:
			n = new FileExplorerFragment();
			tag = "file_exp";
			
			break;
		case 1:
			n = new AppsExplorerFragment();
			tag = "apps_exp";
			break;
		case 2:
			n = new PhotoExplorerFragment();
			tag = "photo_exp";
			break;
		case 3:
			n = new MusicExplorerTabFragment();
			tag = "music_exp";
			break;
		}
		
		if(Actual!=null)
		{
			if (Actual.getTag() != tag && n != null) {
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, n, tag).commit();
			}
		}else
		{
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.content_frame, n, tag).commit();
		}
	}


	@Override
	public void onNewIntent(Intent i)
	{
		super.onNewIntent(i);
		Bundle extra = i.getExtras();
		if(extra!=null && extra.containsKey("position"))
		{
			loadFragment(extra.getInt("position"));
			menu_list.setItemChecked(extra.getInt("position"), true);
		
		}
	}
	



	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("Activity", "destroyed");

		if (isFinishing()) {
			stopService(new Intent(this, ServerService.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.scanQR:
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_WIDTH", 1000);
			intent.putExtra("SCAN_HEIGHT", 1000);
			intent.putExtra("PROMPT_MESSAGE", getString(R.string.frame_qr));
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");


            Base.getTraker().send(new HitBuilders.EventBuilder().setAction("ui_action").setLabel("QRCameraButton").build());

			startActivityForResult(intent, 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if (format.equals("QR_CODE")) {
					Intent i = new Intent(Intent.ACTION_VIEW,
							Uri.parse(contents));
					startActivity(i);
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}



	@Override
	public void onBackPressed() {

		if (getSupportFragmentManager().findFragmentById(R.id.content_frame)
				.getTag() == "file_exp") {
			if (((FileExplorerFragment) getSupportFragmentManager()
					.findFragmentById(R.id.content_frame)).onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	
	
	

}
