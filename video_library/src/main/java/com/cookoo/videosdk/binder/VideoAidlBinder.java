package com.cookoo.videosdk.binder;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.cookoo.videosdk.IVideoSdkService;
import com.cookoo.videosdk.IVideoServiceCallback;
import com.cookoo.videosdk.imp.IVideoModule;
import com.cookoo.videosdk.imp.VideoStateListener;
import com.cookoo.videosdk.manager.VideoManager;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;

/**
 *
 * @author lsf
 * @date 2018/4/18
 */

public class VideoAidlBinder extends IVideoSdkService.Stub implements VideoStateListener {

    private final String TAG = "VideoAidlBinder";
    private RemoteCallbackList<IVideoServiceCallback> remoteCallbackList = new RemoteCallbackList<>();
    private VideoManager mVideoManager = VideoManager.getInstance();
    private volatile boolean beginBroadcast = false;
    private int clientsNum = 0;
    private IVideoModule videoModule;

    public void addVideoListener(IVideoModule iVideoModule) {
        setVideoModule(iVideoModule);
        VideoManager.getInstance().bindMediaStateListener(this);
    }

    public void removeVideoListener() {
        videoModule = null;
        VideoManager.getInstance().unbindMediaStateListener(this);
    }

    @Override
    public void playVideo(MediaData mediaItem) throws RemoteException {
        mVideoManager.playVideo(mediaItem);
    }

    @Override
    public void pauseVideo() throws RemoteException {
        mVideoManager.pauseVideo();
    }

    @Override
    public void stopVideo() throws RemoteException {
        mVideoManager.stopVideo();
    }

    @Override
    public void nextVideo() throws RemoteException {
        mVideoManager.nextVideo();
    }

    @Override
    public void preVideo() throws RemoteException {
        mVideoManager.preVideo();
    }

    @Override
    public int getTotalTime() throws RemoteException {
        return mVideoManager.getTotalTime();
    }

    @Override
    public void playOrPause() throws RemoteException {
        mVideoManager.playOrPause();
    }

    @Override
    public void start() throws RemoteException {
        mVideoManager.start();
    }

    @Override
    public void backForward() throws RemoteException {
        mVideoManager.backForward();
    }

    @Override
    public void fastForward() throws RemoteException {
        mVideoManager.fastForward();
    }

    @Override
    public void seekTo(int position) throws RemoteException {
        mVideoManager.seekTo(position);
    }

    @Override
    public void setVideoLayout() {
        mVideoManager.setVideoLayout();
    }

    @Override
    public void clearUsbVideo(String usbPath) throws RemoteException {
        mVideoManager.clearUsbVideo(usbPath);
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) throws RemoteException {
        return mVideoManager.isCurrentPlayItem(itemInfo);
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) throws RemoteException {
        return mVideoManager.isCurrentUsbMount(usbPath);
    }

    @Override
    public boolean isAllUsbUnMount() throws RemoteException {
        return mVideoManager.isAllUsbUnMount();
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) throws RemoteException {
        return mVideoManager.isAllDeviceScanFinished(isFilterSdcard);
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) throws RemoteException {
        return mVideoManager.isCurrentDeviceScanFinished(usbPath);
    }

    @Override
    public void setCPURefrain(boolean isRefrain) throws RemoteException {
        mVideoManager.setCPURefrain(isRefrain);
    }

    @Override
    public void requestVideoData(String path,int dataType) throws RemoteException {
        mVideoManager.requestVideoData(path,dataType);
    }

    @Override
    public void handleMusicDataChange(String usbPath) throws RemoteException {
        mVideoManager.handleVideoDataChange(usbPath);
    }

    @Override
    public String getSavePlayMediaItemPath() throws RemoteException {
        return mVideoManager.getSavePlayMediaItemPath();
    }

    @Override
    public int getSavePlayMediaItemDataType() throws RemoteException {
        return mVideoManager.getSavePlayMediaItemDataType();
    }

    @Override
    public int getSavePlayProgress() throws RemoteException {
        return mVideoManager.getSavePlayProgress();
    }

    @Override
    public void handleVideoDataChange(String usbPath) {
        mVideoManager.handleVideoDataChange(usbPath);
    }

    @Override
    public List<MediaData> getPlayList() throws RemoteException {
        return mVideoManager.getPlayList();
    }

    @Override
    public List<MediaData> getNewData() throws RemoteException {
        return mVideoManager.getNewData();
    }

    @Override
    public List<MediaListData> getOriginalData() throws RemoteException {
        return mVideoManager.getOriginalData();
    }

    @Override
    public void setPlayList(List<MediaData> list) throws RemoteException {
        mVideoManager.setPlayList(list);
    }

