package com.cookoo.musicsdk.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.text.TextUtils;

import com.cookoo.musicsdk.binder.MusicAidlBinder;
import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.hold.MusicManagerApi;
import com.cookoo.musicsdk.imp.IMusicMode;
import com.cookoo.musicsdk.service.MusicSdkService;
import com.cookoo.musicsdk.utils.FileUtils;
import com.cookoo.musicsdk.utils.GlobalTool;
import com.cookoo.musicsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.MediaUsbService;
import carnetapp.usbmediadata.model.MusicScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.DeviceUtils;

/**
 * @author: lsf
 * @date: 2018/2/7 14:40
 * @decribe:
 */
public class MusicManager extends BaseManager implements MusicManagerApi {
    private final String TAG = MusicManager.class.getSimpleName();
    /**
     * 媒体播放器
     **/
    private MediaPlayer mediaPlayer = null;
    /**
     * 当前播放列表数据
     **/
    private List<MediaData> playList = null;
    /**
     * 原始数据
     **/
    private List<MediaListData> originalData;
    /**
     * 当前播放状态
     **/
    private int currentPlayState = -1;
    /**
     * 当前播放文件对象
     **/
    private MediaData currentPlayMediaItem = null;
    /**
     * 当前播放文件处于列表中的位置
     **/
    private int currentListPosition = 0;
    /**
     * 当前播放时长,做记忆播放位置的时候用到
     **/
    private int currentPlayPosition = 0;
    /**
     * 当前播放总时长,做记忆播放位置的时候用到
     **/
    private int currentPlayDuration = 0;
    /**
     * 当前显示数据类型
     */
    private int currentShowListDataType = CookooMusicConfiguration.getInstance().getParam().getCurrentDataListType();
    /**
     * 当前播放模式
     **/
    private int currentPlayMode = MusicSdkConstants.PlayMode.ALL_LOOP;
    /**
     * 当前浏览目录
     **/
    private String currentShowPath = SpManager.getInstance().getShowMediaListPath();
    /**
     * 当前播放目录
     **/
    private String currentPlayPath = SpManager.getInstance().getMusicPlayMediaListPath();
    /**
     * 当前浏览目录,如果为两级文件夹数据则需要记录哪一个文件夹
     **/
    private String columnContentStr = null;
    /**
     * 远程服务控制对象，用于与远程服务进行交互
     **/
    private MusicAidlBinder musicBinder;
    /**
     * 播放音乐的操作对象
     **/
    private IMusicMode iMusicMode;
    /**
     * 动态设置淡入淡出
     */
    private boolean isFadeInNndOut = true;

