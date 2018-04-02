package it.SFApps.wifiqr.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class Utils {

	public static class BinaryInstallerTask extends AsyncTask<AssetManager,Void,Void>
	{
		private Context c;
		private String data_file_directory;
		public BinaryInstallerTask(Context cont) {
			c = cont;
			data_file_directory = c.getFilesDir().getAbsolutePath();
		}

		private void copyFile(String assetPath, String localPath, AssetManager as) {
		    try {
		    	
		        InputStream in = as.open(assetPath);
		        File localpath = new File(localPath);
		        if(!localpath.getParentFile().exists())
		        {
		        	localpath.getParentFile().mkdirs();
		        }
		        FileOutputStream out = new FileOutputStream(localPath);
		        int read;

		        
		        byte[] buffer = new byte[4096];
		        while ((read = in.read(buffer)) > 0) {
		            out.write(buffer, 0, read);
		        }
		        out.close();
		        in.close();
		    } catch (IOException e) {

		    }
		}

	   private void copyFiles (AssetManager mgr, String path) {
	        try {
	            String list[] = mgr.list(path);
	            if (list != null)
	                for (int i=0; i<list.length; ++i)
	                {
	                    Log.v("Assets:", path +"/"+ list[i]);
	                    copyFile(path+"/"+ list[i], data_file_directory+"/"+ path +"/"+ list[i],mgr);
	                    copyFiles(mgr, path + "/" + list[i]);
	                }
	        } catch (IOException e) {
	             Log.v("List error:", "can't list" + path);
	        }
	     }
		
		@Override
		protected void onPreExecute()
		{
			if(new File(data_file_directory+"/ffmpeg/ffmpeg").exists())
			{
			cancel(true);
			return;
			}
//			Toast.makeText(c, "Installing binaries...", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(AssetManager... params) {
			// TODO Auto-generated method stub
			copyFiles(params[0], "ffmpeg");
	  		  Process chmod;
			try {
				chmod = Runtime.getRuntime().exec("system/bin/chmod -R   777 "+data_file_directory+"/ffmpeg");
		  		chmod.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
//			Toast.makeText(c, "Binary installed!", Toast.LENGTH_SHORT).show();
		}
		
	}
	

	public static void printProcessOutput(Process p)
	{
        try {

		  
          p.waitFor();

          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String line;
			while ((line = reader.readLine()) != null) {
				Log.i("Exec", line);

			}
	          reader.close();

	          // Waits for the command to finish.
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static View copyViewLocations(View cp,View original)
	{
		int location[] = new int [2];
		original.getLocationInWindow(location);
		cp.setX(location[0]);
		cp.setY(location[1]);
		cp.setLayoutParams(new LayoutParams(original.getWidth(), original.getHeight()));
		((ViewGroup)original.getRootView()).addView(cp);

		return cp;
	}
	
	public static void sendAnimation(final View cp,View original)
	{
		if(Build.VERSION.SDK_INT>=14)
		{
		final ViewGroup root =	(ViewGroup) original.getRootView();
		cp.setAlpha(0.0f);
		copyViewLocations(cp,original);
		cp.animate().alpha(1).translationX(root.getWidth()/2-(original.getWidth()/2)).setDuration(800).start();
		cp.animate().translationYBy(-4000).scaleXBy(3).scaleYBy(3).setDuration(3000).setListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if(arg0.getDuration()==3000)root.removeView(cp);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}}).start();

		
	}
	}
	
	public static Bitmap getAlbumart(Long album_id, Context mContext) 
	   {
        Bitmap bm = null;
        try 
        {
            final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = mContext.getContentResolver()
                .openFileDescriptor(uri, "r");

            if (pfd != null) 
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
    } catch (Exception e) {
    }
    return bm;
}
	
}
