package com.cookoo.videosdkclient.hold;

/**
 * @author lsf
 */
public interface VideoStateListener {
	/**
	 * sdk向外部反馈信息接口
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.VideoStateEventId.VideoStateEventId
	 */
	 void onVideoStateChanged(int eventId);

	/**
	 * 扫描库扫描的数据变化回掉
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.VideoStateEventId.VideoStateEventId
	 */
	 void onVideoScanChanged(int eventId);
}
