package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.PlayerActivity;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class GenreActivity extends PlayerActivity{
	private Long genre_id;
	private boolean mBound = false;
	private ServerService srv = null;
	private GridView list;
	private Cursor cursor=null;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.music_page_layout);
		list = (GridView) findViewById(R.id.gridView1);
		list.setVisibility(View.VISIBLE);
		Bundle b = getIntent().getBundleExtra("data");
		genre_id = b.getLong("genre_id", 0);
		String genre_name = b.getString("genre_name");
		setTitle(genre_name);
		
	        Uri uri = Audio.Albums.getContentUri("external");
	        try {
	            String selection = "album_info._id IN "
	                    + "(SELECT (audio_meta.album_id) album_id FROM audio_meta, audio_genres_map "
	                    + "WHERE audio_genres_map.audio_id=audio_meta._id AND audio_genres_map.genre_id=?)";
	            String[] selectionArgs = new String[] { String.valueOf(genre_id) };
	            String[] proj = {"_id",AlbumColumns.ALBUM,AlbumColumns.ARTIST,AlbumColumns.ALBUM_ART,AlbumColumns.NUMBER_OF_SONGS };
	            cursor = getContentResolver().query(uri, proj, selection,
	                    selectionArgs, null);
	            
	            }
	         catch (Exception ex) {
	        Log.i("error","error");
	        ex.printStackTrace();
	        }
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Intent i = new Intent(GenreActivity.this,AlbumActivity.class);
				Cursor c = (Cursor)((CursorAdapter)arg0.getAdapter()).getItem(arg2);
				Bundle b = new Bundle();

				b.putLong("album_id", c.getLong(0));
				b.putString("album_name", c.getString(1));
				i.putExtra("data", b);
				startActivity(i);
			}});
		
		DisableDrawerIndicator();
	}
	
	@Override
	public Intent  getSupportParentActivityIntent() 
	{
		super.getSupportParentActivityIntent();
		finish();
		return null;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		bindService(new Intent( this,ServerService.class), conn, Activity.BIND_AUTO_CREATE);
	}
	 
	@Override
	public void onStop()
	{
		super.onStop();
		if(mBound)
		{
			unbindService(conn);
		}
	}
	
	private ServiceConnection conn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			ServerBinder binder = (ServerBinder)arg1;
			
			srv = binder.getService();
			list.setAdapter(new AlbumAdapter(GenreActivity.this, cursor, false,false));

			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}};
	

}
