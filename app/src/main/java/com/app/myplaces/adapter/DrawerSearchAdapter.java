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

import com.app.myplaces.R;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.object.HomeSearchObject;

/**
 * 
 * DrawerSearchAdapter.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.adapter
 */
public class DrawerSearchAdapter extends BaseAdapter implements IWhereMyLocationConstants {
	public static final String TAG = DrawerSearchAdapter.class.getSimpleName();

	private Context mContext;
	private ArrayList<HomeSearchObject> listHomeObjects;

	private Typeface mTypefaceLight;
	private Typeface mTypefaceBold;

	public DrawerSearchAdapter(Context mContext, ArrayList<HomeSearchObject> listHomeObjects,Typeface mTypefaceBold,Typeface mTypefaceLight) {
		this.mContext = mContext;
		this.listHomeObjects = listHomeObjects;
		this.mTypefaceLight = mTypefaceLight;
		this.mTypefaceBold=mTypefaceBold;
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
			convertView = mInflater.inflate(R.layout.item_drawer_search, null);
			convertView.setTag(mHolder);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_keyword);
		mHolder.mImgIcon = (ImageView) convertView.findViewById(R.id.img_keyword);
		mHolder.mDevider = convertView.findViewById(R.id.devider);

		HomeSearchObject mHomeSearchObject = listHomeObjects.get(position);
		mHolder.mTvName.setText(mHomeSearchObject.getName());
		mHolder.mTvName.setTypeface(mTypefaceLight);
		if(position==listHomeObjects.size()-1){
			mHolder.mDevider.setVisibility(View.GONE);
		}
		else{
			mHolder.mDevider.setVisibility(View.VISIBLE);
		}
		
		int resIconId = TotalDataManager.getInstance().getResMiniIconHome(mContext, mHomeSearchObject.getImg());
		if(resIconId>0){
			mHolder.mImgIcon.setImageResource(resIconId);
		}
		if (mHomeSearchObject.isSelected()) {
			mHolder.mTvName.setTypeface(mTypefaceBold);
		}
		else {
			mHolder.mTvName.setTypeface(mTypefaceLight);
		}
		return convertView;
	}
	
	public void onDestroy() {
		if(listHomeObjects!=null){
			listHomeObjects.clear();
		}
	}
	public void setSelected(int pos) {
		if (pos < 0 || pos >= listHomeObjects.size()) {
			return;
		}
		TotalDataManager.getInstance().setSelectedObject(pos+1);
		notifyDataSetChanged();
	}

	private static class ViewHolder {
		public TextView mTvName;
		public ImageView mImgIcon;
		public View mDevider;
	}
}