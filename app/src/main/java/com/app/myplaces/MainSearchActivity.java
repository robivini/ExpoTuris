package com.app.myplaces;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.TextView;

import com.app.myplaces.adapter.DrawerSearchAdapter;
import com.app.myplaces.adapter.PlaceAdapter;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.dataMng.YPYNetUtils;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.KeywordObject;
import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.object.ResponcePlaceResult;
import com.app.myplaces.provider.MySuggestionDAO;
import com.app.myplaces.settings.SettingManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.ypyproductions.location.utils.LocationUtils;
import com.ypyproductions.net.task.DBTask;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.net.task.IDBTaskListener;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.ypyproductions.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * 
 * MainSearchActivity.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation
 */
public class MainSearchActivity extends DBFragmentActivity implements PopupMenu.OnMenuItemClickListener, OnScrollListener {

	public static final String TAG = MainSearchActivity.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private ListView mLocationListView;

	private CharSequence mTitle;

	private DrawerSearchAdapter mDrawerAdapter;

	private ArrayList<HomeSearchObject> mListHomeObjects;

	private DBTask mDBTask;

	private TotalDataManager mTotalMng;
	private Handler mHandler = new Handler();

	private boolean isAllowRefresh;
	private Location mCurrentLocation;

	private PlaceAdapter mPlaceAdapter;

	private TextView mTvResult;
	private boolean isAllowDestroyAll = true;

	private AdView adView;

	private boolean isAllowAddPage = false;
	private boolean isStartAddingPage;

	private View mFooterView;

	public DisplayImageOptions mImgOptions;

