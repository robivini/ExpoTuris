package com.app.myplaces;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.location.TrackRecordingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ypyproductions.dialog.utils.AlertDialogUtils;
import com.ypyproductions.dialog.utils.AlertDialogUtils.IOnDialogListener;
import com.ypyproductions.dialog.utils.IDialogFragmentListener;
import com.ypyproductions.location.utils.GoogleLocationUtils;
import com.ypyproductions.location.utils.LocationUtils;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.net.task.IDBConstantURL;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.ResolutionUtils;
import com.ypyproductions.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


public class DBFragmentActivity extends FragmentActivity implements IDBConstantURL, IDialogFragmentListener,IWhereMyLocationConstants {
	
	public static final String TAG = DBFragmentActivity.class.getSimpleName();
	private Dialog  mProgressDialog;

	private int screenWidth;
	private int screenHeight;
	
	public TrackLocationBroadcast mTrackLocationReceiver;
	public Location mCurrentLocation;
	
	public Typeface mTypeFaceRobotoBold;
	public Typeface mTypeFaceRobotoLight;
	public Typeface mTypeFaceRobotoMedium;
	public Typeface mTypeFaceRobotoItalic;
	private InterstitialAd mInterstitial;
	public boolean isAllowDestroy=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFormat(PixelFormat.RGBA_8888);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mTypeFaceRobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
		mTypeFaceRobotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		mTypeFaceRobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		mTypeFaceRobotoItalic = Typeface.createFromAsset(getAssets(), "fonts/Roboto-LightItalic.ttf");
	
		
		this.createProgressDialog();
		
