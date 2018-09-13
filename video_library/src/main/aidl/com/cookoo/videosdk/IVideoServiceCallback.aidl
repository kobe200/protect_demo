// IVideoServiceCallback.aidl
package com.cookoo.videosdk;

// Declare any non-default types here with import statements

interface IVideoServiceCallback {

/**
	 * sdk向外部反馈信息接口
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.VideoSdkConstants.VideoStateEventId
	 * @param extra 状态参数
	 */
	void onVideoStateChanged(int eventId);

	/**
	 * 扫描库扫描的数据变化回掉
	 * @param eventId
	 * @param extra
	 */
	void onVideoScanChanged(int eventId);

}
