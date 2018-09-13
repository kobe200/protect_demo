package com.cookoo.extendmusic;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author lsf
 */
public class BaseViewHolder {

	@SuppressWarnings("rawtypes")
	private SparseArray mViews;

	private View mConvertView;

	@SuppressWarnings("rawtypes")
	private BaseViewHolder(Context context, ViewGroup viewGroup, int layoutId) {
		mViews = new SparseArray();
		this.mConvertView = LayoutInflater.from(context).inflate(layoutId, viewGroup, false);
		this.mConvertView.setTag(this);
	}

	public static BaseViewHolder get(Context context, View convertView, ViewGroup viewGroup, int layoutId) {
		if (convertView == null) {
			return new BaseViewHolder(context, viewGroup, layoutId);
		}

		return (BaseViewHolder) convertView.getTag();
	}

	public View getItemView() {
		return mConvertView;

	}

	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int viewId) {
		View view = (View) mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public void setText(int viewId, String text) {
		if (getView(viewId) instanceof TextView) {
			TextView textview = getView(viewId);
			textview.setText(text);
		}
	}

}
