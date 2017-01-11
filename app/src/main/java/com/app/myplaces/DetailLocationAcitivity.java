package com.app.myplaces;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.dataMng.YPYNetUtils;
import com.app.myplaces.fragment.FragmentLocationDetailInfomation;
import com.app.myplaces.fragment.FragmentLocationDetailMap;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.PlaceDetailObject;
import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.object.ResponcePlaceResult;
import com.app.myplaces.object.RouteObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.ypyproductions.net.task.DBTask;
import com.ypyproductions.net.task.IDBTaskListener;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.app.myplaces.R;

import java.util.ArrayList;

/**
 * PodcastTabsPagerAcitivity
 * 
 * @author DOBAO
 * @Email dotrungbao@gmail.com
 * @Skype baopfiev_k50
 * @Date Jun 21, 2013
 * @Packagename com.tharanga.radio
 */
public class DetailLocationAcitivity extends DBFragmentActivity implements  OnMenuItemClickListener {

	public static final String TAG = DetailLocationAcitivity.class.getSimpleName();
	public static final String KEY_TAB = "tab";
	public static final String KEY_INDEX_LOCATION = "indexLocation";

	public TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	private TotalDataManager mTotalMng;
	public Location mCurrentLocation;

	private int mIndexPosition;
	
	public PlaceObject mCurrentPlaceObject;
	public PlaceDetailObject mPlaceDetailObject;
	public RouteObject mRouteObject;
	private String mStartFrom="";
	private AdView adView;
	private Menu mMenu;
	
