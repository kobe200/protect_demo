package com.cookoo.imagesdk;

/**
 * @author lsf
 */
public class ImageSdkConstants {

	/**
	 * 媒体类型
	 * @author kobe
	 * @2018年3月1日@下午2:07:59
	 * @decribe:
	 */
	public interface MediaType {
		int IMAGE = 1;
		int VIDEO = 2;
		int MUSIC = 3;
	}

	public interface ImageHandler{
		int IMAGE_NEXT = 0;
		int IMAGE_PRE = 1;
		int UPDATA_IMAGE_NAME = 2;
	}

	/**
	 *图片解析加载状态
	 */
	public interface ImageParseState {
		int PICTURE_PARSE_FAILURE = -1;
		int PICTURE_PARSING = 0;
		int PICTURE_PARSE_SUCCESS = 1;
	}

	/**
	 * sdk发送到应用客户端的指令
	 * @author kobe
	 * @2018年3月1日@下午2:09:45
	 * @decribe:
	 */
	public interface ImageStateEventId {
		/**更新播放状态**/
		int UPDATE_PLAY_STATE = 201;
		/**更新播放列表**/
		int UPDATE_PLAY_LIST_DATA = 202;
		/**更新播放时间**/
		int UPDATE_PLAY_TIME = 203;
		/**更新播放界面内容**/
		int UPDATE_PLAY_INFO = 204;
		/**图片播放异常**/
		int IMAGE_PLAY_ONERROR = 205;
	}

	public interface ScanStateEventId {
		/**视频数据改变，包括删除文件改变和扫描数据增加改变**/
		int IMAGE_DATA_CHANGE = 0;
		/**所有数据类型返回数据**/
		int IMAGE_ALL_DATA_BACK = IMAGE_DATA_CHANGE + 1;
		/**多级文件夹数据返回**/
		int IMAGE_TREE_FOLDER_DATA_BACK = IMAGE_ALL_DATA_BACK + 1;
		/**两级文件夹第一级数据返回**/
		int IMAGE_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK = IMAGE_TREE_FOLDER_DATA_BACK + 1;
		/**两级文件夹第二级数据返回**/
		int IMAGE_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK = IMAGE_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK + 1;
		/**收藏列表数据返回**/
		int IMAGE_COLLECTED_DATA_BACK = IMAGE_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK + 1;
		/**解析数据返回**/
		int IMAGE_PARSE_BACK = IMAGE_COLLECTED_DATA_BACK + 1;
		/**USB挂载通知**/
		int USB_DISK_MOUNTED = IMAGE_PARSE_BACK + 1;
		/**USB卸载通知**/
		int USB_DISK_UNMOUNTED = USB_DISK_MOUNTED + 1;
		/**扫描完成通知**/
		int FILE_SCAN_FINISHED = USB_DISK_UNMOUNTED + 1;
	}

}
