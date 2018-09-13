package com.cookoo.mediatest.common;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;


/**
 * @author lsf
 */
public abstract class CommonAdapter extends BaseAdapter implements OnClickListener {

	private static final String TAG = "CommonAdapter";
	private Context mContext;

	private List<MediaData> mData;

	private int layoutId;

	private ViewClickCallBack callback;

	public CommonAdapter(Context mContext, List<MediaData> listData, int layoutId, ViewClickCallBack callback) {
		this.mContext = mContext;
		mData = new ArrayList<>();
		if(listData != null){
			this.mData.addAll(listData);
		}
		this.layoutId = layoutId;
		this.callback = callback;
	}

	public synchronized void updateAdapter(List<MediaData> listData) {
		if(listData != null){
			this.mData.clear();
			this.mData.addAll(listData);
			this.notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if (mData == null) {
			return null;
		}
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BaseViewHolder holder = BaseViewHolder.get(mContext, convertView, parent, layoutId);
		afterGetView(position, holder);
		return holder.getItemView();

	}

	protected abstract void afterGetView(int position, BaseViewHolder holder);

	@Override
	public void onClick(View v) {
		if (callback != null) {
			callback.viewClickLintener(v, (Integer) v.getTag());
		}
	}

	public void setViewClickLintener(View v) {
		if (v == null) {
			return;
		}
		v.setOnClickListener(this);
	}

	/** 主要处理ListView、GridView等控件item里面包含Button的点击事件 **/
	public interface ViewClickCallBack {
		/**
		 *
		 * @param v
		 *            ：当前点击的View对象
		 * @param position
		 *            ：当前点击的View的位置
		 */
		public void viewClickLintener(View v,int position);
	}

	public List<MediaData> getData() {
		return mData;
	}
}
