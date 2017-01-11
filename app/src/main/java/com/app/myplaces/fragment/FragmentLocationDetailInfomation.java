package com.app.myplaces.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ypyproductions.net.task.IDBCallback;
import com.ypyproductions.utils.DateTimeUtils;
import com.ypyproductions.utils.ResolutionUtils;
import com.ypyproductions.utils.ShareActionUtils;
import com.ypyproductions.utils.StringUtils;
import com.app.myplaces.DetailLocationAcitivity;
import com.app.myplaces.R;
import com.app.myplaces.ShowUrlActivity;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.object.PlaceDetailObject;
import com.app.myplaces.object.PlaceObject;
import com.app.myplaces.object.PlacePhotoObject;
import com.app.myplaces.object.UserReviewObject;
import com.app.myplaces.settings.SettingManager;

/**
 * 
 * FragmentInfomation.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.fragment
 */
public class FragmentLocationDetailInfomation extends Fragment implements IWhereMyLocationConstants {

	public static final String TAG = FragmentLocationDetailInfomation.class.getSimpleName();

	private static final int MAX_DISPLAY_USER_REVIEWS = 50;

	private View mRootView;

	private DetailLocationAcitivity mContext;

	private boolean isFindView;

	private CheckBox mCbFavorite;

	private TextView mTvNameLocation;
	private RatingBar mRatingBar;
	private TextView mTvNumberReviews;
	private LinearLayout mLayoutImg;

	private LinearLayout mLayoutDetailInfo;
	private LinearLayout mLayoutDetailReviews;

	private TextView mTvTitleImages;
	private TextView mTvTitlePlaceInfos;
	private TextView mTvTitleUserReviews;

