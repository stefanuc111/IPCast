package it.SFApps.wifiqr.music_explorer.fragments;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.activitys.AlbumActivity;
import it.SFApps.wifiqr.music_explorer.activitys.AlbumAdapter;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.ServerFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class AlbumFragment extends ServerFragment implements LoaderCallbacks<Cursor>{
	private AlbumAdapter adapter;
	private GridViewCompat grid;
	private String artist;
	private Long artist_id;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.music_page_layout, container, false);
		Bundle b = getArguments();
		artist = b != null? b.getString("artist_name"): null;
		artist_id = b != null? b.getLong("artist_id"): null;

		grid = (GridViewCompat) view
				.findViewById(R.id.gridView1);
		adapter = new AlbumAdapter(getActivity(), null, false, artist_id==null?false:true);
		
		return view;
	}
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(10, null, this);
        
        grid.setVisibility(View.VISIBLE);
		grid.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Bundle b = new Bundle();
				if(arg2 == 0 && artist_id!=null)
				{
				b.putLong("artist_id", artist_id);
				b.putString("album_name", artist+" - "+getString(R.string.all_songs));
				}else
				{				
				Cursor c = (Cursor)adapter.getItem(arg2);
				b.putLong("album_id", c.getLong(0));
				b.putString("album_name", c.getString(1));
				}
				Intent i = new Intent(getActivity(),AlbumActivity.class);
				i.putExtra("data", b);
				startActivity(i);
			}});
		grid.setAdapter(adapter);
	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		String[] projection = new String[] { Albums._ID, Albums.ALBUM,
				Albums.ARTIST, Albums.ALBUM_ART, Albums.NUMBER_OF_SONGS };

		String sortOrder = Media.ALBUM + " ASC";
		 String where = null;
		if(artist!=null)
		{
			 where = Audio.Albums.ARTIST
		              + "='"+artist+"'";
		}
		return new CursorLoader(getActivity(),Albums.EXTERNAL_CONTENT_URI, projection, where,
				null, sortOrder);	}
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		adapter.swapCursor(arg1);
	}
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}


}
