package com.cookoo.extendmusic;

import android.app.Application;

import com.cookoo.musicsdkclient.manager.MusicAidlManager;
import com.cookoo.musicsdkclient.utils.LogUtils;


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
		super.onCreate();
		LogUtils.print(TAG,"----->>>CarApp  onCreate()->" + getApplicationContext().getPackageName());
		carApp = this;
		MusicAidlManager.getInstance().doBinderMusicService(getApplicationContext());
	}

}
