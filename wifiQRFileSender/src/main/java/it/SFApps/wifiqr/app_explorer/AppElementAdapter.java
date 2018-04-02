package it.SFApps.wifiqr.app_explorer;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.FileList;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.GridViewCompat;

import java.io.File;
import java.util.List;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppElementAdapter extends ArrayAdapter<AppElement> {

	private AppElement apps[];
	private FileList selected;
	private int res;

	public AppElementAdapter(Context context, int resource, AppElement apps[],
			List<File> selected) {
		super(context, resource, apps);
		this.apps = apps;
		this.selected = ((FileList) selected);
		this.res = resource;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		if (row == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			row = inflater.inflate(this.res, parent, false);
		}

		ImageView icon = (ImageView) row.findViewById(R.id.imageView);
		TextView text = (TextView) row.findViewById(R.id.textViewElementName);
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
		
		
		
		if (apps[position].icon != null)
			icon.setImageDrawable(apps[position].icon);

		else
			icon.setImageDrawable(null);

		if (apps[position].name != null)
			text.setText(apps[position].name);
		else
			text.setText(null);

		if (selected.contains(new File(apps[position].info.sourceDir))) {
			if (parent instanceof GridViewCompat) {
				((GridViewCompat) parent).setItemChecked(position, true);
			} else if(parent instanceof ListView) {
				((ListView) parent).setItemChecked(position, true);
			}

		} else {
			if (parent instanceof GridViewCompat) {
				((GridViewCompat) parent).setItemChecked(position, false);
			} else if(parent instanceof ListView){
				((ListView) parent).setItemChecked(position, false);
			}
		}

		row.setTag(position);


		return row;

	}

}
