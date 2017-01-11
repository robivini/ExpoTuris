package com.app.myplaces.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.app.myplaces.adapter.FavoritePlaceAdapter;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.location.DBLastLocationFinder;
import com.app.myplaces.object.PlaceObject;
import com.ypyproductions.dialog.utils.AlertDialogUtils;
import com.ypyproductions.dialog.utils.AlertDialogUtils.IOnDialogListener;
import com.ypyproductions.location.utils.LocationUtils;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.app.myplaces.DetailLocationAcitivity;
import com.app.myplaces.MainActivity;
import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;

/**
 * 
 * FragmentSettings.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.fragment
 */
public class FragmentFavorites extends Fragment implements IWhereMyLocationConstants , DBLastLocationFinder.ILastLocationFinder {

	public static final String TAG = FragmentFavorites.class.getSimpleName();

	private View mRootView;
	private MainActivity mContext;
	private boolean isFindView;
	private ListView mListViewFavorites;

	private TotalDataManager mTotalMng;
	private TextView mTvNoResult;

	private ArrayList<PlaceObject> mListPlaceObjects;

	private boolean isStartFindingLocation;
	private DBLastLocationFinder mDBDbLastLocationFinder;
	private int currentTimeOut;
	private boolean isFinishFinding;

	private PlaceObject mCurrentPlaceObject;
	private int mCurrentIndex;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_favorites, container, false);
		return mRootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!isFindView) {
			isFindView = true;
			this.findView();
		}
	}

	private void findView() {
		this.mContext = (MainActivity) getActivity();
		this.mListViewFavorites = (ListView) mRootView.findViewById(R.id.list_favorites);
		this.mTvNoResult = (TextView) mRootView.findViewById(R.id.tv_no_result);

		this.mTotalMng = TotalDataManager.getInstance();
		mListPlaceObjects = mTotalMng.getListFavoriteObjects();
		if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
			this.mTvNoResult.setVisibility(View.GONE);
			final FavoritePlaceAdapter mPlaceAdapter = new FavoritePlaceAdapter(mContext, mListPlaceObjects, mContext.mTypeFaceRobotoBold, mContext.mTypeFaceRobotoLight,
					mContext.mImgOptions);
			mListViewFavorites.setAdapter(mPlaceAdapter);
			mPlaceAdapter.setOnFavorieteAdapterListener(new FavoritePlaceAdapter.OnFavoriteAdapterListener() {
				
				@Override
				public void onDeletePlace(PlaceObject mPlaceObject) {
					TotalDataManager.getInstance().removeFavoritePlace(mContext, mPlaceObject);
					mPlaceAdapter.notifyDataSetChanged();
					if(mListPlaceObjects==null ||mListPlaceObjects.size()==0){
						mTvNoResult.setVisibility(View.VISIBLE);
					}
					else{
						mTvNoResult.setVisibility(View.GONE);
					}
				}
			});
			mListViewFavorites.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					startViewDetail(position, mListPlaceObjects.get(position));
				}
			});
		}
		else {
			this.mTvNoResult.setVisibility(View.VISIBLE);
		}
	}

	public void deleteAllFavorite() {
		if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
			AlertDialogUtils.createFullDialog(mContext, android.R.drawable.ic_dialog_alert, R.string.title_warning, R.string.title_ok, R.string.title_cancel,
					R.string.info_delete_favorites, new IOnDialogListener() {

						@Override
						public void onClickButtonPositive() {
							mListViewFavorites.setAdapter(null);
							mTotalMng.setListFavoriteObjects(new ArrayList<PlaceObject>());
							mTvNoResult.setVisibility(View.VISIBLE);
							DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
								@Override
								public void onAction() {
									mTotalMng.saveFavoritePlaces(mContext);
								}
							});
						}

						@Override
						public void onClickButtonNegative() {

						}
					}).show();

		}
		else{
			mContext.showToast(R.string.info_no_delete_favorites);
		}
	}

	@Override
	public void onLocationError(Location mLocation) {
		if(currentTimeOut<TIME_OUT){
			startGetLocation();
		}
		else{
			currentTimeOut=0;
			finishFindLocation(mLocation);
		}
	}



	@Override
	public void onLocationSuccess(Location mLocation) {
		currentTimeOut=0;
		finishFindLocation(mLocation);
	}

	@Override
	public void onError() {
		if(currentTimeOut<TIME_OUT){
			startGetLocation();
		}
		else{
			currentTimeOut=0;
			Location mLocation = mDBDbLastLocationFinder.getLastConfigLocation();
			if(mLocation!=null){
				finishFindLocation(mLocation);
			}
			else{
				mContext.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						isStartFindingLocation=false;
						mContext.dimissProgressDialog();
						mContext.showToast(R.string.info_get_location_error);
						if(mDBDbLastLocationFinder!=null){
							mDBDbLastLocationFinder.stopGps();
						}
					}
				});
			}
		}
	}
	public void startGetLocation() {
		if (currentTimeOut < TIME_OUT) {
			currentTimeOut = currentTimeOut + 1;
			mDBDbLastLocationFinder.startGps();
		}
	}
	
	private synchronized void finishFindLocation(Location mLocation) {
		DBLog.d(TAG, "=================>isFinishFinding="+isFinishFinding);
		if(!isFinishFinding){
			isFinishFinding=true;
			TotalDataManager.getInstance().setCurrentLocation(mLocation);
			mContext.dimissProgressDialog();
			if(mCurrentIndex>=0 && mCurrentPlaceObject!=null){
				startViewDetail(mCurrentIndex, mCurrentPlaceObject);
			}
		}
	}
	
	private void startViewDetail(int index,PlaceObject mPlaceObject){
		Location mCurrentLocation = TotalDataManager.getInstance().getCurrentLocation();
		if(mCurrentLocation!=null){
			float distance = LocationUtils.calculateDistance(mCurrentLocation, mPlaceObject.getLocation())/1000f;
			mPlaceObject.setDistance(distance);
			
			Intent mIt = new Intent(mContext, DetailLocationAcitivity.class);
			mIt.putExtra(DetailLocationAcitivity.KEY_INDEX_LOCATION, index);
			mIt.putExtra(DetailLocationAcitivity.KEY_START_FROM, START_FROM_MAIN);
			DirectionUtils.changeActivity(mContext, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIt);
//			startActivity(mIt);
//			mContext.finish();
		}
		else{
			startProcessFinderLocation(index, mPlaceObject);
		}
	}
	
	private void startProcessFinderLocation(int index,PlaceObject mPlaceObject){
		if(!isStartFindingLocation){
			isStartFindingLocation=true;
			try {
				this.mCurrentPlaceObject=mPlaceObject;
				this.mCurrentIndex=index;
				this.mContext.showProgressDialog(R.string.info_process_find_location);
				
				if(mDBDbLastLocationFinder!=null){
					mDBDbLastLocationFinder.stopGps();
					mDBDbLastLocationFinder =null;
				}
				mDBDbLastLocationFinder = new DBLastLocationFinder(mContext);
				mDBDbLastLocationFinder.setOnLocationFinderListener(this);
				mDBDbLastLocationFinder.startGps();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		try {
			if(mDBDbLastLocationFinder!=null){
				mDBDbLastLocationFinder.stopGps();
				mDBDbLastLocationFinder=null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

}
