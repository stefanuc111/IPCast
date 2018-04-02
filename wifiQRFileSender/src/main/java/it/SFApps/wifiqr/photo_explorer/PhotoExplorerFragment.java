package it.SFApps.wifiqr.photo_explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.photo_explorer.PhotoExplorerAdapter.onSubMenuItemClickListener;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.OrderSelectorDialog;
import it.SFApps.wifiqr.tool.OrderSelectorDialog.OrderSelectorListener;
import it.SFApps.wifiqr.tool.ServerFragment;
import it.SFApps.wifiqr.tool.ServerPlayer;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.Utils;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;


public class PhotoExplorerFragment extends ServerFragment implements OrderSelectorListener,onSubMenuItemClickListener {
	private GridViewCompat photoGridView;
	private int mOrder = OrderSelectorDialog.OrderNamePhoto.RECENT;
	private PhotoExplorerElement[] mElements;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if(savedInstanceState!=null && savedInstanceState.containsKey("mOrder"))
		{
			mOrder = savedInstanceState.getInt("mOrder");
		}



	}
	
	@Override
	public void onStart()
	{
		super.onStart();
     
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
	super.onCreateView(inflater, container, savedInstanceState);
	View r = inflater.inflate(R.layout.photo_explorer_fragment, container,false);
	photoGridView = (GridViewCompat) r.findViewById(R.id.gridView1);
	//photoGridView.setChoiceMode(GridViewCompat.CHOICE_MODE_MULTIPLE);
	photoGridView.setClipToPadding(false);
	
	return r;
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.photo_explorer, menu);

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("mOrder", mOrder);
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.order_by:
			OrderSelectorDialog dialog = new OrderSelectorDialog();
			dialog.setOrderNames(R.array.order_names_photo);
			dialog.show(getActivity().getSupportFragmentManager(), null);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onOrderClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		mOrder = which;
		orderPhotoExplorerElement();
    	photoGridView.setAdapter(new PhotoExplorerAdapter(getActivity(), mElements,srv,this));


}

private void orderPhotoExplorerElement()
{
	Comparator<PhotoExplorerElement> c = null;
	switch (mOrder)
	{
	case OrderSelectorDialog.OrderNamePhoto.RECENT:
		c = new Comparator<PhotoExplorerElement>()
			    {
				@Override
				public int compare(PhotoExplorerElement arg0,
						PhotoExplorerElement arg1) {
					// TODO Auto-generated method stub
					if(arg0.date<arg1.date)return 1;
					return -1;
				}
	    };
		
	break;
	case OrderSelectorDialog.OrderNamePhoto.SIZE_ASCEND:
		c = new Comparator<PhotoExplorerElement>(){

			@Override
			public int compare(PhotoExplorerElement arg0,
					PhotoExplorerElement arg1) {
				if(arg0.file.length()>arg1.file.length())return 1;
				return -1;
			}};
	break;
	case OrderSelectorDialog.OrderNamePhoto.SIZE_DECRESCENT:
		c = new Comparator<PhotoExplorerElement>(){

			@Override
			public int compare(PhotoExplorerElement arg0,
					PhotoExplorerElement arg1) {
				if(arg0.file.length()<arg1.file.length())return 1;
				return -1;
			}};
	break;
	case OrderSelectorDialog.OrderNamePhoto.TYPE:
		c = new Comparator<PhotoExplorerElement>(){

			@Override
			public int compare(PhotoExplorerElement arg0,
					PhotoExplorerElement arg1) {
				if(arg1.isPhoto)return 1;
				return -1;
			}};
	break;
	}
	
	if(mElements!=null)Arrays.sort(mElements,c);
	
}
	
