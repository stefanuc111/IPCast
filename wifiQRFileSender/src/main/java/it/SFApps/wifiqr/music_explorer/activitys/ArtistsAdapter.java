package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Artists;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ArtistsAdapter extends CursorAdapter {

	
	
	public ArtistsAdapter(Context context, Cursor cursor, boolean autoRequery) {
		super(context, cursor, autoRequery);
		}
	
	

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView text = (TextView) view.findViewById(R.id.textView1);
		TextView text1 = (TextView) view.findViewById(R.id.textView2);

		String artist = cursor.getString(1);
		if(artist.equals("<unknown>"))
		{
			text.setText(R.string.unknown_artist);
		}else text.setText(artist);
		
		text1.setText(cursor.getInt(cursor.getColumnIndex(Artists.NUMBER_OF_TRACKS))+" "+arg1.getString(R.string.songs));

	}
	
	
	

	
	@Override
	public View newView(Context c, Cursor arg1, ViewGroup arg2) {

		LayoutInflater inflater = LayoutInflater.from(c);
		View view = inflater.inflate(R.layout.music_artist_row,
				arg2, false);

		return view;
	}

}
