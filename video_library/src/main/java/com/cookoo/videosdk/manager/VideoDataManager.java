package com.cookoo.videosdk.manager;


import com.cookoo.videosdk.utils.GlobalTool;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.VideoScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 * @date 2018/3/13
 */

public class VideoDataManager extends BaseManager implements IMediaChangeListener {

    private static final String TAG = "VideoDataManager";
    private static VideoDataManager videoControlManager;
    private VideoScanManager mVideoScanManager = VideoScanManager.getInstance();

    public VideoDataManager() {

    }

    public static VideoDataManager getInstance() {
        if(videoControlManager == null) {
            videoControlManager = new VideoDataManager();
        }
        return videoControlManager;
    }

    public void init() {
        LogUtils.print(TAG,"----->> init()");
        mVideoScanManager.initVideoInfo(GlobalTool.getInstance().getContext(),this,CookooVideoConfiguration.getInstance().getParam().getCurrentDataListType());
    }

    @Override
    public void onUsbDiskMounted(List<String> usbPaths) {
        if (usbPaths == null){
            return;
        }
        LogUtils.print(TAG,"---->> onUsbDiskMounted  size: " + usbPaths.size());
        for(String usbPath : usbPaths) {
            boolean hasUnMountUsbDevice = false;
            for(UsbDevice usbDevice : VideoManager.getInstance().getUsbDeviceList()) {
                //判断UsbDeviceList里面是否含有卸载的usb，存在则替换
                if(usbDevice.getRootPath() == null) {
                    usbDevice.setRootPath(usbPath);
                    usbDevice.setMount(true);
                    hasUnMountUsbDevice = true;
                    usbDevice.setScanFinished(mVideoScanManager.isUsbFileScanFinished(usbPath));
                    break;
                }
            }
            //UsbDeviceList没有卸载的usb，添加一个USB设备
            if(!hasUnMountUsbDevice) {
                UsbDevice usbDevice = new UsbDevice();
                usbDevice.setMount(true);
                usbDevice.setRootPath(usbPath);
                usbDevice.setScanFinished(mVideoScanManager.isUsbFileScanFinished(usbPath));
                VideoManager.getInstance().getUsbDeviceList().add(usbDevice);
            }
        }
        sendScanStateEvent(VideoSdkConstants.ScanStateEventId.USB_DISK_MOUNTED);
    }

    @Override
    public void onUsbDiskUnMounted(String usbPath) {
        LogUtils.print(TAG,"---->> onUsbDiskUnMounted  usbPath: " + usbPath);
        for(UsbDevice usbDevice : VideoManager.getInstance().getUsbDeviceList()) {
            //replace
            if(usbPath.equals(usbDevice.getRootPath())) {
                usbDevice.setScanFinished(false);
                usbDevice.setRootPath(null);
                usbDevice.setMount(false);
            }
        }
        VideoManager.getInstance().clearUsbVideo(usbPath);
        sendScanStateEvent(VideoSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED);
    }

    @Override
    public void onMediaDataChanged() {
        LogUtils.print(TAG,"-------->>>onMediaDataChanged()");
        sendScanStateEvent(VideoSdkConstants.ScanStateEventId.VIDEO_DATA_CHANGE);
    }

