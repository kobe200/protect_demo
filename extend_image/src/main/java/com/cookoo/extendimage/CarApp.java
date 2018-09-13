package com.cookoo.extendimage;

import android.app.Application;

import com.cookoo.imagesdkclient.manager.CookooImageConfiguration;
import com.cookoo.imagesdkclient.manager.ImageAidlManager;
import com.cookoo.imagesdkclient.mode.ImageInitParams;

import carnetapp.usbmediadata.utils.ConstantsUtils;


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
		ImageInitParams imageParams = new ImageInitParams(ConstantsUtils.ListType.ALL_TYPE);
		imageParams.setExtendProcessPackageNameList("com.cookoo.extendimage");
		imageParams.setTimeStep(2000);
		imageParams.setLoadCount(1);
		CookooImageConfiguration.getInstance().initImageConfiguration(getApplicationContext(), imageParams);
		ImageAidlManager.getInstance().doBinderImageService(getApplicationContext());
	}

}
