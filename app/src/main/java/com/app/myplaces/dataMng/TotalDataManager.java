package com.app.myplaces.dataMng;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;

import android.content.Context;
import android.location.Location;

import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.view.NewIOUtils;
import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.location.TrackRecordServiceController;
import com.app.myplaces.object.HomeSearchObject;
import com.app.myplaces.object.KeywordObject;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.StringUtils;

/**
 * 
 * SingletonPattern TotalDataManager to manager all datas of User
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation
 */

public class TotalDataManager implements IWhereMyLocationConstants {

	public static final String TAG = TotalDataManager.class.getSimpleName();

	private static TotalDataManager totalDataManager;
	private ArrayList<HomeSearchObject> listHomeSearchObjects;
	private Location currentLocation;
	private ArrayList<PlaceObject> listFavoriteObjects;
	private ArrayList<KeywordObject> listKeywordObjects;
	private TrackRecordServiceController mTrackingController;

	public static TotalDataManager getInstance() {
		if (totalDataManager == null) {
			totalDataManager = new TotalDataManager();
		}
		return totalDataManager;
	}

	private TotalDataManager() {
		listFavoriteObjects = new ArrayList<PlaceObject>();
	}

	public void onDestroy() {
		if (listFavoriteObjects != null) {
			listFavoriteObjects.clear();
			listFavoriteObjects = null;
		}
		if (listHomeSearchObjects != null) {
			listHomeSearchObjects.clear();
			listHomeSearchObjects = null;
		}
		totalDataManager = null;
	}
	
	public KeywordObject getKeyWordObject(String keyword){
		if(listKeywordObjects!=null && listKeywordObjects.size()>0 && keyword!=null){
			for(KeywordObject mKeywordObject:listKeywordObjects){
				if(mKeywordObject.getKeyword().equalsIgnoreCase(keyword)){
					return mKeywordObject;
				}
			}
		}
		return null;
	}

	public ArrayList<KeywordObject> getListKeywordObjects() {
		return listKeywordObjects;
	}

	public void setListKeywordObjects(ArrayList<KeywordObject> listKeywordObjects) {
		this.listKeywordObjects = listKeywordObjects;
	}

	public ArrayList<HomeSearchObject> getListHomeSearchObjects() {
		return listHomeSearchObjects;
	}

	public void setListHomeSearchObjects(ArrayList<HomeSearchObject> listHomeSearchObjects) {
		this.listHomeSearchObjects = listHomeSearchObjects;
	}

	public HomeSearchObject getHomeSearchSelected() {
		if (listHomeSearchObjects != null && listHomeSearchObjects.size() > 0) {
			for (HomeSearchObject mHomeSearchObject1 : listHomeSearchObjects) {
				if (mHomeSearchObject1.isSelected()) {
					DBLog.d(TAG, "============>home search selected=" + mHomeSearchObject1.getName());
					return mHomeSearchObject1;
				}
			}
			listHomeSearchObjects.get(1).setSelected(true);
			return listHomeSearchObjects.get(1);
		}
		return null;
	}
	
	public void setSelectedObject(int pos){
		if (listHomeSearchObjects != null && listHomeSearchObjects.size() > 0) {
			for (HomeSearchObject mHomeSearchObject : listHomeSearchObjects) {
				if(listHomeSearchObjects.indexOf(mHomeSearchObject)==pos){
					mHomeSearchObject.setSelected(true);
				}
				else{
					mHomeSearchObject.setSelected(false);
				}
			}
		}
	}
	public void setSelectedObject(HomeSearchObject pos){
		if (listHomeSearchObjects != null && listHomeSearchObjects.size() > 0) {
			for (HomeSearchObject mHomeSearchObject : listHomeSearchObjects) {
				if(mHomeSearchObject.equals(pos)){
					mHomeSearchObject.setSelected(true);
				}
				else{
					mHomeSearchObject.setSelected(false);
				}
			}
		}
	}
	
	public HomeSearchObject findHomeSearchObject(String query){
		if(StringUtils.isStringEmpty(query)){
			return null;
		}
		if (listHomeSearchObjects != null && listHomeSearchObjects.size() > 0) {
			for (HomeSearchObject mHomeSearchObject : listHomeSearchObjects) {
				String keyword =mHomeSearchObject.getKeyword();
				if(!StringUtils.isStringEmpty(keyword) && keyword.equalsIgnoreCase(query)){
					return mHomeSearchObject;
				}
			}
		}
		return null;
	}

