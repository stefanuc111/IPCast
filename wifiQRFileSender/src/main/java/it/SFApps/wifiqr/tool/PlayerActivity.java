package it.SFApps.wifiqr.tool;

import java.util.zip.Inflater;
import com.google.android.gms.analytics.GoogleAnalytics;

import it.SFApps.wifiqr.MainActivity;
import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.ServerActivity;
import it.SFApps.wifiqr.SettingsActivity;
import it.SFApps.wifiqr.SettingsPreferencesActivity;
import it.SFApps.wifiqr.playlist.PlayListActivity;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayerListener;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import it.SFApps.wifiqr.tool.ServerService.onIPChangeListener;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends ActionBarActivity implements PlayerListener, OnFileListChangeListener, onIPChangeListener{

	public ServerService srv=null;
	public boolean mBound = false;
	private SeekBar mSeekBar;
	Handler mHandler;
	private RelativeLayout mPlayerLayout, mDrawer;
	private ImageButton mButtonPlay,mButtonPlayNext,mButtonPlayPrevious, mButtonClosePlayer,mPlayList;
	private ListView menu_list, sub_list;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private FrameLayout content;
	private String back_title=null;
	public boolean mDrawerOpen=false;
	private ServerPlayer mPlayer;
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (mBound) {
				if(mPlayer.mMediaPrepared)
				{
				mSeekBar.setMax(mPlayer.getMediaPlayer().getDuration());
				mSeekBar.setProgress(mPlayer.getMediaPlayer().getCurrentPosition());
				}
			}
			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);

		mPlayerLayout = (RelativeLayout) findViewById(R.id.player_layout);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
		mButtonPlayNext = (ImageButton) findViewById(R.id.imageButtonNext);
		mButtonPlayPrevious = (ImageButton) findViewById(R.id.imageButtonPrevious);
		mButtonClosePlayer = (ImageButton) findViewById(R.id.ButtonClosePlayer);
		content = (FrameLayout) findViewById(R.id.content_frame);
		mPlayList = (ImageButton) findViewById(R.id.mute_unmute_button);
		mHandler = new Handler(); 
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawer = (RelativeLayout) mDrawerLayout.findViewById(R.id.drawer);
		menu_list = (ListView) mDrawerLayout.findViewById(R.id.left_drawer);
		sub_list = (ListView) mDrawerLayout.findViewById(R.id.left_drawer_sub);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				mHandler.removeCallbacks(mUpdateTimeTask);
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				if (mBound) {
					mPlayer.getMediaPlayer().seekTo(arg0.getProgress());

				}
				mHandler.post(mUpdateTimeTask);

			}
		});

		mPlayList.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
					startActivity(new Intent(PlayerActivity.this,PlayListActivity.class));
					
				
			}});
		
		mButtonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if(mBound)
					if (mPlayer.isMediaPlaying()) {
						mPlayer.PauseMedia();
					} else {
						mPlayer.PlayMedia();
					}

			}
		});

		menu_list.setAdapter(new ArrayAdapter<String>(this, R.layout.row_menu,
				R.id.textViewMenuElement, this.getResources().getStringArray(
						R.array.drawer_menu)));


		

		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				if(back_title!=null)
				getSupportActionBar().setTitle(back_title);
				mDrawerOpen=false;
				supportInvalidateOptionsMenu();

				
			}
			
			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(R.string.nome_app);
				mDrawerOpen=true;
				supportInvalidateOptionsMenu();

			}
		};

		
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		

	}
	
	public void setTitle(String title)
	{
		back_title = title;
		if(!mDrawerOpen)this.getSupportActionBar().setTitle(title);
	}
	
	public void HidePlayListButton()
	{
		mPlayList.setVisibility(View.GONE);
	}
	
	public void DisableDrawerIndicator()
	{
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		menu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//Richiama mainactivity!!
				Intent i = new Intent(PlayerActivity.this,MainActivity.class);
				i.putExtra("position", position);
				startActivity(i);
				mDrawerLayout.closeDrawers();
			}

		});
		
	}
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString("title", back_title);
		super.onSaveInstanceState(outState);
		
	}

	
	public void closeDrawer()
	{
		mDrawerLayout.closeDrawers();
	}
	@Override
	public void setContentView(int layoutResource)
	{
		LayoutInflater  inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(layoutResource, content);
		//content.addView(inflated);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
		
		mDrawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);

		if(savedInstanceState!=null)
		{
			if(savedInstanceState.containsKey("title"))
			{
				setTitle(savedInstanceState.getString("title"));
			}
			
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	
	
	@Override
	public void onStart() {
		super.onStart();


        GoogleAnalytics.getInstance(this).reportActivityStart(this);
		bindService(new Intent(this, ServerService.class), mConnection,	Context.BIND_AUTO_CREATE);

	}
	
	
	@Override
	public void onStop() {
		super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
		if (mBound) {
			srv.f.unregisterOnFileListChangeListener(this);
			unbindService(mConnection);
			mBound = false;
		}
		// if(mHandler!=null)mHandler.removeCallbacks(mUpdateTimeTask);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.player_activity, menu);
		if(mBound && !srv.f.isEmpty())
		{
			menu.findItem(R.id.clear_selection).setVisible(true);
		}
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
		    if (mDrawerToggle.onOptionsItemSelected(item)) {
		        return true;
		      }	
	        return false;
		case R.id.clear_selection:
			if (mBound)
				srv.f.clear();
			return true;
		case R.id.confirm_selection:
//			launchServerActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onServerConnected(ServerService srv){}//Can be overridden
	public void onServerDisconnected(){} //Can be overridden

	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ServerBinder binder = (ServerBinder) service;
			srv = binder.getService();
			if(srv.interrupted)
			{
				finish();
			}
			
			sub_list.setAdapter(new SubList());
			sub_list.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
						switch(arg2)
						{
						case 0:
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								startActivity(new Intent(getBaseContext(),
										SettingsActivity.class));
							} else {
								startActivity(new Intent(getBaseContext(),
										SettingsPreferencesActivity.class));

							}
						break;
						case 1:
							startActivity(new Intent(getBaseContext(),ServerActivity.class));
						break;
						case 2:
							HelpDialog h = new HelpDialog();
							h.show(getSupportFragmentManager(), "help");
						break;
						}
				}});
			
			srv.f.registerOnFileListChangeListener(PlayerActivity.this);
			mBound = true;
			mPlayer = srv.mPlayer;
			mPlayer.setPlayerListener((PlayerListener) PlayerActivity.this);
			
			
			srv.setOnIPChangeListener(PlayerActivity.this);
			supportInvalidateOptionsMenu();
			onServerConnected(srv);	
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			mHandler.removeCallbacks(mUpdateTimeTask);
			onServerDisconnected();

		}
	};
	
	class SubList extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 3;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			if(arg1==null)
			{
			LayoutInflater i = LayoutInflater.from(arg2.getContext());
			arg1 = i.inflate(R.layout.row_sub_menu, arg2,false);
			}
			TextView text = (TextView) arg1.findViewById(R.id.textViewMenuElement);
			ImageView img =(ImageView) arg1.findViewById(R.id.imageView1);
			
			switch(arg0)
			{
			case 0:
				text.setText(R.string.settings);
				img.setImageResource(R.drawable.settings_icon);
			break;
			case 1:
				if(srv!=null)
				text.setText("http://"+srv.getIPAddress(true)+":"+srv.getPort());
				img.setImageResource(R.drawable.link_icon);
			break;
			case 2:
				text.setText(R.string.guide_title);
				img.setImageResource(R.drawable.help_icon);
			break;
			}
			
			return arg1;
		}
		
	}
	

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onPlayerStart() {
		if(Build.VERSION.SDK_INT>=14)
		{
		mPlayerLayout.setTranslationY(mPlayerLayout.getHeight());
		mPlayerLayout.setVisibility(View.VISIBLE);
		mPlayerLayout.animate().translationY(0).start();
		}else
		{
			mPlayerLayout.setVisibility(View.VISIBLE);

		}

		mHandler.post(mUpdateTimeTask);

		mButtonPlayNext.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mPlayer.PlayNext();
			}});
		
		mButtonPlayPrevious.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mPlayer.PlayPrevious();
			}});
		
		mButtonClosePlayer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mPlayer.ClosePlayer();
			}});
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onPlayerStop() {
		mHandler.removeCallbacks(mUpdateTimeTask);

		mPlayerLayout.setVisibility(View.GONE);

	}

	@Override
	public void onPlayerPause() {
		mButtonPlay.setImageResource(R.drawable.ic_play_black);

	}

	@Override
	public void onPlayerPlay() {
		mButtonPlay.setImageResource(R.drawable.ic_pause_black);

	}
	@Override
	public void onFileListChange() {
		// TODO Auto-generated method stub
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onIPChange() {
		// TODO Auto-generated method stub
		((BaseAdapter)sub_list.getAdapter()).notifyDataSetChanged();
	}
	
	

}
