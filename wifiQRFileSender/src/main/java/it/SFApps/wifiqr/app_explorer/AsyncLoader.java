package it.SFApps.wifiqr.app_explorer;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;

public class AsyncLoader extends AsyncTask<Void, Integer, AppElement[]> {
	
	private PackageManager pk;
	private AppsExplorerFragment f;
	AsyncLoader(AppsExplorerFragment f)
	{
	this.pk = f.getActivity().getPackageManager();
	this.f = f;
	}
	
	
	@SuppressLint("NewApi")
	@Override
	protected AppElement[] doInBackground(Void... arg0) {
		List<ApplicationInfo> apps= pk.getInstalledApplications(PackageManager.GET_META_DATA);
		
		AppElement appsElem[] = new AppElement[apps.size()];
	    Resources ApkResources=null;
		Drawable generic = pk.getDefaultActivityIcon();

	    for(int i=0;i<apps.size();i++)
	    {
	    	if(isCancelled())return null;
		    try {
				ApkResources = pk.getResourcesForApplication(apps.get(i));
				appsElem[i]= new AppElement();
				if(apps.get(i).icon!=0)
				{
					try{
						if(Build.VERSION.SDK_INT>=15)
						{
							try{
							appsElem[i].icon = ApkResources.getDrawableForDensity(apps.get(i).icon, DisplayMetrics.DENSITY_HIGH);
							}catch(NotFoundException e)
							{
								appsElem[i].icon = ApkResources.getDrawable(apps.get(i).icon);
							}
						}else appsElem[i].icon = ApkResources.getDrawable(apps.get(i).icon);
					}catch(NotFoundException e)
					{
						appsElem[i].icon = generic;
					}
					
				}else appsElem[i].icon = generic;
				
				appsElem[i].name = (String) apps.get(i).loadLabel(pk);
				appsElem[i].info = apps.get(i);
		    } catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return appsElem;
	}
	
    protected void onPostExecute(AppElement[] apps) {
    	
    	f.setList(apps);
    }

    
    
}