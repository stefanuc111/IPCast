package it.SFApps.wifiqr.photo_explorer;

import java.io.File;
import java.util.List;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerService;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoExplorerAdapter extends BaseAdapter{

	private Context mContext;
	private List<File> selectedFiles;
	private PhotoExplorerElement mElements[];
	private ServerService srv;
	private onSubMenuItemClickListener sub_menu=null;
	public Integer opened_submenu_position = null;
	
	PhotoExplorerAdapter(Context context,PhotoExplorerElement elements[], ServerService srv, Object obj)
	{
		mContext = context;
		selectedFiles = srv.f;
		mElements = elements;
		this.srv=srv;
		sub_menu = (onSubMenuItemClickListener)obj;

	}
	
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mElements.length;
		 
	}

	@Override
	public PhotoExplorerElement getItem(int position) {

		return mElements[position];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setOnSubMenuItemClick(onSubMenuItemClickListener listener)
	{
		sub_menu = listener;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public View getView(final int position, View row, final ViewGroup parent) {
		// TODO Auto-generated method stub
		if(row==null)
		{
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        row = inflater.inflate(R.layout.photo_explorer_row_grid_element, parent, false);
        

		}
			
		TextView text = (TextView) row.findViewById(R.id.textViewElementName);
		ImageView img = (ImageView) row.findViewById(R.id.imageView);
		ImageView imgPlay = (ImageView) row.findViewById(R.id.imageViewPlay);
		final RelativeLayout menu = (RelativeLayout) row.findViewById(R.id.selected_menu);
        RelativeLayout play_now = (RelativeLayout) row.findViewById(R.id.play_now);
        RelativeLayout add_to_queue = (RelativeLayout) row.findViewById(R.id.add_to_queue);

        final View rowview = row;
        if(sub_menu!=null)
        {
		
        play_now.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				sub_menu.onPlayClick(PhotoExplorerAdapter.this,position,rowview);
				opened_submenu_position=null;
				notifyDataSetChanged();
			}});
        
        
        add_to_queue.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				sub_menu.onAddToQueueClick(PhotoExplorerAdapter.this,position,rowview);
				opened_submenu_position=null;
				notifyDataSetChanged();
			}});
        }
		
		if(opened_submenu_position!=null && opened_submenu_position==position)
		{
			menu.setVisibility(View.VISIBLE);
			imgPlay.setVisibility(View.GONE);

		}else
		{
			menu.setVisibility(View.GONE);
			
			if(mElements[position].isPhoto)
			{
				imgPlay.setVisibility(View.GONE);
			}else
			{
				imgPlay.setVisibility(View.VISIBLE);
			}
		}
		
		
		if(row.getTag()==null||(Integer)row.getTag()!=position)
		{
			new AsyncImageLoad(img).execute(mElements[position]);
		}
		
		
		if(parent instanceof GridViewCompat)
		if(selectedFiles.contains(mElements[position].file))
		{
			((GridViewCompat)parent).setItemChecked(position,true);
		}else
		{
			((GridViewCompat)parent).setItemChecked(position,false);

		}
		
		text.setText(mElements[position].name);

		PlayListElement media = srv.mPlayer.getActualPlaying();
		File image = srv.getImage();
		
		if((media!=null && media.f.equals(mElements[position].file)) || (image!=null && image.equals(mElements[position].file)))
		{
			if(parent instanceof GridViewCompat)((GridViewCompat)parent).setItemChecked(position,true);
			text.setText(R.string.now_playing);
			if(!mElements[position].isPhoto)imgPlay.setImageResource(R.drawable.play_button_pressed);
		}else
		{
			if(!mElements[position].isPhoto)imgPlay.setImageResource(R.drawable.play_button);

		}

		
		
		
		
		
		
		row.setTag(position);
		return row;
	}
	
	
	
	
	public class AsyncImageLoad extends AsyncTask<PhotoExplorerElement, Void, Bitmap>{
		private ImageView img;
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		AsyncImageLoad(ImageView img)
		{
			this.img = img;
			if(img.getTag()!=null)
			{
				
				((AsyncImageLoad)img.getTag()).cancel(true);
			}
			
			img.setTag(this);

			
			img.setImageDrawable(null);
		}
		
		@Override
		protected Bitmap doInBackground(PhotoExplorerElement ... arg0) {
			
			if(arg0[0].isPhoto)
			return MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), arg0[0].id, MediaStore.Images.Thumbnails.MINI_KIND, null);

			return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), arg0[0].id, MediaStore.Images.Thumbnails.MINI_KIND, null);

		}
		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected void onPostExecute(Bitmap result) {
			


			img.setImageBitmap(result);
			if(Build.VERSION.SDK_INT>=11)
			{
				ObjectAnimator fade = ObjectAnimator.ofFloat(img, "alpha", 0.0f,1.0f);
				fade.setDuration(200);
				fade.start();
			}

			
			
	     }


	}
	
	public interface onSubMenuItemClickListener
	{
		public void onPlayClick(BaseAdapter adapter, int position, View v);
		public void onAddToQueueClick(BaseAdapter adapter, int position,View v);
	}

}
