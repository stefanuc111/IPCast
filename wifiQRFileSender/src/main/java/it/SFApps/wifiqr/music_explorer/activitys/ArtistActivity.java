package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.fragments.AlbumFragment;
import it.SFApps.wifiqr.tool.PlayerActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class ArtistActivity extends PlayerActivity{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setTitle(getIntent().getBundleExtra("data").getString("artist_name"));

        if (savedInstanceState == null) {
            Fragment newFragment = new AlbumFragment();
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
