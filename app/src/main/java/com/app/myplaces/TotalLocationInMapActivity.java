package com.app.myplaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.fragment.FragmentTotalLocation;
import com.app.myplaces.object.HomeSearchObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.ypyproductions.utils.DirectionUtils;
import com.ypyproductions.utils.StringUtils;

import java.util.Locale;

/**
 * TabListingsActivity
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :May 5, 2013
 * @project :IOnAuctions
 * @Package :com.auction.ionauctions
 */

public class TotalLocationInMapActivity extends DBFragmentActivity implements IWhereMyLocationConstants,PopupMenu.OnMenuItemClickListener {

	public static final String TAG = TotalLocationInMapActivity.class.getSimpleName();
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	
	private CharSequence mTitle;
	
	public int mResIcon= R.drawable.ic_launcher;
	private AdView adView;
	
	public DisplayImageOptions mImgOptions;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.activity_all_location);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setHomeButtonEnabled(false);
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_action_previous_item, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View view) {
			}

			@Override
			public void onDrawerOpened(View drawerView) {
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		this.mImgOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.icon_location_default)
		.resetViewBeforeLoading(false)
		.cacheInMemory(true).cacheOnDisk(true)
		.considerExifParams(true).build();
		
		this.setUpTitle();
		this.setUpLayoutAdmob();
	}
	
	private void setUpTitle(){
		HomeSearchObject mHomeSearchObject = TotalDataManager.getInstance().getHomeSearchSelected();
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
		mResIcon = TotalDataManager.getInstance().getResIconMapPin(this, mHomeSearchObject.getImg());
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToHome();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void backToHome() {
		Intent mIntent = new Intent(this, MainSearchActivity.class);
		DirectionUtils.changeActivity(this, R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, true, mIntent);
	}
	
	public void goToDetail(int indexLocation){
		Intent mIntent = new Intent(this, DetailLocationAcitivity.class);
		mIntent.putExtra(DetailLocationAcitivity.KEY_INDEX_LOCATION, indexLocation);
		mIntent.putExtra(KEY_START_FROM, START_FROM_TOTAL_PLACE);
		startActivity(mIntent);
	}
	
	private void setUpLayoutAdmob() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_ad);
		boolean b=SHOW_ADVERTISEMENT;
		if(SHOW_ADVERTISEMENT){
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_my_location, menu);
		return true;
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
	protected void onDestroy() {
		super.onDestroy();
		if (adView != null) {
			adView.destroy();
		}
		mImgOptions=null;
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
		FragmentTotalLocation mFragmentTotalLocation = (FragmentTotalLocation)getSupportFragmentManager().findFragmentById(R.id.fragment_location_home);
		mFragmentTotalLocation.setMapType(viewMode, mStrName);
	}
	

}
