package com.cookoo.imagesdk.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * 
 * @ClassName: GlobalTool
 * @Description: 全局工具
 * @author lsf
 * @date 2018年4月13日 下午3:00:06
 * 
 */
public class GlobalTool {

	private Context context;

	public static final boolean DEBUG = true;

	private static GlobalTool instance = new GlobalTool();

	public Context getContext() {

		return context;
	}

	public void setContext(Context context) {
		if (context != null){
			this.context = context.getApplicationContext();
		}
	}

	private GlobalTool() {
	}

	public static GlobalTool getInstance() {

		return instance;
	}

	private static final String TAG = "jdsx";

	public static void jasonLog(String logContent) {

		Log.d(TAG, getFunctionName() + "  :  " + logContent);
	}

	/**
	 * Get The Current Function Name
	 * 
	 * @return
	 */
	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(GlobalTool.class.getName())) {
				continue;
			}
			return "[ " + Thread.currentThread().getName() + ": "
					+ st.getClassName() + ":" + st.getLineNumber() + " "
					+ st.getMethodName() + " ]";
		}
		return null;
	}

	public void putInt(String name, int value) {
		Settings.System.putInt(context.getContentResolver(), name, value);
	}

	public void putString(String name, String value) {
		Settings.System.putString(context.getContentResolver(), name, value);
	}

	public void putFloat(String name, float value) {
		Settings.System.putFloat(context.getContentResolver(), name, value);
	}

	public void putLong(String name, long value) {
		Settings.System.putLong(context.getContentResolver(), name, value);
	}

	public int getInt(String name, int defValue) {
		return Settings.System.getInt(context.getContentResolver(), name,
				defValue);
	}
	
	public String getString(String name) {

		return Settings.System.getString(context.getContentResolver(), name);
	}

	public float getFloat(String name, float defValue) {

		return Settings.System.getFloat(context.getContentResolver(), name,
				defValue);
	}

	public long getLong(String name, long defValue) {
		return Settings.System.getLong(context.getContentResolver(), name,
				defValue);
	}

}