	private MenuItem menuSearchItem;

	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_location);

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);
		mLocationListView = (ListView) findViewById(R.id.list_detail_search);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mTvResult = (TextView) findViewById(R.id.tv_no_result);
		
		this.mFooterView = findViewById(R.id.layout_footer);

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

		mTotalMng = TotalDataManager.getInstance();
		mCurrentLocation = mTotalMng.getCurrentLocation();

		this.setUpDrawer();
		this.setUpLayoutAdmob();
		this.handleIntent(getIntent());
		
		registerBroadCast();
		startFind();
		isAllowRefresh = true;
		
	}

	private void setUpLayoutAdmob() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_ad);
		boolean b= IWhereMyLocationConstants.SHOW_ADVERTISEMENT;
		if(b){
			adView = new AdView(this);
			adView.setAdUnitId(IWhereMyLocationConstants.ADMOB_ID_BANNER);
			adView.setAdSize(AdSize.SMART_BANNER);

			layout.addView(adView);
			AdRequest mAdRequest = new AdRequest.Builder().build();
			adView.loadAd(mAdRequest);
		}
		else{
			layout.setVisibility(View.GONE);
			LayoutParams mCurrentParams = (RelativeLayout.LayoutParams)mFooterView.getLayoutParams();
			
			RelativeLayout.LayoutParams mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			mLayoutParams.leftMargin=mCurrentParams.leftMargin;
			mLayoutParams.rightMargin=mCurrentParams.rightMargin;
			mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			mFooterView.setLayoutParams(mLayoutParams);
		}
	}

	private void showFooterView() {
		if(mFooterView.getVisibility()!=View.VISIBLE){
			this.mFooterView.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideFooterView(){
		if(mFooterView.getVisibility()==View.VISIBLE){
			this.mFooterView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!ApplicationUtils.isOnline(this)) {
			showDialogFragment(DIALOG_LOSE_CONNECTION);
			return;
		}
	}

	private synchronized void startFind() {
		if (mCurrentLocation != null) {
			mTvResult.setVisibility(View.GONE);
			final HomeSearchObject mHomeSearchObject = mTotalMng.getHomeSearchSelected();
			if (mHomeSearchObject != null) {
				ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
				if (mResponcePlaceResult != null) {
					ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();
					if (mListPlaceObjects == null) {
						mListPlaceObjects = new ArrayList<PlaceObject>();
					}
					updateOrCreatePlaceAdapter(mResponcePlaceResult,mListPlaceObjects);
					return;
				}
				if(mPlaceAdapter!=null){
					mLocationListView.post(new Runnable() {
						@Override
						public void run() {
							mLocationListView.setSelection(0);
							hideFooterView();
						}
					});
				}
				mDBTask = new DBTask(new IDBTaskListener() {
					private boolean isSuccess;

					@Override
					public void onPreExcute() {
						String name = mHomeSearchObject.getName();
						if(name.equals(getString(R.string.title_custom_search))){
							name= mHomeSearchObject.getRealName();
							if(StringUtils.isStringEmpty(name)){
								name= mHomeSearchObject.getKeyword().toUpperCase(Locale.US).replaceAll("\\_+", " ");
							}
						}
						else{
							name=mHomeSearchObject.getRealName();
						}
						String message = String.format(getString(R.string.info_format_process_find_location), name);
						showProgressDialog(message);
					}

					@Override
					public void onDoInBackground() {
						ResponcePlaceResult mResponcePlaceResult = null;
						if (mHomeSearchObject.getType() == IWhereMyLocationConstants.TYPE_SEARCH_BY_TYPES) {
							mResponcePlaceResult = YPYNetUtils.getListPlacesBaseOnType(MainSearchActivity.this, mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(),
									mHomeSearchObject.getKeyword());
						}
						else {
							mResponcePlaceResult = YPYNetUtils.getListPlacesBaseOnText(MainSearchActivity.this, mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(),
									mHomeSearchObject.getKeyword());
						}
						if (mResponcePlaceResult != null) {
							String status = mResponcePlaceResult.getStatus();
							DBLog.d(TAG, "==============>status=" + status);
							if (!StringUtils.isStringEmpty(status)) {
								mHomeSearchObject.setResponcePlaceResult(mResponcePlaceResult);
								ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();
								if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
									for (PlaceObject mPlaceObject : mListPlaceObjects) {
										float distance = LocationUtils.calculateDistance(mCurrentLocation, mPlaceObject.getLocation()) / 1000f;
										mPlaceObject.setDistance(distance);
										mPlaceObject.setCategory(mHomeSearchObject.getImg());
									}
									String typeSorting = SettingManager.getPiority(MainSearchActivity.this);
									if (typeSorting.equals(IWhereMyLocationConstants.PIORITY_DISTANCE)) {
										sortingBy(mListPlaceObjects, IWhereMyLocationConstants.PIORITY_DISTANCE);
									}
									else if (typeSorting.equals(IWhereMyLocationConstants.PIORITY_RATING)) {
										sortingBy(mListPlaceObjects, IWhereMyLocationConstants.PIORITY_RATING);
									}
								}
								isSuccess = true;
							}
						}
					}

					@Override
					public void onPostExcute() {
						dimissProgressDialog();
						if (!isSuccess) {
							showToast(R.string.info_server_error);
							isAllowAddPage = false;
						}
						else {
							ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
							ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();
							if (mListPlaceObjects == null) {
								mListPlaceObjects = new ArrayList<PlaceObject>();
							}
							updateOrCreatePlaceAdapter(mResponcePlaceResult,mListPlaceObjects);
						}
					}
				});
				mDBTask.execute();
			}
		}
		else {
			showProgressDialog(R.string.info_process_find_location);
		}
	}

	private void updateOrCreatePlaceAdapter(ResponcePlaceResult mResponcePlaceResult,final ArrayList<PlaceObject> mListPlaceObjects) {
		if (mListPlaceObjects != null) {
			if (mListPlaceObjects.size() == 0) {
				mTvResult.setVisibility(View.VISIBLE);
			}
			if(!StringUtils.isStringEmpty(mResponcePlaceResult.getPageToken())){
				isAllowAddPage = true;
				this.mLocationListView.setOnScrollListener(this);
			}
			else{
				isAllowAddPage = false;
				this.mLocationListView.setOnScrollListener(null);
				hideFooterView();
			}
			if (mPlaceAdapter == null) {
				mPlaceAdapter = new PlaceAdapter(MainSearchActivity.this, mListPlaceObjects, mTypeFaceRobotoBold, mTypeFaceRobotoLight, mImgOptions);
				mLocationListView.setAdapter(mPlaceAdapter);
				mLocationListView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
						if(isStartAddingPage){
							showToast(R.string.info_loading_more);
							return;
						}
						isAllowDestroyAll = false;
						Intent mIntent = new Intent(MainSearchActivity.this, DetailLocationAcitivity.class);
						mIntent.putExtra(DetailLocationAcitivity.KEY_INDEX_LOCATION, position);
						mIntent.putExtra(IWhereMyLocationConstants.KEY_START_FROM, IWhereMyLocationConstants.START_FROM_SEARCH);
						DirectionUtils.changeActivity(MainSearchActivity.this, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIntent);
					}
				});
			}
			else {
				mPlaceAdapter.setListPlaceObjects(mListPlaceObjects);
				mLocationListView.post(new Runnable() {
					@Override
					public void run() {
						mLocationListView.setSelection(0);
					}
				});
			}
			return;
		}
	}

	private void setUpDrawer() {
		mTotalMng = TotalDataManager.getInstance();
		mListHomeObjects = mTotalMng.getListHomeSearchObjects();
		if (mListHomeObjects != null && mListHomeObjects.size() > 0) {
			int size = mListHomeObjects.size();
			final ArrayList<HomeSearchObject> listHomeSearchObjects = new ArrayList<HomeSearchObject>();
			for(int i=0;i<size;i++){
				if(i!=0){
					listHomeSearchObjects.add(mListHomeObjects.get(i));
				}
			}
			mDrawerAdapter = new DrawerSearchAdapter(this, listHomeSearchObjects, mTypeFaceRobotoBold, mTypeFaceRobotoLight);
			mDrawerListView.setAdapter(mDrawerAdapter);
			mDrawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
					if(isStartAddingPage){
						showToast(R.string.info_loading_more);
						mDrawerLayout.closeDrawer(mDrawerListView);
						return;
					}
					setTitle(listHomeSearchObjects.get(position).getName());
					mDrawerAdapter.setSelected(position);
					mDrawerLayout.closeDrawer(mDrawerListView);
					DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
						@Override
						public void onAction() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									startFind();
								}
							});
						}
					});
				}
			});
			HomeSearchObject mHomeSearchObject = mTotalMng.getHomeSearchSelected();
			if (mHomeSearchObject != null) {
				String name = mHomeSearchObject.getName();
				if(name.equals(getString(R.string.title_custom_search))){
					name= mHomeSearchObject.getRealName();
					if(StringUtils.isStringEmpty(name)){
						name= mHomeSearchObject.getKeyword().toUpperCase(Locale.US).replaceAll("\\_+", " ");
					}
				}
				setTitle(name);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main_search, menu);
		
		menuSearchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) menuSearchItem.getActionView();
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.title_search) + "</font>"));

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
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
			if(isStartAddingPage){
				showToast(R.string.info_loading_more);
				return true;
			}
			showMenu(R.id.action_menu, R.menu.menu_sort_by,this);
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
		super.onDestroy();
		if (adView != null) {
			adView.destroy();
		}
		if(mDrawerAdapter!=null){
			mDrawerAdapter.onDestroy();
		}
		if(mDBTask!=null){
			mDBTask.cancel(true);
		}
		DBListExcuteAction.getInstance().onDestroy();
		mImgOptions=null;
		mHandler.removeCallbacksAndMessages(null);
		if (isAllowDestroyAll) {
			mCurrentLocation = null;
			mTotalMng.onResetResultSearch(true);
		}
		unRegisterBroadCast();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent mIntent = new Intent(this, MainActivity.class);
			mIntent.putExtra(IWhereMyLocationConstants.KEY_START_FROM, IWhereMyLocationConstants.START_FROM_SEARCH);
			DirectionUtils.changeActivity(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right, true, mIntent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void processWithNewLocation() {
		super.processWithNewLocation();
		if (isAllowRefresh) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTotalMng.setCurrentLocation(mCurrentLocation);
					mTotalMng.onResetResultSearch(false);
					startFind();
				}
			});
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sort_by_distance:
			if (mPlaceAdapter != null) {
				ArrayList<PlaceObject> mListPlaceObjects = mPlaceAdapter.getListPlaceObjects();
				if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
					sortingBy(mListPlaceObjects, IWhereMyLocationConstants.PIORITY_DISTANCE);
					mPlaceAdapter.notifyDataSetChanged();
					SettingManager.setPiority(this, IWhereMyLocationConstants.PIORITY_DISTANCE);
				}
			}
			return true;
		case R.id.sort_by_rating:
			if (mPlaceAdapter != null) {
				ArrayList<PlaceObject> mListPlaceObjects = mPlaceAdapter.getListPlaceObjects();
				if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
					sortingBy(mListPlaceObjects, IWhereMyLocationConstants.PIORITY_RATING);
					mPlaceAdapter.notifyDataSetChanged();
					SettingManager.setPiority(this, IWhereMyLocationConstants.PIORITY_RATING);
				}
			}
			return true;
		case R.id.action_map:
			if(isStartAddingPage){
				showToast(R.string.info_loading_more);
				return true;
			}
			final HomeSearchObject mHomeSearchObject = mTotalMng.getHomeSearchSelected();
			if (mHomeSearchObject != null) {
				ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
				if (mResponcePlaceResult == null || mResponcePlaceResult.getListPlaceObjects() == null || mResponcePlaceResult.getListPlaceObjects().size() == 0) {
					showToast(R.string.info_no_location);
					return super.onOptionsItemSelected(item);
				}
			}
			isAllowDestroyAll = false;
			Intent mIntent = new Intent(this, TotalLocationInMapActivity.class);
			DirectionUtils.changeActivity(this, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top, true, mIntent);
			return true;
		case R.id.action_refresh:
			if(isStartAddingPage){
				showToast(R.string.info_loading_more);
				return true;
			}
			isAllowAddPage=false;
			isStartAddingPage=false;
			final HomeSearchObject mHomeSearchObject1 = mTotalMng.getHomeSearchSelected();
			if (mHomeSearchObject1 != null) {
				mHomeSearchObject1.setResponcePlaceResult(null);
				startFind();
			}
			return true;
		default:
			return false;
		}
	}

	private void sortingBy(ArrayList<PlaceObject> mListPlaceObjects, final String type) {
		if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
			Collections.sort(mListPlaceObjects, new Comparator<PlaceObject>() {
				@Override
				public int compare(PlaceObject lhs, PlaceObject rhs) {
					try {
						if (type.equals(IWhereMyLocationConstants.PIORITY_RATING)) {
							float dis1 = lhs.getRating();
							float dis2 = rhs.getRating();
							if (dis1 < dis2) {
								return 1;
							}
							else {
								return -1;
							}
						}
						else if (type.equals(IWhereMyLocationConstants.PIORITY_DISTANCE)) {
							float dis1 = lhs.getDistance();
							float dis2 = rhs.getDistance();
							if (dis1 < dis2) {
								return -1;
							}
							else {
								return 1;
							}
						}

					}
					catch (Exception e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mPlaceAdapter != null) {
			if (isAllowAddPage) {
				int size = mPlaceAdapter.getCount();
				if (mLocationListView.getLastVisiblePosition() == size - 1) {
					if (ApplicationUtils.isOnline(this)) {
						showFooterView();
						if (!isStartAddingPage) {
							isStartAddingPage = true;
							onLoadNextPlaceObject();
						}
					}
				}
				else{
					if(!isStartAddingPage){
						hideFooterView();
					}
				}
			}
			else {
				hideFooterView();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	private void onLoadNextPlaceObject() {
		final HomeSearchObject mHomeSearchObject = mTotalMng.getHomeSearchSelected();
		if (mHomeSearchObject != null) {
			final ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
			if (mResponcePlaceResult != null) {
				final ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();
				if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
					mDBTask = new DBTask(new IDBTaskListener() {
						private boolean isSuccess;
						private ResponcePlaceResult mNewResponcePlaceResult;

						@Override
						public void onPreExcute() {
						}

						@Override
						public void onDoInBackground() {
							if (mHomeSearchObject.getType() == IWhereMyLocationConstants.TYPE_SEARCH_BY_TYPES) {
								mNewResponcePlaceResult = YPYNetUtils.getListNextPlacesBaseOnType(MainSearchActivity.this, mCurrentLocation.getLongitude(),
										mCurrentLocation.getLatitude(), mHomeSearchObject.getKeyword(), mResponcePlaceResult.getPageToken());
							}
							else {
								mNewResponcePlaceResult = YPYNetUtils.getListNextPlacesBaseOnText(MainSearchActivity.this, mCurrentLocation.getLongitude(),
										mCurrentLocation.getLatitude(), mHomeSearchObject.getKeyword(), mResponcePlaceResult.getPageToken());
							}
							if (mResponcePlaceResult != null) {
								String status = mResponcePlaceResult.getStatus();
								DBLog.d(TAG, "==============>status=" + status);
								if (!StringUtils.isStringEmpty(status) && mNewResponcePlaceResult!=null) {
									ArrayList<PlaceObject> mListNewPlaceObjects = mNewResponcePlaceResult.getListPlaceObjects();
									if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
										mResponcePlaceResult.setPageToken(mNewResponcePlaceResult.getPageToken());
										for (PlaceObject mPlaceObject : mListNewPlaceObjects) {
											float distance = LocationUtils.calculateDistance(mCurrentLocation, mPlaceObject.getLocation()) / 1000f;
											mPlaceObject.setDistance(distance);
											mPlaceObject.setCategory(mHomeSearchObject.getImg());
										}
										String typeSorting = SettingManager.getPiority(MainSearchActivity.this);
										if (typeSorting.equals(IWhereMyLocationConstants.PIORITY_DISTANCE)) {
											sortingBy(mListNewPlaceObjects, IWhereMyLocationConstants.PIORITY_DISTANCE);
										}
										else if (typeSorting.equals(IWhereMyLocationConstants.PIORITY_RATING)) {
											sortingBy(mListNewPlaceObjects, IWhereMyLocationConstants.PIORITY_RATING);
										}
										for (PlaceObject mPlaceObject : mListNewPlaceObjects) {
											mListPlaceObjects.add(mPlaceObject);
										}
										mListNewPlaceObjects.clear();
									}
									isSuccess = true;
								}
							}
						}

						@Override
						public void onPostExcute() {
							if (!isSuccess) {
								showToast(R.string.info_server_error);
								isStartAddingPage = false;
							}
							else {
								if (mPlaceAdapter != null) {
									mPlaceAdapter.notifyDataSetChanged();
								}
								isStartAddingPage = false;
								isAllowAddPage=false;
								if(mNewResponcePlaceResult!=null){
									if(!StringUtils.isStringEmpty(mNewResponcePlaceResult.getPageToken())){
										isAllowAddPage=true;
										mLocationListView.setOnScrollListener(MainSearchActivity.this);
									}
									else{
										mLocationListView.setOnScrollListener(null);
										hideFooterView();
									}
								}
							}
						}
					});
					mDBTask.execute();
				}
				else {
					isAllowAddPage = false;
				}
			}
		}

	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			DBLog.d(TAG, "===============>ACTION_SEARCH =" + query);
			processSearchData(IWhereMyLocationConstants.TYPE_SEARCH_BY_TEXT, query,query,true);
		}
		else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri mQueryUri = intent.getData();
			if(mQueryUri!=null){
				DBLog.d(TAG, "===============>mQueryUri=" + mQueryUri.toString());
				String keyword = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
				KeywordObject mKeywordObject = TotalDataManager.getInstance().getKeyWordObject(keyword);
				String realName="";
				if(mKeywordObject!=null){
					realName=mKeywordObject.getName();
				}
				processSearchData(IWhereMyLocationConstants.TYPE_SEARCH_BY_TYPES, keyword,realName, false);
			}
		}
	}
	
	private void processSearchData(int type,String query,String realname,boolean isAllowAddRecent){
		HomeSearchObject mHomeSearchObject = TotalDataManager.getInstance().findHomeSearchObject(query);
		if(mHomeSearchObject!=null){
			TotalDataManager.getInstance().setSelectedObject(mHomeSearchObject);
			if(mDrawerAdapter!=null){
				mDrawerAdapter.notifyDataSetChanged();
			}
		}
		else{
			TotalDataManager.getInstance().setSelectedObject(0);
			mHomeSearchObject = TotalDataManager.getInstance().getListHomeSearchObjects().get(0);
			if(mHomeSearchObject!=null){
				mHomeSearchObject.setResponcePlaceResult(null);
				mHomeSearchObject.setKeyword(query);
				mHomeSearchObject.setType(type);
				mHomeSearchObject.setRealName(realname);
				if(isAllowAddRecent){
					KeywordObject mKeywordObject = MySuggestionDAO.getPrivateData(this, query);
					DBLog.d(TAG, "==============>mKeywordObject="+mKeywordObject);
					if(mKeywordObject==null){
						KeywordObject mKeywordObject2 = new KeywordObject(query, query);
						MySuggestionDAO.insertData(this, mKeywordObject2);
					}
				}
			}
		}
		String name = mHomeSearchObject.getName();
		if(name.equals(getString(R.string.title_custom_search))){
			name= mHomeSearchObject.getRealName();
			if(StringUtils.isStringEmpty(name)){
				name= mHomeSearchObject.getKeyword().toUpperCase(Locale.US).replaceAll("\\_+", " ");
			}
		}
		setTitle(name);
		startFind();
	}

}
