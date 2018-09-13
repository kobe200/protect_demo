package com.cookoo.videosdk.manager;


import com.cookoo.videosdk.utils.CacheDataUtil;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import carnetapp.usbmediadata.bean.MediaData;


/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public class SpManager {

    /**
     * 播放模式
     **/
    public final String VIDEO_PLAY_MODE = "VIDEO_PLAY_MODE ";
    /**
     * 播放视频进度
     **/
    public final String VIDEO_PLAY_PROGRESS = "VIDEO_PLAY_PROGRESS";
    /**
     * 播放媒体列表路径
     **/
    public final String VIDEO_PLAY_MEDIA_LIST_PATH = "VIDEO_PLAY_MEDIA_LIST_PATH";
    /**
     * 显示媒体列表路径
     **/
    public final String VIDEO_SHOW_MEDIA_LIST_PATH = "VIDEO_SHOW_MEDIA_LIST_PATH";
    /**
     * 播放媒体文件路径
     **/
    public final String VIDEO_PLAY_MEDIA_PATH = "VIDEO_PLAY_MEDIA_PATH";
    /**
     * 播放媒体数据类型
     **/
    public final String VIDEO_PLAY_MEDIA_DATA_TYPE = "VIDEO_PLAY_MEDIA_DATA_TYPE";

    public final String VIDEO_PLAY_FORMAT = "VIDEO_PLAY_FORMAT";

    private SpManager() {
    }

    public static SpManager getInstance() {
        return SpManagerInstance.spfManager;
    }

    public void savePlayFormat(float format){
        CacheDataUtil.getInstance().saveData(VIDEO_PLAY_FORMAT,format);
    }

    public float getSavePlayFormat() {
        return CacheDataUtil.getInstance().getFloatData(VIDEO_PLAY_FORMAT, VideoSdkConstants.VideoPlayFormat.AUTO_SCALE);
    }

    public void savePlayProgress(int progress) {
        CacheDataUtil.getInstance().saveData(VIDEO_PLAY_PROGRESS,progress);
    }

    public int getSavePlayProgress() {
        return CacheDataUtil.getInstance().getIntData(VIDEO_PLAY_PROGRESS,0);
    }

    public void savePlayMediaItem(MediaData mediaItemInfo){
        if (mediaItemInfo == null){
            return;
        }
        LogUtils.print("test"," savePlayMediaItem : "+mediaItemInfo.getFilePath());
        CacheDataUtil.getInstance().saveData(VIDEO_PLAY_MEDIA_PATH,mediaItemInfo.getFilePath());
        CacheDataUtil.getInstance().saveData(VIDEO_PLAY_MEDIA_DATA_TYPE,mediaItemInfo.getDataType());
        LogUtils.print("test"," savePlayMediaItem : "+getSavePlayMediaItemPath());
    }

    public String getSavePlayMediaItemPath(){
        return CacheDataUtil.getInstance().getData(VIDEO_PLAY_MEDIA_PATH);
    }

    public int getSavePlayMediaItemDataType(){
        return CacheDataUtil.getInstance().getIntData(VIDEO_PLAY_MEDIA_DATA_TYPE, CookooVideoConfiguration.getInstance().getParam().getCurrentDataListType());
    }

    public void setVideoPlayMediaListPath(String path){
        CacheDataUtil.getInstance().saveData(VIDEO_PLAY_MEDIA_LIST_PATH , path);
    }

    public String getVideoPlayMediaListPath() {
        return CacheDataUtil.getInstance().getData(VIDEO_PLAY_MEDIA_LIST_PATH);
    }

    public String getVideoShowMediaListPath() {
        return CacheDataUtil.getInstance().getData(VIDEO_SHOW_MEDIA_LIST_PATH);
    }

    public void setVideoShowMediaListPath(String path){
        CacheDataUtil.getInstance().saveData(VIDEO_SHOW_MEDIA_LIST_PATH , path);
    }

    private static class SpManagerInstance {
        private static final SpManager spfManager = new SpManager();
    }


}
