package com.cookoo.extendmusic.Utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * @author kobe
 */
public class AudioUtil {

	/**
	 * 解析时间
	 * 
	 * @param time
	 *            给定时间；
	 * @return 得到想要的时间格式；
	 */
	@SuppressWarnings("resource")
	public static String formatDurationInt(long time) {
		if (time < 0) {
			time = 0;
		}
		// 用于时间解析
		StringBuilder formatBuilder = new StringBuilder();
		Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
		String formatTime = "";
		int totalSeconds = (int) time / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		formatBuilder.setLength(0);
		if (hours > 0) {
			// 过滤(时超过99的不显示)
			if (hours > 24 || seconds > 60 || minutes > 60) {
				return "00:00:00";
			}
			formatTime = formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			formatTime = formatter.format("%02d:%02d", minutes, seconds).toString();
		}
		return formatTime;
	}

}
