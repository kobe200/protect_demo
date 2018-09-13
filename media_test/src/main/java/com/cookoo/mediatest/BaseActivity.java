package com.cookoo.mediatest;


import android.app.Activity;
import android.os.Bundle;

import com.cookoo.musicsdk.utils.LogUtils;


/**
 * @author lsf
 */
public abstract class BaseActivity extends Activity {

	/**
	 * 获取子Activity
	 * @return
	 */
	public abstract Activity getChildContext();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