    @Override
    public void onMediaDataCallback(List<MediaListData> list,String path,int dataType) {
        if(list != null && list.size() > 0 && list.get(0).getData() != null) {
            LogUtils.print(TAG,"------>> onMediaDataCallback() dataType:  " + dataType + " usbPath: " + path + " size: " + list.get(0).getData().size());
        }
        MediaData currentPlayItem = VideoManager.getInstance().getCurrentPlayMediaItem();
        if(currentPlayItem == null) {
            updateShowListData(list,dataType);
            return;
        }
        String playItemParentPath = VideoManager.getInstance().getUpperLevelFolderPath(currentPlayItem.getFilePath());
        int showDataType = VideoManager.getInstance().getCurrentDataListType();
        //（1）如果返回的数据类型是当前播放数据类型，并且不是正在浏览的数据的类型，则表示是请求播放列表数据
        // (如果请求的路径是当前播放对象的根目录，只需要更新playlist data，不用刷新ui)
        // ---》 只需要更新playlist data，不用刷新ui
        //（2）如果播放的数据类型是正在浏览的数据类型
        // ---》 更新playlist data与originalData data，并且刷新ui
        //（3）如果返回的数据类型是浏览的数据类型
        // ---》只需要更新originalData data，并且刷新ui
        LogUtils.print(TAG,"showDataType: " + showDataType + " currentPlayDataType" + currentPlayItem.getDataType() + " dataType: " + dataType);
        if(dataType == currentPlayItem.getDataType() && dataType != showDataType) {
            if(dataType != ConstantsUtils.ListType.TREE_FOLDER_TYPE || path.equals(playItemParentPath)) {
                updatePlayListData(list);
            }
        } else if(currentPlayItem.getDataType() == showDataType) {
            if(dataType == ConstantsUtils.ListType.TREE_FOLDER_TYPE) {
                if(playItemParentPath.equals(path)) {
                    updatePlayListData(list);
                }
                //表示请求的是浏览列表数据
                updateShowListData(list,dataType);
            } else if(dataType == ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE){
                LogUtils.print(TAG,"==PlayPath==" + VideoManager.getInstance().getCurrentPlayPath());
                LogUtils.print(TAG,"==ShowDataPath==" + VideoManager.getInstance().getCurrentShowDataPath());
                if(VideoManager.getInstance().getCurrentShowDataPath().equals(VideoManager.getInstance().getCurrentPlayPath())){
                    //如果当前查询的数据和播放的数据路径相同，则更新播放列表数据
                    updatePlayListData(list);
                }
                updateShowListData(list,dataType);
            } else {
                updatePlayListData(list);
                updateShowListData(list,dataType);
            }
        } else if(dataType == showDataType) {
            updateShowListData(list,dataType);
        }
    }

    @Override
    public void onFileScanFinished(String usbPath) {
        LogUtils.print(TAG,"---->> onFileScanFinished  usbPath: " + usbPath);
        for(UsbDevice usbDevice : VideoManager.getInstance().getUsbDeviceList()) {
            if(usbPath.equals(usbDevice.getRootPath())) {
                usbDevice.setScanFinished(true);
                break;
            }
        }
        sendScanStateEvent(VideoSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED);
    }

    @Override
    public void OnMediaParseBack(String filePath) {
        //TODO 只有首次插入U盘时会返回解析完成事件
    }

    private void updateShowListData(List<MediaListData> list,int dataType) {
        LogUtils.print(TAG,"===updateShowListData===");
        VideoManager.getInstance().getOriginalData().clear();
        VideoManager.getInstance().getOriginalData().addAll(list);
        sendScanStateEvent(dataType);
    }

    private void updatePlayListData(List<MediaListData> list) {
        LogUtils.print(TAG,"===updatePlayListData===");
        List<MediaData> dataList = new ArrayList<>();
        for(MediaListData ml : list) {
            dataList.addAll(ml.getData());
        }
        VideoManager.getInstance().setPlayList(dataList);
        //更新当前播放对象位置
        if(dataList.size() == 0){
            VideoManager.getInstance().setCurrentListPosition(-1);
        }else{
            for(int i = 0; i < dataList.size(); i++) {
                if(dataList.get(i).getFilePath() != null && dataList.get(i).getFilePath().equals(VideoManager.getInstance().getCurrentPlayMediaItem().getFilePath())) {
                    VideoManager.getInstance().setCurrentListPosition(i);
                    VideoManager.getInstance().setCurrentPlayMediaItem(dataList.get(i));
                    break;
                }
            }
        }
        sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_INFO);
    }

}

