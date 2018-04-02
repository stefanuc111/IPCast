package it.SFApps.wifiqr.music_explorer.activitys;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.SongsAdapter.AsyncImageLoad;
import it.SFApps.wifiqr.tool.Utils;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends CursorAdapter {

	Cursor c;
	Boolean Allsong=false;
	
	public AlbumAdapter(Context context, Cursor cursor, boolean autoRequery, boolean t) {
		super(context, cursor, autoRequery);
		Allsong = t;
		}
	

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView text = (TextView) view.findViewById(R.id.textViewElementName);
		ImageView art = (ImageView) view.findViewById(R.id.imageView);
		if(cursor!=null)
		{
			text.setText(cursor.getString(1));
			
			if(view.getTag()==null||(Integer)view.getTag()!=cursor.getPosition())
			{
				art.setImageResource(R.drawable.album);
				new AsyncImageLoad(art).execute(cursor.getString(3));
			}
		}
	}
	
	@Override
	public Object getItem(int position)
	{
		if(Allsong)
		{
		if(position == 0) return "all_item";
		
		return super.getItem(position-1);
		}
		return super.getItem(position);
	}
	
	@Override
	public int getCount()
	{
		if(Allsong)	return super.getCount()+1;
		return super.getCount();
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	        if(position == 0 && Allsong) {
	        	if(convertView==null)
	        	{
	        	LayoutInflater inflater = LayoutInflater.from(mContext);
	            convertView = inflater.inflate(R.layout.music_album_row_grid_element, parent,false);
	        	}
	            TextView tv = (TextView) convertView.findViewById(R.id.textViewElementName);
	    		ImageView art = (ImageView) convertView.findViewById(R.id.imageView);
	    		art.setImageResource(R.drawable.album);
	            tv.setText(mContext.getString(R.string.all_songs));
	            
	            return convertView;
	        }
	        else {
	        	if(Allsong)position--;
	            try {
	                return super.getView(position, convertView, parent);
	            }
	            catch (IllegalStateException e) {
	            }
	        }
        return convertView;
    }
	
	

	
	@Override
	public View newView(Context c, Cursor arg1, ViewGroup arg2) {

		LayoutInflater inflater = LayoutInflater.from(c);
		View view = inflater.inflate(R.layout.music_album_row_grid_element,
				arg2, false);

		return view;
	}
	
	
	public class AsyncImageLoad extends AsyncTask<String, Void, Bitmap>{
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
		protected Bitmap doInBackground(String ... arg0) {
			
			Bitmap b = BitmapFactory.decodeFile(arg0[0]);
			return b;
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
