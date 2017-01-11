package com.app.myplaces.adapter;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ypyproductions.utils.StringUtils;
import com.app.myplaces.R;

/**
 * MapInfoAdapter
 * @author DOBAO
 * @Email dotrungbao@gmail.com
 * @Skype baopfiev_k50
 * @Date May 7, 2013
 * @Packagename com.auction.adapter
 */

public class DetailMapInfoAdapter implements InfoWindowAdapter {

	public static final String TAG = DetailMapInfoAdapter.class.getSimpleName();
	
	private View mWindow;
	private DisplayImageOptions mImgFetcher;

	private Typeface mTypeFaceRobotoLight;
	private Typeface mTypeFaceRobotoBold;

	public DetailMapInfoAdapter(FragmentActivity mContext, DisplayImageOptions mImgFetcher, Typeface mTypeFaceRobotoBold,Typeface mTypeFaceRobotoLight) {
		this.mImgFetcher=mImgFetcher;
		this.mWindow = mContext.getLayoutInflater().inflate(R.layout.custom_detail_info_window, null, false);
		this.mTypeFaceRobotoLight=mTypeFaceRobotoLight;
		this.mTypeFaceRobotoBold=mTypeFaceRobotoBold;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		render(marker);
		return mWindow;
	}

	private void render(final Marker mMarker) {
		String nameStore = mMarker.getTitle();
		if(nameStore==null){
			nameStore="";
		}
		TextView mTvStore = ((TextView) mWindow.findViewById(R.id.tv_name_location));
		TextView mTvAddress = ((TextView) mWindow.findViewById(R.id.tv_sumary));
		ImageView mImgDetail = ((ImageView) mWindow.findViewById(R.id.img_detail_location));
		
		mTvStore.setText(nameStore);
		mTvStore.setTypeface(mTypeFaceRobotoBold);
		String snippet = mMarker.getSnippet();
		if (!StringUtils.isStringEmpty(snippet) && snippet.contains("|") && !snippet.startsWith("|")) {
			String[] address = snippet.split("\\|+");
			if (address != null) {
				mTvAddress.setText(address[0]);
				mTvAddress.setTypeface(mTypeFaceRobotoLight);
				if (address.length >= 2) {
					String urlIcon = address[1];
					if (urlIcon.startsWith("http")) {
						ImageLoader.getInstance().displayImage(urlIcon, mImgDetail, mImgFetcher);
					}
					else {
						mImgDetail.setImageResource(R.drawable.icon_location_default);
					}
				}
			}
		}
	}
	

}
