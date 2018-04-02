package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class SeekBarPreference extends Preference{
public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
public SeekBarPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	// TODO Auto-generated constructor stub
	

}

private Context c;
private ServerService srv;
private boolean mBound=false;
private SharedPreferences pref;
	@Override 
	public void onClick()
	{
		c = getContext();
		pref = PreferenceManager.getDefaultSharedPreferences(c);
		showDialog();
		c.bindService(new Intent(c,ServerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void showDialog()
	{

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        
        final SeekBar bar = new SeekBar(c);
        
        bar.setMax(350);
        bar.setProgress(pref.getInt("audio_offset", 0)+100);
        builder.setView(bar);
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener (){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				if(mBound)
				{
					if(srv.mPlayer.isMediaPlaying())
					{
						srv.setPlayerLatncyOffset(-(arg1-100));
						
					}
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}});
        AlertDialog sliding = builder.create();
        
        sliding.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				if(mBound)c.unbindService(mConnection);
				pref.edit().putInt("audio_offset", bar.getProgress()-100).commit();
			}});
        sliding.show();
        
	}
	
	
	 private ServiceConnection mConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // We've bound to LocalService, cast the IBinder and get LocalService instance
	            ServerBinder binder = (ServerBinder) service;
	            srv = binder.getService();
	            mBound = true;
	            
	            if(!srv.mPlayer.isMediaPlaying())Toast.makeText(c, R.string.should_start_music_video, Toast.LENGTH_LONG).show();
	            else
	            {
	            	Toast.makeText(c, R.string.slide_slow, Toast.LENGTH_LONG).show();
	            }
	        	
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName arg0) {
	            mBound = false;
	        }
	    };

}
