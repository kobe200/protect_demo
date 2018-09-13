package com.cookoo.musicsdk.binder;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.cookoo.musicsdk.IMusicSdkService;
import com.cookoo.musicsdk.IMusicServiceCallback;
import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.imp.IMusicMode;
import com.cookoo.musicsdk.imp.MusicStateListener;
import com.cookoo.musicsdk.manager.MusicManager;
import com.cookoo.musicsdk.utils.LogUtils;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;

/**
 * @author lsf
 * @date 2018/4/2
 */
public class MusicAidlBinder extends IMusicSdkService.Stub implements MusicStateListener {
    private final String TAG = "MusicAidlBinder";
    private RemoteCallbackList<IMusicServiceCallback> remoteCallbackList = new RemoteCallbackList<>();
    private MusicManager mMusicManager = MusicManager.getInstance();
    private volatile boolean beginBroadcast = false;
    private int clientsNum = 0;

    public void addMusicListener(IMusicMode iMusicMode) {
        MusicManager.getInstance().setMusicMode(iMusicMode);
        MusicManager.getInstance().bindMediaStateListener(this);
    }

    public void removeMusicListener() {
        MusicManager.getInstance().setMusicMode(null);
        MusicManager.getInstance().unbindMediaStateListener(this);
    }

    /***********************************************************以下音乐播放的操作方法************************************************************************************/

    @Override
    public void pauseMusic() throws RemoteException {
        mMusicManager.pauseMusic();
    }

    @Override
    public void stopMusic() throws RemoteException {
        mMusicManager.stopMusic();
    }

    @Override
    public void backForward() throws RemoteException {
        mMusicManager.backForward();
    }

    @Override
    public void fastForward() throws RemoteException {
        mMusicManager.fastForward();
    }

    @Override
    public void nextMusic(boolean isUser) throws RemoteException {
        mMusicManager.nextMusic(isUser);
    }

    @Override
    public void preMusic() throws RemoteException {
        mMusicManager.preMusic();
    }

    @Override
    public void playOrPause() throws RemoteException {
        mMusicManager.playOrPause();
    }

    @Override
    public void startMusic() throws RemoteException {
        mMusicManager.startMusic();
    }

    @Override
    public void playMusic(MediaData MediaData) throws RemoteException {
        mMusicManager.playMusic(MediaData);
    }

    @Override
    public void seekTo(int position) throws RemoteException {
        mMusicManager.seekTo(position);
    }

    @Override
    public void clearUsbMusic(String usbPath) throws RemoteException {
        mMusicManager.clearUsbMusic(usbPath);
    }

    @Override
    public int getTotalTime() throws RemoteException {
        return mMusicManager.getTotalTime();
    }

    @Override
    public int getCurrentTime() throws RemoteException {
        return mMusicManager.getCurrentTime();
    }

    @Override
    public void savePlayMode(int position) throws RemoteException {
        mMusicManager.savePlayMode(position);
    }

    @Override
    public int getCurrentPlayMode() throws RemoteException {
        return mMusicManager.getCurrentPlayMode();
    }

    @Override
    public void setCurrentPlayMode(int currentPlayMode) throws RemoteException {
        mMusicManager.setCurrentPlayMode(currentPlayMode);
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) throws RemoteException {
        return mMusicManager.isCurrentPlayItem(itemInfo);
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) throws RemoteException {
        return mMusicManager.isCurrentUsbMount(usbPath);
    }

    @Override
    public boolean isAllUsbUnMount() throws RemoteException {
        return mMusicManager.isAllUsbUnMount();
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) throws RemoteException {
        return mMusicManager.isAllDeviceScanFinished(isFilterSdcard);
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) throws RemoteException {
        return mMusicManager.isCurrentDeviceScanFinished(usbPath);
    }

    @Override
    public void setCPURefrain(boolean isRefrain) throws RemoteException {
        mMusicManager.setCPURefrain(isRefrain);
    }

    @Override
    public void requestMusicData(String usbPath,String columnContentStr,int dataType) throws RemoteException {
        mMusicManager.requestMusicData(usbPath,columnContentStr,dataType);
    }

    @Override
    public void handleMusicDataChange(String usbPath) throws RemoteException {
        mMusicManager.handleMusicDataChange(usbPath);
    }

    @Override
    public boolean unCollected(MediaData mediaData) throws RemoteException{
        return mMusicManager.unCollected(mediaData);
    }

    @Override
    public boolean collected(MediaData mediaData) throws RemoteException{
        return mMusicManager.collected(mediaData);
    }

    @Override
    public MediaData getCurrentPlayMediaItem() throws RemoteException {
        return mMusicManager.getCurrentPlayMediaItem();
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData mediaData) throws RemoteException {
        mMusicManager.setCurrentPlayMediaItem(mediaData);
    }

    @Override
    public String getSavePlayMediaItemPath() {
        return mMusicManager.getSavePlayMediaItemPath();
    }

    @Override
    public int getSavePlayMediaItemDataType() {
        return mMusicManager.getSavePlayMediaItemDataType();
    }

    @Override
    public int getSavePlayProgress() {
        return mMusicManager.getSavePlayProgress();
    }

    @Override
    public int getSavePlayMode() {
        return mMusicManager.getSavePlayMode();
    }

