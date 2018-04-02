package it.SFApps.wifiqr.file_explorer;

import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.ServerActivity;
import it.SFApps.wifiqr.tool.FileList.OnFileListChangeListener;
import it.SFApps.wifiqr.tool.GridViewCompat;
import it.SFApps.wifiqr.tool.OrderSelectorDialog;
import it.SFApps.wifiqr.tool.OrderSelectorDialog.OrderSelectorListener;
import it.SFApps.wifiqr.tool.ServerFragment;
import it.SFApps.wifiqr.tool.ServerService.ServerBinder;
import it.SFApps.wifiqr.tool.ServerService;
import it.SFApps.wifiqr.tool.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileExplorerFragment extends ServerFragment implements
		OrderSelectorListener {

	private File ExternalRoot = Environment.getExternalStorageDirectory();
	private File ActualDirectory = ExternalRoot;
	private ListView fileView;
	private GridViewCompat fileGridView;
	private boolean useGridView = false;
	private TextView empty_view;
	private SharedPreferences pref;
	private FileElementAdapter FEA;
	private TextView position;
	private Integer file_order = OrderSelectorDialog.OrderNameFiles.NAME_ASCEND;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View r = inflater.inflate(R.layout.file_explorer_fragment, container,
				false);

		fileView = (ListView) r.findViewById(R.id.fileListView);
		fileGridView = (GridViewCompat) r.findViewById(R.id.gridView1);
		//fileGridView.setChoiceMode(GridViewCompat.CHOICE_MODE_MULTIPLE);
		empty_view = (TextView) r.findViewById(R.id.textEmpty);
		position = (TextView) r.findViewById(R.id.textViewPosition);


//		ActualDirectory = ExternalRoot;

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("ActualDirectoy"))
				ActualDirectory = new File(
						savedInstanceState.getString("ActualDirectoy"));
			else
				ActualDirectory = ExternalRoot;

			if (savedInstanceState.containsKey("useGridView"))
				useGridView = savedInstanceState.getBoolean("useGridView");

		}

		if (savedInstanceState != null) {

			if (savedInstanceState.containsKey("file_order")) {
				file_order = savedInstanceState.getInt("file_order");
			}

		}

		return r;
	}

	public void launchServerActivity() {
		Intent i = new Intent(getActivity(), ServerActivity.class);
		startActivity(i);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("ActualDirectoy", ActualDirectory.getAbsolutePath());
		outState.putInt("file_order", file_order);
		outState.putBoolean("useGridView", useGridView);

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void loadfilelist() {

		if (ActualDirectory.exists() && isAdded()) {
			getActivity().supportInvalidateOptionsMenu();

			if (ActualDirectory.getAbsolutePath().startsWith(
					ExternalRoot.getAbsolutePath())
					&& !ExternalRoot.equals(ActualDirectory)) {
				position.setText(ActualDirectory.getAbsolutePath()
						.replaceFirst(
								"^" + ExternalRoot.getAbsolutePath() + "/", ""));
			} else if (ExternalRoot.equals(ActualDirectory)) {
				position.setText(getText(R.string.user_data));
			} else {
				position.setText(ActualDirectory.getAbsolutePath());
			}
			File list[] = ActualDirectory.listFiles();
			if(list == null)list = new File[0];
			
			switch (file_order) {
			case OrderSelectorDialog.OrderNameFiles.NAME_ASCEND:
				Arrays.sort(list);

				break;
			case OrderSelectorDialog.OrderNameFiles.NAME_DECRESENT:
				Arrays.sort(list, Collections.reverseOrder());

				break;
			case OrderSelectorDialog.OrderNameFiles.SIZE_ASCEND:
				Arrays.sort(list, new Comparator<File>() {
					@Override
					public int compare(File file1, File file2) {
						if (file1.length() > file2.length())
							return 1;
						else if (file1.length() < file2.length())
							return -1;

						return 0;
					}
				});
				break;
			case OrderSelectorDialog.OrderNameFiles.SIZE_DECRESCENT:
				Arrays.sort(list, new Comparator<File>() {
					@Override
					public int compare(File file1, File file2) {
						if (file1.length() > file2.length())
							return -1;
						else if (file1.length() < file2.length())
							return 1;

						return 0;
					}
				});
				break;
			}

			if (FEA != null)
				pref.unregisterOnSharedPreferenceChangeListener(FEA);
			List<File> f;
			if (srv != null)
				f = srv.f;
			else
				f = new ArrayList<File>();

			if (useGridView)
				FEA = new FileElementAdapter(this.getActivity(),
						R.layout.file_explorer_row_grid_element, list, f);
			else
				FEA = new FileElementAdapter(this.getActivity(),
						R.layout.file_explorer_row_element, list, f);

			pref.registerOnSharedPreferenceChangeListener(FEA);
			setList();

			if (list.length == 0) {
				// empty
				empty_view.setVisibility(View.VISIBLE);
			} else {
				empty_view.setVisibility(View.GONE);

			}

		}
	}

	private void setList() {
		if (srv != null) {
			if (useGridView) {
				fileView.setVisibility(View.GONE);
				fileGridView.setVisibility(View.VISIBLE);
				fileGridView.setAdapter(FEA);

			} else {
				fileView.setVisibility(View.VISIBLE);
				fileGridView.setVisibility(View.GONE);
				fileView.setAdapter(FEA);
			}

		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.file_explorer, menu);
		
		if (ActualDirectory.equals(ExternalRoot)) {
			menu.findItem(R.id.external_directory).setEnabled(false);
		} else {
			menu.findItem(R.id.external_directory).setEnabled(true);
		}

		MenuItem v = menu.findItem(R.id.view_type);
		if (useGridView) {
			v.setIcon(R.drawable.ic_action_list);
			v.setTitle(R.string.list_type);
		} else {
			v.setIcon(R.drawable.ic_action_grid);
			v.setTitle(R.string.grid_type);

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.order_by:
			OrderSelectorDialog dialog = new OrderSelectorDialog();
			dialog.show(getActivity().getSupportFragmentManager(), null);
			return true;
		case R.id.external_directory:
			ActualDirectory = ExternalRoot;
			loadfilelist();
			return true;
		case R.id.view_type:
			useGridView = !useGridView;
			loadfilelist();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	public boolean onBackPressed() {
		
		File parent = ActualDirectory.getParentFile();
		if (parent != null) {
			ActualDirectory = parent;
			loadfilelist();
		} else
			return false;
		return true;
	}

	@Override
	public void onOrderClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		file_order = which;
		loadfilelist();
	}


	public void onServerConnected(ServerService s) {
		// We've bound to LocalService, cast the IBinder and get
		// LocalService instance
		loadfilelist();

		fileView.setOnItemClickListener(new OnItemClickListener() {

			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long arg3) {
				
				View cp = adapter.getAdapter().getView(position, null, (ViewGroup) view.getRootView());

				
				File f = (File) adapter.getItemAtPosition(position);
				if (f.isDirectory()) {

					if (f.canRead()) {
						if(Build.VERSION.SDK_INT>=14 && useGridView)
						{
	    				cp = Utils.copyViewLocations(cp, view);
						cp.animate().scaleX(20).scaleY(20).setDuration(1000).start();
						cp.animate().alpha(0).setDuration(200).start();
						}
						ActualDirectory = f;
						loadfilelist();
					} else {
						Toast.makeText(getActivity(),
								R.string.no_permission, Toast.LENGTH_LONG)
								.show();
						fileView.setItemChecked(position, false);
					}
				} else {

    				if(useGridView)Utils.sendAnimation(cp, view);
    				
					if (!srv.f.contains(f)) {

						srv.f.add(f);
					} else if (srv.f.contains(f)) {
						srv.f.remove(f);
					}
					//launchServerActivity();
				}

			}

		});

		fileView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter,
					View view, int position, long arg3) {
				File f = (File) adapter.getItemAtPosition(position);
				if (f.isDirectory()) {
					if(f.canWrite())
					{
					pref.edit()
							.putString("receive_path", f.getAbsolutePath())
							.commit();
					Toast.makeText(
							getActivity(),
							getString(R.string.directory_setted) + " "
									+ f.getAbsolutePath(),
							Toast.LENGTH_SHORT).show();
					((FileElementAdapter) adapter.getAdapter())
					.notifyDataSetChanged();
					}else
					{
						Toast.makeText(
								getActivity(),
								getString(R.string.no_permission_write),
								Toast.LENGTH_SHORT).show();
					}
				}

				
				
				
				return true;
			}
		});

		fileGridView.setOnItemClickListener(fileView
				.getOnItemClickListener());
		fileGridView.setOnItemLongClickListener(fileView
				.getOnItemLongClickListener());
	}

	@Override
	public void onServerDisconnected() {
		fileView.setOnItemClickListener(null);
		fileView.setOnLongClickListener(null);
	}


	@Override
	public void onFileListChange() {
		FEA.notifyDataSetChanged();
	}

}