	public void onResetResultSearch(boolean isResetAll) {
		if (listHomeSearchObjects != null && listHomeSearchObjects.size() > 0) {
			for (HomeSearchObject mHomeSearchObject1 : listHomeSearchObjects) {
				mHomeSearchObject1.setResponcePlaceResult(null);
			}
			boolean isSelected = listHomeSearchObjects.get(0).isSelected();
			if(isSelected && isResetAll){
				listHomeSearchObjects.get(0).setSelected(false);
				listHomeSearchObjects.get(0).setType(TYPE_SEARCH_BY_TYPES);
				listHomeSearchObjects.get(0).setKeyword(null);
				listHomeSearchObjects.get(1).setSelected(true);
			}
		}
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public int getResIconMapPin(Context mContext, String img) {
		if (StringUtils.isStringEmpty(img)) {
			return R.drawable.icon_custom_search;
		}
		if (img.equals("atm.png")) {
			return R.drawable.icon_pin_atm;
		}
		else if (img.equals("bank.png")) {
			return R.drawable.icon_pin_bank;
		}
		else if (img.equals("police.png")) {
			return R.drawable.icon_pin_police;
		}
		else if (img.equals("university.png")) {
			return R.drawable.icon_pin_university;
		}
		else if (img.equals("gas_station.png")) {
			return R.drawable.icon_pin_gas;
		}
		else if (img.equals("taxi_stand.png")) {
			return R.drawable.icon_pin_taxi;
		}
		else if (img.equals("bus_station.png")) {
			return R.drawable.icon_pin_bus;
		}
		else if (img.equals("airport.png")) {
			return R.drawable.icon_pin_airport;
		}
		else if (img.equals("hospital.png")) {
			return R.drawable.icon_pin_hospital;
		}
		else if (img.equals("hotel.png")) {
			return R.drawable.icon_pin_hotel;
		}
		else if (img.equals("park.png")) {
			return R.drawable.icon_pin_park;
		}
		else if (img.equals("zoo.png")) {
			return R.drawable.icon_pin_zoo;
		}
		else if (img.equals("cinema.png")) {
			return R.drawable.icon_pin_cinema;
		}
		else if (img.equals("shop.png")) {
			return R.drawable.icon_pin_shop;
		}
		else if (img.equals("cafe.png")) {
			return R.drawable.icon_pin_cafe;
		}
		else if (img.equals("bar.png")) {
			return R.drawable.icon_pin_bar;
		}
		return R.drawable.icon_custom_search;
	}

	public int getResIconHome(Context mContext, String keyword) {
		if (StringUtils.isStringEmpty(keyword)) {
			return -1;
		}
		if (keyword.equals("atm.png")) {
			return R.drawable.atm;
		}
		else if (keyword.equals("bank.png")) {
			return R.drawable.bank;
		}
		else if (keyword.equals("police.png")) {
			return R.drawable.police;
		}
		else if (keyword.equals("university.png")) {
			return R.drawable.university;
		}
		else if (keyword.equals("gas_station.png")) {
			return R.drawable.gas_station;
		}
		else if (keyword.equals("taxi_stand.png")) {
			return R.drawable.taxi_stand;
		}
		else if (keyword.equals("bus_station.png")) {
			return R.drawable.bus_station;
		}
		else if (keyword.equals("airport.png")) {
			return R.drawable.airport;
		}
		else if (keyword.equals("hospital.png")) {
			return R.drawable.hospital;
		}
		else if (keyword.equals("hotel.png")) {
			return R.drawable.hotel;
		}
		else if (keyword.equals("park.png")) {
			return R.drawable.park;
		}
		else if (keyword.equals("zoo.png")) {
			return R.drawable.zoo;
		}
		else if (keyword.equals("cinema.png")) {
			return R.drawable.cinema;
		}
		else if (keyword.equals("shop.png")) {
			return R.drawable.shop;
		}
		else if (keyword.equals("cafe.png")) {
			return R.drawable.cafe;
		}
		else if (keyword.equals("bar.png")) {
			return R.drawable.bar;
		}
		return -1;
	}

	public int getResMiniIconHome(Context mContext, String name) {
		if (StringUtils.isStringEmpty(name)) {
			return -1;
		}
		if (name.equals("atm.png")) {
			return R.drawable.mini_atm;
		}
		else if (name.equals("bank.png")) {
			return R.drawable.mini_bank;
		}
		else if (name.equals("police.png")) {
			return R.drawable.mini_police;
		}
		else if (name.equals("university.png")) {
			return R.drawable.mini_university;
		}
		else if (name.equals("gas_station.png")) {
			return R.drawable.mini_gas_station;
		}
		else if (name.equals("taxi_stand.png")) {
			return R.drawable.mini_taxi_stand;
		}
		else if (name.equals("bus_station.png")) {
			return R.drawable.mini_bus_station;
		}
		else if (name.equals("airport.png")) {
			return R.drawable.mini_airport;
		}
		else if (name.equals("hospital.png")) {
			return R.drawable.mini_hospital;
		}
		else if (name.equals("hotel.png")) {
			return R.drawable.mini_hotel;
		}
		else if (name.equals("park.png")) {
			return R.drawable.mini_park;
		}
		else if (name.equals("zoo.png")) {
			return R.drawable.mini_zoo;
		}
		else if (name.equals("cinema.png")) {
			return R.drawable.mini_cinema;
		}
		else if (name.equals("shop.png")) {
			return R.drawable.mini_shop;
		}
		else if (name.equals("cafe.png")) {
			return R.drawable.mini_cafe;
		}
		else if (name.equals("bar.png")) {
			return R.drawable.mini_bar;
		}
		return -1;
	}

	public ArrayList<PlaceObject> getListFavoriteObjects() {
		return listFavoriteObjects;
	}

	public void setListFavoriteObjects(ArrayList<PlaceObject> listFavoriteObjects) {
		if (listFavoriteObjects != null) {
			this.listFavoriteObjects.clear();
			this.listFavoriteObjects = null;
			this.listFavoriteObjects = listFavoriteObjects;
		}
	}

	public boolean isFavoriteLocation(String id) {
		if (listFavoriteObjects != null && listFavoriteObjects.size() > 0 && !StringUtils.isStringEmpty(id)) {
			for (PlaceObject mPlaceObject : listFavoriteObjects) {
				String idNew = mPlaceObject.getId();
				if (!StringUtils.isStringEmpty(idNew) && idNew.equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addFavoritePlace(final Context mContext, PlaceObject mPlaceObject) {
		if (mPlaceObject != null && listFavoriteObjects != null) {
			boolean isAdd = isFavoriteLocation(mPlaceObject.getId());
			if (!isAdd) {
				listFavoriteObjects.add(mPlaceObject);
				DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
					@Override
					public void onAction() {
						saveFavoritePlaces(mContext);
					}
				});
			}
		}
	}

	public void removeFavoritePlace(final Context mContext, PlaceObject mPlaceObject) {
		if (mPlaceObject != null && listFavoriteObjects != null) {
			Iterator<PlaceObject> mListIterator = listFavoriteObjects.iterator();
			String id = mPlaceObject.getId();
			boolean isSyncAgain = false;
			while (mListIterator.hasNext()) {
				PlaceObject placeObject = (PlaceObject) mListIterator.next();
				String idNew = placeObject.getId();
				if (!StringUtils.isStringEmpty(idNew) && !StringUtils.isStringEmpty(id) && id.equals(idNew)) {
					mListIterator.remove();
					isSyncAgain = true;
					break;
				}
			}
			DBLog.d(TAG, "============>isSyncAgain=" + isSyncAgain);
			if (isSyncAgain) {
				DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
					@Override
					public void onAction() {
						saveFavoritePlaces(mContext);
					}
				});
			}
		}
	}

	public synchronized void saveFavoritePlaces(Context mContext) {
		if (!ApplicationUtils.hasSDcard()) {
			return;
		}
		File mFile = NewIOUtils.getDiskCacheDir(mContext, DIR_DATA);
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		if (listFavoriteObjects != null && listFavoriteObjects.size() > 0) {
			JSONArray mJsArray = new JSONArray();
			for (PlaceObject mSongObject : listFavoriteObjects) {
				mJsArray.put(mSongObject.toJson());
			}
			DBLog.d(TAG, "=============>favoriteDatas=" + mJsArray.toString());
			NewIOUtils.writeString(mFile.getAbsolutePath(), FILE_FAVORITE_PLACES, mJsArray.toString());
			return;
		}
		NewIOUtils.writeString(mFile.getAbsolutePath(), FILE_FAVORITE_PLACES, "");
	}
	
	public void onRegisterTrackingService(Context mContext) {
		if (mTrackingController == null) {
			mTrackingController = TrackRecordServiceController.getInstance();
			mTrackingController.bindTrackingService(mContext, null);
			mTrackingController.startTracking();
		}
	}

	public void onDestroyTrackingService(Context mContext) {
		try {
			if (mTrackingController != null) {
				mTrackingController.stopTracking();
				mTrackingController.unbindTrackingServiceAndStop(mContext);
				mTrackingController.onDestroy();
				mTrackingController = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
