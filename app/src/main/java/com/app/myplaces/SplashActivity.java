package com.app.myplaces;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.view.NewIOUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ypyproductions.net.task.DBTask;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.net.task.IDBTaskListener;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.JsonParsingUtils;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.KeywordObject;
import com.app.myplaces.provider.MySuggestionDAO;
import com.app.myplaces.settings.SettingManager;

/**
 * SplashActivity
 * @author  :DOBAO
 * @Email   :dotrungbao@gmail.com
 * @Skype   :baopfiev_k50
 * @Phone   :+84983028786
 * @Date    :May 5, 2013
 * @project :IOnAuctions
 * @Package :com.auction.ionauctions
 */

public class SplashActivity extends DBFragmentActivity implements IDBTaskListener, IWhereMyLocationConstants {
	
	public static final String TAG=SplashActivity.class.getSimpleName();
	public static final int PLAY_SERVICES_RESOLUTION_REQUEST =1000;
	
	private ProgressBar mProgressBar;
	private boolean isPressBack;
	private DBTask mDBTask;

	private Handler mHandler = new Handler();
	private ArrayList<HomeSearchObject> mListHomeObjects;
	private TextView mTvCopyright;
	private ArrayList<KeywordObject> mListKeywordObjects;

	private TextView mTvStatus;

	private boolean isShowingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.splash);
		this.mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		this.mTvCopyright =(TextView) findViewById(R.id.tv_copyright);
		this.mTvStatus = (TextView) findViewById(R.id.tv_status);
		
		this.mTvStatus.setTypeface(mTypeFaceRobotoItalic);
		this.mTvCopyright.setTypeface(mTypeFaceRobotoMedium);
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).memoryCacheExtraOptions(240, 240).diskCacheExtraOptions(240, 240, null)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 50 Mb
				.tasksProcessingOrder(QueueProcessingType.FIFO).writeDebugLogs().build();
		ImageLoader.getInstance().init(config);

		
		DBLog.setDebug(DEBUG);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!isShowingDialog){
			isShowingDialog=true;
			if(!ApplicationUtils.isOnline(this)){
				showDialogTurnOnInternet(new IDBCallback() {
					@Override
					public void onAction() {
						isShowingDialog=false;
					}
				});
			}
			else{
				checkTurnOnGps(new IDBCallback() {
					@Override
					public void onAction() {
						mProgressBar.setVisibility(View.VISIBLE);
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								registerBroadCast();
								mDBTask = new DBTask(SplashActivity.this);
								mDBTask.execute();
							}
						}, 1000);
					}
				});
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(isPressBack){
				TotalDataManager.getInstance().onDestroy();
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPreExcute() {
		this.mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void onDoInBackground() {
		String dataHome = NewIOUtils.readStringFromAssets(this, "homedata.dat");
		String dataKeyword =NewIOUtils.readStringFromAssets(this, "types_search.dat");
		
		mListHomeObjects = JsonParsingUtils.parsingListHomeObjects(dataHome);
		if(mListHomeObjects!=null && mListHomeObjects.size()>0){
			HomeSearchObject mHomeSearchObject = new HomeSearchObject(TYPE_SEARCH_BY_TYPES,
					getString(R.string.title_custom_search), "", "");
			mListHomeObjects.add(0,mHomeSearchObject);
			TotalDataManager.getInstance().setListHomeSearchObjects(mListHomeObjects);
			if(mListHomeObjects.size()>=2){
				mListHomeObjects.get(1).setSelected(true);
			}
		}
		mListKeywordObjects = JsonParsingUtils.parsingListKeywordObjects(dataKeyword);
		if(mListKeywordObjects!=null && mListKeywordObjects.size()>0){
			TotalDataManager.getInstance().setListKeywordObjects(mListKeywordObjects);
			if(!SettingManager.isInitProvider(this)){
				for(KeywordObject mKeywordObject:mListKeywordObjects){
					MySuggestionDAO.insertData(this, mKeywordObject);
				}
				SettingManager.setInitProvider(this, true);
			}
		}
		
		File mCacheFile = NewIOUtils.getDiskCacheDir(this, DIR_DATA);
		if(!mCacheFile.exists()){
			mCacheFile.mkdirs();
		}
		String dataFavorite = NewIOUtils.readLogFile(this, mCacheFile.getAbsolutePath(), FILE_FAVORITE_PLACES);
		ArrayList<PlaceObject> listFavorites = JsonParsingUtils.parsingListFavoriteObjects(dataFavorite);
		TotalDataManager.getInstance().setListFavoriteObjects(listFavorites);
		
		if (mListHomeObjects != null && mListHomeObjects.size() > 0 && isTurnOnGps()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTvStatus.setText(R.string.info_process_find_location);
					TotalDataManager.getInstance().onRegisterTrackingService(getApplicationContext());
				}
			});
		}
	}
	
	@Override
	public void onPostExcute() {
		if (mListHomeObjects == null || mListHomeObjects.size() == 0) {
			this.mProgressBar.setVisibility(View.INVISIBLE);
			isPressBack=true;
			showToast(R.string.info_parse_error);
			return;
		}
	}
	@Override
	public void processWithNewLocation() {
		super.processWithNewLocation();
		goToMain();
	}
	private void goToMain() {
        isAllowDestroy=false;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProgressBar.setVisibility(View.INVISIBLE);
				Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
				mIntent.putExtra(KEY_START_FROM, START_FROM_SPLASH);
				DirectionUtils.changeActivity(SplashActivity.this, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIntent);
			}
		});
	}

	
	private void checkTurnOnGps(final IDBCallback mDBCallback) {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status == ConnectionResult.SUCCESS) {
			final LocationManager manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
			boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!isGpsProvider && !isNetworkProvider) {
				showDialogTurnOnLocationService(new IDBCallback() {

					@Override
					public void onAction() {
						isShowingDialog = false;
					}
				}, new IDBCallback() {

					@Override
					public void onAction() {
						onDestroyData();
						finish();
					}
				});
				return;
			}
			if (mDBCallback != null) {
				mDBCallback.onAction();
			}
		}
		else {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				GooglePlayServicesUtil.getErrorDialog(status, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else {
				DBLog.i(TAG, "This device is not supported.");
				finish();
			}
		}
	}
}
