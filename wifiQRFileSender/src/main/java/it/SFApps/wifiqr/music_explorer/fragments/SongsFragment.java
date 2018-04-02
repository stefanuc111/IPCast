package it.SFApps.wifiqr.music_explorer.fragments;

import java.io.File;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.SongsAdapter;
import it.SFApps.wifiqr.tool.ServerFragment;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;
import it.SFApps.wifiqr.tool.ServerService;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class SongsFragment extends ServerFragment implements LoaderCallbacks<Cursor>{
	private ListView list;
	private SongsAdapter adapter;
	private Long album_id;
	private Long artist_id;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.music_page_layout, container, false);
		list = (ListView) view.findViewById(R.id.listView1);
		list.setVisibility(View.VISIBLE);
		Bundle b = this.getArguments();
		if(b!=null)
		{
		album_id = b.containsKey("album_id") ? b.getLong("album_id") : null;
		artist_id = b.containsKey("artist_id") ? b.getLong("artist_id") : null;
		}

		return view;
	}
	@Override
	public void onServerConnected(ServerService s)
	{

		adapter = new SongsAdapter(getActivity(),null, false,srv, list);
		list.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Cursor c = (Cursor) arg0.getAdapter().getItem(arg2);
				File f = new File(c.getString(5));
				if(srv.f.contains(f))
				{
					srv.f.remove(f);
				}else
				{
					srv.f.add(f);
				}
				getActivity().supportInvalidateOptionsMenu();
			}});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Cursor c = (Cursor) ((CursorAdapter) arg0.getAdapter())
						.getItem(position);
				File f = new File(c.getString(5));
				if(srv!=null)
				{
					srv.mPlayer.addToPlaylist(new PlayListElement(f,FileType.Music,c.getLong(4),c.getString(1)), false);
					((CursorAdapter)arg0.getAdapter()).notifyDataSetChanged();
					return true;
				}
				return false;
			}
			
		});
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onFileListChange()
	{
		((CursorAdapter)list.getAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onActualPlayingChange()
	{
		((CursorAdapter)list.getAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		String[] projection = new String[] { Audio.Media._ID,
				Audio.Media.TITLE, Audio.Media.ARTIST,
				Audio.Media.DURATION, Audio.Media.ALBUM_ID,Audio.Media.DATA,Audio.Media.ARTIST_ID };
		String where = null;
		
		if(album_id!=null)
		{
			where = Audio.Media.ALBUM_ID+"="+album_id;
		}
		
		if(artist_id!=null)
		{
			where = Media.ARTIST_ID+"="+artist_id;
		}
		String sortOrder = Media.DEFAULT_SORT_ORDER;

		return new CursorLoader(getActivity(),Media.EXTERNAL_CONTENT_URI, projection, where,
				null, sortOrder);
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
