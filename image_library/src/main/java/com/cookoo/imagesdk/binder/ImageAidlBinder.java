package com.cookoo.imagesdk.binder;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.cookoo.imagesdk.IImageSdkService;
import com.cookoo.imagesdk.IImageServiceCallback;
import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.imp.IImageModule;
import com.cookoo.imagesdk.imp.ImageStateListener;
import com.cookoo.imagesdk.manager.ImageManager;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;

/**
 *
 * @author lsf
 * @date 2018/4/2
 */
public class ImageAidlBinder extends IImageSdkService.Stub implements ImageStateListener {
    private final String TAG = "ImageAidlBinder";
    private RemoteCallbackList<IImageServiceCallback> remoteCallbackList = new RemoteCallbackList<>();
    private ImageManager mImageManager = ImageManager.getInstance();
    private volatile boolean beginBroadcast = false;
    private int clientsNum = 0;

    public void addImageListener(IImageModule iImageMode) {
        ImageManager.getInstance().setImageModule(iImageMode);
        ImageManager.getInstance().bindMediaStateListener(this);
    }

    public void removeImageListener() {
        ImageManager.getInstance().setImageModule(null);
        ImageManager.getInstance().unbindMediaStateListener(this);
    }



    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) throws RemoteException {
        return mImageManager.isCurrentPlayItem(itemInfo);
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) throws RemoteException {
        return mImageManager.isCurrentUsbMount(usbPath);
    }

    @Override
    public boolean isAllUsbUnMount() throws RemoteException {
        return mImageManager.isAllUsbUnMount();
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) throws RemoteException {
        return mImageManager.isAllDeviceScanFinished(isFilterSdcard);
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) throws RemoteException {
        return mImageManager.isCurrentDeviceScanFinished(usbPath);
    }

    @Override
    public void setCPURefrain(boolean isRefrain) throws RemoteException {
        mImageManager.setCPURefrain(isRefrain);
    }

    @Override
    public void requestImageData(String usbPath, int dataType) {
        mImageManager.requestImageData(usbPath,dataType);
    }

    @Override
    public void handleImageDataChange(String usbPath) throws RemoteException {
        mImageManager.handleImageDataChange(usbPath);
    }

    @Override
    public List<MediaData> getPlayList() throws RemoteException {
        return mImageManager.getPlayList();
    }

    @Override
    public List<MediaData> getNewData() throws RemoteException {
        return mImageManager.getNewData();
    }

    @Override
    public List<MediaListData> getOriginalData() throws RemoteException {
        return mImageManager.getOriginalData();
    }

    @Override
    public void setPlayList(List<MediaData> list) throws RemoteException {
        mImageManager.setPlayList(list);
    }

    @Override
    public MediaData getSavePlayMediaItem() {
        return mImageManager.getSavePlayMediaItem();
    }

    @Override
    public MediaData getCurrentPlayMediaItem() throws RemoteException {
        return mImageManager.getCurrentPlayMediaItem();
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) throws RemoteException {
        mImageManager.setCurrentPlayMediaItem(currentPlayMediaItem);
    }

    @Override
    public boolean updateMediaItemName(MediaData musicInfo,boolean isCollection) throws RemoteException {
        return mImageManager.updateMediaItemName(musicInfo,isCollection);
    }

    @Override
    public boolean unCollected(MediaData mediaData) throws RemoteException{
        return mImageManager.unCollected(mediaData);
    }

    @Override
    public boolean collected(MediaData mediaData) throws RemoteException{
        return mImageManager.collected(mediaData);
    }

    @Override
    public int getCurrentListPosition() throws RemoteException {
        return mImageManager.getCurrentListPosition();
    }

    @Override
    public void setCurrentListPosition(int listPosition) throws RemoteException {
        mImageManager.setCurrentListPosition(listPosition);
    }

    @Override
    public int getHeightLightPosition() throws RemoteException {
        return mImageManager.getHeightLightPosition();
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() throws RemoteException {
        return mImageManager.getUsbDeviceList();
    }

    @Override
    public int getCurrentDataListType() throws RemoteException {
        return mImageManager.getCurrentDataListType();
    }

    @Override
    public String getCurrentShowDataPath() throws RemoteException {
        return mImageManager.getCurrentShowDataPath();
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) throws RemoteException {
        return mImageManager.getUpperLevelFolderPath(currentPath);
    }

    @Override
    public void upperLevel(String usbPath) throws RemoteException {
        mImageManager.upperLevel(usbPath);
    }

    @Override
    public boolean registerImageServiceCallback(IImageServiceCallback cb) throws RemoteException {
        if(cb != null && cb.asBinder().isBinderAlive()) {
            remoteCallbackList.register(cb);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterImageServiceCallback(IImageServiceCallback cb) throws RemoteException {
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

    /**
     * 回调播放信息状态
     *
     * @param eventId 状态参数，具体参考com.cookoo.mediasdk.ImageSdkConstants.ImageStateEventId
     */

    @Override
    public void onImageStateChanged(final int eventId) {
        if(eventId != ImageSdkConstants.ImageStateEventId.UPDATE_PLAY_TIME) {
            //更新时间的不需要打印
            LogUtils.print(TAG,"----->>> musicStateChanged() eventId:" + eventId);
        }
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IImageServiceCallback iBinderClient) {
                try {
                    iBinderClient.onImageStateChanged(eventId);
                } catch(RemoteException e) {
                    LogUtils.print(TAG,"--->>>musicStateChanged() eeee: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 回调扫描状态
     *
     * @param eventId 状态参数，具体参考com.cookoo.mediasdk.ImageSdkConstants.ImageStateEventId
     */
    @Override
    public void onImageScanChanged(final int eventId) {
        LogUtils.print(TAG,"----->>> musicScanChanged() eventId:" + eventId);
        doCallBack(new DoCallBack() {
            @Override
            public void doCallBack(IImageServiceCallback iBinderClient) {
                try {
                    iBinderClient.onImageScanChanged(eventId);
                } catch(RemoteException e) {
                    LogUtils.print(TAG,"----->>> musicScanChanged() eventId:" + eventId);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void startSlide() throws RemoteException {
        mImageManager.startSlide();
    }

    @Override
    public void endSlidePlay() throws RemoteException {
        mImageManager.endSlidePlay();
    }

    @Override
    public void clearUsbImage(String usbPath) throws RemoteException {
        mImageManager.clearUsbImage(usbPath);
    }

    @Override
    public void zoomOut() throws RemoteException {
        mImageManager.zoomOut();
    }

    @Override
    public void zoomIn() throws RemoteException {
        mImageManager.zoomIn();
    }

    @Override
    public void rotate(int angle) throws RemoteException {
        mImageManager.rotate(angle);
    }

    @Override
    public void preImage() throws RemoteException {
        mImageManager.preImage();
    }

    @Override
    public void nextImage() throws RemoteException {
        mImageManager.nextImage();
    }

    public interface DoCallBack {
        /**
         * 封装通知远程方法
         * @param iBinderClient
         */
        void doCallBack(IImageServiceCallback iBinderClient);
    }
}
