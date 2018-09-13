package com.cookoo.imagesdk.manager;

import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.utils.GlobalTool;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.ImageScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 * @date 2018/3/13
 */

public class ImageDataManager extends BaseManager implements IMediaChangeListener {

    private static final String TAG = "ImageDataManager";
    private static ImageDataManager musicControlManager;

    /**
     * 用于存储u盘中的音乐文件
     */

    public ImageDataManager() {

    }

    public static ImageDataManager getInstance() {
        if(musicControlManager == null) {
            musicControlManager = new ImageDataManager();
        }
        return musicControlManager;
    }

    public void init() {
        LogUtils.print(TAG,"----->> init()");
        ImageScanManager.getInstance().initImageInfo(GlobalTool.getInstance().getContext(),this,CookooImageConfiguration.getInstance().getParam().getFolderType());
    }


    @Override
    public void onUsbDiskMounted(List<String> usbPaths) {
        if (usbPaths == null){
            return;
        }
        LogUtils.print(TAG,"---->> onUsbDiskMounted  size: " + usbPaths.size()+"  usbPath: "+usbPaths.get(0));
        for(String usbPath : usbPaths) {
            boolean hasUnMountUsbDevice = false;
            for(UsbDevice usbDevice : ImageManager.getInstance().getUsbDeviceList()) {
                LogUtils.print(TAG,"---111->> onUsbDiskMounted  size: " + usbPaths.size()+"  usbPath: "+usbPaths.get(0));
                //判断UsbDeviceList里面是否含有卸载的usb，存在则替换
                if(usbDevice.getRootPath() == null) {
                    usbDevice.setMount(true);
                    hasUnMountUsbDevice = true;
                    usbDevice.setRootPath(usbPath);
                    break;
                }
            }
            //UsbDeviceList没有卸载的usb，添加一个USB设备
            LogUtils.print(TAG,"--222-->> onUsbDiskMounted  hasUnMountUsbDevice: " +hasUnMountUsbDevice);
            if(!hasUnMountUsbDevice) {
                UsbDevice usbDevice = new UsbDevice();
                usbDevice.setMount(true);
                usbDevice.setRootPath(usbPath);
                ImageManager.getInstance().getUsbDeviceList().add(usbDevice);
            }
        }
        sendScanStateEvent(ImageSdkConstants.ScanStateEventId.USB_DISK_MOUNTED);
    }

    @Override
    public void onUsbDiskUnMounted(String usbPath) {
        LogUtils.print(TAG,"---->> onUsbDiskUnMounted  usbPath: " + usbPath);
        for(UsbDevice usbDevice : ImageManager.getInstance().getUsbDeviceList()) {
            //replace
            if(usbPath.equals(usbDevice.getRootPath())) {
                usbDevice.setScanFinished(false);
                usbDevice.setRootPath(null);
                usbDevice.setMount(false);
            }
        }
        ImageManager.getInstance().clearUsbImage(usbPath);
        sendScanStateEvent(ImageSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED);
    }

    @Override
    public void onMediaDataChanged() {
        sendScanStateEvent(ImageSdkConstants.ScanStateEventId.IMAGE_DATA_CHANGE);
    }

    @Override
    public void onMediaDataCallback(List<MediaListData> list,String path,int dataType) {
        if(list != null && list.size() > 0 && list.get(0).getData() != null) {
            LogUtils.print(TAG,"------>> onMediaDataCallback() dataType:  " + dataType + " usbPath: " + path + " size: " + list.get(0).getData().size());
        }
        MediaData currentPlayItem = ImageManager.getInstance().getCurrentPlayMediaItem();
        if(currentPlayItem == null) {
            updateShowListData(list,dataType);
            return;
        }
        String playItemParentPath = ImageManager.getInstance().getUpperLevelFolderPath(currentPlayItem.getFilePath());
        int showDataType = ImageManager.getInstance().getCurrentDataListType();
        //（1）如果返回的数据类型是当前播放数据类型，并且不是正在浏览的数据的类型，则表示是请求播放列表数据
        // (如果请求的路径是当前播放对象的根目录，只需要更新playlist data，不用刷新ui)
        // ---》 只需要更新playlist data，不用刷新ui
        //（2）如果播放的数据类型是正在浏览的数据类型
        //---》 更新playlist data与originalData data，并且刷新ui
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
        for(UsbDevice usbDevice : ImageManager.getInstance().getUsbDeviceList()) {
            if(usbPath.equals(usbDevice.getRootPath())) {
                usbDevice.setScanFinished(true);
                break;
            }
        }
        sendScanStateEvent(ImageSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED);
    }

    @Override
    public void OnMediaParseBack(String filePath) {
    }

    private void updateShowListData(List<MediaListData> list,int dataType) {
        ImageManager.getInstance().getOriginalData().clear();
        ImageManager.getInstance().getOriginalData().addAll(list);
        sendScanStateEvent(dataType);
    }

    private void updatePlayListData(List<MediaListData> list) {
        LogUtils.print(TAG,"-------->> updatePlayListData() ");
        List<MediaData> dataList = new ArrayList<>();
        for(MediaListData ml : list) {
            dataList.addAll(ml.getData());
        }
        ImageManager.getInstance().setPlayList(dataList);
    }

}

