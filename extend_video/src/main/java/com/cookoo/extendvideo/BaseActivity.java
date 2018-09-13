package com.cookoo.extendvideo;


import android.app.Activity;
import android.os.Bundle;

import com.cookoo.videosdkclient.utils.LogUtils;


/**
 * @author lsf
 */
public abstract class BaseActivity extends Activity {

	/**
	 * 获取子Activity
	 * @return
	 */
	public abstract Activity getChildContext();

	private String TAG = null ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getChildContext() != null){
			TAG = getChildContext().getClass().getSimpleName();
		}
		LogUtils.print(TAG, " -------->>>onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtils.print(TAG, " -------->>>onResume");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		LogUtils.print(TAG, " -------->>>onRestart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtils.print(TAG, " -------->>>onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtils.print(TAG, " -------->>>onDestroy");
	}

}
