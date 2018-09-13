package com.cookoo.videosdk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import android.content.Context;

import com.cookoo.videosdk.R;

/**
 * @author samy
 */
public class TimeUtil {
	private final static String TAG = "TimeUtil";

	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;
	}

	public static String getDateEN() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date1 = format1.format(new Date(System.currentTimeMillis()));
		return date1;
	}

	/**
	 * 解析时间
	 * 
	 * @param time
	 *            给定时间；
	 * @return 得到想要的时间格式；
	 */
	@SuppressWarnings("resource")
	public static String formatDurationInt(int time) {
		// 用于时间解析
		StringBuilder formatBuilder = new StringBuilder();
		Formatter formatter;

		String formatTime = "";

		formatter = new Formatter(formatBuilder, Locale.getDefault());

		int totalSeconds = time / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		formatBuilder.setLength(0);
		//if (hours > 0) {
			// 过滤(时超过99的不显示)
			if (hours > 24 || seconds > 60 || minutes > 60) {
				return "";
			}
			formatTime = formatter.format("%02d:%02d:%02d", hours, minutes,
					seconds).toString();
		//} 
//		else {
//			formatTime = formatter.format("%02d:%02d", minutes, seconds)
//					.toString();
//		}
		// LogUtil.i(TAG, "------------------:" + formatTime);
		return formatTime;
	}

	@SuppressWarnings("resource")
	public static String formatDurationLong(Long time) {
		// 用于时间解析
		StringBuilder formatBuilder = new StringBuilder();
		Formatter formatter;

		String formatTime = "";

		int tempTime = (Integer.valueOf(time + "")).intValue();

		formatter = new Formatter(formatBuilder, Locale.getDefault());

		int totalSeconds = tempTime / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		formatBuilder.setLength(0);
		//if (hours > 0) {
			// 过滤(时超过99的不显示)
			if (hours > 24 || seconds > 60 || minutes > 60) {
				return "";
			}
			formatTime = formatter.format("%02d:%02d:%02d", hours, minutes,
					seconds).toString();
		//} 
//		else {
//			formatTime = formatter.format("%02d:%02d", minutes, seconds)
//					.toString();
//		}
		// LogUtil.i(TAG, "------------------:" + formatTime);
		return formatTime;
	}

	/**
	 * 格式化时间
	 */
	public static String formatDuration(final Context context, int durationMs) {
		int duration = durationMs / 1000;
		int h = duration / 3600;
		int m = (duration - h * 3600) / 60;
		int s = duration - (h * 3600 + m * 60);
		String durationValue;
		if (h == 0) {
			durationValue = String.format(
					context.getString(R.string.details_ms), m, s);
		} else {
			durationValue = String.format(
					context.getString(R.string.details_hms), h, m, s);
		}
		LogUtils.print(TAG, "durationValue: " + durationValue);
		return durationValue;
	}
	
	private static long lastTime = 0 ,currentTime = 0;
	public static boolean isFastClick(int step){
		currentTime = System.currentTimeMillis();
		if ((currentTime - lastTime) < step) {
			return true;
		}
		lastTime = currentTime;
		return false;
	}
	
	
	//避免改变系统语言后，出现时间判断不对问题
	public static void resetTime(){
		lastTime = 0 ;
		currentTime = 0;
	}


}
