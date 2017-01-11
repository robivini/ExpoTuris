package com.app.myplaces;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.app.myplaces.adapter.DrawerAdapter;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.fragment.FragmentAboutUs;
import com.app.myplaces.fragment.FragmentFavorites;
import com.app.myplaces.fragment.FragmentHome;
import com.app.myplaces.fragment.FragmentMyLocation;
import com.app.myplaces.fragment.FragmentSettings;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.ItemDrawerObject;
import com.app.myplaces.object.KeywordObject;
import com.app.myplaces.view.DBSeekBarView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.ypyproductions.utils.StringUtils;
import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.provider.MySuggestionDAO;
import com.app.myplaces.settings.SettingManager;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 
 * MainActivity.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation
 */
public class MainActivity extends DBFragmentActivity implements IWhereMyLocationConstants, PopupMenu.OnMenuItemClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mListStrDrawerTitles;

	private DrawerAdapter mDrawerAdapter;

	private ArrayList<Fragment> mListFragments = new ArrayList<Fragment>();
	private int mCurrentIndex = HOME_INDEX;
	private Menu mMenu;

	private String mStartFrom;

	private AdView adView;

	private Uri mQueryUri;
	
	public DisplayImageOptions mImgOptions;

	private SearchView searchView;

	private MenuItem menuSearchItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent mIntent = getIntent();
		if (mIntent != null) {
			mStartFrom = mIntent.getStringExtra(KEY_START_FROM);
		}
		mTitle = mDrawerTitle = getTitle();
		mListStrDrawerTitles = getResources().getStringArray(R.array.list_options);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();

			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		this.mImgOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.icon_location_default)
		.resetViewBeforeLoading(false)
		.cacheInMemory(true).cacheOnDisk(true)
		.considerExifParams(true).build();

		this.setUpDrawer();
		this.setUpLayoutAdmob();

		handleIntent(getIntent());
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


	private void setUpDrawer() {
		ArrayList<ItemDrawerObject> listItemDrawerObjects = new ArrayList<ItemDrawerObject>();
		int size = mListStrDrawerTitles.length;
		for (int i = 0; i < size; i++) {
			String mStr = mListStrDrawerTitles[i];
			ItemDrawerObject mItemDrawerObject = new ItemDrawerObject(mStr);
			listItemDrawerObjects.add(mItemDrawerObject);
			if (mStartFrom != null && mStartFrom.equals(START_FROM_DETAIL)) {
				if (i == FAVORITES_INDEX) {
					mItemDrawerObject.setSelected(true);
					showFragmentByTag(TAG_FAVORITE, FAVORITES_INDEX);
					setTitle(mStr);
				}
			}
			else {
				if (i == HOME_INDEX) {
					mItemDrawerObject.setSelected(true);
					showFragmentByTag(TAG_HOME, HOME_INDEX);
					setTitle(mStr);
				}
			}
		}
		mDrawerAdapter = new DrawerAdapter(this, listItemDrawerObjects, mTypeFaceRobotoBold, mTypeFaceRobotoLight);
		mDrawerListView.setAdapter(mDrawerAdapter);
		mDrawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				mDrawerAdapter.setSelectedDrawer(position);
				if (position == HOME_INDEX) {
					showFragmentByTag(TAG_HOME, HOME_INDEX);
					setVisibleButtonMenu(true);
				}
				else if (position == ABOUT_US_INDEX) {
					showFragmentByTag(TAG_ABOUT, ABOUT_US_INDEX);
					setVisibleButtonMenu(false);
				}
				else if (position == SETTINGS_INDEX) {
					showFragmentByTag(TAG_SETTINGS, SETTINGS_INDEX);
					setVisibleButtonMenu(false);
				}
				else if (position == MY_LOCATION_INDEX) {
					showFragmentByTag(TAG_MY_LOCATION, MY_LOCATION_INDEX);
					setVisibleButtonMenu(true);
				}
				else if (position == FAVORITES_INDEX) {
					showFragmentByTag(TAG_FAVORITE, FAVORITES_INDEX);
					setVisibleButtonMenu(true);
				}
			}
		});
	}


	private void setVisibleButtonMenu(boolean visible) {
		if (mMenu != null) {
			MenuItem mMenuItem = mMenu.findItem(R.id.action_menu);
			if (mMenuItem != null) {
				mMenuItem.setVisible(visible);
			}
			MenuItem mMenuDeleteItem = mMenu.findItem(R.id.action_delete_all);
			if (mMenuDeleteItem != null) {
				mMenuDeleteItem.setVisible(visible);
			}
			MenuItem mMenuSearchItem = mMenu.findItem(R.id.action_search);
			if (mMenuSearchItem != null) {
				mMenuSearchItem.setVisible(visible);
			}
			MenuItem mMenuDistanceItem = mMenu.findItem(R.id.action_quick_distance);
			if (mMenuDistanceItem != null) {
				mMenuDistanceItem.setVisible(visible);
			}
		}
	}

	public void showFragmentByTag(String mTag, int index) {
		if (StringUtils.isStringEmpty(mTag)) {
			return;
		}
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
		Fragment mFragment = mFragmentManager.findFragmentByTag(mTag);

		this.mCurrentIndex = index;

		if (mListFragments.size() > 0) {
			for (Fragment mFragment1 : mListFragments) {
				if (mFragment1 != null && !mFragment1.getTag().equals(mTag)) {
					mFragmentTransaction.hide(mFragment1);
				}
			}
		}
		if (mFragment == null) {
			if (mTag.equals(TAG_ABOUT)) {
				FragmentAboutUs mFragmentAboutUs = new FragmentAboutUs();
				mFragmentTransaction.add(R.id.content_frame, mFragmentAboutUs, mTag);
				mListFragments.add(mFragmentAboutUs);
			}
			else if (mTag.equals(TAG_HOME)) {
				FragmentHome mFragmentHome = new FragmentHome();
				mFragmentTransaction.add(R.id.content_frame, mFragmentHome, mTag);
				mListFragments.add(mFragmentHome);
			}
			else if (mTag.equals(TAG_SETTINGS)) {
				FragmentSettings mFragmentSettings = new FragmentSettings();
				mFragmentTransaction.add(R.id.content_frame, mFragmentSettings, mTag);
				mListFragments.add(mFragmentSettings);
			}
			else if (mTag.equals(TAG_MY_LOCATION)) {
				FragmentMyLocation mFragmentMyLocation = new FragmentMyLocation();
				mFragmentTransaction.add(R.id.content_frame, mFragmentMyLocation, mTag);
				mListFragments.add(mFragmentMyLocation);
			}
			else if (mTag.equals(TAG_FAVORITE)) {
				FragmentFavorites mFragmentFavorite = new FragmentFavorites();
				mFragmentTransaction.add(R.id.content_frame, mFragmentFavorite, mTag);
				mListFragments.add(mFragmentFavorite);
			}
		}
		else {
			mFragmentTransaction.show(mFragment);
		}
		setTitle(mListStrDrawerTitles[index]);
		mFragmentTransaction.commit();
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mCurrentIndex == FAVORITES_INDEX) {
			getMenuInflater().inflate(R.menu.menu_favorite, menu);
		}
		else if (mCurrentIndex == MY_LOCATION_INDEX) {
			getMenuInflater().inflate(R.menu.menu_my_location, menu);
		}
		else if (mCurrentIndex == HOME_INDEX) {
			getMenuInflater().inflate(R.menu.menu_main, menu);
			menuSearchItem = menu.findItem(R.id.action_search);
			searchView = (SearchView) menuSearchItem.getActionView();
			searchView.setSubmitButtonEnabled(true);
			searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.title_search) + "</font>"));

			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}
		this.mMenu = menu;
		if (mCurrentIndex == HOME_INDEX || mCurrentIndex == MY_LOCATION_INDEX || mCurrentIndex == FAVORITES_INDEX) {
			setVisibleButtonMenu(true);
		}

		else if (mCurrentIndex == ABOUT_US_INDEX || mCurrentIndex == SETTINGS_INDEX) {
			setVisibleButtonMenu(false);
		}
		return true;
	}
	
	public boolean hiddenVirtualKeyBoard() {
		DBLog.d(TAG, "============>hiddenVirtualKeyBoard=" + searchView.isIconified());
		if (searchView != null && !searchView.isIconified()) {
			searchView.setQuery("", false);
			searchView.clearFocus();
			searchView.setIconified(true);
			menuSearchItem.collapseActionView();
			return true;
		}
		return false;
	}
	@Override
	public void onBackPressed() {
		if (!hiddenVirtualKeyBoard()) {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_menu:
			if (mCurrentIndex == MY_LOCATION_INDEX) {
				showMenu(R.id.action_menu, R.menu.location_options, this);
			}
			break;
		case R.id.action_delete_all:
			if (mCurrentIndex == FAVORITES_INDEX) {
				for (Fragment mFragment : mListFragments) {
					if (mFragment instanceof FragmentFavorites) {
						((FragmentFavorites) mFragment).deleteAllFavorite();
						break;
					}
				}
			}
			break;
		case R.id.action_quick_distance:
			showDialogChangeDistance();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		mImgOptions=null;
		if (mListFragments != null) {
			mListFragments.clear();
			mListFragments = null;
		}
		super.onDestroy();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public void onDestroyData() {
		super.onDestroyData();
		ImageLoader.getInstance().stop();
		TotalDataManager.getInstance().onDestroyTrackingService(getApplicationContext());
		TotalDataManager.getInstance().onDestroy();
		showIntertestialAds();
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
		case R.id.action_details:
			for (Fragment mFragment : mListFragments) {
				if (mFragment instanceof FragmentMyLocation) {
					((FragmentMyLocation) mFragment).showDialogLocationDetails();
					break;
				}
			}
			return true;
		default:
			return false;
		}
	}

	public void setViewModeForFragmentLocation(int viewMode, String mStrName) {
		for (Fragment mFragment : mListFragments) {
			if (mFragment instanceof FragmentMyLocation) {
				((FragmentMyLocation) mFragment).setMapType(viewMode, mStrName);
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mCurrentIndex != HOME_INDEX) {
				mDrawerAdapter.setSelectedDrawer(HOME_INDEX);
				showFragmentByTag(TAG_HOME, HOME_INDEX);
				invalidateOptionsMenu();
				setVisibleButtonMenu(true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			processSearchData(TYPE_SEARCH_BY_TEXT, query,query, true);
		}
		else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			mQueryUri = intent.getData();
			if (mQueryUri != null) {
				String keyword = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
				
				KeywordObject mKeywordObject = TotalDataManager.getInstance().getKeyWordObject(keyword);
				String realName="";
				if(mKeywordObject!=null){
					realName=mKeywordObject.getName();
				}
				processSearchData(TYPE_SEARCH_BY_TYPES, keyword,realName, false);
			}
		}
	}

	private void processSearchData(int type, String query, String realname,boolean isAllowAddRecent) {
		HomeSearchObject mHomeSearchObject = TotalDataManager.getInstance().findHomeSearchObject(query);
		if (mHomeSearchObject != null) {
			TotalDataManager.getInstance().setSelectedObject(mHomeSearchObject);
		}
		else {
			TotalDataManager.getInstance().setSelectedObject(0);
			mHomeSearchObject = TotalDataManager.getInstance().getListHomeSearchObjects().get(0);
			if (mHomeSearchObject != null) {
				mHomeSearchObject.setKeyword(query);
				mHomeSearchObject.setType(type);
				mHomeSearchObject.setRealName(realname.toUpperCase(Locale.US));
				if (isAllowAddRecent) {
					KeywordObject mKeywordObject = MySuggestionDAO.getPrivateData(this, query);
					DBLog.d(TAG, "==============>mKeywordObject=" + mKeywordObject);
					if (mKeywordObject == null) {
						KeywordObject mKeywordObject2 = new KeywordObject(query, query);
						MySuggestionDAO.insertData(this, mKeywordObject2);
					}
				}
			}
		}
		Intent mIntent = new Intent(this, MainSearchActivity.class);
		DirectionUtils.changeActivity(this, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIntent);
	}

	private void showDialogChangeDistance() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_quick_distance);
		LinearLayout mLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_quick_distance, null);
		final TextView mTvInfoRadius = (TextView) mLinearLayout.findViewById(R.id.tv_radius);
		final TextView mTvMinRadius = (TextView) mLinearLayout.findViewById(R.id.tv_min_radius);
		final TextView mTvMaxRadius = (TextView) mLinearLayout.findViewById(R.id.tv_max_radius);
		
		DBSeekBarView mDBSeekbar = (DBSeekBarView)mLinearLayout.findViewById(R.id.dBSeekBarView1);
		mDBSeekbar.setOnSeekBarChangeListener(new DBSeekBarView.OnSeekBarChangeListener() {
			
			@Override
			public void onUpdateProcess(int process) {
				SettingManager.setRadius(MainActivity.this, process);
				if(SettingManager.getMetric(MainActivity.this).equals(UNIT_KILOMETTER)){
					mTvInfoRadius.setText(String.format(getString(R.string.format_radius), process,"km"));
					mTvMaxRadius.setText(String.valueOf(MAX_RADIUS));
					mTvMinRadius.setText(String.valueOf(MIN_RADIUS));
				}
				else{
					int miles =  (int) ((float)process/ONE_MILE);
					mTvInfoRadius.setText(String.format(getString(R.string.format_radius), miles,"mi"));
					mTvMaxRadius.setText(String.valueOf((int) (MAX_RADIUS/ONE_MILE)));
					mTvMinRadius.setText(String.valueOf((int) (MIN_RADIUS/ONE_MILE)));
				}
			}
			
			@Override
			public void onSeekBarChangeListener(int process) {
				SettingManager.setRadius(MainActivity.this, process);
				if(SettingManager.getMetric(MainActivity.this).equals(UNIT_KILOMETTER)){
					mTvInfoRadius.setText(String.format(getString(R.string.format_radius), process,"km"));
					mTvMaxRadius.setText(String.valueOf(MAX_RADIUS));
					mTvMinRadius.setText(String.valueOf(MIN_RADIUS));
				}
				else{
					int miles =  (int) ((float)process/ONE_MILE);
					mTvInfoRadius.setText(String.format(getString(R.string.format_radius), miles,"mi"));
					mTvMaxRadius.setText(String.valueOf((int) (MAX_RADIUS/ONE_MILE)));
					mTvMinRadius.setText(String.valueOf((int) (MIN_RADIUS/ONE_MILE)));
				}
			}
		});
		int process =SettingManager.getRadius(this);
		if(SettingManager.getMetric(this).equals(UNIT_KILOMETTER)){
			mTvInfoRadius.setText(String.format(getString(R.string.format_radius), process,"km"));
			mTvMaxRadius.setText(String.valueOf(MAX_RADIUS));
			mTvMinRadius.setText(String.valueOf(MIN_RADIUS));
		}
		else{
			int miles =  (int) ((float)process/ONE_MILE);
			mTvInfoRadius.setText(String.format(getString(R.string.format_radius), miles,"mi"));
			mTvMaxRadius.setText(String.valueOf((int) (MAX_RADIUS/ONE_MILE)));
			mTvMinRadius.setText(String.valueOf((int) (MIN_RADIUS/ONE_MILE)));
		}
		mDBSeekbar.setProgress(process, true);
		builder.setView(mLinearLayout);
		builder.setPositiveButton(R.string.title_ok, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public interface OnSearchListener {
		public void onSearch(HomeSearchObject mHomeSearchObject);
	}

}
