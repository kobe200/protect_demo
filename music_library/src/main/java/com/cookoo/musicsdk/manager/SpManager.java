package com.cookoo.musicsdk.manager;

import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.utils.CacheDataUtil;

import carnetapp.usbmediadata.bean.MediaData;


/**
 * @author lsf
 * @date 2018/3/14
 */

public class SpManager {

    /**
     * 播放模式
     **/
    private final String MUSIC_PLAY_MODE = "MUSIC_PLAY_MODE ";
    /**
     * 播放音乐进度
     **/
    private final String MUSIC_PLAY_PROGRESS = "MUSIC_PLAY_PROGRESS";
    /**
     * 播放媒体文件路径
     **/
    private final String MUSIC_PLAY_MEDIA_PATH = "MUSIC_PLAY_MEDIA_PATH";
    /**
     * 播放媒体数据类型
     **/
    private final String MUSIC_PLAY_MEDIA_DATA_TYPE = "MUSIC_PLAY_MEDIA_DATA_TYPE";
    /**
     * 播放媒体列表路径
     **/
    private final String MUSIC_PLAY_MEDIA_LIST_PATH = "MUSIC_PLAY_MEDIA_LIST_PATH";
    /**
     * 显示媒体列表路径
     **/
    private final String SHOW_MEDIA_LIST_PATH = "SHOW_MEDIA_LIST_PATH";

    private SpManager() {
    }

    public static SpManager getInstance() {
        return SpManagerInstance.spfManager;
    }

    public void savePlayMode(int position) {
        CacheDataUtil.getInstance().saveData(MUSIC_PLAY_MODE,position);
    }

    public int getSavePlayMode() {
        return CacheDataUtil.getInstance().getIntData(MUSIC_PLAY_MODE,MusicSdkConstants.PlayMode.ALL_LOOP);
    }

    public void savePlayProgress(int progress) {
        CacheDataUtil.getInstance().saveData(MUSIC_PLAY_PROGRESS,progress);
    }

    public int getSavePlayProgress() {
        return CacheDataUtil.getInstance().getIntData(MUSIC_PLAY_PROGRESS,0);
    }

    public void savePlayMediaItem(MediaData mediaItemInfo) {
        if(mediaItemInfo == null) {
            return;
        }
        CacheDataUtil.getInstance().saveData(MUSIC_PLAY_MEDIA_PATH,mediaItemInfo.getFilePath());
        CacheDataUtil.getInstance().saveData(MUSIC_PLAY_MEDIA_DATA_TYPE,mediaItemInfo.getDataType());
    }

    public String getSavePlayMediaItemPath() {
        return CacheDataUtil.getInstance().getData(MUSIC_PLAY_MEDIA_PATH);
    }

    public int getSavePlayMediaItemDataType() {
        return CacheDataUtil.getInstance().getIntData(MUSIC_PLAY_MEDIA_DATA_TYPE,CookooMusicConfiguration.getInstance().getParam().getCurrentDataListType());
    }

    public String getMusicPlayMediaListPath() {
        return CacheDataUtil.getInstance().getData(MUSIC_PLAY_MEDIA_LIST_PATH);
    }

    public void setMusicPlayMediaListPath(String path) {
        CacheDataUtil.getInstance().saveData(MUSIC_PLAY_MEDIA_LIST_PATH,path);
    }

    public String getShowMediaListPath() {
        return CacheDataUtil.getInstance().getData(SHOW_MEDIA_LIST_PATH);
    }

    public void setShowMediaListPath(String path) {
        CacheDataUtil.getInstance().saveData(SHOW_MEDIA_LIST_PATH,path);
    }

    private static class SpManagerInstance {
        private static final SpManager spfManager = new SpManager();
    }


}
