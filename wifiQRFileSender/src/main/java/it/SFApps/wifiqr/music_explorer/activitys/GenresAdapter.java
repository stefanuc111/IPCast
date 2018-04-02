package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class GenresAdapter extends CursorAdapter {

	public GenresAdapter(Context context, Cursor cursor, boolean autoRequery) {
		super(context, cursor, autoRequery);
		}

	

	
	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView text = (TextView) view.findViewById(R.id.textView1);

		text.setText(cursor.getString(1));
	}
	
	
	

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup arg2) {

		LayoutInflater inflater = LayoutInflater.from(c);
		View view = inflater.inflate(R.layout.music_genres_row,
				arg2, false);

		return view;
	}
	



}
