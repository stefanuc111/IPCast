package it.SFApps.wifiqr.tool;

import java.io.IOException;

import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement;
import it.SFApps.wifiqr.tool.ServerPlayer.PlayListElement.FileType;
import android.media.MediaPlayer;

public class CustomMediaPlayer extends MediaPlayer{
	

	private AudioTrackMp3Player ap;
		
	public void setDataSource(PlayListElement e) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException
	{
		if(e.type == FileType.Music && useAudioTrack)
		{
			
			ap = new AudioTrackMp3Player();
			ap.setDataSource(e.f.getAbsolutePath());
			ap.setOnPrepareListener(preparedlistener);
		}else
		{
			
			super.setDataSource(e.f.getAbsolutePath());
		}
	}
	private boolean useAudioTrack = true;
	public void useAudioTrack(boolean b)
	{
		if(b!=useAudioTrack)
		{
				this.reset();
				useAudioTrack = b;
		}
	}
	
	@Override
	public void setOnCompletionListener(OnCompletionListener listener)
	{
		if(ap==null)
		{
		super.setOnCompletionListener(listener);
		}else
		{
			ap.setOnCompletionListener(listener);
		}
	}
	
	@Override
	public void reset()
	{
		if(ap!=null)
		{
			ap.realese();
			ap=null;
		}
		super.reset();
	}
	
	@Override
	public void start()
	{
		if(ap == null)
		{
		super.start();
		}
		else
		{
		ap.play();
		}
	}
	
	@Override
	public void stop()
	{
		if(ap == null)
		{
		super.stop();
		}
		else
		{
		ap.stop();
		}
	}
	
	@Override
	public void pause()
	{
		if(ap == null)
		{
		super.pause();
		}
		else
		{
		ap.pause();
		}
	}
	
	@Override
	public boolean isPlaying()
	{
		if(ap == null)
		{
			return super.isPlaying();
		}else
		{
			return ap.isPlaying();
		}
	}
	
	@Override
	public void seekTo(int ms)
	{
		if(ap == null)
		{
			super.seekTo(ms);
		}else
		{
			ap.seekTo(ms);
			if(l!=null)l.onSeekComplete(this);
		}
	}
	

	@Override
	public void prepareAsync()
	{
		if(ap == null)
		{
			super.prepareAsync();
		}
		else
		{
			ap.prepareAsync();
		}
	}
	
	
	OnPreparedListener preparedlistener;
	@Override
	public void setOnPreparedListener(OnPreparedListener listener)
	{
		preparedlistener = listener;
		if(ap == null)
		{
		super.setOnPreparedListener(listener);
		}else
		{
		ap.setOnPrepareListener(listener);
		}
	}
	@Override
	public void release()
	{
		super.release();
		if(ap!=null)ap.realese();
	}
	
	
	@Override
	public int getCurrentPosition()
	{
		Integer pos;
		if(ap == null)
		{
			pos =  super.getCurrentPosition();
		}else
		{
			pos = ap.getPosition();
		}
		return pos;
	}
	
	@Override
	public int getDuration()
	{
		Integer duration;
		if(ap==null)
		{
			duration = super.getDuration();
		}else
		{
			duration = ap.getDuration();
		}
		return duration;
	}
	
	private OnSeekCompleteListener l;
	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener)
	{
		if(ap == null)
		{
			super.setOnSeekCompleteListener(listener);
		}
		l = listener;
	}
	
	public int getSampleRate()
	{
		if(ap!=null)
		{
			return ap.getSampleRate();
		}
		return 44100;
	}

	

	
}
