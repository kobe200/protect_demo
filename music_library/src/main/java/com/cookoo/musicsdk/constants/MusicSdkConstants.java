package com.cookoo.musicsdk.constants;

/**
 * @author lsf
 * @2018年3月1日@下午2:07:59
 * @decribe: 常量类
 */
public class MusicSdkConstants {

	/**
	 * 媒体类型
	 * @author kobe
	 * @2018年3月1日@下午2:07:59
	 * @decribe:
	 */
	public interface MediaType {
		int MUSIC = 1;
		int VIDEO = 2;
		int IMAGE = 3;
	}

	/**
	 * 播放模式
	 * @author kobe
	 * @2018年3月1日@下午2:08:18
	 * @decribe:
	 */
	public interface PlayMode {
		/** 列表循环 **/
		int ALL_LOOP = 0;
		/** 随机循环 **/
		int RANDOM_LOOP = 1;
		/** 单曲循环 **/
		int SINGLE_LOOP = 2;
		/** 顺序循环 **/
		int ORDER_LOOP = 3;
		/** 所有顺序循环 **/
		int LIST_LOOP = 4;
	}

	/**
	 * 播放状态
	 * @author kobe
	 * @2018年3月1日@下午2:08:45
	 * @decribe:
	 */
	public interface PlayState {
		/**播放**/
		int PLAY = 1;
		/**暂停**/
		int PAUSE = 2;
		/**准备播放**/
		int PREPARE = 3;
		/**停止**/
		int STOP = 4;
		/**错误**/
		int ERROR = 5;
		/**播放完成**/
		int COMPLETED = 6;
		/**切换播放**/
		int SWITCH = 7;
	}

	/**
	 * HANDLE 操作指令
	 * @author kobe
	 * @2018年3月1日@下午2:08:45
	 * @decribe:
	 */
	public interface MusicHandler {
		/**请求快进播放**/
		int FAST_FORWARD = 101;
		/**请求快退播放**/
		int BACK_FORWARD = 102;
		/**更新播放进度**/
		int UPDATE_PLAY_TIME = 103;
		/**准备播放**/
		int MUSIC_PREPARE_PLAY = 104;
		/**发送启动扩展程序广播**/
		int MUSIC_START_EXTEND_PROCESS = 105;
		/**开始播放**/
		int MUSIC_PLAY_START = 106;
		/**暂停播放**/
		int MUSIC_PAUSE = 107;
	}

	/**
	 * sdk发送到应用客户端的指令
	 * @author kobe
	 * @2018年3月1日@下午2:09:45
	 * @decribe:
	 */
	public interface MusicStateEventId {
		/**更新播放状态**/
		int UPDATE_PLAY_STATE = 201;
		/**更新播放列表**/
		int UPDATE_PLAY_LIST_DATA = 202;
		/**更新播放时间**/
		int UPDATE_PLAY_TIME = 203;
		/**更新播放界面内容**/
		int UPDATE_PLAY_INFO = 204;
		/**音乐播放异常**/
		int MUSIC_PLAY_ERROR = 205;
		/**音乐播放异常**/
		int IS_USB_UNMOUNT_STOPMUSIC = 206;
	}

	public interface ScanStateEventId {
		/**音乐数据改变，包括删除文件改变和扫描数据增加改变**/
		int MUSIC_DATA_CHANGE = 0;
		/**所有数据类型返回数据**/
		int MUSIC_ALL_DATA_BACK = MUSIC_DATA_CHANGE + 1;
		/**多级文件夹数据返回**/
		int MUSIC_TREE_FOLDER_DATA_BACK = MUSIC_ALL_DATA_BACK + 1;
		/**两级文件夹第一级数据返回**/
		int MUSIC_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK = MUSIC_TREE_FOLDER_DATA_BACK + 1;
		/**两级文件夹第二级数据返回**/
		int MUSIC_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK = MUSIC_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK + 1;
		/**收藏列表数据返回**/
		int MUSIC_COLLECTED_DATA_BACK = MUSIC_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK + 1;
		/**专辑第一级数据返回**/
		int MUSIC_ALBUM_LEVEL1_DATA_BACK = MUSIC_COLLECTED_DATA_BACK + 1;
		/**专辑第二级数据返回**/
		int MUSIC_ALBUM_LEVEL2_DATA_BACK = MUSIC_ALBUM_LEVEL1_DATA_BACK + 1;
		/**作者第一级数据返回**/
		int MUSIC_ARTIST_LEVEL1_DATA_BACK = MUSIC_ALBUM_LEVEL2_DATA_BACK + 1;
		/**作者第二级数据返回**/
		int MUSIC_ARTIST_LEVEL2_DATA_BACK = MUSIC_ARTIST_LEVEL1_DATA_BACK + 1;
		/**解析数据返回**/
		int MUSIC_PARSE_BACK = MUSIC_ARTIST_LEVEL2_DATA_BACK + 1;
		/**USB挂载通知**/
		int USB_DISK_MOUNTED = MUSIC_PARSE_BACK + 1;
		/**USB卸载通知**/
		int USB_DISK_UNMOUNTED = USB_DISK_MOUNTED + 1;
		/**扫描完成通知**/
		int FILE_SCAN_FINISHED = USB_DISK_UNMOUNTED + 1;
	}

	public interface MoveType{
		/**不断移动**/
		int KEEP_MOVING = 1;
		/**只移动一次**/
		int ONLY_MOVE_ONE_TIME = 1;
	}

}
