package com.cookoo.musicsdkclient.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.cookoo.musicsdk.IMusicSdkService;
import com.cookoo.musicsdk.IMusicServiceCallback;
import com.cookoo.musicsdkclient.MusicSdkConstants;
import com.cookoo.musicsdkclient.hold.MusicManagerApi;
import com.cookoo.musicsdkclient.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;


/**
 * @author lsf
 * @date 2018/4/8
 */


public class MusicAidlManager extends BaseManager implements MusicManagerApi {

    private static final String TAG = "MusicAidlManager";
    private final static int MUSIC_STATE_CHANGED = 0;
    private final static int MUSIC_SCAN_CHANGED = 1;
    private Context mContext;
    private IMusicSdkService iMusicService;
    private boolean isBinder;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch((Integer) msg.obj) {
                case MUSIC_STATE_CHANGED:
                    sendMusicStateEvent(msg.what);
                    break;
                case MUSIC_SCAN_CHANGED:
                    sendScanStateEvent(msg.what);
                    break;
                default:
            }
        }
    };
    private IMusicServiceCallback.Stub callback = new IMusicServiceCallback.Stub() {
        @Override
        public void onMusicStateChanged(int eventId) throws RemoteException {
            //不用打印时间信息
            if(eventId != MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_TIME) {
                LogUtils.print(TAG,"------->> onMusicStateChanged() eventId: " + eventId);
            }
            mHandler.removeMessages(eventId);
            mHandler.obtainMessage(eventId,MUSIC_STATE_CHANGED).sendToTarget();
        }

        @Override
        public void onMusicScanChanged(int eventId) throws RemoteException {
            LogUtils.print(TAG,"------->> onMusicScanChanged() eventId: " + eventId);
            mHandler.removeMessages(eventId);
            mHandler.obtainMessage(eventId,MUSIC_SCAN_CHANGED).sendToTarget();
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,IBinder iBinder) {
            iMusicService = IMusicSdkService.Stub.asInterface(iBinder);
            isBinder = true;
            LogUtils.print(TAG,"------->> onServiceConnected() iMusicService： " + iMusicService);
            try {
                iMusicService.registerMusicServiceCallback(callback);
            } catch(Exception e) {
                LogUtils.print(TAG,"------->> onServiceConnected() eee： " + e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.print(TAG,"------->> onServiceDisconnected()");
            try {
                iMusicService.unregisterMusicServiceCallback(callback);
            } catch(Exception e) {
                e.printStackTrace();
            }
            iMusicService = null;
            isBinder = false;
        }
    };

    private MusicAidlManager() {
    }

    public static MusicAidlManager getInstance() {
        return MusicManagerInstance.MUSIC_AIDL_MANAGER;
    }

    public void doBinderMusicService(Context context) {
        LogUtils.print(TAG,"------->> doBinderMusicService()");
        if(context == null || isBinder && iMusicService != null) {
            return;
        }
        this.mContext = context;
        Intent serviceIntent = new Intent("carnetapp.music.musicsdk.musicservice");
        //        serviceIntent.setPackage("com.cookoo.mediatest");
        //TODO 发布的版本需要使用以下的包名
        serviceIntent.setPackage("carnetapp.music");
        context.bindService(serviceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    public void doUnbinderMusicService(Context context) {
        context.unbindService(serviceConnection);
    }

    @Override
    public int getTotalTime() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getTotalTime();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getCurrentTime() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentTime();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void preMusic() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.preMusic();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void nextMusic(boolean isUser) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.nextMusic(isUser);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playOrPause() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.playOrPause();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startMusic() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.startMusic();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playMusic(MediaData musicInfo) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.playMusic(musicInfo);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pauseMusic() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.pauseMusic();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopMusic() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.stopMusic();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void backForward() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.backForward();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fastForward() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.fastForward();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void seekTo(int position) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.seekTo(position);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clearUsbMusic(String usbPath) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.clearUsbMusic(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void savePlayMode(int position) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.savePlayMode(position);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentPlayMode() {
        LogUtils.print(TAG,"------>>> getPlayMode() isBinder: " + isBinder + "  iMusicService: " + iMusicService);
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentPlayMode();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentPlayMode(int currentPlayMode) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentPlayMode(currentPlayMode);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isCurrentPlayItem(itemInfo);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isCurrentUsbMount(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllUsbUnMount() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isAllUsbUnMount();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isAllDeviceScanFinished(isFilterSdcard);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isCurrentDeviceScanFinished(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setCPURefrain(boolean isRefrain) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCPURefrain(isRefrain);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestMusicData(String filePath,String columnContentStr,int dataType) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.requestMusicData(filePath,columnContentStr,dataType);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleMusicDataChange(String usbPath) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.handleMusicDataChange(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean unCollected(MediaData mediaData){
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.unCollected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean collected(MediaData mediaData){
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.collected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public List<MediaData> getPlayList() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getPlayList();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void setPlayList(List<MediaData> list) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setPlayList(list);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getUsbDeviceList();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void upperLevel(String usbPath) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.upperLevel(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getMediaItemInfo(filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<MediaData> getNewData() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getNewData();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<MediaListData> getOriginalData() {
        LogUtils.print(TAG,"  ----->> getAllUsbMusicData() isBinder: " + isBinder + "  iMusicService: " + iMusicService);
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getOriginalData();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public MediaData getCurrentPlayMediaItem() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentPlayMediaItem();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentPlayMediaItem(currentPlayMediaItem);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getSavePlayMediaItemPath() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getSavePlayMediaItemPath();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getSavePlayMediaItemDataType() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getSavePlayMediaItemDataType();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getSavePlayProgress() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getSavePlayProgress();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getSavePlayMode() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getSavePlayMode();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getCurrentShowDataPath() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentShowDataPath();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getCurrentPlayPath() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentPlayPath();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setCurrentPlayPath(String filePath) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentPlayPath(filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getColumnContentStr() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getColumnContentStr();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void handleMemoryPlayback() {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.handleMemoryPlayback();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isFadeInNndOut() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.isFadeInNndOut();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setFadeInNndOut(boolean fadeInNndOut) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setFadeInNndOut(fadeInNndOut);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getUpperLevelFolderPath(currentPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean updateMediaItemName(MediaData itemInfo,boolean isCollection) {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.updateMediaItemName(itemInfo,isCollection);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int getCurrentPlayState() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentPlayState();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentPlayState(currentPlayState);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentDataListType() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentDataListType();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getCurrentListPosition() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentListPosition();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentListPosition(int listPosition) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentListPosition(listPosition);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentPlayPosition() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getCurrentPlayPosition();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) {
        if(isBinder && iMusicService != null) {
            try {
                iMusicService.setCurrentPlayPosition(currentPlayPosition);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getHeightLightPosition() {
        if(isBinder && iMusicService != null) {
            try {
                return iMusicService.getHeightLightPosition();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static class MusicManagerInstance {
        private static final MusicAidlManager MUSIC_AIDL_MANAGER = new MusicAidlManager();
    }

}
