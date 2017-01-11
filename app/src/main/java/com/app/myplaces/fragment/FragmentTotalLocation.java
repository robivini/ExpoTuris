package com.app.myplaces.fragment;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.myplaces.R;
import com.app.myplaces.TotalLocationInMapActivity;
import com.app.myplaces.adapter.DetailMapInfoAdapter;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.location.DBLastLocationFinder;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.object.PlacePhotoObject;
import com.app.myplaces.object.ResponcePlaceResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ypyproductions.net.task.DBTask;
import com.ypyproductions.net.task.IDBTaskListener;
import com.ypyproductions.utils.StringUtils;

import java.util.ArrayList;

/**
 * 
 * FragmentLocationHome.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Jan 12, 2014
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.fragment
 */
public class FragmentTotalLocation extends Fragment implements IWhereMyLocationConstants, OnMarkerClickListener {

	public static final String TAG = FragmentTotalLocation.class.getSimpleName();
	public static final int TIME_OUT = 20;

	private View mRootlayout;
	private boolean isFindView;

	private TotalLocationInMapActivity mContext;
	private GoogleMap mMap;

	private ArrayList<LatLng> lisLatLngs = new ArrayList<LatLng>();
	private ArrayList<MarkerOptions> listMarkerOptions = new ArrayList<MarkerOptions>();
	private DetailMapInfoAdapter mMapAdapter;

	private Address mLocationAddress;
	private LatLng mCurrentLatLng;
	private MarkerOptions mCurrentLocationMarker;
	private DBLastLocationFinder mDBLocationFinder;
	private int mCurrentMapType=GoogleMap.MAP_TYPE_NORMAL;
	private Handler mHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.mRootlayout = inflater.inflate(R.layout.fragment_all_location, container, false);
		return mRootlayout;
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
		this.mContext = (TotalLocationInMapActivity) getActivity();
		this.mDBLocationFinder = new DBLastLocationFinder(mContext);
		this.setUpMapIfNeeded();
		drawMyLocation();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detail_map)).getMap();
		}
	}

	private void setUpMap() {
		if (listMarkerOptions != null && listMarkerOptions.size() > 0) {
			listMarkerOptions.clear();
			listMarkerOptions = null;
			listMarkerOptions = new ArrayList<MarkerOptions>();
		}

		if (lisLatLngs != null && lisLatLngs.size() > 0) {
			lisLatLngs.clear();
			lisLatLngs = null;
			lisLatLngs = new ArrayList<LatLng>();
		}
		if (mMapAdapter != null) {
			mMap.setInfoWindowAdapter(null);
			mMapAdapter = null;
		}
		try {
			mMap.clear();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		mMap.getUiSettings().setZoomControlsEnabled(false);

		final HomeSearchObject mHomeSearchObject = TotalDataManager.getInstance().getHomeSearchSelected();
		if (mHomeSearchObject != null) {
			ResponcePlaceResult mResponcePlaceResult = mHomeSearchObject.getResponcePlaceResult();
			if (mResponcePlaceResult != null) {
				ArrayList<PlaceObject> mListPlaceObjects = mResponcePlaceResult.getListPlaceObjects();

				if (mListPlaceObjects != null && mListPlaceObjects.size() > 0) {
					int size = mListPlaceObjects.size();
					for (int i = 0; i < size; i++) {
						PlaceObject mPlaceObject = mListPlaceObjects.get(i);
						LatLng mLatLng = new LatLng(mPlaceObject.getLocation().getLatitude(), mPlaceObject.getLocation().getLongitude());
						lisLatLngs.add(mLatLng);

						String urlPhoto = mPlaceObject.getIcon();
						ArrayList<PlacePhotoObject> mListPhotoObjects = mPlaceObject.getListPhotoObjects();
						if (mListPhotoObjects != null && mListPhotoObjects.size() > 0) {
							PlacePhotoObject mPlacePhotoObject = mListPhotoObjects.get(0);
							String photoRef = mPlacePhotoObject.getPhotoReference();
							if (!StringUtils.isStringEmpty(photoRef)) {
								urlPhoto = String.format(FORMAT_URL_PHOTO, photoRef, API_KEY);
							}
						}
						String address = getString(R.string.title_unknown_address);
						if (!StringUtils.isStringEmpty(mPlaceObject.getVicinity())) {
							address = mPlaceObject.getVicinity();
						}
						String snippet = address + "|" + urlPhoto+"|"+String.valueOf(i);
						MarkerOptions mMarkerOptions = new MarkerOptions().position(mLatLng).title(mPlaceObject.getName()).snippet(snippet)
								.icon(BitmapDescriptorFactory.fromResource(mContext.mResIcon));
						listMarkerOptions.add(mMarkerOptions);
						mMap.addMarker(mMarkerOptions);
					}
				}
			}
		}
		if(mCurrentLatLng!=null){
			if(mCurrentLocationMarker!=null){
				mMap.addMarker(mCurrentLocationMarker);
			}
		}

		mMapAdapter = new DetailMapInfoAdapter(mContext, mContext.mImgOptions,mContext.mTypeFaceRobotoBold,mContext.mTypeFaceRobotoLight);
		mMap.setInfoWindowAdapter(mMapAdapter);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker mMarker) {
				String snippet = mMarker.getSnippet();
				if(!StringUtils.isStringEmpty(snippet)){
					try {
						String[] datas = snippet.split("\\|+");
						if(datas!=null && datas.length>=3){
							int index = Integer.parseInt(datas[2]);
							if(index>=0){
								mContext.goToDetail(index);
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		});

		final Builder mMapBuilder = new LatLngBounds.Builder();
		for (LatLng mLatLng : lisLatLngs) {
			mMapBuilder.include(mLatLng);
		}
		final View mapView = getChildFragmentManager().findFragmentById(R.id.detail_map).getView();
//		if (mapView.getViewTreeObserver().isAlive()) {
//			mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//				@SuppressWarnings("deprecation")
//				// We use the new method when supported
//				@SuppressLint("NewApi")
//				// We check which build version we are using.
//				@Override
//				public void onGlobalLayout() {
//					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//						mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//					}
//					else {
//						mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//					}
//					if(lisLatLngs!=null && lisLatLngs.size()>0){
//						LatLngBounds bounds = mMapBuilder.build();
//						CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,20);
//						mMap.moveCamera(cu);
//						mMap.animateCamera(cu);
//						mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
//					}
//
//				}
//			});
//		}
//		else{
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try{
					if(lisLatLngs!=null && lisLatLngs.size()>0){
						LatLngBounds bounds = mMapBuilder.build();
						CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,20);
						mMap.moveCamera(cu);
						mMap.animateCamera(cu);
						mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
					}
				}
				catch (Exception e){
					e.printStackTrace();
				}

			}
		},1000);
