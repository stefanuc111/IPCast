package it.SFApps.wifiqr.music_explorer.fragments;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.activitys.GenreActivity;
import it.SFApps.wifiqr.music_explorer.activitys.GenresAdapter;
import it.SFApps.wifiqr.tool.ServerFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class GenreFragment extends ServerFragment implements LoaderCallbacks<Cursor>{
	private GenresAdapter gen_adap;
	private ListView list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.music_page_layout, container, false);
	

		list = (ListView) view.findViewById(R.id.listView2);
		list.setVisibility(View.VISIBLE);
		gen_adap = new GenresAdapter(getActivity(), null, false);
		list.setAdapter(gen_adap);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(1, null, this);
        
        list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Cursor c = (Cursor) arg0.getAdapter().getItem(arg2);
				Intent i = new Intent(getActivity(),GenreActivity.class);
				Bundle b = new Bundle();
				b.putLong("genre_id", c.getLong(0));
				b.putString("genre_name", c.getString(1));
				i.putExtra("data", b);
				startActivity(i);
			}});
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		
		
		String[] projection = new String[] { Audio.Genres._ID,
				Audio.Genres.NAME};

		String sortOrder = Audio.Genres.DEFAULT_SORT_ORDER;

		return new CursorLoader(getActivity(),Audio.Genres.EXTERNAL_CONTENT_URI, projection, null,null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		gen_adap.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
