package com.cookoo.imagesdkclient.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author kobe
 * @describe file cache utils
 * @2016年3月24日下午2:46:56
 *
 */
public class CacheDataUtil {
	private static final String SHARED_PREFERENCES_CACHE_FILE = "car_music_sdk_cache_file";

	private final String TAG = "CacheDataManager";

	private Context mContext;

	private static CacheDataUtil instance = new CacheDataUtil();

	public static CacheDataUtil getInstance() {
		return instance;
	}

	private CacheDataUtil() {
	}

	public void init(Context context) {
		mContext = context.getApplicationContext();
	}

	/**
	 * 缓存数据到配置文件
	 * @param cacheKey
	 * @param data
	 */
	public void saveData(String cacheKey, String data) {
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(cacheKey, data);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "saveData(String cacheKey, String data)\n" + e);
		}
	}

	/**
	 * 缓存数据到配置文件
	 * 
	 * @param cacheKey
	 * @param data
	 */
	public void saveData(String cacheKey, int data) {
		try {
			LogUtils.print(TAG, "saveData(String cacheKey, long data):" + cacheKey + "|" + data);
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt(cacheKey, data);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "saveData(String cacheKey, int data)\n" + e);
		}

	}

	/**
	 * 缓存数据到配置文件
	 * 
	 * @param cacheKey
	 * @param data
	 */
	public void saveData(String cacheKey, long data) {
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putLong(cacheKey, data);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "saveData(String cacheKey, long data)\n" + e);
		}
	}

	public void saveData(String cacheKey, boolean data) {
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(cacheKey, data);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "saveData(String cacheKey, boolean data)\n" + e);
		}
	}

	public void saveData(String cacheKey, float data) {
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putFloat(cacheKey, data);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "saveData(String cacheKey, boolean data)\n" + e);
		}
	}

	/**
	 * @Author: kobe
	 * @CreteDate: 2015-4-20 上午9:42:32
	 * @Title:
	 * @Description:
	 * @ModifiedBy:
	 * @param cacheKey
	 */
	public void deleteData(String cacheKey) {
		try {
			LogUtils.print(TAG, "[deleteData]cacheKey=" + cacheKey);
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(cacheKey);
			editor.commit();
		} catch (Exception e) {
			LogUtils.print(TAG, "deleteData()\n" + e);
		}
	}

	/**
	 * 從配置文件裏面获取缓存数据
	 * @param cacheKey
	 * @return
	 */
	public String getData(String cacheKey) {
		String data = "";
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			LogUtils.print(TAG, "[getData]cacheKey=" + cacheKey + "-data=" + sharedPreferences.getString(cacheKey, ""));
			data = sharedPreferences.getString(cacheKey, "");
		} catch (Exception e) {
			Log.e(TAG, "getData():Exception");
		}
		return data;
	}

	/**
	 * 從配置文件裏面获取缓存数据
	 * @param cacheKey
	 * @return
	 */
	public long getLongData(String cacheKey) {
		long data = 0l;
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			LogUtils.print(TAG, "[getData]cacheKey=" + cacheKey + "-data=" + sharedPreferences.getLong(cacheKey, 0l));
			data = sharedPreferences.getLong(cacheKey, 0l);
		} catch (Exception e) {
			Log.e(TAG, "getLongData():Exception");
		}
		return data;
	}

	public float getFloatData(String cacheKey, float def) {
		float data = 0l;
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			LogUtils.print(TAG, "[getData]cacheKey=" + cacheKey + "-data=" + sharedPreferences.getFloat(cacheKey, def));
			data = sharedPreferences.getFloat(cacheKey, def);
		} catch (Exception e) {
			Log.e(TAG, "getLongData():Exception");
		}
		return data;
	}

	/**
	 * @Author: kobe
	 * @CreteDate: 2015-4-20 上午9:42:26
	 * @Title:
	 * @Description:
	 * @ModifiedBy:
	 * @param cacheKey
	 * @return
	 */
	public int getIntData(String cacheKey, int def) {
		int data = 0;
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			LogUtils.print(TAG, "[getData]cacheKey=" + cacheKey + "-data=" + sharedPreferences.getInt(cacheKey, def));
			data = sharedPreferences.getInt(cacheKey, def);
		} catch (Exception e) {
			Log.e(TAG, "getIntData():Exception");
		}
		return data;
	}

	/**
	 * @Author: kobe
	 * @CreteDate: 2015-4-20 上午9:42:20
	 * @Title:
	 * @Description:
	 * @ModifiedBy:
	 * @param cacheKey
	 * @param defValue
	 * @return
	 */
	public boolean getBoolData(String cacheKey, boolean defValue) {
		boolean data = defValue;
		try {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_CACHE_FILE, Activity.MODE_PRIVATE);
			data = sharedPreferences.getBoolean(cacheKey, defValue);
		} catch (Exception e) {
			Log.e(TAG, "getBoolData():Exception" + e.getMessage());
		}
		return data;
	}

	/**
	 * 从文件里面获取缓存数据
	 * 
	 * @param fileName
	 * @return
	 */
	public Object getDataFile(String fileName) {
		Object object = null;
		FileInputStream fin = null;
		ObjectInputStream in = null;
		try {
			fin = mContext.openFileInput(fileName + ".ser");
			in = new ObjectInputStream(fin);
			object = in.readObject();
		} catch (Exception e) {
			LogUtils.print(TAG, "getDataFile()#:Exception");
			e.printStackTrace();
		} finally {
			try {
				if (fin != null) {
					fin.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return object;
	}

	/**
	 * 保存缓存数据到文件
	 * @return
	 */
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public boolean saveDataFile(String fileName, Object object) {
		if (object == null) {
			return false;
		}
		FileOutputStream fout = null;
		try {
			// 需要一个文件输出流和对象输出流；文件输出流用于将字节输出到文件，对象输出流用于将对象输出为字节
			fout = mContext.openFileOutput(fileName + ".ser", Activity.MODE_WORLD_READABLE);
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(object);
			out.close();
			return true;
		} catch (IOException e) {
			LogUtils.print(TAG, "saveDataFile:#Exception" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (fout != null) {
					fout.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @Author: kobe
	 * @CreteDate: 2015-4-20 上午9:42:04
	 * @Title:
	 * @Description:
	 * @ModifiedBy:
	 * @param fileName
	 * @return
	 */
	public boolean deleteDataFile(String fileName) {
		try {
			File file = mContext.getFileStreamPath(fileName + ".ser");
			if (file.exists()) {
				return file.delete();
			}
		} catch (Exception e) {
			Log.e(TAG, "deleteDataFile()::Exception");
		}
		return false;
	}
}
