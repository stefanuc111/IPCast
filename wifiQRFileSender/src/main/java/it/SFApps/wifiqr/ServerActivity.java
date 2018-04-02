package it.SFApps.wifiqr;

import it.SFApps.wifiqr.tool.QRCodeEncoder;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import it.SFApps.wifiqr.tool.ServerService.onIPChangeListener;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ServerActivity extends ActionBarActivity {


	private TextView textViewLink;
	public int srv_port;
	private Intent Serverintent;
	private WifiManager wifi;
	private SharedPreferences sharedPref;
	private String pass;
	private NfcAdapter nfc;
	public ServerService serv;
    public boolean mBound = false;

    
	OnClickListener positive_click = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			if(setAPEnabled(true))
			{
				Toast.makeText(getBaseContext(), R.string.creating_ap,
						Toast.LENGTH_LONG).show();
			}else
			{
				Toast.makeText(getBaseContext(), R.string.error_creating_ap,
						Toast.LENGTH_LONG).show();
			}
			startService(Serverintent);
		}
	};

	OnClickListener negative_click = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int arg1) {
			dialog.dismiss();
			stopService(Serverintent);
			finish();
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_download);
		
		Serverintent = new Intent(getBaseContext(), ServerService.class);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		pass = sharedPref.getString("ap_password", "");
		srv_port = Integer.parseInt(sharedPref.getString("srv_port", "8080"));
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		textViewLink = (TextView) findViewById(R.id.textViewHTTP);

		startService(Serverintent);

		
		//============ NFC ============

		if (nfc_compatible()) {
			RelativeLayout nfcLayout = (RelativeLayout) this
					.findViewById(R.id.RelativeLatoutNFC);
			nfcLayout.setVisibility(View.VISIBLE);
		}
		//===========================


		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		

//================ Show thetering mode ===================
		if (!mWifi.isConnected() && !isWifiApEnabled()) {
			String message = "";
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (!pass.equals("")) {
				message = getString(R.string.waring_mobile_data)
						+ "\n Password: " + pass;
			} else {
				message = getString(R.string.waring_mobile_data);

			}

			builder.setMessage(message).setTitle(R.string.enable_ap)
					.setPositiveButton(R.string.yes, positive_click)
					.setNegativeButton(R.string.no, negative_click);
			AlertDialog dialog = builder.create();

			dialog.show();

		}
//=======================================================
		

	}

	public Boolean isWifiApEnabled() {
		try{
		Method m = wifi.getClass().getMethod("isWifiApEnabled");
		return (Boolean) m.invoke(wifi);
		}catch(Exception e)
		{
		}
		return false;

	}

	public Boolean setAPEnabled(boolean enable) {
		if(enable)wifi.setWifiEnabled(false);

		WifiConfiguration netConfig = new WifiConfiguration();
		netConfig.SSID = "WifiQr File Downloader";
		if (!pass.equals("")) {
			netConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			netConfig.preSharedKey = pass;
		} else {
			netConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.NONE);
		}
		
		try {

			Method method = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);

			return (Boolean) method.invoke(wifi, netConfig, enable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server_activity, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.stop_service:
			if(mBound)
			{
				serv.interrupted=true;
			stopService(Serverintent);
			finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        bindService(Serverintent, mConnection, Context.BIND_AUTO_CREATE);
		
		

	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();

		if (nfc_compatible()) {
			if(serv!=null)
			{
			NdefRecord extRecord = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,
					("http://" + serv.getIPAddress(true) + ":" + srv_port)
							.getBytes(Charset.forName("US-ASCII")),
					new byte[0], new byte[0]);

			NdefMessage message = new NdefMessage(extRecord);
			nfc.enableForegroundNdefPush(this, message);
			}
		}
	}

	@SuppressLint("NewApi")
	private boolean nfc_compatible() {
		if (Build.VERSION.SDK_INT >= 10)
			nfc = NfcAdapter.getDefaultAdapter(this);
		
		if (nfc != null && nfc.isEnabled()) {
			if (Build.VERSION.SDK_INT >= 10 && Build.VERSION.SDK_INT < 16)
				return true;
			if (Build.VERSION.SDK_INT >= 16 && nfc.isNdefPushEnabled())
				return true;
		}
		return false;
	}

	@SuppressLint("NewApi")
	public void drawData() {
		ImageView imageViewQR = (ImageView) findViewById(R.id.imageViewQR);
		textViewLink.setText(serv.getIPAddress(true) + ":" + srv_port);

	
		String qrData = "http://" + serv.getIPAddress(true) + ":" + srv_port + "/";

		int qrCodeDimention = 500;

		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrData, null,
				Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
				qrCodeDimention);
		try {
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			imageViewQR.setImageBitmap(bitmap);
		} catch (WriterException e) {
			imageViewQR.setImageResource(R.drawable.no_qr_code);
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

	}

	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		super.onPause();
		if (nfc != null)
			nfc.disableForegroundNdefPush(this);
	}

	private File getFileFromURI(Uri u) {
		String path = null;

		try {
			String scheme = u.getScheme();
			if (scheme.equals("file")) {
				path = u.getPath();
			} else if (scheme.equals("content")) {
				path = getRealPathFromURI(u);
			}
		} catch (Exception e) {
		}

		if (path != null)
			return new File(path);
		else {
			Toast.makeText(getBaseContext(), "Not supported!",
					Toast.LENGTH_LONG).show();
			finish();
		}
		return null;
	}

	private String getRealPathFromURI(Uri contentUri) {

		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(getBaseContext(), contentUri,
				proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	
	 private ServiceConnection mConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // We've bound to LocalService, cast the IBinder and get LocalService instance
	            ServerBinder binder = (ServerBinder) service;
	            serv = binder.getService();
	            mBound = true;
	            
	            
	            Intent in = getIntent();

	    		String action = in.getAction();
	    		String type = in.getType();

	    		if (Intent.ACTION_SEND.equals(action) && type != null) {
	    			Uri uri = (Uri) in.getParcelableExtra(Intent.EXTRA_STREAM);
	    			serv.f.add(getFileFromURI(uri));

	    		}

	    		if (Intent.ACTION_SEND_MULTIPLE.equals(action)
	    				&& in.hasExtra(Intent.EXTRA_STREAM)) {
	    			ArrayList<Parcelable> list = in
	    					.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    			for (Parcelable p : list) {
	    				Uri uri = (Uri) p;

	    				serv.f.add(getFileFromURI(uri));
	    			}
	    		}
	    		

	    		
	    		drawData();
	    		
	    		serv.setOnIPChangeListener(new onIPChangeListener(){

					@Override
					public void onIPChange() {
						drawData();
					}});
	            
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName arg0) {
	            mBound = false;
	        }
	    };

}
