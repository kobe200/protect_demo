package com.cookoo.videosdkclient.manager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.cookoo.videosdk.IVideoSdkService;
import com.cookoo.videosdk.IVideoServiceCallback;
import com.cookoo.videosdkclient.hold.VideoManagerApi;
import com.cookoo.videosdkclient.load.DisCacheUtil;
import com.cookoo.videosdkclient.utils.LogUtils;
import com.cookoo.videosdkclient.utils.VideoSdkConstants;

import java.util.ArrayList;
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

public class VideoAidlManager extends BaseManager implements VideoManagerApi {


    private static final String TAG = "VideoAidlManager";

    private VideoAidlManager() {
    }

    public static VideoAidlManager getInstance() {
        return VideoManagerInstance.BINDER_AIDL_MANAGER;
    }

    private static class VideoManagerInstance {
        private static final VideoAidlManager BINDER_AIDL_MANAGER = new VideoAidlManager();
    }

    private Context mContext;
    private IVideoSdkService iVideoService;
    private boolean isBinder;

    private final static int VIDEO_STATE_CHANGED = 0;
    private final static int VIDEO_SCAN_CHANGED = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VIDEO_STATE_CHANGED:
                    sendVideoStateEvent(msg.arg1);
                    break;
                case VIDEO_SCAN_CHANGED:
                    sendScanStateEvent(msg.arg1);
                    break;
                default:
            }
        }
    };

    public Context getContext() {
        return mContext;
    }

    public void doBinderVideoService(Context context){
        LogUtils.print(TAG,"------->> doBinderVideoService()->" + context.getPackageName());
        if (context == null || isBinder && iVideoService != null) {
            return;
        }
        if (!(context instanceof Application)) {
            this.mContext = context.getApplicationContext();
        }else {
            this.mContext = context;
        }
        DisCacheUtil.getInstance(mContext).removeCacheBitmap();
        Intent serviceIntent = new Intent("carnetapp.video.videosdk.videoservice");
        serviceIntent.setPackage("com.cookoo.mediatest");
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void doUnbinderVideoService(Context context){
        context.unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iVideoService = IVideoSdkService.Stub.asInterface(iBinder);
            isBinder = true;
            LogUtils.print(TAG,"------->> onServiceConnected() iVideoService： "+iVideoService);
            try {
                iVideoService.registerVideoServiceCallback(callback);
            } catch (Exception e) {
                LogUtils.print(TAG,"------->> onServiceConnected() eee： "+e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.print(TAG,"------->> onServiceDisconnected()");
            try {
                iVideoService.unregisterVideoServiceCallback(callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
            iVideoService = null;
            isBinder = false;
        }
    };

    private IVideoServiceCallback.Stub callback = new IVideoServiceCallback.Stub() {
        @Override
        public void onVideoStateChanged(int eventId) throws RemoteException {
            //不用打印时间信息
            if (eventId != VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME) {
                LogUtils.print(TAG, "------->> onVideoStateChanged() eventId: " + eventId);
            }
            mHandler.removeMessages(VIDEO_STATE_CHANGED);
            Message message = Message.obtain();
            message.what = VIDEO_STATE_CHANGED;
            message.arg1 = eventId;
            mHandler.sendMessage(message);
        }

        @Override
        public void onVideoScanChanged(int eventId) throws RemoteException {
            LogUtils.print(TAG,"------->> onVideoScanChanged() eventId: "+eventId);
            mHandler.removeMessages(VIDEO_SCAN_CHANGED);
            Message message = Message.obtain();
            message.what = VIDEO_SCAN_CHANGED;
            message.arg1 = eventId;
            mHandler.sendMessage(message);
        }
    };

    @Override
    public int getTotalTime() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getTotalTime();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void preVideo() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.preVideo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void nextVideo() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.nextVideo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playOrPause() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.playOrPause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.start();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopVideo() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.stopVideo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playVideo(MediaData mediaData) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.playVideo(mediaData);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pauseVideo() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.pauseVideo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void backForward() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.backForward();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fastForward() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.fastForward();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void seekTo(int position) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.seekTo(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setVideoLayout() {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setVideoLayout();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clearUsbVideo(String usbPath) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.clearUsbVideo(usbPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) {
        if (isBinder && iVideoService != null) {
            try {
                return  iVideoService.isCurrentPlayItem(itemInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.isCurrentUsbMount(usbPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllUsbUnMount() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.isAllUsbUnMount();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.isAllDeviceScanFinished(isFilterSdcard);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.isCurrentDeviceScanFinished(usbPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setCPURefrain(boolean isRefrain) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setCPURefrain(isRefrain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleVideoDataChange(String usbPath) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.handleVideoDataChange(usbPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestVideoData(String path,int dataType) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.requestVideoData(path,dataType);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getSavePlayMediaItemPath() {
        if (isBinder && iVideoService != null) {
            try {
                return  iVideoService.getSavePlayMediaItemPath();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getSavePlayMediaItemDataType() {
        if (isBinder && iVideoService != null) {
            try {
                return  iVideoService.getSavePlayMediaItemDataType();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getSavePlayProgress() {
        if (isBinder && iVideoService != null) {
            try {
                return  iVideoService.getSavePlayProgress();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public MediaData getCurrentPlayMediaItem() {
        if (isBinder && iVideoService != null) {
            try {
                return  iVideoService.getCurrentPlayMediaItem();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setCurrentPlayMediaItem(currentPlayMediaItem);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public MediaItemInfo getMediaItemInfo(String filePath){
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getMediaItemInfo(filePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean updateMediaItemName(MediaData itemInfo, boolean isCollection) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.updateMediaItemName(itemInfo,isCollection);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean unCollected(MediaData mediaData){
        if(isBinder && iVideoService != null) {
            try {
                return iVideoService.unCollected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean collected(MediaData mediaData){
        if(isBinder && iVideoService != null) {
            try {
                return iVideoService.collected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public int getCurrentPlayState() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getCurrentPlayState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentPlayState(int currentPlayState) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setCurrentPlayState(currentPlayState);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentListPosition() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getCurrentListPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentListPosition(int listPosition) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setCurrentListPosition(listPosition);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentPlayPosition() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getCurrentPlayPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentPlayPosition(int currentPlayPosition) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setCurrentPlayPosition(currentPlayPosition);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setVideoPosition(int[] videoPosition) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setVideoPosition(videoPosition);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getHeightLightPosition() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getHeightLightPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getCurrentDataListType() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getCurrentDataListType();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getUsbRootPathByFilePath(String filePath) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getUsbRootPathByFilePath(filePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getCurrentShowDataPath() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getCurrentShowDataPath();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getUpperLevelFolderPath(currentPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setPlayList(List<MediaData> playList) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.setPlayList(playList);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<MediaData> getPlayList() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getPlayList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<MediaData> getNewData() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getNewData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<MediaListData> getOriginalData() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getOriginalData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() {
        if (isBinder && iVideoService != null) {
            try {
                return iVideoService.getUsbDeviceList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void upperLevel(String usbPath) {
        if (isBinder && iVideoService != null) {
            try {
                iVideoService.upperLevel(usbPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
