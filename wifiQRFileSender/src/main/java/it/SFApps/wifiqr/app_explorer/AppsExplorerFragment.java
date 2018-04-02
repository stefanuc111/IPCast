package it.SFApps.wifiqr.app_explorer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.OrderSelectorDialog;
import it.SFApps.wifiqr.tool.OrderSelectorDialog.OrderSelectorListener;
import it.SFApps.wifiqr.tool.ServerFragment;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.Utils;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class AppsExplorerFragment extends ServerFragment implements OrderSelectorListener {
	private ListView appsListView;
	private GridViewCompat appsGridView;
	private boolean useGridView = false;
	private AppElement apps[];
	private BaseAdapter apps_adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		
		int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;

		switch (screenSize) {
		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
			useGridView = true;
			break;
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			useGridView = true;
			break;
		}

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		load.cancel(true);

	}
	
	private AsyncLoader load;
	@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
	super.onCreateView(inflater, container, savedInstanceState);
	View r = inflater.inflate(R.layout.app_explorer_fragment, container,false);
	appsListView = (ListView) r.findViewById(R.id.appsListView);
	appsGridView = (GridViewCompat) r.findViewById(R.id.gridView1);
	//appsGridView.setChoiceMode(GridViewCompat.CHOICE_MODE_MULTIPLE);
	if(savedInstanceState!=null)
	{
		if(savedInstanceState.containsKey("useGridView"))useGridView = savedInstanceState.getBoolean("useGridView");
	}

	
	load = new AsyncLoader(this);
	load.execute();
	
	
	return r;
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.app_explorer, menu);
		
		MenuItem v = menu.findItem(R.id.view_type);
		if(useGridView)
		{
			v.setIcon(R.drawable.ic_action_list);
			v.setTitle(R.string.list_type);
		}
		else{ v.setIcon(R.drawable.ic_action_grid);
		v.setTitle(R.string.grid_type);
		
		}
		


	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean("useGridView", useGridView);
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.order_by:
			OrderSelectorDialog dialog = new OrderSelectorDialog();
			dialog.show(getActivity().getSupportFragmentManager(), null);
			return true;
		case R.id.view_type:
			useGridView=!useGridView;
			getActivity().supportInvalidateOptionsMenu();
			setList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onOrderClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
		switch(which)
		{
		case OrderSelectorDialog.OrderNameFiles.NAME_ASCEND:
			Arrays.sort(apps,new Comparator<AppElement>()
				    {
					@Override
					public int compare(AppElement arg0, AppElement arg1) {
						return arg0.name.compareTo(arg1.name);
					}
		    });
		break;
		case OrderSelectorDialog.OrderNameFiles.NAME_DECRESENT:
			Arrays.sort(apps,new Comparator<AppElement>()
				    {
					@Override
					public int compare(AppElement arg0, AppElement arg1) {
						return arg1.name.compareTo(arg0.name);
					}
		    });
		break;
		case OrderSelectorDialog.OrderNameFiles.SIZE_ASCEND:
			Arrays.sort(apps,new Comparator<AppElement>()
				    {
					@Override
					public int compare(AppElement arg0, AppElement arg1) {
						if(new File(arg0.info.sourceDir).length()> new File(arg1.info.sourceDir).length())return 1;else
						return -1;
					}
				    
		    });
		break;
		case OrderSelectorDialog.OrderNameFiles.SIZE_DECRESCENT:
			Arrays.sort(apps,new Comparator<AppElement>()
				    {
					@Override
					public int compare(AppElement arg0, AppElement arg1) {
						if(new File(arg0.info.sourceDir).length()< new File(arg1.info.sourceDir).length())return 1;else
						return -1;
					}
				});
				    
		break;
		}
		
		apps_adapter.notifyDataSetChanged();

}
	
	public void setList(AppElement[] apps)
	{
		this.apps = apps;
		setList();
	}
	
	private void setList()
	{
		if(srv!=null && apps!=null)
		{
			if(useGridView)
			{
				appsListView.setVisibility(View.GONE);
				appsGridView.setVisibility(View.VISIBLE);
				apps_adapter = new AppElementAdapter(getActivity(), R.layout.app_explorer_row_grid_element, apps, srv.f);
				appsGridView.setAdapter(apps_adapter);
				
			}else{
				appsListView.setVisibility(View.VISIBLE);
				appsGridView.setVisibility(View.GONE);
				apps_adapter = new AppElementAdapter(getActivity(), R.layout.app_explorer_row_element, apps, srv.f);
				appsListView.setAdapter(apps_adapter);
			}
		
			getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);
			
		}
	}
	
	

        @Override
        public void onServerConnected(ServerService s) {

        	appsListView.setOnItemClickListener(new OnItemClickListener(){
        		
				@Override
        		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
        				long arg3) {
        			// TODO Auto-generated method stub
        			AppElement app = (AppElement)arg0.getAdapter().getItem(position);
        			
        			File newFile = new File(app.info.sourceDir);
        			if(newFile.getName().equals("pkg.apk"))
        			{
        				Toast.makeText(getActivity(), R.string.protected_app, Toast.LENGTH_LONG).show();
        				apps_adapter.notifyDataSetChanged();
        				return;
        			}
        				if(srv.f.contains(newFile))
        				{
        					srv.f.remove(newFile);
        				}else{
        					srv.f.add(newFile);
        					
                			
                			
            				if(Build.VERSION.SDK_INT>=14 && useGridView)
            				{
            				View cp = arg0.getAdapter().getView(position, null, (ViewGroup) arg1.getRootView());
            				Utils.sendAnimation(cp, arg1);
            				}
        				}
        				getActivity().supportInvalidateOptionsMenu();
        				
        		}
        		
        		
        		
        	});
        	appsGridView.setOnItemClickListener(appsListView.getOnItemClickListener());
            
        }

        @Override
        public void onServerDisconnected() {
            appsListView.setOnItemClickListener(null);
        }


	@Override
	public void onFileListChange() {
		// TODO Auto-generated method stub
		if(apps_adapter!=null)apps_adapter.notifyDataSetChanged();
	}


	
		
}
