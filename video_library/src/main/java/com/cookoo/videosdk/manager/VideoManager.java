package com.cookoo.videosdk.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.cookoo.videosdk.binder.VideoAidlBinder;
import com.cookoo.videosdk.imp.IVideoModule;
import com.cookoo.videosdk.imp.VideoManagerApi;
import com.cookoo.videosdk.load.DisCacheUtil;
import com.cookoo.videosdk.service.VideoSdkService;
import com.cookoo.videosdk.utils.FileUtils;
import com.cookoo.videosdk.utils.GlobalTool;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.ScreenUtil;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.MediaUsbService;
import carnetapp.usbmediadata.model.MusicScanManager;
import carnetapp.usbmediadata.model.VideoScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 *
 * @author lsf
 * @date 2018/3/21
 */

public class VideoManager extends BaseManager implements VideoManagerApi {

    private final String TAG = VideoManager.class.getSimpleName();

    private VideoManager() {
    }

    public static VideoManager getInstance() {
        return VideoManagerInstance.VIDEO_MANAGER;
    }

    private static class VideoManagerInstance {
        private static final VideoManager VIDEO_MANAGER = new VideoManager();
    }

    private MediaPlayer mediaPlayer;
    private IVideoModule videoModule;
    private VideoAidlBinder videoBinder;
    /**当前播放状态**/
    private int currentPlayState = -1;
    /**当前播放文件对象**/
    private MediaData currentPlayMediaItem = null;
    /**
     * 当前浏览目录
     **/
    private String currentShowPath = SpManager.getInstance().getVideoShowMediaListPath();
    /**
     * 当前播放目录
     **/
    private String currentPlayPath = SpManager.getInstance().getVideoPlayMediaListPath();
    /**
     * 当前浏览目录,如果为两级文件夹数据则需要记录哪一个文件夹
     **/
    private String columnContentStr = null;
    /**当前播放文件位置**/
    private int currentListPosition = 0;
    /**当前播放时长**/
    private int currentPlayPosition = 0;
    /**
     * 当前播放总时长,做记忆播放位置的时候用到
     **/
    private int currentPlayDuration = 0;
    private int currentShowListDataType = CookooVideoConfiguration.getInstance().getParam().getCurrentDataListType();
    /**当前播放列表数据**/
    private List<MediaData> playList = null;
    private List<UsbDevice> usbDeviceList;
    /**
     * 原始数据
     **/
    private List<MediaListData> originalData;
    /**控制视频显示在屏幕上的位置**/
    private int [] videoPosition;
    /**
     * 动态设置淡入淡出
     */
    private boolean isFadeInNndOut = true;

    public void init(){
        LogUtils.print(TAG,"---->>> init()");
        DisCacheUtil.getInstance(GlobalTool.getInstance().getContext()).removeCacheBitmap();
        MediaAudioManager.getInstance().init();
        SharedPreferenceManager.getInstance().init();
        VideoDataManager.getInstance().init();
        startVideoService();
        startScanService();
        videoPosition = new int[]{ScreenUtil.getResolution(GlobalTool.getInstance().getContext()).first,ScreenUtil.getResolution(GlobalTool.getInstance().getContext()).second,0,0};
    }

    private void startVideoService() {
        LogUtils.print(TAG,"---->>> startVideoService()");
        Intent intent = new Intent(GlobalTool.getInstance().getContext(),VideoSdkService.class);
        intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
        GlobalTool.getInstance().getContext().bindService(intent, videoServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startScanService(){
        Intent intent = new Intent(GlobalTool.getInstance().getContext(),MediaUsbService.class);
        intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
        GlobalTool.getInstance().getContext().startService(intent);
    }

    private ServiceConnection videoServiceConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            videoBinder = (VideoAidlBinder) service;
            videoModule = videoBinder.getVideoModule();
            LogUtils.print(TAG, "------>>> onServiceConnected  videoBinder: "+ videoBinder);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.print(TAG, "------>>> onServiceDisconnected");
            videoBinder.setVideoModule(null);
            videoModule = null;
            videoBinder = null;
        }
    };

    public void initSurfaceHolder(SurfaceView surfaceView){
        if (videoModule != null){
            videoModule.initSurfaceHolder(surfaceView);
        }
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;
    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) {
        this.currentPlayState = currentPlayState;
    }

