package it.SFApps.wifiqr.file_explorer;


import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.GridViewCompat;

import java.io.File;
import java.util.List;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FileElementAdapter extends ArrayAdapter<File> implements OnSharedPreferenceChangeListener{

	private File files[];
	//private String ellipsized_names[];
	private List<File> selected;
	private SharedPreferences sharedPref;
	private File receive_directory;
	private int res;
	public FileElementAdapter(Context context, int resource, File files[], List<File> selected) {
		super(context, resource, files);
		
		this.files = files;
		this.selected = selected;
		this.res = resource;
		//ellipsized_names = new String[files.length];
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());

		receive_directory = new File(sharedPref.getString("receive_path", ""));
	
	}
	
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View row, ViewGroup parent)
	{
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            row = inflater.inflate(res, parent, false);
        }
        
		if (Build.VERSION.SDK_INT >= 14 && parent instanceof GridViewCompat) {
			
			ObjectAnimator animation = (ObjectAnimator) row.getTag(R.id.actual_animation);

			
			if(row.getTag()==null||(Integer)row.getTag()!=position)
				{
				
				if(animation!=null)
				{
					animation.cancel();
				}
				
				ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(row, "alpha", 0f, 1f);
				fadeAnim.setDuration((int)(Math.random()*2000));
				fadeAnim.start();
				row.setTag(R.id.actual_animation, fadeAnim);
				}
		}
        
        
        
        
        ImageView icon = (ImageView) row.findViewById(R.id.imageView);
        TextView text = (TextView) row.findViewById(R.id.textViewElementName);
        text.setMaxLines(2);
        /*text.addEllipsizeListener(new EllipsizeListener(){

			@Override
			public void ellipsizeStateChanged(boolean ellipsized) {
				// TODO Auto-generated method stub
				ellipsized_names[(Integer)text.getTag()] = (String) text.getText();
			}});*/
        
        if(files[position].isFile())
        {
        	icon.setImageResource(R.drawable.file_icon);
        	if(selected.contains(files[position]))
        	{
                if(parent instanceof GridViewCompat)
                {
                	 ((GridViewCompat)parent).setItemChecked(position, true);
                }else if(parent instanceof ListView){
               	 ((ListView)parent).setItemChecked(position, true);
                }

        	}else
        	{
                if(parent instanceof GridViewCompat)
                {
                	 ((GridViewCompat)parent).setItemChecked(position, false);
                }else if(parent instanceof ListView)
                {
               	 		((ListView)parent).setItemChecked(position, false);
                }
        	}
        }else if(receive_directory.getAbsolutePath().equals(files[position].getAbsolutePath()))
        {
        	icon.setImageResource(R.drawable.receive_folder_icon);
        }else
        {
        	icon.setImageResource(R.drawable.folder_icon);
        }
       /* if(ellipsized_names[position]!=null)
        {
            text.setText(ellipsized_names[position]);
        }else */
        text.setText(files[position].getName());
        row.setTag(position);
		
		return row;
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		receive_directory = new File(sharedPref.getString("receive_path", ""));
		//Toast.makeText(getContext(), "changed", Toast.LENGTH_LONG).show();
	}

}

