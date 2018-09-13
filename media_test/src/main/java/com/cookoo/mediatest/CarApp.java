package com.cookoo.mediatest;

import android.app.Application;

import com.cookoo.imagesdk.manager.CookooImageConfiguration;
import com.cookoo.imagesdk.mode.ImageInitParams;
import com.cookoo.musicsdk.manager.CookooMusicConfiguration;
import com.cookoo.musicsdk.modle.MusicInitParams;
import com.cookoo.musicsdk.utils.LogUtils;
import com.cookoo.videosdk.manager.CookooVideoConfiguration;
import com.cookoo.videosdk.modle.VideoInitParams;

import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.PermissionUtils;


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
		LogUtils.print(TAG,"----->>>CarApp  onCreate()->" + getApplicationContext().getPackageName());
		super.onCreate();
		carApp = this;
		//初始化默认参数
		VideoInitParams videoParams = new VideoInitParams(ConstantsUtils.ListType.ALL_TYPE);
		//设置扩展程序应用包名
		videoParams.setExtendProcessPackageNameList("com.cookoo.extendvideo");
		//设置淡入淡出效果
		videoParams.setFadeInNndOut(true);
		//初始化设置
		CookooVideoConfiguration.getInstance().initVideoConfiguration(getApplicationContext(), videoParams);



		//初始化默认参数
		MusicInitParams musicParams = new MusicInitParams(ConstantsUtils.ListType.ALL_TYPE);
		//设置扩展程序应用包名
		musicParams.setExtendProcessPackageNameList("com.cookoo.extendmusic");
		//设置淡入淡出效果
		musicParams.setFadeInNndOut(true);
		//初始化设置
		CookooMusicConfiguration.getInstance().initMusicConfiguration(getApplicationContext(), musicParams);


		//初始化默认参数
		ImageInitParams imageParams = new ImageInitParams(ConstantsUtils.ListType.ALL_TYPE);
		//设置扩展程序应用包名
		imageParams.setExtendProcessPackageNameList("com.cookoo.extendimage");
		imageParams.setTimeStep(2000);
		imageParams.setLoadCount(1);
		//初始化设置
		CookooImageConfiguration.getInstance().initImageConfiguration(getApplicationContext(), imageParams);

		PermissionUtils.getInstance().init(getApplicationContext());
	}

}
