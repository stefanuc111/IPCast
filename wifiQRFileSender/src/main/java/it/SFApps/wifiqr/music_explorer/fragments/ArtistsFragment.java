package it.SFApps.wifiqr.music_explorer.fragments;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.activitys.ArtistActivity;
import it.SFApps.wifiqr.music_explorer.activitys.ArtistsAdapter;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.ServerFragment;
import it.SFApps.wifiqr.tool.ServerService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Artists;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

public class ArtistsFragment extends ServerFragment implements LoaderCallbacks<Cursor>{
	private ArtistsAdapter adapter;
	private ListView list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.music_page_layout, container, false);

		adapter = new ArtistsAdapter(getActivity(), null, false);

		list = (ListView) view.findViewById(R.id.listView2);

		list.setAdapter(adapter);
		return view;
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		list.setVisibility(View.VISIBLE);
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Intent i = new Intent(getActivity(),ArtistActivity.class);
				Cursor c = (Cursor)((CursorAdapter)arg0.getAdapter()).getItem(arg2);
				Bundle b = new Bundle();
				b.putLong("artist_id", c.getLong(0));
				String artist = c.getString(1);
				if(artist.equals("<unknown>"))artist = getString(R.string.unknown_artist);
				b.putString("artist_name",artist);
				i.putExtra("data", b);
					startActivity(i);
			}});
		super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(4, null, this);
	}
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { Artists._ID, Artists.ARTIST,
				Artists.NUMBER_OF_ALBUMS, Artists.NUMBER_OF_TRACKS };

		String sortOrder = Artists.DEFAULT_SORT_ORDER;

		return new CursorLoader(getActivity(),Audio.Artists.EXTERNAL_CONTENT_URI, projection, null,
				null, sortOrder);
		// TODO Auto-generated method stub
		
	}
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
