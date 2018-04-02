package it.SFApps.wifiqr.playlist;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.PlayerActivity;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerService;

public class PlayListActivity extends PlayerActivity implements PlayListChangeListener{
	
	private DynamicListView mQueueList;
	private PlayListAdapter adapter = null;
	private ServerService srv;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_list_activity);
		mQueueList = (DynamicListView) findViewById(R.id.listViewQueue);
		
		mQueueList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				srv.mPlayer.PlayMedia(position);
			}});
		
		setTitle(getString(R.string.queue));
		DisableDrawerIndicator();
		HidePlayListButton();
	}
	
	@Override
	public Intent  getSupportParentActivityIntent() 
	{
		super.getSupportParentActivityIntent();
		finish();
		return null;
	}
	
	@Override
	public void onServerConnected(ServerService srv)
	{
		this.srv=srv;
		mQueueList.setPlaylist((ArrayList<PlayListElement>)srv.mPlayer.playlist);
		adapter = new PlayListAdapter(this,srv);
		mQueueList.setAdapter(adapter);
		srv.mPlayer.registerActualPlayingChangeListener(this);
	}
	
	@Override
	public void onPlayerStop()
	{
		super.onPlayerStop();
		srv.mPlayer.unregisterActualPlayingChangeListener(this);
		finish();
	}

	@Override
	public void onActualPlayingChange() {
		// TODO Auto-generated method stub
		if(adapter!=null)adapter.notifyDataSetChanged();
	}

	@Override
	public void onPlaylistChange() {
		// TODO Auto-generated method stub
		if(adapter!=null)adapter.notifyDataSetChanged();
	}

}