    private ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name,IBinder service) {
            musicBinder = (MusicAidlBinder) service;
            LogUtils.print(TAG,"------>>> onServiceConnected  musicService: " + musicBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.print(TAG,"------>>> onServiceDisconnected");
            musicBinder = null;
        }
    };

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        return MusicManagerInstance.musicManager;
    }

    public void init() {
        LogUtils.print(TAG,"-------->> init()");
        MediaAudioManager.getInstance().init();
        MusicDataManager.getInstance().init();
        MusicManager.getInstance().setCurrentPlayMode(SpManager.getInstance().getSavePlayMode());
        startMusicService();
        startScanService();
    }

    @SuppressLint("NewApi")
    private void startScanService() {
        Intent intent = new Intent(GlobalTool.getInstance().getContext(),MediaUsbService.class);
        intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
        GlobalTool.getInstance().getContext().startService(intent);
    }

    @SuppressLint("NewApi")
    private void startMusicService() {
        Intent intent = new Intent(GlobalTool.getInstance().getContext(),MusicSdkService.class);
        intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
        GlobalTool.getInstance().getContext().bindService(intent,musicServiceConnection,Context.BIND_AUTO_CREATE);
    }

    public void setMusicMode(IMusicMode iMusicMode) {
        LogUtils.print(TAG,"setMusicMode()->" + iMusicMode);
        this.iMusicMode = iMusicMode;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        LogUtils.print(TAG,"setMediaPlayer()->" + mediaPlayer);
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public MediaData getCurrentPlayMediaItem() {
        return currentPlayMediaItem;
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData itemInfo) {
        LogUtils.print(TAG,"setCurrentPlayMediaItem()->" + itemInfo);
        this.currentPlayMediaItem = itemInfo;
        SpManager.getInstance().savePlayMediaItem(itemInfo);
    }

    @Override
    public String getSavePlayMediaItemPath() {
        return SpManager.getInstance().getSavePlayMediaItemPath();
    }

    @Override
    public int getSavePlayMediaItemDataType() {
        return SpManager.getInstance().getSavePlayMediaItemDataType();
    }

    @Override
    public int getSavePlayProgress() {
        return SpManager.getInstance().getSavePlayProgress();
    }

    @Override
    public int getSavePlayMode() {
        return SpManager.getInstance().getSavePlayMode();
    }

    @Override
    public int getCurrentPlayPosition() {
        return currentPlayPosition;
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) {
        this.currentPlayPosition = currentPlayPosition;
    }

    @Override
    public boolean isFadeInNndOut() {
        return isFadeInNndOut;
    }

    @Override
    public void setFadeInNndOut(boolean fadeInNndOut) {
        isFadeInNndOut = fadeInNndOut;
    }

    @Override
    public int getHeightLightPosition() {
        MediaData mediaData = getCurrentPlayMediaItem();
        List<MediaData> showDataList = getNewData();
        int playDataListType;
        int showDataListType = getCurrentDataListType();
        if(mediaData != null) {
            playDataListType = mediaData.getDataType();
            if(playDataListType == showDataListType) {
                return currentListPosition;
            } else {
                for(int i = 0; i < showDataList.size(); i++) {
                    if(isCurrentPlayItem(showDataList.get(i))) {
                        return i;
                    }
                }
            }
        }
        int heightLight = getFirstFilePosition(showDataList,0);
        LogUtils.print(TAG," ---->> getHeightLightPosition() heightLight" + heightLight);
        return heightLight;
    }

    private int getFirstFilePosition(List<MediaData> showDataList,int currentPosition) {
        if(currentPosition >= showDataList.size()) {
            return -1;
        }
        if(showDataList.get(currentPosition).isFolder()) {
            currentPosition = currentPosition + 1;
            getFirstFilePosition(showDataList,currentPosition);
        }
        return currentPosition;
    }

    @Override
    public int getCurrentListPosition() {
        LogUtils.print(TAG,"getCurrentListPosition()->" + currentListPosition);
        return currentListPosition;
    }

    @Override
    public void setCurrentListPosition(int currentListPosition) {
        LogUtils.print(TAG,"setCurrentListPosition()->" + currentListPosition);
        this.currentListPosition = currentListPosition;
    }

    @Override
    public int getCurrentPlayMode() {
        LogUtils.print(TAG,"getCurrentPlayMode()->" + currentPlayMode);
        return currentPlayMode;
    }

    @Override
    public void setCurrentPlayMode(int currentPlayMode) {
        LogUtils.print(TAG,"setCurrentPlayMode()->" + currentPlayMode);
        this.currentPlayMode = currentPlayMode;
    }

    @Override
    public int getTotalTime() {
        if(mediaPlayer == null || !mediaPlayer.isPlaying()) {
            return currentPlayDuration;
        }
        currentPlayDuration = mediaPlayer.getDuration();
        return  currentPlayDuration< 0 ? 0 :currentPlayDuration;
    }

    @Override
    public int getCurrentTime() {
        if(mediaPlayer == null) {
            return 0;
        }
        return currentPlayPosition;
    }

    @Override
    public synchronized List<MediaData> getPlayList() {
        if(playList == null) {
            playList = new ArrayList<>();
        }
        LogUtils.print(TAG,"getPlayList()->" + playList.size());
        return playList;
    }

    @Override
    public void setPlayList(List<MediaData> playList) {
        LogUtils.print(TAG,"setPlayList()->" + playList.size());
        this.playList = playList;
    }

    @Override
    public synchronized List<MediaListData> getOriginalData() {
        if(originalData == null) {
            originalData = new ArrayList<>();
        }
        LogUtils.print(TAG,"getOriginalData()->" + originalData.size());
        return originalData;
    }

    @Override
    public void preMusic() {
        LogUtils.print(TAG,"preMusic()->");
        if(iMusicMode != null) {
            iMusicMode.preMusic();
        }
    }

    @Override
    public void playOrPause() {
        LogUtils.print(TAG,"playOrPause()->");
        if(iMusicMode != null) {
            iMusicMode.playOrPause();
        }
    }

    @Override
    public void startMusic() {
        LogUtils.print(TAG,"startMusic()->");
        if(iMusicMode != null) {
            iMusicMode.startMusic();
        }
    }

    @Override
    public void playMusic(MediaData mediaData) {
        LogUtils.print(TAG," playMusic()-> " + mediaData);
        if(iMusicMode != null) {
            iMusicMode.playMusic(mediaData);
        }
    }

    @Override
    public void pauseMusic() {
        LogUtils.print(TAG," pauseMusic()");
        if(iMusicMode != null) {
            iMusicMode.pauseMusic();
        }
    }

    @Override
    public void stopMusic() {
        LogUtils.print(TAG," stopMusic()");
        if(iMusicMode != null) {
            iMusicMode.stopMusic();
            currentPlayDuration = 0 ;
        }
    }

    @Override
    public void backForward() {
        LogUtils.print(TAG," backForward()->" + iMusicMode);
        if(iMusicMode != null) {
            iMusicMode.backForward();
        }
    }

    @Override
    public void fastForward() {
        LogUtils.print(TAG," fastForward()->" + iMusicMode);
        if(iMusicMode != null) {
            iMusicMode.fastForward();
        }
    }

    @Override
    public void seekTo(int position) {
        LogUtils.print(TAG,"seekTo()->" + position + "|" + getTotalTime());
        if(mediaPlayer == null || position < 0 || position > getTotalTime()) {
            LogUtils.print(TAG,"seekTo()-> exception");
            return;
        }
        mediaPlayer.seekTo(position);
        setCurrentPlayPosition(position);
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) {
        boolean isCurrentItem = false;
        if(itemInfo != null && currentPlayMediaItem != null) {
            if(itemInfo.getFilePath() != null && currentPlayMediaItem.getFilePath() != null) {
                if(itemInfo.getFilePath().equals(currentPlayMediaItem.getFilePath())) {
                    isCurrentItem = true;
                }
            }
        }
        LogUtils.print(TAG,"isCurrentPlayItem()->" + isCurrentItem);
        return isCurrentItem;
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) {
        LogUtils.print(TAG,"isAllUsbScanFinished()->");
        for(UsbDevice ud : getUsbDeviceList()) {
            if(!TextUtils.isEmpty(ud.getRootPath()) && !isCurrentDeviceScanFinished(ud.getRootPath())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearUsbMusic(String usbPath) {
        LogUtils.print(TAG,"clearUsbMusic()->" + usbPath);
        if(iMusicMode != null) {
            iMusicMode.clearUsbMusic(usbPath);
        }
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) {
        LogUtils.print(TAG,"isCurrentDeviceScanFinished()->" + usbPath);
        return MusicScanManager.getInstance().isUsbFileScanFinished(usbPath);
    }

    @Override
    public void nextMusic(boolean isUser) {
        LogUtils.print(TAG,"nextMusic()->" + isUser);
        if(iMusicMode != null) {
            iMusicMode.nextMusic(isUser);
        }
    }

    @Override
    public void savePlayMode(int position) {
        LogUtils.print(TAG,"savePlayMode()->" + position);
        setCurrentPlayMode(position);
        SpManager.getInstance().savePlayMode(position);
    }

    @Override
    public void setCPURefrain(boolean isRefrain) {
        LogUtils.print(TAG,"setCPURefrain()->" + isRefrain);
        MusicScanManager.getInstance().setCPURefrain(isRefrain,2);
    }

    @Override
    public void requestMusicData(String usbPath,String columnContentStr,int dataType) {
        LogUtils.print(TAG,"requestUsbMusicData()->  usbPath: " + usbPath + " columnContentStr: " + columnContentStr + " dataType: " + dataType);
        setCurrentDataListType(dataType);
        setCurrentShowDataPath(usbPath);
        this.columnContentStr = columnContentStr;
        MusicScanManager.getInstance().requestMusicData(GlobalTool.getInstance().getContext(),usbPath,columnContentStr,dataType);
    }

    @Override
    public boolean collected(MediaData mediaData){
        if (mediaData == null || TextUtils.isEmpty(mediaData.getFilePath())){
            return false;
        }
        LogUtils.print(TAG,"---->> collected() isCollected: " + mediaData.isCollected());
        if (!mediaData.isCollected()){
            updateMediaCollected(true,mediaData.getFilePath());
        }
        return true;
    }

    @Override
    public boolean unCollected(MediaData mediaData){
        if (mediaData == null || TextUtils.isEmpty(mediaData.getFilePath())){
            return false;
        }
        LogUtils.print(TAG,"---->> unCollected() isCollected: " + mediaData.isCollected());
        if (mediaData.isCollected()){
            updateMediaCollected(false,mediaData.getFilePath());
        }
        return true;
    }

    private boolean updateMediaCollected(boolean isCollected,String filePath) {
        LogUtils.print(TAG,"---->>updateMediaCollected() isCollected: " +isCollected);
        return MusicScanManager.getInstance().updateMediaCollecte(isCollected, filePath);
    }

    @Override
    public boolean updateMediaItemName(MediaData itemInfo,boolean isCollection) {
        LogUtils.print(TAG,"updateMediaItemName()->" + itemInfo);
        //TODO
        return false;
    }

    @Override
    public int getCurrentPlayState() {
        LogUtils.print(TAG,"getCurrentPlayState()->");
        //TODO
        return currentPlayState;
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) {
        LogUtils.print(TAG,"setCurrentPlayState()->" + currentPlayState);
        this.currentPlayState = currentPlayState;
    }

    @Override
    public int getCurrentDataListType() {
        return currentShowListDataType;
    }

    public void setCurrentDataListType(int dataType) {
        this.currentShowListDataType = dataType;
    }

    @Override
    public List<MediaData> getNewData() {
        List<MediaData> list = new ArrayList<>();
        for(MediaListData ml : getOriginalData()) {
            if(ml == null || ml.getData() == null) {
                getOriginalData().remove(ml);
                continue;
            }
            list.addAll(ml.getData());
        }
        LogUtils.print(TAG,"getNewData()->" + list.size());
        return list;
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) {
        LogUtils.print(TAG," isCurrentUsbMount size: " + usbPath);
        for(UsbDevice ud : getUsbDeviceList()) {
            if(!TextUtils.isEmpty(ud.getRootPath()) && ud.getRootPath().equals(usbPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAllUsbUnMount() {
        LogUtils.print(TAG," isAllUsbUnMount()-> " + getUsbDeviceList().size());
        for(UsbDevice ud : getUsbDeviceList()) {
            if(!TextUtils.isEmpty(ud.getRootPath())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() {
        return DeviceUtils.getMountedDevices(false);
    }

    @Override
    public String getCurrentShowDataPath() {
        return currentShowPath;
    }

    private void setCurrentShowDataPath(String path){
        this.currentShowPath = path;
        SpManager.getInstance().setShowMediaListPath(currentShowPath);
    }

    @Override
    public String getCurrentPlayPath() {
        return currentPlayPath;
    }

    @Override
    public void setCurrentPlayPath(String currentPlayPath) {
        this.currentPlayPath = currentPlayPath;
        SpManager.getInstance().setMusicPlayMediaListPath(currentPlayPath);
    }

    @Override
    public String getColumnContentStr() {
        return columnContentStr;
    }

    @Override
    public void handleMemoryPlayback() {
        if(currentPlayMediaItem == null) {
            //获取最后记录播放文件
            String saveFilePath = getSavePlayMediaItemPath();
            boolean isFileExit = FileUtils.isFileExit(saveFilePath);
            LogUtils.print(TAG,"handleUsbDisMounted  saveFilePath: " + saveFilePath + "  isFileExit: " + isFileExit);
            if(isFileExit) {
                MusicScanManager.getInstance().requestMusicParse(saveFilePath);
                MediaData mediaData = new MediaData();
                mediaData.setFilePath(saveFilePath);
                mediaData.setDataType(getSavePlayMediaItemDataType());
                setCurrentPlayMediaItem(mediaData);
                playMusic(mediaData);
            }
        }
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) {
        if(TextUtils.isEmpty(currentPath) || !currentPath.contains("/")) {
            return null;
        }
        return currentPath.substring(0,currentPath.lastIndexOf("/"));
    }

    @Override
    public void upperLevel(String usbPath) {
        LogUtils.print(TAG," == upperLevel() == " + getCurrentDataListType() + "|" + usbPath);
        switch(getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                handleAlbumTypeUpperLevel(usbPath);
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                handleAuthorTypeUpperLevel(usbPath);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                handleTreeClassicFolderUpperLevel(usbPath);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                handleTwoClassicFolderUpperLevel(usbPath);
                break;
        }
    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath) {
        return MusicScanManager.getInstance().getMusicMediaItem(GlobalTool.getInstance().getContext(),filePath);
    }

    private void handleTwoClassicFolderUpperLevel(String usbPath) {
        if(isAllUsbUnMount()) {
            return;
        }
        requestMusicData(usbPath,null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
    }

    private void handleTreeClassicFolderUpperLevel(String usbPath) {
        LogUtils.print(TAG,"==handleTreeClassicFolderUpperLevel 1 ==" + currentShowPath);
        if(TextUtils.isEmpty(currentShowPath)) {
            return;
        }
        //如果当前显示文件的根目录就是usb的目录，直接返回总目录
        for(UsbDevice ud : getUsbDeviceList()) {
            if(currentShowPath.equals(ud.getRootPath())) {
                setCurrentShowDataPath(null);
                requestMusicData(currentShowPath,null,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                return;
            }
        }
        String folderPath = getUpperLevelFolderPath(currentShowPath);
        LogUtils.print(TAG,"==handleTreeClassicFolderUpperLevel 2 ==" + folderPath);
        if(TextUtils.isEmpty(folderPath)) {
            return;
        }
        LogUtils.print(TAG," ---->> handleTreeFolderTypeUpperLevel  folderPath:" + folderPath);
        requestMusicData(folderPath,null,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
    }

    private void handleAuthorTypeUpperLevel(String usbPath) {
        if(getNewData().size() <= 0 || getNewData().get(0).isFolder()) {
            return;
        }
        requestMusicData(usbPath,null,ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE);
    }

    private void handleAlbumTypeUpperLevel(String usbPath) {
        if(getNewData().size() <= 0 || getNewData().get(0).isFolder()) {
            return;
        }
        requestMusicData(usbPath,null,ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE);
    }

    @Override
    public void handleMusicDataChange(String usbPath) {
        handleShowListDataChange(usbPath);
        handlePlayListDataChange(usbPath);
    }

    public void handleShowListDataChange(String usbPath) {
        LogUtils.print(TAG,"==handleShowListDataChange==currentShowPath:" + currentShowPath + ",columnContentStr:" + columnContentStr + ",currentShowListDataType:" + currentShowListDataType);
        requestMusicData(currentShowPath,columnContentStr,currentShowListDataType);
    }

    public void handlePlayListDataChange(String usbPath) {
        int showDataType = getCurrentDataListType();
        if(getCurrentPlayMediaItem() == null || showDataType == getCurrentPlayMediaItem().getDataType()) {
            return;
        }
        int playListDataType = getCurrentPlayMediaItem().getDataType();
        //更新播放列表数据
        switch(playListDataType) {
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                List<MediaData> list = getNewData();
                if(list.size() > 0) {
                    MediaData mediaData = list.get(0);
                    MusicScanManager.getInstance().requestMusicData(GlobalTool.getInstance().getContext(),usbPath,mediaData.getName(),playListDataType);
                }
                break;
            case ConstantsUtils.ListType.ALL_TYPE:
            case ConstantsUtils.ListType.COLLECTION_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                MusicScanManager.getInstance().requestMusicData(GlobalTool.getInstance().getContext(),usbPath,null,playListDataType);
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
                MusicScanManager.getInstance().requestMusicData(GlobalTool.getInstance().getContext(),usbPath,null,playListDataType);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                MusicScanManager.getInstance().requestMusicData(GlobalTool.getInstance().getContext(),currentShowPath,null,playListDataType);
                break;
            default:
        }
    }

    private static class MusicManagerInstance {
        private static final MusicManager musicManager = new MusicManager();
    }

}
