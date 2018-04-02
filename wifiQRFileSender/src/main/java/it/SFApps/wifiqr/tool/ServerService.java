package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.channels.FileChannel;
import java.nio.channels.NotYetConnectedException;

import it.SFApps.wifiqr.ServerActivity;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.NanoHTTPD.ProgressUpdates;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class ServerService extends Service implements ProgressUpdates, OnSharedPreferenceChangeListener,OnFileListChangeListener {
	
	
    public class ServerBinder extends Binder {
    	public ServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerService.this;
        }
    }

    
    private MyReceiver mReceiver;

    // use this as an inner class like here or as a top-level class
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {

				// get Wi-Fi Hotspot state here
				int state = intent.getIntExtra("wifi_state", 0);
					if(iplistener!=null)iplistener.onIPChange();
				}
			
			ConnectivityManager conMan = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = conMan.getActiveNetworkInfo();
			if (netInfo != null
					&& netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				if(iplistener!=null)iplistener.onIPChange();
			} else {
				// Log.d("WifiReceiver", "Don't have Wifi Connection");
			}
			
			}
    }
    


	
	private int srv_port;
	private boolean auto_download;
	public FileList f = new FileList();
	public boolean interrupted=false;
	private UploadServer serv;
	public AssetManager as;
	private SharedPreferences sharedPref;
	private File externalData;
	private WifiManager wifi;
	private Integer download_percent = 0;
    private final IBinder mBinder = new ServerBinder();
	private int mPlayerOffset = 0;
	private boolean sound_beam = false;
	private WSserver WSserv;
	public ServerPlayer mPlayer;
	public boolean debug_mode;
	private WifiLock wifilock;
	private WakeLock cpulock;

	@Override
	public void onCreate() {
		super.onCreate();
		f.registerOnFileListChangeListener(this);
		
		as = this.getAssets();
		String path = getFilesDir().getPath();
		if(!new File(path+"/ffmpeg/ffmpeg").exists())
		{
			new Utils.BinaryInstallerTask(this).execute(as);
		}
		  
		  
		mPlayer = new ServerPlayer(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		auto_download = sharedPref.getBoolean("auto_download", true);
		mPlayerOffset = -sharedPref.getInt("audio_offset", 0);
		sound_beam = sharedPref.getBoolean("sound_beam", false);
		mPlayer.EnableSoundBeam(sound_beam);

		srv_port = Integer.parseInt(sharedPref.getString("srv_port", "8080"));
		debug_mode = sharedPref.getBoolean("debug", false);

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		wifilock= wifi.createWifiLock(WifiManager.WIFI_MODE_FULL, "IPCastWifiLock");
		wifilock.acquire();
		
		PowerManager p = (PowerManager) getSystemService(Context.POWER_SERVICE);
		cpulock = p.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IPCastCPULock");
		cpulock.acquire();
				
		Intent intent = new Intent(this, ServerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent
				.getActivity(this, 0, intent, 0);

		Notification noti = new NotificationCompat.Builder(getBaseContext())
				.setContentTitle(getString(R.string.nome_app))
				.setContentText(getString(R.string.server_running))
				.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(false)
				.setOngoing(true).setContentIntent(pendIntent).build();

		startForeground(1234, noti);

		externalData = Environment.getExternalStorageDirectory();


		WSserv = new WSserver(new InetSocketAddress(srv_port+1));
		WSserv.start();
		
		serv = new UploadServer();
		try {
			serv.start(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mReceiver = new MyReceiver();
		IntentFilter mIntentFilter = new IntentFilter(
				"android.net.wifi.WIFI_AP_STATE_CHANGED");
		registerReceiver(mReceiver, mIntentFilter);
		IntentFilter mIntentFilter2 = new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mReceiver, mIntentFilter2);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		
		cpulock.release();
		wifilock.release();
		unregisterReceiver(mReceiver);

		mPlayer.release();
		serv.stop();
		try {
			WSserv.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isWifiApEnabled())
			Toast.makeText(getBaseContext(),
					getString(R.string.disable_tether), Toast.LENGTH_LONG)
					.show();
		f.unregisterOnFileListChangeListener(this);
	}

	public Boolean isWifiApEnabled() {
		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		for (Method method : wmMethods) {
			if (method.getName().equals("isWifiApEnabled")) {
				try {
					return (Boolean) method.invoke(wifi);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return false;

	}
	
	
	public interface onIPChangeListener
	{
		void onIPChange();
	}
	
	private onIPChangeListener iplistener = null;
	
	public void setOnIPChangeListener(onIPChangeListener l)
	{
		iplistener = l;
	}
	
	public Integer getPort()
	{
	return this.srv_port;
	}
	
	public String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";

	}
	

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    
    

	public class UploadServer extends NanoHTTPD {
		public UploadServer() {
			super(srv_port);

		}

		private String getMimeType(String fileUrl) {
			fileUrl = fileUrl.replace(" ", "");
			String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
			if(extension.equals("js"))return "application/x-javascript";
			
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					extension);
		}

		private void copyFile(File source, File dest) throws IOException {
			FileChannel inputChannel = null;
			FileChannel outputChannel = null;
			try {
				inputChannel = new FileInputStream(source).getChannel();
				outputChannel = new FileOutputStream(dest).getChannel();
				outputChannel
						.transferFrom(inputChannel, 0, inputChannel.size());
			} finally {
				inputChannel.close();
				outputChannel.close();
			}
		}

		private File getEmpthyFile(File f) {
			if (f.exists()) {
				String name = f.getName();
				if (name.matches("^*.*$")) {
					String extension = name.substring(name.lastIndexOf("."));
					String filename = name.substring(0, name.lastIndexOf("."));

					return getEmpthyFile(new File(f.getParent() + "/"
							+ filename + "(1)" + extension));
				} else {
					return getEmpthyFile(new File(f.getAbsolutePath() + "(1)"));

				}

			}
			return f;

		}

		@Override
		public Response serve(String uri, Method method,
				Map<String, String> header, Map<String, String> parms,
				Map<String, String> files) {

			if (uri.contentEquals("/")) {

				return new Response(HTMLBuilder.home(f.toArray(new File[f.size()]), auto_download,getBaseContext()));

			}

			if (uri.contentEquals("/send_files")) {

				int file_number = files.size();
				String st = "";
				List<String> destination_files = new ArrayList<String>();
				File uploaded_file[] = new File[file_number];
				int i = 0;

				for (Map.Entry<String, String> entry : files.entrySet()) {
					st += entry.getKey() + "/" + entry.getValue() + "</ br>";
					String file_name = parms.get("file_" + i);
					uploaded_file[i] = new File(entry.getValue());
					try {
						if (!sharedPref.contains("receive_path"))
							sharedPref
									.edit()
									.putString("receive_path",
											externalData.getAbsolutePath())
									.commit();

						File dest = getEmpthyFile(new File(
								sharedPref.getString("receive_path", "") + "/"
										+ file_name));
						if (dest.createNewFile()) {
							if(!uploaded_file[i].renameTo(dest))this.copyFile(uploaded_file[i], dest); //prova a spostare i file altrimenti se file system differenti li copia
							destination_files.add(dest.getAbsolutePath());
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					i++;
				}
				MediaScannerConnection.scanFile(getBaseContext(), (String[]) destination_files.toArray(new String[destination_files.size()]),null,null);

				return new Response(st);
			}
		
		
			if (uri.contentEquals("/api.json")) {
				JSONObject json = new JSONObject();
				try {
					json.put("auto_download", auto_download);
					json.put("downloaded", download_percent);
					json.put("SoundBeam", sound_beam);
					PlayListElement actual_playing = mPlayer.getActualPlaying();
					if(actual_playing!=null)
					{
						json.put("display_data", actual_playing.f.hashCode()+"_stream");
						switch (actual_playing.type)
						{
						case Video:
							json.put("display_data_type","video");
						break;
						case Music:
							json.put("display_data_type","audio");
							break;
						default:
							break;
						}
						
						PlayListElement next = mPlayer.GetNext();
						if(next!=null && next.type == FileType.Music)
						{
							json.put("nex_audio_file", next.f.hashCode()+"_stream");
						}
						
					}else if(image!=null)
					{
						json.put("display_data", image.hashCode()+"_stream");
						json.put("display_data_type","image");
					}
					
					
					
					json.put("isPlaying", mPlayer.isMediaPlaying());
					if(mPlayer.isMediaPlaying())
					{
					json.put("CurrentTime", mPlayer.getMediaPlayer().getCurrentPosition()+mPlayerOffset);
					}

					json.put("files", getJSONFileListArray());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				


				Response r = new Response(json.toString());
				
				r.addHeader("Cache-Control",
						"no-cache, no-store, must-revalidate"); // HTTP 1.1.
				r.addHeader("Pragma", "no-cache"); // HTTP 1.0.
				r.addHeader("Expires", "0"); // Proxies.
				return r;
			}

			

			
			
			Response r; // response
			
			for(PlayListElement element:mPlayer.playlist)
			{
				if(uri.contentEquals("/"+element.f.hashCode()+"_stream"))
				{
	

				
						try {
							FileInputStream inputStream = new FileInputStream(element.f);	
							r = new Response(NanoHTTPD.Response.Status.OK,
									getMimeType(element.f.getAbsolutePath()),
									inputStream);
							r.addHeader("Accept-Ranges", "bytes");			
							r.setRangeLimitFromHeader(header);
							r.setDataSize((int) element.f.length());
							return r;
	
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	
				}
			}
			
			if(getExternalCacheDir()!=null && new File(getExternalCacheDir().getAbsolutePath()+uri).exists())
			{
				File f = new File(getExternalCacheDir().getAbsolutePath()+uri);
				try {
					FileInputStream inputStream = new FileInputStream(f);	
					r = new Response(NanoHTTPD.Response.Status.OK,
							getMimeType(f.getAbsolutePath()),
							inputStream);
					r.addHeader("Accept-Ranges", "bytes");			
					r.setRangeLimitFromHeader(header);
					r.setDataSize((int) f.length());
					return r;

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			if(image!=null && uri.contentEquals("/"+image.hashCode()+"_stream"))
			{

				try {
					r = new Response(NanoHTTPD.Response.Status.OK,
							getMimeType(image.getAbsolutePath()),
							new FileInputStream(image));
					r.setDataSize((int) image.length());
					return r;
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

			}
			
			
			if (f != null)
				for (File e : f) {
					if (uri.contentEquals("/" + e.hashCode())) {
						try {							
							
							r = new Response(NanoHTTPD.Response.Status.OK,
									"application/octet-stream",
									new FileInputStream(e));
							r.addHeader("Content-Disposition",
									"attachment; filename=\"" + e.getName()
											+ "\"");
							r.setDataSize((int) e.length());
							return r;
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}

			try {

				InputStream file = as.open(uri.substring(1));

				r = new Response(NanoHTTPD.Response.Status.OK,
						getMimeType(uri), file);
				r.addHeader("Cache-Control",
						"no-cache, no-store, must-revalidate"); // HTTP 1.1.
				r.addHeader("Pragma", "no-cache"); // HTTP 1.0.
				r.addHeader("Expires", "0"); // Proxies.
				return r;

			} catch (IOException e) {
			}

			return new Response(NanoHTTPD.Response.Status.NOT_FOUND,NanoHTTPD.MIME_HTML,"<script>setTimeout(\"window.location='/'\",1000)</script> Error: " + uri + " Not found!");
		}
	}
	
	
	public class WSserver extends WebSocketServer{
		private Thread WakeUp;
		public WSserver(InetSocketAddress address) {
			super(address);
			// TODO Auto-generated constructor stub
			WakeUp = new Thread(new Runnable(){

				@Override
				public void run() {	
					// TODO Auto-generated method stub
					while(!Thread.interrupted())
					{
					sendToAll("w");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							break;
						}
					}
				}});
			WakeUp.start();
		}
		@Override
		public void stop() throws IOException, InterruptedException{
			WakeUp.interrupt();
			
			super.stop();
			
		}
		
	
		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			// TODO Auto-generated method stub
			conn.send(getJSONSettingsChanges());			
			conn.send(getJSONCurrentData());
			conn.send(getJSONPlayStatus());
			conn.send(getJSONFileList());
			
			
			if(mPlayer.GetNext()!=null)conn.send(getJSONNextData());
		
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason,
				boolean remote) {
			// TODO Auto-generated method stub
			
		}	
		@Override
		public void onMessage(WebSocket conn, String message) {
		// TODO Auto-generated method stub
			JSONObject response = null;
			String type = null;
			if(message.equals("getCurrentTime"))
			{
				response = new JSONObject();
				type = "CurrentTime";
				try {
					if(mPlayer.mMediaPrepared)
					response.put("value",mPlayer.getMediaPlayer().getCurrentPosition()+mPlayerOffset);
					else
					response.put("value",0);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e("error2", "error2");
				}
			}

			if(response!=null)
			{
				try {
					response.put("type",type);
				} catch (JSONException e) {
					Log.e("error1", "error1");
						
				}
				synchronized(conn)
				{
				conn.send(response.toString());
				}
			}
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			// TODO Auto-generated method stub
			
		}
	
	}
	
	
	public String getJSONPlayStatus()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "PlayStatusChange");
			json.put("isPlaying",mPlayer.isMediaPlaying());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public String getJSONSettingsChanges()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "SettingsChanges");
			json.put("AutoDownload",auto_download);
			json.put("SoundBeam", sound_beam);
			json.put("Debug", debug_mode);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	
	public String getJSONNextData()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "NextData");
			PlayListElement e = mPlayer.GetNext();			
			if(e!=null)
			{
				json.put("data",e.f.hashCode()+"_stream");
				if(e.type == FileType.Music)
				{
					json.put("data_type", "audio");
				}else
				{
					json.put("data_type","video");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}	
	public String getJSONCurrentData()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "CurrentData");
			PlayListElement e = mPlayer.getActualPlaying();
			if(image != null)
			{
				json.put("data",image.hashCode()+"_stream");
				json.put("data_type", "image");
			}else				
			if(e!=null)
			{
				json.put("data",e.f.hashCode()+"_stream");
				if(e.type == FileType.Music)
				{
					json.put("data_type", "audio");
					Integer sample = ((CustomMediaPlayer)mPlayer.getMediaPlayer()).getSampleRate();
					json.put("sample_rate", sample);
				}else
				{
					json.put("data_type","video");
				}
				json.put("duration", mPlayer.getMediaPlayer().getDuration()/1000);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public String getJSONCurrentTime()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "CurrentTime");
			json.put("data",mPlayer.getMediaPlayer().getCurrentPosition()+mPlayerOffset);
			if(mPlayer.getActualPlaying().type == FileType.Music)
			{
				json.put("data_type", "audio");
			}else
			{
				json.put("data_type","video");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public String getJSONFileList()
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", "FileListChange");
			json.put("files", getJSONFileListArray());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	private JSONArray getJSONFileListArray()
	{
		JSONArray json_files = new JSONArray();
		for (File file : f) {
			JSONObject json_details = new JSONObject();
			try {
				json_details.put("name", file.getName());
				json_details.put("hash", file.hashCode());
			
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			json_files.put(json_details);
		}
		return json_files;
	}
	 
	
	
	public String getMessage(String type, String message)
	{
		JSONObject json = new JSONObject();
		
		try {
			json.put("type", type);
			json.put("value",message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	
	public void sendToAll( String text ) {
		if(WSserv!=null)
		{
			Collection<WebSocket> con = WSserv.connections();
			synchronized ( con ) {
				for( WebSocket c : con ) {
					try{
						c.send( text );
					}catch(NotYetConnectedException e)
					{

					}
				}
			}
		}
	}

	@Override
	public void onUpdate(Integer percentual) {
		// TODO Auto-generated method stub
		if(Math.abs(percentual-download_percent)>1)
		{
				JSONObject json = new JSONObject();
				
				try {
					json.put("type", "UploadedStatusChange");
					json.put("value",percentual);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendToAll(json.toString());
				download_percent = percentual;
		}
	}



	public void setPlayerLatncyOffset(int i) {
		// TODO Auto-generated method stub
		mPlayerOffset =  i;

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0,
			String key) {
		// TODO Auto-generated method stub
		auto_download = sharedPref.getBoolean("auto_download", true);
		srv_port = Integer.parseInt(sharedPref.getString("srv_port", "8080"));
		sound_beam = sharedPref.getBoolean("sound_beam", false);
		debug_mode = sharedPref.getBoolean("debug", false);
		
		if(key.equals("sound_beam"))mPlayer.EnableSoundBeam(sound_beam);
		if(key.equals("srv_port"))
		{
			serv.stop();
			try {
				serv = new UploadServer();
				serv.start(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				WSserv.stop();
				WSserv = new WSserver(new InetSocketAddress(srv_port+1));
				WSserv.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		
		}
		sendToAll(getJSONSettingsChanges());

	}
	
    File image=null;
	public void setImage(File file)
	{
		mPlayer.ClosePlayer();
		image = file;
		sendToAll(getJSONCurrentData());
	}

	public File getImage() {
		// TODO Auto-generated method stub
		return this.image;
	}

	@Override
	public void onFileListChange() {
		// TODO Auto-generated method stub
		sendToAll(getJSONFileList());
	}
}
