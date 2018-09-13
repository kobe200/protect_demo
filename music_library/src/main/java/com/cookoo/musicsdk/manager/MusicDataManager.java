package com.cookoo.musicsdk.manager;

import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.utils.GlobalTool;
import com.cookoo.musicsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.MusicScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 * @date 2018/3/13
 */
public class MusicDataManager extends BaseManager implements IMediaChangeListener {

    private static final String TAG = "MusicDataManager";
    private static MusicDataManager musicControlManager;
    private MusicScanManager mMusicScanManager = MusicScanManager.getInstance();

    public MusicDataManager() {
    }

    public static MusicDataManager getInstance() {
        if(musicControlManager == null) {
            musicControlManager = new MusicDataManager();
        }
        return musicControlManager;
    }

    public void init() {
        LogUtils.print(TAG,"----->> init()");
        mMusicScanManager.initMusicInfo(GlobalTool.getInstance().getContext(),this,CookooMusicConfiguration.getInstance().getParam().getCurrentDataListType());
    }

    @Override
    public void onUsbDiskMounted(List<String> usbPaths) {
        if (usbPaths == null){
            return;
        }
        LogUtils.print("scan","---->> onUsbDiskMounted  size: " + usbPaths.size()+"  usbPath: "+usbPaths.get(0));
        for(String usbPath : usbPaths) {
            boolean hasUnMountUsbDevice = false;
            for(UsbDevice usbDevice : MusicManager.getInstance().getUsbDeviceList()) {
                //判断UsbDeviceList里面是否含有卸载的usb，存在则替换
                if(usbDevice.getRootPath() == null) {
                    usbDevice.setRootPath(usbPath);
                    usbDevice.setMount(true);
                    hasUnMountUsbDevice = true;
                    usbDevice.setScanFinished(mMusicScanManager.isUsbFileScanFinished(usbPath));
                    break;
                }
            }
            //UsbDeviceList没有卸载的usb，添加一个USB设备
            if(!hasUnMountUsbDevice) {
                UsbDevice usbDevice = new UsbDevice();
                usbDevice.setMount(true);
                usbDevice.setRootPath(usbPath);
                usbDevice.setScanFinished(mMusicScanManager.isUsbFileScanFinished(usbPath));
                MusicManager.getInstance().getUsbDeviceList().add(usbDevice);
            }
        }
        sendScanStateEvent(MusicSdkConstants.ScanStateEventId.USB_DISK_MOUNTED);
    }

    @Override
    public void onUsbDiskUnMounted(String usbPath) {
        LogUtils.print("scan","---->> onUsbDiskUnMounted  usbPath: " + usbPath);
        for(UsbDevice usbDevice : MusicManager.getInstance().getUsbDeviceList()) {
            //replace
            if(usbPath.equals(usbDevice.getRootPath())) {
                usbDevice.setScanFinished(false);
                usbDevice.setRootPath(null);
                usbDevice.setMount(false);
            }
        }
        MusicManager.getInstance().clearUsbMusic(usbPath);
        sendScanStateEvent(MusicSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED);
    }

    @Override
    public void onMediaDataChanged() {
        LogUtils.print(TAG,"==onMediaDataChanged()==" + MusicManager.getInstance().getCurrentDataListType());
        //数据发生变化后，需要请求刷新当前列表数据
        sendScanStateEvent(MusicSdkConstants.ScanStateEventId.MUSIC_DATA_CHANGE);
    }

    @Override
    public void onMediaDataCallback(List<MediaListData> list,String path,int dataType) {
        if(list != null && list.size() > 0 && list.get(0).getData() != null) {
            LogUtils.print(TAG,"------>> onMediaDataCallback() dataType:  " + dataType + " usbPath: " + path + " list.size: "+list.size()+" size: " + list.get(0).getData().size());
            if (list.size() > 1){
                LogUtils.print(TAG,"----onMediaDataCallback()  000getUsbRootPath: "+list.get(0).getUsbRootPath()+"  1111getUsbRootPath: "+list.get(1).getUsbRootPath());
            }
        }
        MediaData currentPlayItem = MusicManager.getInstance().getCurrentPlayMediaItem();
        if(currentPlayItem == null) {
            updateShowListData(list,dataType);
            return;
        }
        String playItemParentPath = MusicManager.getInstance().getUpperLevelFolderPath(currentPlayItem.getFilePath());
        int showDataType = MusicManager.getInstance().getCurrentDataListType();
        //（1）如果返回的数据类型是当前播放数据类型，并且不是正在浏览的数据的类型，则表示是请求播放列表数据
        // (如果请求的路径是当前播放对象的根目录，只需要更新playlist data，不用刷新ui)
        // ---》 只需要更新playlist data，不用刷新ui
        //（2）如果播放的数据类型是正在浏览的数据类型
        //---》 更新playlist data与originalData data，并且刷新ui
        //（3）如果返回的数据类型是浏览的数据类型
        // ---》只需要更新originalData data，并且刷新ui
        LogUtils.print(TAG,"showDataType: " + showDataType + " currentPlayDataType:" + currentPlayItem.getDataType() + " dataType: " + dataType);
        if(dataType == currentPlayItem.getDataType() && dataType != showDataType) {
            if(dataType != ConstantsUtils.ListType.TREE_FOLDER_TYPE || playItemParentPath.equals(path)) {
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
                LogUtils.print(TAG,"playlist: " + MusicManager.getInstance().getCurrentPlayPath() + "|" + MusicManager.getInstance().getCurrentShowDataPath());
                if(MusicManager.getInstance().getCurrentPlayPath().equals(MusicManager.getInstance().getCurrentShowDataPath())){
                    //如果当前查询的数据和播放的数据路径相同，则更新播放列表数据
                    updatePlayListData(list);
                }
                updateShowListData(list,dataType);
            }else {
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
        sendScanStateEvent(MusicSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED);
    }

    @Override
    public void OnMediaParseBack(String filePath) {
        LogUtils.print(TAG,"---->> OnMediaParseBack() filePath: " + filePath);
    }

    private void updateShowListData(List<MediaListData> list,int dataType) {
        LogUtils.print(TAG,"--updateShowListData-->> ");
        MusicManager.getInstance().getOriginalData().clear();
        MusicManager.getInstance().getOriginalData().addAll(list);
        sendScanStateEvent(dataType);
    }

    private void updatePlayListData(List<MediaListData> list) {
        LogUtils.print(TAG,"--updatePlayListData-->> ");
        List<MediaData> dataList = new ArrayList<>();
        for(MediaListData ml : list) {
            dataList.addAll(ml.getData());
        }
        //更新当地播放列表数据
        MusicManager.getInstance().setPlayList(dataList);
        //更新当前播放对象位置
        if(dataList.size() == 0){
            MusicManager.getInstance().setCurrentListPosition(-1);
        }else{
            for(int i = 0; i < dataList.size(); i++) {
                if(dataList.get(i).getFilePath() != null && dataList.get(i).getFilePath().equals(MusicManager.getInstance().getCurrentPlayMediaItem().getFilePath())) {
                    MusicManager.getInstance().setCurrentListPosition(i);
                    MusicManager.getInstance().setCurrentPlayMediaItem(dataList.get(i));
                    break;
                }
            }
        }
        sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_INFO);
    }

}

