package com.cookoo.imagesdk.component;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * @author lsf
 */
public class ImageViewPager extends ViewPager{

	private Context mContext;
	private GestureDetector mGestureDetector;
	public ImageViewPager(Context context) {
		super(context);
		this.mContext = context;
		init();
	}
	
	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context,attrs);
		init();
	}
	
	private void init(){
		mGestureDetector = new GestureDetector(mContext, new SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (showOrHideSystemUi != null){
					showOrHideSystemUi.showOrHideView();
				}
				return super.onSingleTapConfirmed(e);
			}
		});
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}
	
	/** 显示或者隐藏systemUi接口
	 * @author Administrator
	 */
	public interface ShowOrHideSystemUiListener {
		/**
		 * 显示或隐藏view
		 */
		void showOrHideView();
	}
	
	public ShowOrHideSystemUiListener showOrHideSystemUi;
	
	public void setShowOrHideSystemUiListener(ShowOrHideSystemUiListener showOrHideSystemUi) {
		this.showOrHideSystemUi = showOrHideSystemUi;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			return super.onTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
}
