package com.cookoo.imagesdkclient.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;

import com.cookoo.imagesdk.IImageSdkService;
import com.cookoo.imagesdk.IImageServiceCallback;
import com.cookoo.imagesdkclient.ImageSdkConstants;
import com.cookoo.imagesdkclient.hold.ImageManagerApi;
import com.cookoo.imagesdkclient.utils.GlobalTool;
import com.cookoo.imagesdkclient.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import uk.co.senab.photoview.PhotoView;


/**
 * @author lsf
 * @date 2018/4/8
 */


public class ImageAidlManager extends BaseManager implements ImageManagerApi {

    private static final String TAG = "ImageAidlManager";
    private final static int IMAGE_STATE_CHANGED = 0;
    private final static int IMAGE_SCAN_CHANGED = 1;
    private final static int IMAGE_SLIDER_PLAY = 2;
    private Context mContext;
    private IImageSdkService iImageService;
    private boolean isBinder;
    /**
     * 当前播放列表路径
     */
    private String currentPlayFilePath = "";
    /***幻灯片播放标记*/
    private boolean isSlidePlay = false;
    private ViewPager viewPager;
    private PhotoView currentPhotoView;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case IMAGE_STATE_CHANGED:
                    sendImageStateEvent(msg.arg1);
                    break;
                case IMAGE_SCAN_CHANGED:
                    sendScanStateEvent(msg.arg1);
                    break;
                case IMAGE_SLIDER_PLAY:
                    nextImage();
                    mHandler.sendEmptyMessageDelayed(IMAGE_SLIDER_PLAY , CookooImageConfiguration.getInstance().getParam().getTimeStep());
                    break;
                default:
            }
        }
    };
    private IImageServiceCallback.Stub callback = new IImageServiceCallback.Stub() {
        @Override
        public void onImageStateChanged(int eventId) throws RemoteException {
            //不用打印时间信息
            if(eventId != ImageSdkConstants.ImageStateEventId.UPDATE_PLAY_TIME) {
                LogUtils.print(TAG,"------->> musicStateChanged() eventId: " + eventId);
            }
            mHandler.removeMessages(IMAGE_STATE_CHANGED);
            Message message = Message.obtain();
            message.what = IMAGE_STATE_CHANGED;
            message.arg1 = eventId;
            mHandler.sendMessage(message);
        }

        @Override
        public void onImageScanChanged(int eventId) throws RemoteException {
            LogUtils.print(TAG,"------->> musicScanChanged() eventId: " + eventId);
            mHandler.removeMessages(IMAGE_SCAN_CHANGED);
            Message message = Message.obtain();
            message.what = IMAGE_SCAN_CHANGED;
            message.arg1 = eventId;
            mHandler.sendMessage(message);
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,IBinder iBinder) {
            iImageService = IImageSdkService.Stub.asInterface(iBinder);
            isBinder = true;
            LogUtils.print(TAG,"------->> onServiceConnected() iImageService： " + iImageService);
            try {
                iImageService.registerImageServiceCallback(callback);
            } catch(RemoteException e) {
                LogUtils.print(TAG,"------->> onServiceConnected() eee： " + e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.print(TAG,"------->> onServiceDisconnected()");
            try {
                iImageService.unregisterImageServiceCallback(callback);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
            iImageService = null;
            isBinder = false;
        }
    };

    private ImageAidlManager() {
    }

    public static ImageAidlManager getInstance() {
        return ImageManagerInstance.MUSIC_AIDL_MANAGER;
    }

    public void doBinderImageService(Context context) {
        LogUtils.print(TAG,"------->> doBinderImageService()->" + context.getPackageName());
        if(isBinder && iImageService != null) {
            return;
        }
        this.mContext = context;
        GlobalTool.getInstance().setContext(context);
        Intent serviceIntent = new Intent("carnetapp.image.imagesdk.imageservice");
        serviceIntent.setPackage("com.cookoo.mediatest");
        context.bindService(serviceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    public void doUnbinderImageService(Context context) {
        context.unbindService(serviceConnection);
    }

    @Override
    public void startSlide() {
        if(!isSlidePlay()) {
            setSlidePlay(true);
            mHandler.sendEmptyMessageDelayed(IMAGE_SLIDER_PLAY , CookooImageConfiguration.getInstance().getParam().getTimeStep());
        }
    }

    @Override
    public void endSlidePlay() {
        if(isSlidePlay()) {
            setSlidePlay(false);
            mHandler.removeMessages(IMAGE_SLIDER_PLAY);
        }
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public PhotoView getCurrentPhotoView() {
        return currentPhotoView;
    }

    @Override
    public void setCurrentPhotoView(PhotoView currentPhotoView) {
        this.currentPhotoView = currentPhotoView;
    }

    public boolean isSlidePlay() {
        return isSlidePlay;
    }

    public void setSlidePlay(boolean isSlidePlay) {
        this.isSlidePlay = isSlidePlay;
    }

    public String getCurrentPlayFilePath() {
        return currentPlayFilePath;
    }

    public void setCurrentPlayFilePath(String currentPlayFilePath) {
        this.currentPlayFilePath = currentPlayFilePath;
    }

    @Override
    public void clearUsbImage(String usbPath) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.clearUsbImage(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void zoomOut() {
        if(currentPhotoView == null) {
            return;
        }
        float currentScale = currentPhotoView.getScale();
        float minScale = currentPhotoView.getMinScale();
        if(currentScale - CookooImageConfiguration.getInstance().getParam().getPerScale() <= minScale) {
            currentScale = minScale;
        } else {
            currentScale = currentScale - CookooImageConfiguration.getInstance().getParam().getPerScale();
        }
        currentPhotoView.setScale(currentScale,true);
    }

    @Override
    public void zoomIn() {
        if(currentPhotoView == null) {
            return;
        }
        float currentScale = currentPhotoView.getScale();
        float maxScale = currentPhotoView.getMaxScale();
        if(currentScale + CookooImageConfiguration.getInstance().getParam().getPerScale() >= maxScale) {
            currentScale = maxScale;
        } else {
            currentScale = currentScale + CookooImageConfiguration.getInstance().getParam().getPerScale();
        }
        currentPhotoView.setScale(currentScale,true);
    }

    @Override
    public void rotate(int angle) {
        if(currentPhotoView == null) {
            return;
        }
        currentPhotoView.setRotationBy(angle);
    }

    @Override
    public void preImage() {
        int position = getCurrentListPosition();
        int allSize = getPlayList().size();
        LogUtils.print(TAG,"===preImage 1====position:" + position + "|allSize:" + allSize);
        if(allSize < 1) {
            return;
        }
        if(position < 1) {
            position = allSize - 1;
        } else if(position > allSize - 1) {
            position = allSize - 1;
        } else {
            position -= 1;
        }
        LogUtils.print(TAG,"===preImage 2====position:" + position);
        if(getPlayList().get(position).isFolder()) {
            position = allSize - 1;
        }
        LogUtils.print(TAG," ===preImage 3===getCurrentItem: " + getCurrentPlayMediaItem() + "  position: " + getCurrentListPosition() + "  getCount: " + getPlayList().size());
        setCurrentListPosition(position);
        setCurrentPlayMediaItem(getPlayList().get(position));
        if(viewPager != null) {
            viewPager.setCurrentItem(position);
        }
    }

    @Override
    public void nextImage() {
        int position = getCurrentListPosition();
        int allSize = getPlayList().size();
        LogUtils.print(TAG,"===nextImage 1====position:" + position + "|allSize:" + allSize);
        if(allSize < 1) {
            return;
        }
        if(position < 0) {
            position = 0;
        } else if(position >= allSize - 1) {
            position = 0;
        } else {
            position += 1;
        }
        LogUtils.print(TAG,"===nextImage 2====position:" + position);
        if(getPlayList().get(position).isFolder()) {
            nextImage();
            return;
        }
        LogUtils.print(TAG,"===nextImage 3==== getCurrentItem: " + getCurrentPlayMediaItem() + "  position: " + getCurrentListPosition() + "  getCount: " + getPlayList().size());
        setCurrentListPosition(position);
        setCurrentPlayMediaItem(getPlayList().get(position));
        if(getViewPager() != null) {
            getViewPager().setCurrentItem(position);
        }
    }

    @Override
    public boolean isCurrentPlayItem(MediaData itemInfo) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.isCurrentPlayItem(itemInfo);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentUsbMount(String usbPath) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.isCurrentUsbMount(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllUsbUnMount() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.isAllUsbUnMount();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isAllDeviceScanFinished(boolean isFilterSdcard) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.isAllDeviceScanFinished(isFilterSdcard);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentDeviceScanFinished(String usbPath) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.isCurrentDeviceScanFinished(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setCPURefrain(boolean isRefrain) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.setCPURefrain(isRefrain);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestImageData(String usbPath,int dataType) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.requestImageData(usbPath,dataType);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleImageDataChange(String usbPath) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.handleImageDataChange(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public MediaData getSavePlayMediaItem() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getSavePlayMediaItem();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<MediaData> getPlayList() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getPlayList();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void setPlayList(List<MediaData> list) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.setPlayList(list);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<MediaData> getNewData() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getNewData();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<MediaListData> getOriginalData() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getOriginalData();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getUsbDeviceList();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int getCurrentDataListType() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getCurrentDataListType();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getCurrentShowDataPath() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getCurrentShowDataPath();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getUpperLevelFolderPath(String currentPath) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getUpperLevelFolderPath(currentPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void upperLevel(String usbPath) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.upperLevel(usbPath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public MediaData getCurrentPlayMediaItem() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getCurrentPlayMediaItem();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.setCurrentPlayMediaItem(currentPlayMediaItem);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateMediaItemName(MediaData itemInfo,boolean isCollection) {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.updateMediaItemName(itemInfo,isCollection);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean unCollected(MediaData mediaData){
        if(isBinder && iImageService != null) {
            try {
                return iImageService.unCollected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean collected(MediaData mediaData){
        if(isBinder && iImageService != null) {
            try {
                return iImageService.collected(mediaData);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int getHeightLightPosition() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getHeightLightPosition();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getCurrentListPosition() {
        if(isBinder && iImageService != null) {
            try {
                return iImageService.getCurrentListPosition();
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void setCurrentListPosition(int listPosition) {
        if(isBinder && iImageService != null) {
            try {
                iImageService.setCurrentListPosition(listPosition);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ImageManagerInstance {
        private static final ImageAidlManager MUSIC_AIDL_MANAGER = new ImageAidlManager();
    }

}