    @Override
    public String getCurrentShowDataPath() throws RemoteException{
        return mMusicManager.getCurrentShowDataPath();
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath){
        return mMusicManager.getUpperLevelFolderPath(currentPath);
    }

    @Override
    public List<MediaData> getNewData() throws RemoteException {
        return mMusicManager.getNewData();
    }

    @Override
    public List<MediaData> getPlayList() throws RemoteException {
        return mMusicManager.getPlayList();
    }

    @Override
    public void setPlayList(List<MediaData> playList) throws RemoteException {
        mMusicManager.setPlayList(playList);
    }

    @Override
    public boolean updateMediaItemName(MediaData itemInfo,boolean isCollection) throws RemoteException {
        return mMusicManager.updateMediaItemName(itemInfo,isCollection);
    }

    @Override
    public int getCurrentPlayState() throws RemoteException {
        return mMusicManager.getCurrentPlayState();
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) throws RemoteException {
        mMusicManager.setCurrentPlayState(currentPlayState);
    }

    @Override
    public int getCurrentDataListType() throws RemoteException {
        return mMusicManager.getCurrentDataListType();
    }

    @Override
    public int getCurrentListPosition() throws RemoteException {
        return mMusicManager.getCurrentListPosition();
    }

    @Override
    public void setCurrentListPosition(int listPosition) throws RemoteException {
        mMusicManager.setCurrentListPosition(listPosition);
    }

    @Override
    public int getCurrentPlayPosition() throws RemoteException {
        return mMusicManager.getCurrentPlayPosition();
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) throws RemoteException {
        mMusicManager.setCurrentPlayPosition(currentPlayPosition);
    }

    @Override
    public int getHeightLightPosition() throws RemoteException {
        return mMusicManager.getHeightLightPosition();
    }

    @Override
    public List<MediaListData> getOriginalData() throws RemoteException {
        return mMusicManager.getOriginalData();
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() throws RemoteException {
        return mMusicManager.getUsbDeviceList();
    }

    @Override
    public void upperLevel(String usbPath) throws RemoteException {
        mMusicManager.upperLevel(usbPath);
    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath) throws RemoteException {
        return mMusicManager.getMediaItemInfo(filePath);
    }

    @Override
    public String getCurrentPlayPath() throws RemoteException {
        return mMusicManager.getCurrentPlayPath();
    }

    @Override
    public void setCurrentPlayPath(String currentPlayPath) throws RemoteException {
        mMusicManager.setCurrentPlayPath(currentPlayPath);
    }

    @Override
    public String getColumnContentStr() throws RemoteException {
        return mMusicManager.getColumnContentStr();
    }

    @Override
    public void handleMemoryPlayback() throws RemoteException {
        mMusicManager.handleMemoryPlayback();
    }

    @Override
    public void setFadeInNndOut(boolean fadeInNndOut) throws RemoteException {
        mMusicManager.setFadeInNndOut(fadeInNndOut);
    }

    @Override
    public boolean isFadeInNndOut() throws RemoteException {
        return mMusicManager.isFadeInNndOut();
    }


    /***********************************************************以上音乐播放的操作方法************************************************************************************/


    @Override
    public boolean registerMusicServiceCallback(IMusicServiceCallback cb) throws RemoteException {
        LogUtils.print(TAG , "==registerMusicServiceCallback==" + cb);
        if(cb != null && cb.asBinder().isBinderAlive()) {
            remoteCallbackList.register(cb);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterMusicServiceCallback(IMusicServiceCallback cb) throws RemoteException {
        LogUtils.print(TAG , "==unregisterMusicServiceCallback==" + cb);
        if(cb != null) {
            remoteCallbackList.unregister(cb);
            return true;
        }
        return false;
    }

    public void doCallBack(DoCallBack doCallBack) {
        if(!beginBroadcast) {
            beginBroadcast = true;
            clientsNum = remoteCallbackList.beginBroadcast();
        }
        for(int i = 0; i < clientsNum; i++) {
            try {
                if(remoteCallbackList.getBroadcastItem(i) != null){
                    doCallBack.doCallBack(remoteCallbackList.getBroadcastItem(i));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(beginBroadcast) {
            remoteCallbackList.finishBroadcast();
            beginBroadcast = false;
        }
    }

    /**
     * 回调播放信息状态
     * @param eventId 状态参数，具体参考com.cookoo.mediasdk.MusicSdkConstants.MusicStateEventId
     */

    @Override
    public void onMusicStateChanged(final int eventId) {
        if(eventId != MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_TIME) {
            //更新时间的不需要打印
            LogUtils.print(TAG,"---start2-->>> musicStateChanged() eventId:" + eventId);
        }
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IMusicServiceCallback iBinderClient) {
                try {
                    iBinderClient.onMusicStateChanged(eventId);
                } catch(RemoteException e) {
                    LogUtils.print(TAG,"-Exception-->>>musicStateChanged() eeee: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 回调扫描状态
     * @param eventId 状态参数，具体参考com.cookoo.mediasdk.MusicSdkConstants.MusicStateEventId
     */
    @Override
    public void onMusicScanChanged(final int eventId) {
        LogUtils.print(TAG,"--onMusicScanChanged--->>> musicScanChanged() eventId:" + eventId);
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IMusicServiceCallback iBinderClient) {
                try {
                    iBinderClient.onMusicScanChanged(eventId);
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface DoCallBack {
        void doCallBack(IMusicServiceCallback iBinderClient);
    }
}