private PhotoExplorerElement[] getPhotoAndVideo(ContentResolver cr)
{
	
	 String mProjection[] = {MediaStore.Images.Media.TITLE,MediaStore.Images.Media.DATA,MediaStore.Images.Media.DATE_ADDED,MediaStore.Images.Media._ID};
	 Cursor mCursorPhoto = cr.query(
			    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // The content URI of the words table
			    mProjection,                        // The columns to return for each row
			    null,                    // Selection criteria
			    null,                     // Selection criteria
			    MediaStore.Images.Media.DEFAULT_SORT_ORDER);// The sort order for the returned rows
		
	 String mProjection1[] = {MediaStore.Video.Media.TITLE,MediaStore.Video.Media.DATA,MediaStore.Video.Media.DATE_ADDED,MediaStore.Video.Media._ID};

	 Cursor mCursorVideo = cr.query(
			    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,   // The content URI of the words table
			    mProjection1,                        // The columns to return for each row
			    null,                    // Selection criteria
			    null,                     // Selection criteria
			    MediaStore.Images.Media.DEFAULT_SORT_ORDER);// The sort order for the returned rows
		int photos = 0;
		int videos = 0;
	 	if(mCursorPhoto!=null)photos = mCursorPhoto.getCount();
	 	if(mCursorVideo!=null)videos = mCursorVideo.getCount();
	 	
	 PhotoExplorerElement  elements[] = new PhotoExplorerElement[photos+videos];
	 		int i=0;
	 		
	 		if(mCursorPhoto!=null)
	 		{
		 		while(mCursorPhoto.moveToNext())
		 		{
		 			elements[i] = new PhotoExplorerElement(mCursorPhoto.getString(0), new File(mCursorPhoto.getString(1)), mCursorPhoto.getInt(2), true, mCursorPhoto.getLong(3));
		 			i++;
		 		}
		 		mCursorPhoto.close();
	 		}

	 		
	 		if(mCursorVideo!=null)
	 		{
		 		while(mCursorVideo.moveToNext())
		 		{
		 			elements[i] = new PhotoExplorerElement(mCursorVideo.getString(0), new File(mCursorVideo.getString(1)), mCursorVideo.getInt(2), false, mCursorVideo.getLong(3));
		 			i++;
		 		}
		 		mCursorVideo.close();
	 		}


	 		return elements;
}

private class AsyncLoadPhoto extends AsyncTask<Void,Void,PhotoExplorerElement[]>
{
	private ContentResolver cr;
	@Override
	protected void onPreExecute()
	{
		if(isAdded())
		{
			cr = getActivity().getContentResolver();
		}else cancel(true);
	}
	@Override
	protected PhotoExplorerElement[] doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return getPhotoAndVideo(cr);
	}
	
	@Override
	protected void onPostExecute(PhotoExplorerElement[] photo)
	{
		if(isAdded() && srv!=null)
		{
 		mElements = photo;
    	photoGridView.setAdapter(new PhotoExplorerAdapter(getActivity(), mElements,srv,PhotoExplorerFragment.this));
		}
	}

	
}
	
	

    public void onServerConnected(ServerService s) {
 
    	new AsyncLoadPhoto().execute();
    	
        
    	photoGridView.setOnItemClickListener(new OnItemClickListener(){
    		
    		@Override
    		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
    				long arg3) {
    			PhotoExplorerElement e = (PhotoExplorerElement) arg0.getAdapter().getItem(position);
    			if(srv.f.contains(e.file))
    			{
    				srv.f.remove(e.file);
    			}else
    			{
    				srv.f.add(e.file);
    				if(Build.VERSION.SDK_INT>=14)
    				{
    				View cp = arg0.getAdapter().getView(position, null, (ViewGroup) arg1.getRootView());
    				Utils.sendAnimation(cp, arg1);
    				
    				}
    			}
    		}
    		
    	});
    	
    	photoGridView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				PhotoExplorerAdapter a = ((PhotoExplorerAdapter)arg0.getAdapter());
				PhotoExplorerElement elem = a.getItem(position);
				if(elem.isPhoto)
				{
					if(elem.file.equals(srv.getImage()))
					{
						srv.setImage(null);
					}else
					{
					srv.setImage(elem.file);
    					if(Build.VERSION.SDK_INT>=14)
    					{
	    					View cp = arg0.getAdapter().getView(position, null, (ViewGroup) arg1.getRootView());
	    					Utils.sendAnimation(cp, arg1);
    					}
    				srv.mPlayer.ClosePlayer();
					}
				}else
				{
				a.opened_submenu_position=position;
				}
				a.notifyDataSetChanged();

				return true;
			}});
        
    }

    @Override
    public void onServerDisconnected() {
        photoGridView.setOnItemClickListener(null);
    }
	@Override
	public void onPlayClick(BaseAdapter adapter, int position,View v) {
		// TODO Auto-generated method stub
		ServerPlayer mPlayer = srv.mPlayer;
		mPlayer.addToPlaylist(new PlayListElement(mElements[position].file,PlayListElement.FileType.Video,mElements[position].id,mElements[position].name),true);
		mPlayer.PlayMedia(mPlayer.playlist.size()-1);
		if(Build.VERSION.SDK_INT>=14)
		{
		View cp = adapter.getView(position, null, (ViewGroup) v.getRootView());
		Utils.sendAnimation(cp, v);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onAddToQueueClick(BaseAdapter adapter,int position, View v) {
		srv.mPlayer.addToPlaylist(new PlayListElement(mElements[position].file,PlayListElement.FileType.Video,mElements[position].id,mElements[position].name),false);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onFileListChange() {
		// TODO Auto-generated method stub
		((BaseAdapter)photoGridView.getAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onActualPlayingChange()
	{
		((BaseAdapter)photoGridView.getAdapter()).notifyDataSetChanged();

	}


	
		
}
