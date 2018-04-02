package it.SFApps.wifiqr.music_explorer;

import java.io.File;
import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.Utils;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SongsAdapter extends CursorAdapter {

	
	ServerService server;
	ListView list;
	public SongsAdapter(Context context, Cursor cursor, boolean autoRequery, ServerService srv,ListView list) {
		super(context, cursor, autoRequery);
		server=srv;
		this.list = list;		
		}

	

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView text = (TextView) view.findViewById(R.id.textView1);
		TextView text1 = (TextView) view.findViewById(R.id.textView2);
	    ImageView coverAlbum=(ImageView)view.findViewById(R.id.imageView1);
		ImageView isPlaying = (ImageView) view.findViewById(R.id.imageView2);

		text.setText(cursor.getString(1));
		
		try{
			String artist = cursor.getString(2);
			if(artist.equals("<unknown>"))
			{
				text1.setText(R.string.unknown_artist);
			}else text1.setText(artist);
		}catch(Exception e)
		{
			text1.setText(R.string.unknown_artist);
		}
		
		PlayListElement media = server.mPlayer.getActualPlaying();
		if(media!=null && media.f.equals(new File(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))))
		{
			isPlaying.setVisibility(View.VISIBLE);
			text1.setText(R.string.now_playing);
		}else
		{
			isPlaying.setVisibility(View.GONE);

		}
		
		if(view.getTag()==null||(Integer)view.getTag()!=cursor.getPosition())
		{
			coverAlbum.setImageResource(R.drawable.album);
			new AsyncImageLoad(coverAlbum).execute(cursor.getLong(4));
		}
		
		if(server.f.contains(new File(cursor.getString(5))))
		{
			list.setItemChecked(cursor.getPosition(), true);
		}else
		{
			list.setItemChecked(cursor.getPosition(), false);
		}
		view.setTag(cursor.getPosition());
	}
	
	
	

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup arg2) {

		LayoutInflater inflater = LayoutInflater.from(c);
		View view = inflater.inflate(R.layout.music_songs_row,
				arg2, false);

		return view;
	}
	
	
	public class AsyncImageLoad extends AsyncTask<Long, Void, Bitmap>{
		private ImageView img;
		AsyncImageLoad(ImageView img)
		{
			this.img = img;
			if(img.getTag()!=null)
			{
				((AsyncImageLoad)img.getTag()).cancel(true);
			}
			
			img.setTag(this);

			
		}
		
		@Override
		protected Bitmap doInBackground(Long ... arg0) {
			
			
			return Utils.getAlbumart(arg0[0],mContext);
		}
		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected void onPostExecute(Bitmap result) {
			

			if(result!=null)
			{
			img.setImageBitmap(result);
				if(Build.VERSION.SDK_INT>=11)
				{
					ObjectAnimator fade = ObjectAnimator.ofFloat(img, "alpha", 0.0f,1.0f);
					fade.setDuration(200);
					fade.start();
				}
			}
			
	     }


	}

}
