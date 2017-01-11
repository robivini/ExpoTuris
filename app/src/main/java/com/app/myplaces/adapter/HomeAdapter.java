package com.app.myplaces.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.object.HomeSearchObject;

/**
 * 
 * HomeAdapter.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.adapter
 */
public class HomeAdapter extends BaseAdapter implements IWhereMyLocationConstants {
	public static final String TAG = HomeAdapter.class.getSimpleName();

	private Context mContext;
	private ArrayList<HomeSearchObject> listHomeObjects;

	private Typeface mTypefaceLight;

	public HomeAdapter(Context mContext, ArrayList<HomeSearchObject> listHomeObjects, Typeface mTypefaceLight) {
		this.mContext = mContext;
		this.listHomeObjects = listHomeObjects;
		this.mTypefaceLight = mTypefaceLight;
	}

	@Override
	public int getCount() {
		if (listHomeObjects != null) {
			return listHomeObjects.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if (listHomeObjects != null) {
			return listHomeObjects.get(arg0);
		}
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		LayoutInflater mInflater;
		if (convertView == null) {
			mHolder = new ViewHolder();
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.item_home, null);
			convertView.setTag(mHolder);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
		mHolder.mImgIcon = (ImageView) convertView.findViewById(R.id.img_icon);
		mHolder.mImgIndicator = (ImageView) convertView.findViewById(R.id.img_indicator);

		HomeSearchObject mHomeSearchObject = listHomeObjects.get(position);
		mHolder.mTvName.setText(mHomeSearchObject.getName());
		mHolder.mTvName.setTypeface(mTypefaceLight);
		
		int resIconId = TotalDataManager.getInstance().getResIconHome(mContext, mHomeSearchObject.getImg());
		if(resIconId>0){
			mHolder.mImgIcon.setImageResource(resIconId);
		}
		if (mHomeSearchObject.isSelected()) {
			mHolder.mImgIndicator.setVisibility(View.VISIBLE);
		}
		else {
			mHolder.mImgIndicator.setVisibility(View.GONE);
		}
		return convertView;
	}

	public void setSelected(int pos) {
		if (pos < 0 || pos >= listHomeObjects.size()) {
			return;
		}
		TotalDataManager.getInstance().setSelectedObject(pos+1);
		notifyDataSetChanged();
	}
	
	public void onDestroy() {
		if(listHomeObjects!=null){
			listHomeObjects.clear();
		}
	}

	private static class ViewHolder {
		public TextView mTvName;
		public ImageView mImgIcon;
		public ImageView mImgIndicator;
	}
}