//		}

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		return false;
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacksAndMessages(null);
		lisLatLngs.clear();
		lisLatLngs = null;
		listMarkerOptions.clear();
		listMarkerOptions = null;
		super.onDestroy();
	}

	private void drawMyLocation() {
		if (mMap != null) {
			final Location mLocation = TotalDataManager.getInstance().getCurrentLocation();
			if(mLocation!=null){
				IDBTaskListener mDBTaskListener = new IDBTaskListener() {
					private String mLocationName;
					
					@Override
					public void onPreExcute() {
						mContext.showProgressDialog();
					}
					
					@Override
					public void onDoInBackground() {
						mLocationAddress = mDBLocationFinder.getAddressLocation(mLocation);
						mLocationName = mDBLocationFinder.getNameLocation(mLocationAddress);
						if (StringUtils.isStringEmpty(mLocationName)) {
							mLocationName = getString(R.string.title_unknown_location);
						}
					}
					
					@Override
					public void onPostExcute() {
						mContext.dimissProgressDialog();
						try {
							mCurrentLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
							mCurrentLocationMarker = new MarkerOptions().position(mCurrentLatLng).title(mContext.getString(R.string.title_my_location))
									.snippet(mLocationName + "|" + "1"+"|"+"-1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_location));
					
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						setUpMap();
					}
				};
				DBTask mDBTask = new DBTask(mDBTaskListener);
				mDBTask.execute();
			}
			else{
				setUpMap();
			}
		}
	}
	public void setMapType(int mapType,String mStrName){
		if(mMap!=null){
			if(mCurrentMapType!=mapType){
				this.mCurrentMapType = mapType;
				mContext.showToast(mStrName+ " On");
			}
			else{
				mContext.showToast(mStrName+ " Off");
				this.mCurrentMapType= GoogleMap.MAP_TYPE_NORMAL;
			}
			mMap.setMapType(mCurrentMapType);
		}
		
	}

}
