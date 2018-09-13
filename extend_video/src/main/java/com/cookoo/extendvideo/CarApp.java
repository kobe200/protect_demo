package com.cookoo.extendvideo;

import android.app.Application;

import com.cookoo.videosdkclient.manager.VideoAidlManager;
import com.cookoo.videosdkclient.utils.LogUtils;


/**
 * @author lsf
 */
public class CarApp extends Application {
	private static final String TAG = "CarApp";
	private static CarApp carApp = null ;
	
	public static CarApp getInstance(){
		return carApp;
	}
	
	@Override
	public void onCreate() {
		LogUtils.print(TAG,"----->>>CarApp  onCreate()");
		super.onCreate();
		carApp = this;
		VideoAidlManager.getInstance().doBinderVideoService(getApplicationContext());
	}

}
