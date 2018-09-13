// IMusicServiceCallback.aidl
package com.cookoo.imagesdk;

// Declare any non-default types here with import statements

interface IImageServiceCallback {

/**
	 * sdk向外部反馈信息接口
	 * @param eventId 状态参数，具体参考com.cookoo.mediasdk.ImageSdkConstants.ImageStateEventId
	 * @param extra 状态参数
	 */
	void onImageStateChanged(int eventId);

	/**
	 * 扫描库扫描的数据变化回掉
	 * @param eventId
	 * @param extra
	 */
	void onImageScanChanged(int eventId);

}
