package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.SongsAdapter;
import it.SFApps.wifiqr.music_explorer.fragments.AlbumFragment;
import it.SFApps.wifiqr.music_explorer.fragments.SongsFragment;
import it.SFApps.wifiqr.tool.PlayerActivity;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

public class AlbumActivity extends PlayerActivity{
	private Long album_id;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.music_page_layout);
		
		setTitle(getIntent().getBundleExtra("data").getString("album_name"));
			
        if (savedInstanceState == null) {
            Fragment newFragment = new SongsFragment();
            newFragment.setArguments(getIntent().getBundleExtra("data"));
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, newFragment).commit();
        }
			

		DisableDrawerIndicator();
	    }
	

	@Override
	public Intent  getSupportParentActivityIntent() 
	{
		super.getSupportParentActivityIntent();
		finish();
		return null;
	}
	

}