    @Override
    public MediaData getCurrentPlayMediaItem() throws RemoteException {
        return mVideoManager.getCurrentPlayMediaItem();
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) throws RemoteException {
        mVideoManager.setCurrentPlayMediaItem(currentPlayMediaItem);
    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath){
        return mVideoManager.getMediaItemInfo(filePath);
    }

    @Override
    public boolean updateMediaItemName(MediaData musicInfo, boolean isCollection) throws RemoteException {
        return mVideoManager.updateMediaItemName(musicInfo, isCollection);
    }

    @Override
    public boolean unCollected(MediaData mediaData) throws RemoteException{
        return mVideoManager.unCollected(mediaData);
    }

    @Override
    public boolean collected(MediaData mediaData) throws RemoteException{
        return mVideoManager.collected(mediaData);
    }

    @Override
    public int getCurrentPlayState() throws RemoteException {
        return mVideoManager.getCurrentPlayState();
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) throws RemoteException {
        mVideoManager.setCurrentPlayState(currentPlayState);
    }

    @Override
    public int getCurrentListPosition() throws RemoteException {
        return mVideoManager.getCurrentListPosition();
    }

    @Override
    public void setCurrentListPosition(int listPosition) throws RemoteException {
        mVideoManager.setCurrentListPosition(listPosition);
    }

    @Override
    public int getCurrentPlayPosition() throws RemoteException {
        return mVideoManager.getCurrentPlayPosition();
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) throws RemoteException {
        mVideoManager.setCurrentPlayPosition(currentPlayPosition);
    }

    @Override
    public void setVideoPosition(int[] videoPosition) {
        mVideoManager.setVideoPosition(videoPosition);
    }

    @Override
    public int getHeightLightPosition() throws RemoteException {
        return mVideoManager.getHeightLightPosition();
    }

    @Override
    public int getCurrentDataListType() throws RemoteException {
        return mVideoManager.getCurrentDataListType();
    }

    @Override
    public String getUsbRootPathByFilePath(String filePath) throws RemoteException {
        return mVideoManager.getUsbRootPathByFilePath(filePath);
    }

    @Override
    public String getCurrentShowDataPath() throws RemoteException {
        return mVideoManager.getCurrentShowDataPath();
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) throws RemoteException {
        return mVideoManager.getUpperLevelFolderPath(currentPath);
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() throws RemoteException {
        return mVideoManager.getUsbDeviceList();
    }

    @Override
    public void upperLevel(String usbPath) throws RemoteException {
        mVideoManager.upperLevel(usbPath);
    }

    @Override
    public boolean registerVideoServiceCallback(IVideoServiceCallback cb) throws RemoteException {
        if(cb != null && cb.asBinder().isBinderAlive()) {
            remoteCallbackList.register(cb);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterVideoServiceCallback(IVideoServiceCallback cb) throws RemoteException {
        if(cb != null) {
            remoteCallbackList.unregister(cb);
            return true;
        }
        return false;
    }

    public synchronized void doCallBack(DoCallBack doCallBack) {
        if(!beginBroadcast) {
            beginBroadcast = true;
            clientsNum = remoteCallbackList.beginBroadcast();
        }
        for(int i = 0; i < clientsNum; i++) {
            try {
                doCallBack.doCallBack(remoteCallbackList.getBroadcastItem(i));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(beginBroadcast) {
            remoteCallbackList.finishBroadcast();
            beginBroadcast = false;
        }
    }

    @Override
    public void onVideoStateChanged(final int eventId) {
        if(eventId != VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME) {
            //更新时间的不需要打印
            LogUtils.print(TAG,"----->>> onVideoStateChanged() eventId:" + eventId);
        }
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IVideoServiceCallback iBinderClient) {
                try {
                    iBinderClient.onVideoStateChanged(eventId);
                } catch(RemoteException e) {
                    LogUtils.print(TAG,"--->>>onVideoStateChanged() eeee: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onVideoScanChanged(final int eventId) {
        LogUtils.print(TAG,"----->>> onVideoScanChanged() eventId:" + eventId);
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IVideoServiceCallback iBinderClient) {
                try {
                    iBinderClient.onVideoScanChanged(eventId);
                } catch(RemoteException e) {
                    LogUtils.print(TAG,"----->>> onVideoScanChanged() eventId:" + eventId);
                    e.printStackTrace();
                }
            }
        });
    }

    public IVideoModule getVideoModule() {
        return videoModule;
    }

    public void setVideoModule(IVideoModule videoModule) {
        this.videoModule = videoModule;
    }

    public interface DoCallBack {
        /**
         * 封装通知远程方法
         * @param iBinderClient
         */
        void doCallBack(IVideoServiceCallback iBinderClient);
    }
}
