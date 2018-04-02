package it.SFApps.wifiqr.playlist;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.ServerPlayer;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.Utils;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PlayListAdapter extends BaseAdapter{

	private Context mContext;
	private ServerPlayer mPlayer;
    final int INVALID_ID = -1;
    
	PlayListAdapter(Context c,ServerService srv)
	{
		mPlayer = srv.mPlayer;
		mContext = c;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPlayer.playlist.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mPlayer.playlist.get(arg0);
	}

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mPlayer.playlist.size()) {
            return INVALID_ID;
        }
        return mPlayer.playlist.get(position).hashCode();
    }
	
    @Override
    public boolean hasStableIds() {
        return true;
    }

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		if(view==null)
		{
			LayoutInflater in = LayoutInflater.from(parent.getContext());
			view = in.inflate(R.layout.play_list_activity_row, parent,false);
		}
		TextView text = (TextView) view.findViewById(R.id.textView1);
		ImageView image = (ImageView) view.findViewById(R.id.imageView1);
		ImageView remove = (ImageView) view.findViewById(R.id.imageViewRemove);
		
		Long prev_id = view.getTag()!=null? (Long)view.getTag():null;
		
		if(prev_id==null||getItemId(position)!=prev_id)
		{
			image.setImageResource(R.drawable.album);
			new AsyncImageLoad(image).execute(position);
		}
		
		view.setTag(getItemId(position));
		remove.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mPlayer.RemovePlayListElement(position);
			}});
		
		
		text.setText(mPlayer.playlist.get(position).name);
		if(mPlayer.indexOfActualPlaying()==position)
		{
			((ListView)parent).setItemChecked(position,true);
		}else
		{
			((ListView)parent).setItemChecked(position,false);

		}
		
		
		return view;
	}
	
	
	
	public class AsyncImageLoad extends AsyncTask<Integer, Void, Bitmap>{
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
		protected Bitmap doInBackground(Integer ... arg0) {
			
			
			PlayListElement e = mPlayer.playlist.get(arg0[0]);
			
			if(e.type == FileType.Video)
			{
				return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), e.id, MediaStore.Images.Thumbnails.MINI_KIND, null);

			}
	
			if(e.type == FileType.Music)
			{
				return Utils.getAlbumart(e.id,mContext);
			}
	return null;
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