	private boolean isNeedUpdateDistance;
	private TextView mTvDistance;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_location_detail_infomation, container, false);
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
		this.mContext = (DetailLocationAcitivity) getActivity();
		this.mCbFavorite =(CheckBox) mRootView.findViewById(R.id.cb_add_favorite);
		this.mTvNameLocation=(TextView) mRootView.findViewById(R.id.tv_name_location);
		this.mRatingBar=(RatingBar) mRootView.findViewById(R.id.ratingBar1);
		this.mTvNumberReviews=(TextView) mRootView.findViewById(R.id.tv_number_reviews);
		
		this.mTvTitleImages=(TextView) mRootView.findViewById(R.id.tv_title_img);
		this.mTvTitlePlaceInfos=(TextView) mRootView.findViewById(R.id.tv_title_info);
		this.mTvTitleUserReviews=(TextView) mRootView.findViewById(R.id.tv_title_user_reviews);
		
		this.mTvTitleImages.setTypeface(mContext.mTypeFaceRobotoBold);
		this.mTvTitlePlaceInfos.setTypeface(mContext.mTypeFaceRobotoBold);
		this.mTvTitleUserReviews.setTypeface(mContext.mTypeFaceRobotoBold);
		
		this.mLayoutImg =(LinearLayout) mRootView.findViewById(R.id.layout_img);
		this.mLayoutDetailInfo=(LinearLayout) mRootView.findViewById(R.id.layout_detail_info);
		this.mLayoutDetailReviews=(LinearLayout) mRootView.findViewById(R.id.layout_detail_reviews);
		
		
		PlaceDetailObject mPlaceDetailObject = mContext.mPlaceDetailObject;
		if(mPlaceDetailObject!=null){
			this.mTvNameLocation.setTypeface(mContext.mTypeFaceRobotoBold);
			this.mTvNameLocation.setText(mPlaceDetailObject.getName());
			
			this.mRatingBar.setRating(mPlaceDetailObject.getRating());
			
			ArrayList<UserReviewObject> mListReviewObjects=mPlaceDetailObject.getListReviewObjects();
			if(mListReviewObjects!=null && mListReviewObjects.size()>0){
				int size = mListReviewObjects.size();
				String review="";
				if(size>1){
					review = String.format(mContext.getString(R.string.format_reviews), String.valueOf(size));
				}
				else{
					review = String.format(mContext.getString(R.string.format_review), String.valueOf(size));
				}
				mTvNumberReviews.setTypeface(mContext.mTypeFaceRobotoLight);
				mTvNumberReviews.setText(review);
			}
			else{
				mTvNumberReviews.setVisibility(View.INVISIBLE);
			}
			setUpInfomation(mPlaceDetailObject, mContext.mCurrentPlaceObject);
			
			ArrayList<UserReviewObject> mListUserReviews = mPlaceDetailObject.getListReviewObjects();
			if(mListUserReviews==null || mListUserReviews.size()==0){
				mTvTitleUserReviews.setVisibility(View.GONE);
			}
			else{
				int size = mListUserReviews.size();
				for(int i=0;i<MAX_DISPLAY_USER_REVIEWS;i++){
					if(i<size){
						UserReviewObject mUserReviewObject = mListUserReviews.get(i);
						addItemReviews(mUserReviewObject.getAuthorName(), mUserReviewObject.getText(), mUserReviewObject.getRating(),
								mUserReviewObject.getTime());
					}
				}
			}
			addListImage(mPlaceDetailObject, mContext.mCurrentPlaceObject);
			mCbFavorite.setChecked(TotalDataManager.getInstance().isFavoriteLocation(mContext.mCurrentPlaceObject.getId()));
			
			mCbFavorite.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mCbFavorite.isChecked()){
						String info = String.format(getString(R.string.format_add_favorite), mContext.mCurrentPlaceObject.getName());
						TotalDataManager.getInstance().addFavoritePlace(mContext, mContext.mCurrentPlaceObject);
						mContext.showToast(info);
					}
					else{
						String info = String.format(getString(R.string.format_remove_favorite), mContext.mCurrentPlaceObject.getName());
						TotalDataManager.getInstance().removeFavoritePlace(mContext, mContext.mCurrentPlaceObject);
						mContext.showToast(info);
					}
				}
			});
		}
	}
	
	private void addListImage(PlaceDetailObject mPlaceDetailObject,PlaceObject mPlaceObject){
		ArrayList<PlacePhotoObject> mListPlacePhotos = mPlaceDetailObject.getListPhotoObjects();
		if(mListPlacePhotos!=null && mListPlacePhotos.size()>0){
			for(PlacePhotoObject mPlacePhotoObject:mListPlacePhotos){
				View mView = LayoutInflater.from(mContext).inflate(R.layout.item_image, null);
				ImageView mImgView = (ImageView) mView.findViewById(R.id.item_image);
				LinearLayout.LayoutParams mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				if(mListPlacePhotos.indexOf(mPlacePhotoObject)>=1){
					mLayoutParams.leftMargin=(int) ResolutionUtils.convertDpToPixel(mContext, 5);
				}
				mLayoutImg.addView(mView, mLayoutParams);
				String photoRef = mPlacePhotoObject.getPhotoReference();
				if(!StringUtils.isStringEmpty(photoRef)){
					String urlPhoto = String.format(FORMAT_URL_PHOTO, photoRef,API_KEY);
					ImageLoader.getInstance().displayImage(urlPhoto, mImgView, mContext.mImgOptions);
				}
			}
			return;
		}
		String urlPhoto =mPlaceObject.getIcon();
		View mView = LayoutInflater.from(mContext).inflate(R.layout.item_image, null);
		ImageView mImgView = (ImageView) mView.findViewById(R.id.item_image);
		LinearLayout.LayoutParams mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutImg.addView(mView, mLayoutParams);
		if(!StringUtils.isStringEmpty(urlPhoto)){
			ImageLoader.getInstance().displayImage(urlPhoto, mImgView, mContext.mImgOptions);
		}
	}
	
	private void setUpInfomation(PlaceDetailObject mPlaceDetailObject,PlaceObject mPlaceObject){
		String address = mPlaceDetailObject.getAddress();
		if(!StringUtils.isStringEmpty(address)){
			addItemInfomation(address, R.drawable.icon_address, null,false,false);
		}
		String distance="";
		if(mContext.mRouteObject==null){
			isNeedUpdateDistance=true;
			String metric = SettingManager.getMetric(mContext);
			if(metric.equals(UNIT_KILOMETTER)){
				distance=String.format(mContext.getString(R.string.format_distance), String.valueOf(mPlaceObject.getDistance())+" km");
			}
			else if(metric.equals(UNIT_MILE)){
				float convertDistance =  Math.round((float)mPlaceObject.getDistance()/ONE_MILE);
				distance=String.format(mContext.getString(R.string.format_distance), String.valueOf(convertDistance)+" mi");
			}
		}
		else{
			distance= getString(R.string.title_distance)+": "+mContext.mRouteObject.getDistance();
		}
		if(!StringUtils.isStringEmpty(distance)){
			addItemInfomation(distance, R.drawable.icon_distance, null,false,false);
		}
		final String website = mPlaceDetailObject.getWebsite();
		if(!StringUtils.isStringEmpty(website)){
			addItemInfomation(website, R.drawable.icon_website, new IDBCallback() {
				
				@Override
				public void onAction() {
					Intent mIntent = new Intent(mContext, ShowUrlActivity.class);
					mIntent.putExtra(KEY_URL, website);
					startActivity(mIntent);
				}
			},false,true);
			
		}
		final String phone = mPlaceDetailObject.getPhone();
		if(!StringUtils.isStringEmpty(phone)){
			addItemInfomation(phone, R.drawable.icon_phone, new IDBCallback() {
				@Override
				public void onAction() {
					ShareActionUtils.callNumber(mContext, phone);
				}
			},false,false);
		}
		String direction = mContext.getString(R.string.title_get_directions);
		if(!StringUtils.isStringEmpty(direction)){
			addItemInfomation(direction, R.drawable.icon_directions, new IDBCallback() {
				
				@Override
				public void onAction() {
					mContext.mTabHost.setCurrentTab(MAP_INDEX);
				}
			},false,true);
		}
	}
	
	public void updateDistance(){
		if(isNeedUpdateDistance){
			if(mContext.mRouteObject!=null){
				if(mTvDistance!=null){
					mTvDistance.setText(getText(R.string.title_distance)+": "+mContext.mRouteObject.getDistance());
					isNeedUpdateDistance=false;
				}
			}
		}
	}
	
	private void addItemInfomation(String name,int resIconId,final IDBCallback mDBCallback,boolean isEnded, boolean isShowIconMore){
		RelativeLayout mRelativeLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.item_information, null);
		TextView mTvName = (TextView) mRelativeLayout.findViewById(R.id.tv_name);
		ImageView mImgIcon = (ImageView) mRelativeLayout.findViewById(R.id.img_icon);
		ImageView mImgIconMore = (ImageView) mRelativeLayout.findViewById(R.id.img_indicator);
		
		if(!isShowIconMore){
			mImgIconMore.setVisibility(View.GONE);
		}
		
		mTvName.setText(name);
		mTvName.setTypeface(mContext.mTypeFaceRobotoLight);
		mImgIcon.setImageResource(resIconId);
		if(mDBCallback!=null){
			mRelativeLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDBCallback.onAction();
				}
			});
		}
		LayoutParams mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if(mLayoutDetailInfo.getChildCount()!=0 && !isEnded){
			mLayoutParams.topMargin=(int) ResolutionUtils.convertDpToPixel(mContext, 2);
		}
		mLayoutDetailInfo.addView(mRelativeLayout, mLayoutParams);
		if(resIconId==R.drawable.icon_distance){
			this.mTvDistance = mTvName;
		}
	}
	
	private void addItemReviews(String nameUser,String content,float rating,long time){
		RelativeLayout mRelativeLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.item_review, null);
		TextView mTvNameUser = (TextView) mRelativeLayout.findViewById(R.id.tv_name_user);
		mTvNameUser.setTypeface(mContext.mTypeFaceRobotoBold);
		mTvNameUser.setText(nameUser);
		
		TextView mTvContent = (TextView) mRelativeLayout.findViewById(R.id.tv_content);
		mTvContent.setTypeface(mContext.mTypeFaceRobotoLight);
		mTvContent.setText(Html.fromHtml(content));
		
		RatingBar mRatingBar=(RatingBar) mRelativeLayout.findViewById(R.id.user_ratingBar1);
		mRatingBar.setRating(rating);
		
		TextView mTvTime = (TextView) mRelativeLayout.findViewById(R.id.tv_date);
		mTvTime.setTypeface(mContext.mTypeFaceRobotoLight);
		String date = DateTimeUtils.convertMilliToStrDate(time*1000, "dd/MM/yyyy");
		mTvTime.setText(date);
		
		LayoutParams mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if(mLayoutDetailInfo.getChildCount()!=0 ){
			mLayoutParams.topMargin=(int) ResolutionUtils.convertDpToPixel(mContext, 2);
		}
		mLayoutDetailReviews.addView(mRelativeLayout, mLayoutParams);
	}
	
	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
