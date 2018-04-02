package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListChangeListener;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;

public class ServerFragment extends Fragment implements OnFileListChangeListener, PlayListChangeListener{
	
	public ServerService srv=null;
	@Override
	public void onStart()
	{
	super.onStart();

	this.getActivity().bindService(new Intent(getActivity(), ServerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onStop()
	{
	super.onStop();
	
		if(srv!=null)
		{
			srv.f.unregisterOnFileListChangeListener(this);
			srv.mPlayer.unregisterActualPlayingChangeListener(this);
			getActivity().unbindService(mConnection);
			srv=null;
		}
	}
	
	public void onServerConnected(ServerService srv){}
	public void onServerDisconnected(){}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ServerBinder binder = (ServerBinder) service;
			srv = binder.getService();
			srv.f.registerOnFileListChangeListener(ServerFragment.this);
			srv.mPlayer.registerActualPlayingChangeListener(ServerFragment.this);

			onServerConnected(srv);	
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			onServerDisconnected();

		}
	};
	@Override
	public void onFileListChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActualPlayingChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlaylistChange() {
		// TODO Auto-generated method stub
		
	}
}
