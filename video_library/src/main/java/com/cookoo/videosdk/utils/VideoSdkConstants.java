package com.cookoo.videosdk.utils;

/**
 *
 * @author lsf
 * @date 2018/3/21
 */

public class VideoSdkConstants {

    public interface ServiceAction{
        String START_VIDEO_SERVICE_ACTION = "carnetapp.video.videoservice";
    }

    public interface MoveType{
        /**不断移动**/
        int KEEP_MOVING = 1;
        /**只移动一次**/
        int ONLY_MOVE_ONE_TIME = 1;
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
        /**空闲**/
        int IDLE = 0;
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
        /**播放结束**/
        int COMPLETED = 6;
        /**切换歌曲**/
        int SWITCH = 7;
    }


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

    public interface VideoHandler {
        /**请求快进**/
        int FAST_FORWARD = 101;
        /**请求快退**/
        int BACK_FORWARD = 102;
        /**更新播放进度**/
        int UPDATE_PLAY_TIME = 103;
        /**播放错误**/
        int VIDEO_PLAY_ERROR = 104;
        /**准备播放**/
        int VIDEO_PREPARE_PLAY = 105;
        /**播放下一个**/
        int VIDEO_PLAY_NEXT = 106;
        /**开始播放**/
        int VIDEO_PLAY_START = 107;
        /**暂停播放**/
        int VIDEO_PAUSE = 108;
    }

    /**
     * sdk发送到应用客户端的指令
     * @author kobe
     * @2018年3月1日@下午2:09:45
     * @decribe:
     */
    public interface VideoStateEventId {
        /**更新播放状态**/
        int UPDATE_PLAY_STATE = 201;
        /**更新播放时间**/
        int UPDATE_PLAY_TIME = 203;
        /**更新播放界面内容**/
        int UPDATE_PLAY_INFO = 204;
        /**视频播放异常**/
        int VIDEO_PLAY_ERROR = 205;
        /**是断开当前播放的usb，导致停止的，要与普通异常区分**/
        int IS_USB_UN_MOUNT_STOP_VIDEO = 206;
    }
    public interface ScanStateEventId {
        /**视频数据改变，包括删除文件改变和扫描数据增加改变**/
        int VIDEO_DATA_CHANGE = 0;
        /**所有数据类型返回数据**/
        int VIDEO_ALL_DATA_BACK = VIDEO_DATA_CHANGE + 1;
        /**多级文件夹数据返回**/
        int VIDEO_TREE_FOLDER_DATA_BACK = VIDEO_ALL_DATA_BACK + 1;
        /**两级文件夹第一级数据返回**/
        int VIDEO_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK = VIDEO_TREE_FOLDER_DATA_BACK + 1;
        /**两级文件夹第二级数据返回**/
        int VIDEO_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK = VIDEO_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK + 1;
        /**收藏列表数据返回**/
        int VIDEO_COLLECTED_DATA_BACK = VIDEO_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK + 1;
        /**解析数据返回**/
        int VIDEO_PARSE_BACK = VIDEO_COLLECTED_DATA_BACK + 1;
        /**USB挂载通知**/
        int USB_DISK_MOUNTED = VIDEO_PARSE_BACK + 1;
        /**USB卸载通知**/
        int USB_DISK_UNMOUNTED = USB_DISK_MOUNTED + 1;
        /**扫描完成通知**/
        int FILE_SCAN_FINISHED = USB_DISK_UNMOUNTED + 1;
    }

    public interface VideoPlayFormat{
        float AUTO_SCALE = (float) 1.0;
        float SMALL_SCALE = (float)4/3;
        float LARGE_SCALE = (float)16/9;
    }

}