		int[] mRes=ResolutionUtils.getDeviceResolution(this);
		if(mRes!=null && mRes.length==2){
			screenWidth=mRes[0];
			screenHeight=mRes[1];
		}
	}
	
	public void showIntertestialAds() {
		boolean b=SHOW_ADVERTISEMENT;
		if(b){
			mInterstitial = new InterstitialAd(getApplicationContext());
			mInterstitial.setAdUnitId(ADMOB_ID_INTERTESTIAL);
			AdRequest adRequest = new AdRequest.Builder().build();
			mInterstitial.loadAd(adRequest);
			mInterstitial.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					mInterstitial.show();
				}
			});
		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialogFragment(DIALOG_QUIT_APPLICATION);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void showDialogFragment(int idDialog) {
		FragmentManager mFragmentManager = getSupportFragmentManager();
		switch (idDialog) {
		case DIALOG_LOSE_CONNECTION:
			createWarningDialog(DIALOG_LOSE_CONNECTION, R.string.title_warning, R.string.info_lose_internet).show(mFragmentManager, "DIALOG_LOSE_CONNECTION");
			break;
		case DIALOG_EMPTY:
			createWarningDialog(DIALOG_EMPTY, R.string.title_warning, R.string.info_empty).show(mFragmentManager, "DIALOG_EMPTY");
			break;
		case DIALOG_QUIT_APPLICATION:
			createQuitDialog().show(mFragmentManager, "DIALOG_QUIT_APPLICATION");
			break;
		case DIALOG_SEVER_ERROR:
			createWarningDialog(DIALOG_SEVER_ERROR, R.string.title_warning, R.string.info_server_error).show(mFragmentManager, "DIALOG_SEVER_ERROR");
			break;
		default:
			break;
		}
	}

	public DialogFragment createWarningDialog(int idDialog, int titleId, int messageId) {
		DBAlertFragment mDAlertFragment = DBAlertFragment.newInstance(idDialog, android.R.drawable.ic_dialog_alert, titleId, android.R.string.ok, messageId);
		return mDAlertFragment;
	}

	private DialogFragment createQuitDialog() {
		int mTitleId = R.string.title_confirm;
		int mYesId = R.string.title_yes;
		int mNoId = R.string.title_no;
		int iconId = R.drawable.ic_launcher;
		int messageId = R.string.quit_message;

		DBAlertFragment mDAlertFragment = DBAlertFragment.newInstance(DIALOG_QUIT_APPLICATION, iconId, mTitleId, mYesId, mNoId, messageId);
		return mDAlertFragment;

	}

	private void createProgressDialog() {
		this.mProgressDialog = new Dialog(this);
		this.mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mProgressDialog.setContentView(R.layout.item_progress_bar);
		this.mProgressDialog.setCancelable(false);
		this.mProgressDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
		
		
	}

	public void showProgressDialog() {
		if (mProgressDialog != null) {
			TextView mTvMessage = (TextView) mProgressDialog.findViewById(R.id.tv_message);
			mTvMessage.setText(R.string.loading);
			if(!mProgressDialog.isShowing()){
				mProgressDialog.show();
			}
		}
	}
	public void showProgressDialog(int messageId) {
		DBLog.d(TAG, "============>mProgressDialog="+mProgressDialog);
		if (mProgressDialog != null) {
			TextView mTvMessage = (TextView) mProgressDialog.findViewById(R.id.tv_message);
			mTvMessage.setText(messageId);
			if(!mProgressDialog.isShowing()){
				mProgressDialog.show();
			}
		}
	}
	public void showProgressDialog(String message) {
		if (mProgressDialog != null) {
			TextView mTvMessage = (TextView) mProgressDialog.findViewById(R.id.tv_message);
			mTvMessage.setText(message);
			if(!mProgressDialog.isShowing()){
				mProgressDialog.show();
			}
		}
	}

	public void dimissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	@Override
	public void doPositiveClick(int idDialog) {
		switch (idDialog) {
		case DIALOG_QUIT_APPLICATION:
			onDestroyData();
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void doNegativeClick(int idDialog) {

	}
	
	public void onDestroyData(){
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterBroadCast();
		if(isAllowDestroy){
			boolean isPressingHomeButton = isApplicationSentToBackground();
			DBLog.d(TAG, "=============>isRUnningBg=" + isPressingHomeButton);
			if (isPressingHomeButton) {
				ImageLoader.getInstance().stop();
				TotalDataManager.getInstance().onDestroyTrackingService(getApplicationContext());
			}
		}
	}
	
	public void showMenu(int resMenuId, int resId, PopupMenu.OnMenuItemClickListener mOnClick) {
		View mView = findViewById(resMenuId);
		PopupMenu popup = new PopupMenu(this, mView);
		try {
			Field[] fields = popup.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(popup);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(resId, popup.getMenu());
		popup.setOnMenuItemClickListener(mOnClick);
		popup.show();
	}
	
	public void registerBroadCast() {
		if (mTrackLocationReceiver == null && isTurnOnGps()) {
			try {
				mTrackLocationReceiver = new TrackLocationBroadcast();
				IntentFilter mIntentFilter = new IntentFilter();
				mIntentFilter.addAction(TrackRecordingService.ACTION_UPDATE_LOCATION);
				registerReceiver(mTrackLocationReceiver, mIntentFilter);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void unRegisterBroadCast() {
		try {
			if (mTrackLocationReceiver != null) {
				unregisterReceiver(mTrackLocationReceiver);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class TrackLocationBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent != null) {
					String action = intent.getAction();
					if (!StringUtils.isStringEmpty(action) && action.equals(TrackRecordingService.ACTION_UPDATE_LOCATION)) {
						final double mLastLocLat = intent.getDoubleExtra(TrackRecordingService.KEY_LAT, INVALID_VALUE);
						final double mLastLocLng = intent.getDoubleExtra(TrackRecordingService.KEY_LNG, INVALID_VALUE);
						mCurrentLocation=TotalDataManager.getInstance().getCurrentLocation();
						DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
							@Override
							public void onAction() {
								boolean isNeedRefresh = false;
								if (mCurrentLocation == null) {
									if (mLastLocLat != INVALID_VALUE && mLastLocLng != INVALID_VALUE) {
										mCurrentLocation = new Location(LocationManager.GPS_PROVIDER);
										mCurrentLocation.setLongitude(mLastLocLng);
										mCurrentLocation.setLatitude(mLastLocLat);
										isNeedRefresh = true;
									}
								}
								else {
									if (mLastLocLat != INVALID_VALUE && mLastLocLng != INVALID_VALUE) {
										Location mNewLocation = new Location(LocationManager.GPS_PROVIDER);
										mNewLocation.setLongitude(mLastLocLng);
										mNewLocation.setLatitude(mLastLocLat);

										float distance = LocationUtils.calculateDistance(mCurrentLocation, mNewLocation);
										if (distance > MAX_DISTANCE_TO_UPDATE) {
											isNeedRefresh = true;
										}
										mCurrentLocation = null;
										mCurrentLocation = mNewLocation;
									}
								}
								DBLog.d(TAG, "============>isNeedRefresh=" + isNeedRefresh);
								if (isNeedRefresh) {
									TotalDataManager.getInstance().setCurrentLocation(mCurrentLocation);
									processWithNewLocation();
								}
							}
						});
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void processWithNewLocation(){
		
	}
	
	public boolean isTurnOnGps() {
		final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (isGpsProvider || isNetworkProvider) {
			return true;
		}
		return false;
	}
	
	public void showDialogTurnOnLocationService(final IDBCallback mPositiveCallback,final IDBCallback mNegativeCallback) {
		String data = String.format(getString(R.string.info_location_services_disable), getString(R.string.app_name));
		Dialog mDialog = AlertDialogUtils.createFullDialog(this, 0, R.string.title_location_services_disable, R.string.title_settings, R.string.title_cancel,
				data, new IOnDialogListener() {

					@Override
					public void onClickButtonPositive() {
						if(mPositiveCallback!=null){
							mPositiveCallback.onAction();
						}
						Intent intent = GoogleLocationUtils.isAvailable(DBFragmentActivity.this) ? new Intent(GoogleLocationUtils.ACTION_GOOGLE_LOCATION_SETTINGS) : new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}

					@Override
					public void onClickButtonNegative() {
						if(mNegativeCallback!=null){
							mNegativeCallback.onAction();
						}
					}
				});
		mDialog.show();
	}
	
	public void showDialogTurnOnInternet(final IDBCallback mCallback) {
		Dialog mDialog = AlertDialogUtils.createFullDialog(this, 0, R.string.title_warning, R.string.title_settings, R.string.title_cancel, R.string.info_lose_internet,
				new IOnDialogListener() {

					@Override
					public void onClickButtonPositive() {
						if (mCallback != null) {
							mCallback.onAction();
						}
						Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}

					@Override
					public void onClickButtonNegative() {
						onDestroyData();
						finish();
					}
				});
		mDialog.show();
	}
	public void showToastWithLongTime(int resId) {
		showToastWithLongTime(getString(resId));
	}

	public void showToastWithLongTime(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	public void showToast(int resId) {
		showToast(getString(resId));
	}

	public void showToast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	public boolean isApplicationSentToBackground() {
		try {
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasks = am.getRunningTasks(1);
			if (!tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				DBLog.d(TAG, "==============>topActivity="+topActivity.getPackageName());
				if (!topActivity.getPackageName().equals(getPackageName())) {
					return true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