    /**获取当前媒体播放对象**/
    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) {
        this.currentPlayMediaItem = currentPlayMediaItem;
        SpManager.getInstance().savePlayMediaItem(currentPlayMediaItem);
        sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_INFO);
    }

    /**获取当前媒体对象**/
    @Override
    public MediaData getCurrentPlayMediaItem() {
        return currentPlayMediaItem;
    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath) {
        return VideoScanManager.getInstance().getVideoMediaItem(GlobalTool.getInstance().getContext(),filePath);
    }

    /**获取当前播放状态**/
    @Override
    public int getCurrentPlayState() {
        return currentPlayState;
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) {
        this.currentPlayPosition = currentPlayPosition;
    }

    @Override
    public int getCurrentPlayPosition() {
        return currentPlayPosition;
    }

    @Override
    public int getCurrentListPosition() {
        return currentListPosition;
    }

    @Override
    public void setCurrentListPosition(int currentListPosition) {
        this.currentListPosition = currentListPosition;
    }

    public int[] getVideoPosition() {
        return videoPosition;
    }

    @Override
    public void setVideoPosition(int[] videoPosition) {
        this.videoPosition = videoPosition;
    }
    public boolean isFadeInNndOut() {
        return isFadeInNndOut;
    }

    /**
     * 动态设置淡入淡出效果
     * @param fadeInNndOut true为淡入淡出，false为非淡入淡出
     */
    public void setFadeInNndOut(boolean fadeInNndOut) {
        isFadeInNndOut = fadeInNndOut;
    }

    @Override
    public List<MediaListData> getOriginalData() {
        if (originalData  == null){
            originalData = new ArrayList<>();
        }
        return originalData;
    }

    @Override
    public List<MediaData> getPlayList() {
        if (playList  == null){
            playList = new ArrayList<>();
        }
        return playList;
    }

    @Override
    public void setPlayList(List<MediaData> playList) {
        this.playList = playList;
    }

    /**获取当前媒体文件总时长**/
    @Override
    public int getTotalTime() {
        if(mediaPlayer == null || !mediaPlayer.isPlaying()) {
            return currentPlayDuration;
        }
        currentPlayDuration = mediaPlayer.getDuration();
        return  currentPlayDuration< 0 ? 0 :currentPlayDuration;
    }


    @Override
    public void preVideo() {
        if (videoModule != null) {
            videoModule.preVideo();
        }
    }

    @Override
    public void nextVideo() {
        if (videoModule != null) {
            videoModule.nextVideo();
        }
    }

    @Override
    public void stopVideo(){
        if (videoModule != null) {
            videoModule.stopVideo();
            currentPlayDuration = 0 ;
        }
    }

    @Override
    public void playOrPause() {
        if (videoModule != null) {
            videoModule.playOrPause();
        }
    }

    @Override
    public void start() {
        if (videoModule != null) {
            videoModule.start();
        }
    }

    @Override
    public void playVideo(MediaData mediaItemInfo) {
        LogUtils.print(TAG," videoModule: "+ videoModule);
        if (videoModule != null) {
            videoModule.playVideo(mediaItemInfo);
        }
    }

    @Override
    public void pauseVideo() {
        if (videoModule != null) {
            videoModule.pauseVideo();
        }
    }

    @Override
    public void backForward() {
        if (videoModule != null) {
            videoModule.backForward();
        }
    }

    @Override
    public void fastForward() {
        if (videoModule != null) {
            videoModule.fastForward();
        }
    }

    @Override
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
        setCurrentPlayPosition(position);
    }

    @Override
    public void setVideoLayout() {
        if (videoModule != null) {
            videoModule.setVideoLayout();
        }
    }

    @Override
    public void clearUsbVideo(String usbPath){
        if (videoModule != null) {
            videoModule.clearUsbVideo(usbPath);
        }
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
    public boolean isCurrentPlayItem(MediaData itemInfo){
        boolean isCurrentPlayItem = false;
        if (itemInfo != null && currentPlayMediaItem != null){
            if (itemInfo.getFilePath() != null && currentPlayMediaItem.getFilePath() != null){
                if (itemInfo.getFilePath().equals(currentPlayMediaItem.getFilePath())){
                    isCurrentPlayItem = true;
                }
            }
        }
        return isCurrentPlayItem;
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard){
        for(UsbDevice device: getUsbDeviceList()){
            if(!TextUtils.isEmpty(device.getRootPath()) && !isCurrentDeviceScanFinished(device.getRootPath())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath){
        return VideoScanManager.getInstance().isUsbFileScanFinished(usbPath);
    }

    @Override
    public void setCPURefrain(boolean isRefrain){
		VideoScanManager.getInstance().setCPURefrain(isRefrain,1);
    }

    /*************************************************以下请求扫描数据*****************************************************/

    @Override
    public void requestVideoData(String usbPath, int dataType) {
        LogUtils.print(TAG,"---->> requestVideoData() dataType: "+dataType+" path: "+usbPath);
        setCurrentShowListDataType(dataType);
        setCurrentShowDataPath(usbPath);
        this.columnContentStr = null;
        VideoScanManager.getInstance().requestVideoData(GlobalTool.getInstance().getContext(),usbPath,dataType);
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
        return VideoScanManager.getInstance().updateMediaCollecte(isCollected, filePath);
    }

    /*************************************************以上请求扫描数据*****************************************************/


    @Override
    public boolean updateMediaItemName(MediaData itemInfo, boolean isCollection) {
        return false;
    }

    @Override
    public int getHeightLightPosition(){
        MediaData mediaData = getCurrentPlayMediaItem();
        List<MediaData> showDataList = getNewData();
        int playDataListType;
        int showDataListType = getCurrentDataListType();
        if (mediaData != null){
            playDataListType = mediaData.getDataType();
            if (playDataListType == showDataListType){
                return currentListPosition;
            }else {
                for (int i = 0; i < showDataList.size(); i++) {
                    if (isCurrentPlayItem(showDataList.get(i))){
                        return i;
                    }
                }
            }
        }
        int heightLight = getFirstFilePosition(showDataList,0);
        LogUtils.print(TAG," ---->> getHeightLightPosition() heightLight"+heightLight);
        return heightLight;
    }

    public int getFirstFilePosition(List<MediaData> showDataList,int currentPosition){
        if (currentPosition >= showDataList.size()){
            return -1;
        }
        if (showDataList.get(currentPosition).isFolder()){
            currentPosition = currentPosition + 1;
            getFirstFilePosition(showDataList,currentPosition);
        }
        return currentPosition;
    }

    @Override
    public int getCurrentDataListType() {
        return currentShowListDataType;
    }

    public void setCurrentShowListDataType(int dataType){
        this.currentShowListDataType = dataType;
    }


    @Override
        public String getUsbRootPathByFilePath(String filePath){
        if (TextUtils.isEmpty(filePath)){
            return null;
        }
        for (UsbDevice device:getUsbDeviceList()){
            if (filePath.contains(device.getRootPath())){
                return device.getRootPath();
            }
        }
        return null;
    }

    @Override
    public String getCurrentShowDataPath(){
        return currentShowPath;
    }

    private void setCurrentShowDataPath(String path){
        this.currentShowPath = path;
        SpManager.getInstance().setVideoShowMediaListPath(currentShowPath);
    }

    public String getCurrentPlayPath(){
        return currentPlayPath;
    }

    public void setCurrentPlayPath(String currentPlayPath) {
        this.currentPlayPath = currentPlayPath;
        SpManager.getInstance().setVideoPlayMediaListPath(currentPlayPath);
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath){
        if (TextUtils.isEmpty(currentPath)){
            return null;
        }
        return currentPath.substring(0, currentPath.lastIndexOf("/"));
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
    public List<UsbDevice> getUsbDeviceList() {
        if (usbDeviceList == null){
            usbDeviceList = new ArrayList<>();
        }
        return usbDeviceList;
    }

    @Override
    public void upperLevel(String usbPath) {
        LogUtils.print(TAG," == upperLevel() == " + getCurrentDataListType());
        switch(getCurrentDataListType()) {
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                handleTreeClassicFolderUpperLevel(usbPath);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                handleTwoClassicFolderUpperLevel(usbPath);
                break;
            default:
        }
    }

    private void handleTwoClassicFolderUpperLevel(String usbPath){
        if(isAllUsbUnMount()) {
            return;
        }
        requestVideoData(usbPath,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
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
                requestVideoData(currentShowPath,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                return;
            }
        }
        String folderPath = getUpperLevelFolderPath(currentShowPath);
        LogUtils.print(TAG,"==handleTreeClassicFolderUpperLevel 2 ==" + folderPath);
        if(TextUtils.isEmpty(folderPath)) {
            return;
        }
        LogUtils.print(TAG," ---->> handleTreeFolderTypeUpperLevel  folderPath:" + folderPath);
        requestVideoData(folderPath,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
    }

    @Override
    public void handleVideoDataChange(String usbPath){
        handleShowListDataChange(usbPath);
        handlePlayListDataChange(usbPath);
    }

    public void handleShowListDataChange(String usbPath){
        LogUtils.print(TAG,"==handleShowListDataChange==currentShowPath:" + currentShowPath + ",columnContentStr:" + columnContentStr + ",currentShowListDataType:" +
                currentShowListDataType);
        requestVideoData(currentShowPath,currentShowListDataType);
    }

    public void handlePlayListDataChange(String usbPath){
        int showDataType = getCurrentDataListType();
        if (getCurrentPlayMediaItem() == null || showDataType == getCurrentPlayMediaItem().getDataType()){
            return;
        }
        int playListDataType = getCurrentPlayMediaItem().getDataType();
        //更新播放列表数据
        switch (playListDataType){
            case ConstantsUtils.ListType.ALL_TYPE:
            case ConstantsUtils.ListType.COLLECTION_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                VideoScanManager.getInstance().requestVideoData(GlobalTool.getInstance().getContext(),usbPath,playListDataType);
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
                VideoScanManager.getInstance().requestVideoData(GlobalTool.getInstance().getContext(),usbPath,playListDataType);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                VideoScanManager.getInstance().requestVideoData(GlobalTool.getInstance().getContext(),currentShowPath,playListDataType);
                break;
            default:
        }
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
                playVideo(mediaData);
            }
        }
    }

}
