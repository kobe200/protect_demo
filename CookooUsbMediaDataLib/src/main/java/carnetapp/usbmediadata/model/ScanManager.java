package carnetapp.usbmediadata.model;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.DeviceUtils;

/**
 * Created by Administrator on 2018/3/27 0027.
 */

public class ScanManager {
    /**
     * 请求USB挂载路径
     **/
    public List<UsbDevice> getUsbDevice(boolean isFilterSdcard) {
        List<UsbDevice> devices = new ArrayList<>();
        for(UsbDevice device : DeviceUtils.DEVICE.values()) {
            //过滤内置SD卡
            if(isFilterSdcard && device.getRootPath().equals(ProviderHelper.USB_INNER_SD_PATH)){
                continue;
            }
            if(!device.isMount()) {
                device.setRootPath(null);
            }
            devices.add(device);
        }
        return null;
    }

    /***
     * 进入界面加快扫描速度，退出界面降低扫描速度
     * @param isRefrain: 是否限制扫描速度
     * @param mediaType： 优先的媒体类型：UNKNOWN (0), VIDEO (1), AUDIO (2), IMAGE (3), ALL (4);
     */
    public void setCPURefrain(boolean isRefrain,int mediaType) {
        if(MediaUsbService.iMediaService != null) {
            try {
                MediaUsbService.iMediaService.setCPURefrain(isRefrain);
                if(!isRefrain) {
                    MediaUsbService.iMediaService.setStoreMediaType(mediaType);
                }
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 请求删除某个文件
     **/
    public boolean deleteFile(String filePath) {
        if(MediaUsbService.iMediaService != null) {
            try {
                return MediaUsbService.iMediaService.removeMediaFile(filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 收藏文件
     * @param isCollected: 是否要收藏
     * @param filePath： 要收藏的文件路径
     * @return
     */
    public boolean updateMediaCollecte(boolean isCollected,String filePath) {
        if(MediaUsbService.iMediaService != null) {
            try {
                return MediaUsbService.iMediaService.mediaFileCollecte(isCollected,filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 媒体扫描服务扫描是否完成
     * @param path: U盘路径
     */
    public boolean isUsbFileScanFinished(String path) {
        boolean isfinish = false;
        if(MediaUsbService.iMediaService != null) {
            try {
                isfinish = MediaUsbService.iMediaService.isFileScanFinished(path);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        return isfinish;
    }
}