	private final ArrayList<Fragment> mListFragments = new ArrayList<Fragment>();
	public DisplayImageOptions mImgOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_location);

		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setHomeButtonEnabled(false);

		Intent mIt = getIntent();
		if (mIt != null) {
			mIndexPosition = mIt.getIntExtra(KEY_INDEX_LOCATION, 0);
			DBLog.d(TAG, "================>mIndexPosition=" + mIndexPosition);
			mStartFrom =mIt.getStringExtra(KEY_START_FROM);
		}
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString(KEY_TAB));
		}
		mTotalMng = TotalDataManager.getInstance();
		mCurrentLocation = mTotalMng.getCurrentLocation();
		
		this.mImgOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.icon_location_default)
		.resetViewBeforeLoading(false)
		.cacheInMemory(true).cacheOnDisk(true)
		.considerExifParams(true).build();
		
		this.initLocationObject();
		this.setUpLayoutAdmob();
		
	}
	
	private void setUpLayoutAdmob() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_ad);
		boolean b=SHOW_ADVERTISEMENT;
		if(b){
			adView = new AdView(this);
			adView.setAdUnitId(ADMOB_ID_BANNER);
			adView.setAdSize(AdSize.SMART_BANNER);

			layout.addView(adView);
			AdRequest mAdRequest = new AdRequest.Builder().build();
			adView.loadAd(mAdRequest);
		}
		else{
			layout.setVisibility(View.GONE);
		}
	}

	private void initLocationObject() {
		if(!mStartFrom.equals(START_FROM_MAIN)){
			HomeSearchObject mHomeSearchObject = mTotalMng.getHomeSearchSelected();
			if (mHomeSearchObject != null) {
				ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
				if (mResponcePlaceResult != null) {
					ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();
					if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
						int size = mListPlaceObjects.size();
						if (mIndexPosition >= 0 & mIndexPosition < size) {
							mCurrentPlaceObject = mListPlaceObjects.get(mIndexPosition);
							if(mCurrentPlaceObject!=null){
								mPlaceDetailObject=mCurrentPlaceObject.getPlaceDetailObject();
								mRouteObject = mCurrentPlaceObject.getRouteObject();
								if(mPlaceDetailObject==null){
									startGetDetailPlaceObject();
								}
								else{
									mTabsAdapter = new TabsAdapter(DetailLocationAcitivity.this, mTabHost, mViewPager);
									createTab();
								}
							}
						}
					}
				}
			}
		}
		else{
			ArrayList<PlaceObject> mListFavoriteObjects = TotalDataManager.getInstance().getListFavoriteObjects();
			if(mListFavoriteObjects!=null && mListFavoriteObjects.size()>0){
				int size = mListFavoriteObjects.size();
				if (mIndexPosition >= 0 & mIndexPosition < size) {
					mCurrentPlaceObject = mListFavoriteObjects.get(mIndexPosition);
					if(mCurrentPlaceObject!=null){
						startGetDetailPlaceObject();
					}
				}
			}
		}
	}

	private void startGetDetailPlaceObject() {
		if(!ApplicationUtils.isOnline(this)){
			showDialogFragment(DIALOG_LOSE_CONNECTION);
			return;
		}
		DBTask mDbTask = new DBTask(new IDBTaskListener() {

			@Override
			public void onPreExcute() {
				showProgressDialog(String.format(getString(R.string.info_format_process_detail_location), mCurrentPlaceObject.getName()));
			}

			@Override
			public void onDoInBackground() {
				mPlaceDetailObject = YPYNetUtils.getPlaceDetailObject(DetailLocationAcitivity.this, mCurrentPlaceObject.getReferenceToken());
				mRouteObject = YPYNetUtils.getRouteObject(DetailLocationAcitivity.this, mCurrentLocation, mCurrentPlaceObject.getLocation());
			}

			@Override
			public void onPostExcute() {
				dimissProgressDialog();
				if(mPlaceDetailObject==null){
					showToast(R.string.info_server_error);
					return;
				}
				mCurrentPlaceObject.setPlaceDetailObject(mPlaceDetailObject);
				if(mRouteObject!=null){
					mCurrentPlaceObject.setRouteObject(mRouteObject);
				}
				mTabsAdapter = new TabsAdapter(DetailLocationAcitivity.this, mTabHost, mViewPager);
				createTab();
			}

		});
		mDbTask.execute();
	}


	private void createTab() {
		Bundle mBundle1 = new Bundle();
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.tab_infomation)).setIndicator(getString(R.string.tab_infomation)), FragmentLocationDetailInfomation.class,
				mBundle1);

		Bundle mBundle2 = new Bundle();
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.tab_map)).setIndicator(getString(R.string.tab_map)), FragmentLocationDetailMap.class, mBundle2);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_my_location, menu);
		this.mMenu= menu;
		setVisibleButtonMenu(false);
		return true;
	}
	
	private void setVisibleButtonMenu(boolean visible) {
		if (mMenu != null) {
			MenuItem mMenuItem = mMenu.findItem(R.id.action_menu);
			if (mMenuItem != null) {
				mMenuItem.setVisible(visible);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToHome();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_TAB, mTabHost.getCurrentTabTag());
	}

	public class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		public final class TabInfo {
			private final Class<?> mClss;
			private final Bundle mArgs;
			private String mTagid = "";

			TabInfo(String pTag, Class<?> pClass, Bundle pArgs) {
				mClss = pClass;
				mArgs = pArgs;
			}
		}

		private class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			Fragment mFragment = isFragmentExits(info.mClss.getName());
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mContext, info.mClss.getName(), info.mArgs);
				info.mTagid = info.mClss.getName();
				mListFragments.add(mFragment);
			}
			return mFragment;
		}

		public Fragment isFragmentExits(String id) {
			int index = -1;
			for (int i = 0; i < mTabs.size(); i++) {
				TabInfo mTabInfo = mTabs.get(i);
				if (mTabInfo.mTagid.equals(id)) {
					index = i;
					break;
				}
			}
			if (index > 0) {
				return mListFragments.get(index);
			}
			return null;
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
			
			Fragment mFragment = mListFragments.get(position);
			if (mFragment instanceof FragmentLocationDetailMap) {
				((FragmentLocationDetailMap) mFragment).startRoute();
				setVisibleButtonMenu(true);
			}
			else if(mFragment instanceof FragmentLocationDetailInfomation){
				((FragmentLocationDetailInfomation) mFragment).updateDistance();
				setVisibleButtonMenu(false);
			}

		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	}

	private void backToHome() {
		if(mStartFrom!=null && !mStartFrom.equals(START_FROM_SEARCH)){
			if(mStartFrom.equals(START_FROM_TOTAL_PLACE)){
				finish();
			}
			else if(mStartFrom.equals(START_FROM_MAIN)){
				Intent mIntent = new Intent(this, MainActivity.class);
				mIntent.putExtra(KEY_START_FROM, START_FROM_DETAIL);
				DirectionUtils.changeActivity(DetailLocationAcitivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right, true, mIntent);
			}
		}
		else{
			Intent mIntent = new Intent(this, MainSearchActivity.class);
			DirectionUtils.changeActivity(DetailLocationAcitivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right, true, mIntent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mStartFrom.equals(START_FROM_MAIN)){
			DBLog.d(TAG, "===============>destroy place object");
			mCurrentPlaceObject.onDestroy();
		}
		mImgOptions=null;
		if (adView != null) {
			adView.destroy();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_menu:
			showMenu(R.id.action_menu, R.menu.total_location_options,this);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_terrain:
			setViewModeForFragmentLocation(GoogleMap.MAP_TYPE_TERRAIN, getString(R.string.menu_location_terrain));
			return true;
		case R.id.action_satellite:
			setViewModeForFragmentLocation(GoogleMap.MAP_TYPE_SATELLITE, getString(R.string.menu_location_satellite));
			return true;
		case R.id.action_traffic:
			setViewModeForFragmentLocation(GoogleMap.MAP_TYPE_HYBRID, getString(R.string.menu_location_traffic));
			return true;
		default:
			return false;
		}
	}
	public void setViewModeForFragmentLocation(int viewMode, String mStrName) {
		for (Fragment mFragment : mListFragments) {
			if (mFragment instanceof FragmentLocationDetailMap) {
				((FragmentLocationDetailMap) mFragment).setMapType(viewMode, mStrName);
				break;
			}
		}
	}
}
