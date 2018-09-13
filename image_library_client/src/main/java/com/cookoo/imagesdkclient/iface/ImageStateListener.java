package com.cookoo.imagesdkclient.iface;

/**
 * @author lsf
 */
public interface ImageStateListener {
	/**
	 * sdk向外部反馈信息接口
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.ImageSdkConstants.ImageStateEventId
	 */
	 void onImageStateChanged(int eventId);

	/**
	 * 扫描库扫描的数据变化回掉
	 * @param eventId
	 */
	 void onImageScanChanged(int eventId);
}
