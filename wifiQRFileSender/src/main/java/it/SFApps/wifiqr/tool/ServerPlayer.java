package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ServerPlayer implements OnPreparedListener {
	
	 
    public interface PlayerListener
    {
    	void onPlayerStart();
    	void onPlayerStop();
    	void onPlayerPause();
    	void onPlayerPlay();
    
    }
    
    public interface PlayListChangeListener
    {
    	void onActualPlayingChange();
    	void onPlaylistChange();
    }
    

    
    static public class PlayListElement
    {
    	public static enum FileType {Music,Video,Image};
    	public File f;
    	public FileType type;
    	public Long id;
    	public String name;
    	public PlayListElement(File f, FileType ty,Long id_media,String name)
    	{
    		this.f=f;
    		type = ty;
    		id = id_media;
    		this.name = name;
    	}
    	@Override
    	public boolean equals(Object o)
    	{
    		if(o instanceof PlayListElement && ((PlayListElement)o).f.equals(this.f))return true;
    		return false;
    	}
    }
	
    private PlayerListener mPlayerListener=null;
	public List<PlayListElement>playlist = new ArrayList<PlayListElement>();
	CustomMediaPlayer mMediaPlayer = null;
	public boolean mMediaPrepared = false;
	private PlayListElement actual_playing =null;
    private List<PlayListChangeListener> ActualChangeListeners = new ArrayList<PlayListChangeListener>();
	private Context mContext;
    private ServerService srv;
    

	ServerPlayer(ServerService srv)
	{
		mContext = srv;
		this.srv = srv;
		
		mMediaPlayer = new CustomMediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener(){

			@Override
			public void onSeekComplete(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				ServerPlayer.this.srv.sendToAll("{\"type\":\"Seeked\"}");
			}});

		PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false);

	}
	
	
    public void setPlayerListener(PlayerListener obj)
    {
    	mPlayerListener = obj;
    	if(mMediaPrepared)
    	{
    		mPlayerListener.onPlayerStart();
        	if(mMediaPlayer.isPlaying())
        		mPlayerListener.onPlayerPlay();
        	else
        		mPlayerListener.onPlayerPause();

    	}else
    	{
    		mPlayerListener.onPlayerStop();
    		isNewSession = true;
    	}
    	
    }
    	
    public void registerActualPlayingChangeListener(PlayListChangeListener l)
    {
    	ActualChangeListeners.add(l);	
    }
    
    public void unregisterActualPlayingChangeListener(PlayListChangeListener l)
    {
    	ActualChangeListeners.remove(l);	
    }
    
	
	public void EnableSoundBeam(boolean b)
	{
		boolean wasPlaying = isMediaPlaying();
		if(b)
		{
			mMediaPrepared = false;
			mMediaPlayer.useAudioTrack(true);
			mMediaPlayer.setVolume(1, 1);
		}else
		{
			mMediaPrepared = false;
			mMediaPlayer.useAudioTrack(false);
			mMediaPlayer.setVolume(0, 0);
		}
		if(wasPlaying)PlayMedia();
		else
		{
			if(actual_playing!=null)
			{
				PrepareMedia(false);
			}
		}

	}
	
		
	public void PlayMedia()
	{	
		if(mMediaPrepared)
		{
			mMediaPlayer.start();
			if(mPlayerListener!=null)mPlayerListener.onPlayerPlay();
			srv.sendToAll(srv.getJSONPlayStatus());
						
		}else
		{
			if(actual_playing==null && !playlist.isEmpty())
			{
				actual_playing=playlist.get(0);
			}

			if(actual_playing!=null)
			{
			PlayListElement e = actual_playing;
			if(e.type == FileType.Music || e.type==FileType.Video)
			PrepareMedia(true);
			}
			
		}
		
	}
	public boolean addToPlaylist(PlayListElement e,boolean at_end)
	{	
		if(e.type == FileType.Music && !e.f.getName().endsWith(".mp3"))
		{
			Toast.makeText(srv, R.string.only_mp3, Toast.LENGTH_LONG).show();
			return false;
		}
		
		
		if(!playlist.contains(e))
		{
			playlist.add(e);
			if(playlist.size()==1)
			{
				PlayMedia(0);
			}
			onPlaylistChange();
			return true;
		}else
		{
			playlist.remove(e);
			playlist.add(e);
			onPlaylistChange();
		}
		return false;
		
	}
	
	public void RemovePlayListElement(int position)
	{
		if(indexOfActualPlaying()==position)
		{
			
			if(!PlayNext())
			{
				if(!PlayPrevious())
				{
					ClosePlayer();
					return;
				}				
			}
		}
		
		onPlaylistChange();
		playlist.remove(position);
		
	}
	
	public void PlayMedia(int position)
	{	
		if(actual_playing!=null && playlist.indexOf(actual_playing)==position)
		{
			PlayMedia();
			return;
		}
			
			if(!playlist.isEmpty())
			{
				actual_playing=playlist.get(position);
				mMediaPrepared = false;
				PlayMedia();
			}
	
	}
	
		
	public void ClosePlayer()
	{
		if(mMediaPrepared)
		{
			mMediaPlayer.stop();
			mMediaPrepared = false;
		}
		if(mPlayerListener!=null)mPlayerListener.onPlayerStop();
		actual_playing=null;
		playlist.clear();
		srv.sendToAll(srv.getJSONCurrentData());
		isNewSession = true;
	}
	
	
	public boolean PlayNext()
	{
		PlayListElement next = GetNext();
		if(next!=null)
		{
			PlayMedia(playlist.indexOf(next));
			return true;
		}
		return false;
		
	}
	public PlayListElement GetNext()
	{
		if(actual_playing!=null && (playlist.size()-1)>playlist.indexOf(actual_playing))
		{
			int index = playlist.indexOf(actual_playing);
			return playlist.get(index+1);
		}
		return null;
	}
	

	
	
	public boolean PlayPrevious()
	{
		if(actual_playing!=null && playlist.indexOf(actual_playing)-1>=0)
		{
			int index = playlist.indexOf(actual_playing);
			PlayMedia(index-1);
			return true;
		}
		return false;
	}
	public boolean isMediaPlaying()
	{
		if(mMediaPrepared && mMediaPlayer.isPlaying())
		{
			return true;
		}
		return false;
	}
	
	public void PauseMedia()
	{
		if(mMediaPrepared)
		{
			mMediaPlayer.pause();
			if(mPlayerListener!=null)mPlayerListener.onPlayerPause();
			srv.sendToAll(srv.getJSONPlayStatus());

		}
	}
	public boolean isNewSession = false;
	private void PrepareMedia(boolean autoplay)
	{
		mMediaPlayer.setOnCompletionListener(null);
		needToPlay = autoplay;
		if(isMediaPlaying())
		{
			mMediaPlayer.pause();
		}
		
		mMediaPrepared = false;
		mMediaPlayer.reset();
		
		try {
			mMediaPlayer.setDataSource(actual_playing);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


    public MediaPlayer getMediaPlayer()
    {
    	return mMediaPlayer;
    }
    
 

	public PlayListElement getActualPlaying()
	{
		if(actual_playing!=null)
		{
		return actual_playing;
		}
		return null;
	}
	
	public int indexOfActualPlaying()
	{
		return playlist.indexOf(actual_playing);
	}
	
	public boolean needToPlay = false;
	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		mMediaPrepared = true;
		if(isNewSession)mPlayerListener.onPlayerStart();
		isNewSession = false;
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer arg0) {
				if(mPlayerListener!=null)mPlayerListener.onPlayerPause();
				srv.sendToAll(srv.getJSONPlayStatus());
				PlayNext();
			}});
		onActualPlayingChange();
		if(needToPlay)PlayMedia();
	}
	
	private void onPlaylistChange()
	{
		for(PlayListChangeListener l:ActualChangeListeners)l.onPlaylistChange();
		
		if(GetNext()!=null && GetNext().type == PlayListElement.FileType.Music)
		{
			srv.sendToAll(srv.getJSONNextData());
		}
		 
	}
	
	
	private void onActualPlayingChange()
	{
		for(PlayListChangeListener l:ActualChangeListeners)l.onActualPlayingChange();

		if(actual_playing.type == PlayListElement.FileType.Music)
		{
			new spliceMp3Thread().start();
		}
		
		srv.sendToAll(srv.getJSONCurrentData());
		
		
		
		if(GetNext()!=null && GetNext().type == PlayListElement.FileType.Music)
		{
			srv.sendToAll(srv.getJSONNextData());
		}
	}
	
	public void release()
	{
		mMediaPlayer.release();
	}

	public void seekTo(int msec) {
		// TODO Auto-generated method stub
		mMediaPlayer.seekTo(msec);
	}

	private class spliceMp3Thread extends Thread implements Runnable
	{
		String path = actual_playing.f.getAbsolutePath();
		String cache_dir = mContext.getExternalCacheDir().getAbsolutePath();
		int file_hash = actual_playing.f.hashCode();
		int duration = mMediaPlayer.getDuration();
		int sample_rate = mMediaPlayer.getSampleRate();
		String ffmpeg_path = mContext.getFilesDir().getAbsolutePath()+"/ffmpeg/ffmpeg";
		@Override
		public void run()
		{
			double ms_frame = 1152/(double)sample_rate;
			int n=0;
			
			while(duration/1000f>n*(ms_frame*1000))
			{
				if(!new File(cache_dir+"/"+file_hash+"_stream_"+n).exists())
				{
					ProcessBuilder builder=null;
					if(n > 0) {		
						String[] commands = {ffmpeg_path,"-i",path,"-map","0","-vn","-acodec","copy","-ss",String.format(Locale.US,"%.5f", (ms_frame*1000*n-ms_frame)),"-t",String.format(Locale.US,"%.5f", ms_frame*1002),"-f","mp3",cache_dir+"/"+file_hash+"_stream_"+n,"-y"};
					  builder = new ProcessBuilder(commands);
					}else
					{
						String[] commands = {ffmpeg_path,"-i",path,"-map","0","-vn","-acodec","copy","-t",String.format(Locale.US,"%.5f", ms_frame*1001),"-f","mp3",cache_dir+"/"+file_hash+"_stream_"+n,"-y"};
						builder = new ProcessBuilder(commands);
					}	
					  builder.redirectErrorStream(true);
						
					  try {
						Process nativeApp = builder.start();
						nativeApp.waitFor();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  File f = new File(cache_dir);
					  List<File> init_list = new ArrayList<File>(Arrays.asList(f.listFiles()));
					  long size = getSize(init_list);
					  int i = 0;
					  while(size/1000 >= 50000 && i < init_list.size())
					  {
						  
						  if(!init_list.get(i).getName().contains(file_hash+""))
						  {
							  init_list.get(i).delete();
							  init_list.remove(i);
							  size = getSize(init_list);
						  }else i++;
						  
					  }
				}
				n++;

			}
		}
		private long getSize(List<File> list)
		{
			long size = 0;
			  for (File a:list) {
				    size = size+a.length();
				}
			  return size;
		}
	}


}
