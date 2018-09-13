package com.cookoo.musicsdkclient.iface;

/**
 * @author lsf
 */
public interface MusicStateListener {
	/**
	 * sdk向外部反馈信息接口
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.MusicSdkConstants.MusicStateEventId
	 */
	 void onMusicStateChanged(int eventId);

	/**
	 * 扫描库扫描的数据变化回掉
	 * @param eventId
	 */
	 void onMusicScanChanged(int eventId);
}
