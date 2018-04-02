package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

public class DebugActivity extends Activity{
	
	private ServerService srv=null;
	
	Handler h;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		h = new Handler();
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_debug);
		setTitle("Debug");
		final TextView time = (TextView)findViewById(R.id.textViewTime);
		
		h.post(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(srv!=null && srv.mPlayer.isMediaPlaying())
				{
					time.setText((int)srv.mPlayer.getMediaPlayer().getCurrentPosition()+"");
				}
				h.postDelayed(this, 10);
				
			}});
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		bindService(new Intent(this,ServerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		if(srv!=null)unbindService(mConnection);
	}
	
	
	 private ServiceConnection mConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // We've bound to LocalService, cast the IBinder and get LocalService instance
	            ServerBinder binder = (ServerBinder) service;
	            srv = binder.getService();

	        }

	        @Override
	        public void onServiceDisconnected(ComponentName arg0) {
	        	srv = null;
	        }
	    };

}
