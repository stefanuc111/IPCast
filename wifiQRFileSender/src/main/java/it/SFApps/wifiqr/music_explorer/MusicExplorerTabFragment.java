package it.SFApps.wifiqr.music_explorer;


import it.SFApps.common.view.SlidingTabLayout;
import it.SFApps.wifiqr.R;
import it.SFApps.wifiqr.music_explorer.fragments.AlbumFragment;
import it.SFApps.wifiqr.music_explorer.fragments.ArtistsFragment;
import it.SFApps.wifiqr.music_explorer.fragments.GenreFragment;
import it.SFApps.wifiqr.music_explorer.fragments.SongsFragment;
import it.SFApps.wifiqr.tool.ServerFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MusicExplorerTabFragment extends ServerFragment {

	static final String LOG_TAG = "SlidingTabsBasicFragment";
	private String[] pages;
	private SlidingTabLayout mSlidingTabLayout;
	private ViewPager mViewPager;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.music_explorer_fragment, container,
				false);
	}


	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		pages = getActivity().getResources().getStringArray(R.array.music_view);

		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mSlidingTabLayout = (SlidingTabLayout) view
				.findViewById(R.id.sliding_tabs);
		mViewPager.setAdapter(new SamplePagerAdapter(getChildFragmentManager()));
		mViewPager.setOffscreenPageLimit(2);
		
		mSlidingTabLayout.setViewPager(mViewPager);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

 
	class SamplePagerAdapter extends FragmentPagerAdapter {

		public SamplePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		
		@Override
		public int getCount() {
			return pages.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return pages[position];
		}



		
		@Override
		public Fragment getItem(int arg0) {
			Fragment f = null;
			switch(arg0)
			{
			case 0:
				f = new ArtistsFragment();
			break;
			case 1:
				f = new AlbumFragment();
			break;
			case 2:
				f = new SongsFragment();
			break;
			case 3:
				f = new GenreFragment();
			break;
			}
			return f;
		}

	}
}