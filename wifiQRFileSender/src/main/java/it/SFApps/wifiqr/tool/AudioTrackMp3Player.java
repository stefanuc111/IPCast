package it.SFApps.wifiqr.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class AudioTrackMp3Player {
	File file;
	AudioTrack at;
	ArrayList<byte[]> decoded;
	private OnCompletionListener completation_listener=null;
	private MyHandler hl = new MyHandler(this);
	private Integer SampleRate = null, Duration, ChannelCount;
	private Integer lastSeekPosition = 0;
	private Thread decode;
	private OnPreparedListener preparelistener=null;
	
	
	private static class MyHandler extends Handler {
		private final WeakReference<AudioTrackMp3Player> audioTrack;

		public MyHandler(AudioTrackMp3Player a) {
			audioTrack = new WeakReference<AudioTrackMp3Player>(a);
		}

		@Override
		public void handleMessage(Message msg) {
			AudioTrackMp3Player a = audioTrack.get();
			if (msg.what == 1) {
				a.stop();
				if (a.completation_listener != null)
					a.completation_listener.onCompletion(null);
			}
			if(msg.what == 2){
				if(a.preparelistener != null)
					a.preparelistener.onPrepared(null);
			}

			super.handleMessage(msg);
		}
	};

	public void setDataSource(String path) {
		file = new File(path);
	}



	public void play() {
		if(at==null)
		{
		int FormatChannel = AudioFormat.CHANNEL_OUT_STEREO;
		if (ChannelCount == 1)
			FormatChannel = AudioFormat.CHANNEL_OUT_MONO;
		int bufferSize = AudioTrack.getMinBufferSize(SampleRate, FormatChannel,
				AudioFormat.ENCODING_PCM_16BIT);
		
		int BitSample = AudioFormat.ENCODING_PCM_16BIT;
		at = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate,
				FormatChannel, BitSample, bufferSize*4, AudioTrack.MODE_STREAM);
		}
		
		
		at.play();
		decode = new Thread(new DecodeRunnable(lastSeekPosition));
		decode.start();
		
	}

	public boolean isPlaying() {
		if (at!=null && at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
			return true;
		return false;
	}

	public void pause() {
		if (isPlaying()) {
			lastSeekPosition = getPosition();
			decode.interrupt();
			at.pause();
			at.flush();
		}
	}

	public void stop() {
		// TODO Auto-generated method stub
		if (isPlaying()) {
			decode.interrupt();
			at.pause();
			at.flush();
			lastSeekPosition = 0;
			at.stop();
			at.release();
			at = null;
		}
	}

	public void realese() {
		stop();
		preparing.interrupt();
	}

	public void seekTo(int msc) {

		if (isPlaying()) {
			stop();
			lastSeekPosition = msc;
			play();
			return;
		}
		lastSeekPosition = msc;

	}
	
	public void setOnPrepareListener(OnPreparedListener preparelistener)
	{
		this.preparelistener = preparelistener;
	}

	Thread preparing;
	public void prepareAsync() {
		preparing = new Thread(new Runnable() {

			@Override
			public void run() {
				float totalMs = 0;
				InputStream inputStream = null;
				try {
					inputStream = new BufferedInputStream(new FileInputStream(
							file), 8 * 1024);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Bitstream bitstream = new Bitstream(inputStream);
					Header frameHeader = bitstream.readFrame();

					while (frameHeader != null) {
						totalMs += frameHeader.ms_per_frame();
						bitstream.closeFrame();
						frameHeader = bitstream.readFrame();
					}
		
					Duration = (int) totalMs;
					getInfo();

					if(!Thread.interrupted())
					{
						Message msg = Message.obtain();
						msg.what = 2;
						hl.sendMessage(msg);
					}
				} catch (BitstreamException e) {

				}
			}
		});
		preparing.start();
	}

	public Integer getPosition() {
		if(at==null)return Duration;
		
		return (int) (lastSeekPosition + (at.getPlaybackHeadPosition()
				/ (float) at.getPlaybackRate() * 1000));

	}

	public Integer getDuration() {
		return Duration;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		completation_listener = listener;
	}

	public void onCompletition() {
		completation_listener.onCompletion(null);

	}
	
	public Integer getSampleRate()
	{
		if(SampleRate!=null)return SampleRate;
		return -1;
	}

	private void getInfo() {
		try {
			Bitstream h = new Bitstream(new FileInputStream(file));
			SampleRate = h.readFrame().frequency();
			ChannelCount = 1;
			if (h.readFrame().mode() != 3)
				ChannelCount = 2;
			// SampleBuffer output = (SampleBuffer)
			// decoder.decodeFrame(frameHeader, bitstream);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BitstreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ByteArrayOutputStream data = new ByteArrayOutputStream(1024);

	public class DecodeRunnable implements Runnable {
		private Integer startMs;

		DecodeRunnable(Integer start) {
			startMs = start;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			float totalMs = 0;
			boolean seeking = true;
			boolean mono = false;
			synchronized(ChannelCount)
			{
				if(ChannelCount==1)mono = true;
			}
			
			InputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(
						new FileInputStream(file), 8 * 1024);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Bitstream bitstream = new Bitstream(inputStream);
				Decoder decoder = new Decoder();

				boolean done = false;
				while (!done) {
					Header frameHeader = bitstream.readFrame();
					if (frameHeader == null) {
						Message msg = Message.obtain();
						msg.what = 1;
						hl.sendMessage(msg);
						done = true;
					} else {
						
						totalMs += frameHeader.ms_per_frame();
						if (totalMs >= startMs) {
							if(seeking) synchronized(lastSeekPosition){lastSeekPosition = (int) totalMs;}
							seeking = false;
						}

						if (!seeking) {
							SampleBuffer output = (SampleBuffer) decoder
									.decodeFrame(frameHeader, bitstream);

							ByteArrayOutputStream outStream = new ByteArrayOutputStream(
									1024);

							short[] pcm = output.getBuffer();
							
							int length = mono ? pcm.length/2 : pcm.length;

							for (int i=0; i<length;i++) {
								outStream.write(pcm[i] & 0xff);
								outStream.write((pcm[i] >> 8) & 0xff);			
							}

							byte[] b = outStream.toByteArray();
							if (Thread.interrupted())
								break;
							try{
								synchronized (at) {
									at.write(b, 0, b.length);
								}
							}catch(Exception e)
							{
								
							}
						}
					}
					bitstream.closeFrame();
					if (Thread.interrupted())
						break;
				}

			} catch (BitstreamException e) {

			} catch (DecoderException e) {

			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
