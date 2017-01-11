package com.app.myplaces.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.ypyproductions.utils.DirectionUtils;
import com.app.myplaces.MainActivity;
import com.app.myplaces.MainSearchActivity;
import com.app.myplaces.R;
import com.app.myplaces.adapter.HomeAdapter;
import com.app.myplaces.constanst.IWhereMyLocationConstants;
import com.app.myplaces.dataMng.TotalDataManager;
import com.app.myplaces.object.HomeSearchObject;

/**
 * 
 * FragmentHome.java
 * @author  :DOBAO
 * @Email   :dotrungbao@gmail.com
 * @Skype   :baopfiev_k50
 * @Phone   :+84983028786
 * @Date    :Nov 26, 2013
 * @project :WhereMyLocation
 * @Package :com.ypyproductions.wheremylocation.fragment
 */
public class FragmentHome extends Fragment implements IWhereMyLocationConstants {
	
	public static final String TAG = FragmentHome.class.getSimpleName();
	
	private View mRootView;

	private MainActivity mContext;

	private boolean isFindView;
	private GridView mGridView;

	private ArrayList<HomeSearchObject> mListHomeObjects;
	private HomeAdapter mHomeAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_home, container, false);
		return mRootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(!isFindView){
			isFindView=true;
			this.findView();
		}
	}
	
	private void findView(){
		this.mContext = (MainActivity) getActivity();
		this.mGridView =(GridView) mRootView.findViewById(R.id.gridview);
		this.mListHomeObjects = TotalDataManager.getInstance().getListHomeSearchObjects();
		if(mListHomeObjects!=null){
			int size = mListHomeObjects.size();
			ArrayList<HomeSearchObject> listHomeSearchObjects = new ArrayList<HomeSearchObject>();
			for(int i=0;i<size;i++){
				if(i!=0){
					listHomeSearchObjects.add(mListHomeObjects.get(i));
				}
			}
			this.mHomeAdapter = new HomeAdapter(mContext, listHomeSearchObjects, mContext.mTypeFaceRobotoLight);
			this.mGridView.setAdapter(mHomeAdapter);
			mGridView.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					mHomeAdapter.setSelected(position);
					Intent mIntent = new Intent(mContext, MainSearchActivity.class);
					DirectionUtils.changeActivity(mContext, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIntent);
				}
			});
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mHomeAdapter!=null){
			mHomeAdapter.onDestroy();
		}
	}
}
