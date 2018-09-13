package carnetapp.usbmediadata.utils;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.db.ProviderHelper;


public class DeviceUtils {

    /**
     * USB设备挂载根目录
     */
    public static final String USB_MEDIA_ROOT_PATH = "/storage/udisk";
    public static Map<String,UsbDevice> DEVICE = new HashMap<String,UsbDevice>() {
        {
            put(USB_MEDIA_ROOT_PATH,new UsbDevice(USB_MEDIA_ROOT_PATH,false));
            put(ProviderHelper.USB_INNER_SD_PATH,new UsbDevice(ProviderHelper.USB_INNER_SD_PATH,true));
            put(ProviderHelper.USB_OUTER_SD_PATH,new UsbDevice(ProviderHelper.USB_OUTER_SD_PATH,false));
        }
    };
    private static List<String> currentUsbPathList = null;

    public static List<String> getCurrentUsbPathList(Context context) {
        return currentUsbPathList;
    }

    public static boolean isUsbExist(boolean isFilterSdcard) {
        List<String> mountedPath = getMountedPath(isFilterSdcard);
        if(mountedPath != null && mountedPath.size() >= 1) {
            return true;
        }
        LogUtils.i("no USB device mounted!");
        return false;
    }

    /**
     * 挂载
     */
    public static void setMounted(String path) {
        if(DEVICE.get(path) != null) {
            DEVICE.get(path).setMount(true);
        } else {
            DEVICE.put(path,new UsbDevice(path,true));
        }
    }

    /**
     * 卸载
     */
    public static void setUnMounted(String path) {
        if(DEVICE.get(path) != null) {
            DEVICE.get(path).setMount(false);
            DEVICE.get(path).setScanFinished(false);
        }
    }

    /**
     * 获取挂载设备的路径
     */
    public static List<String> getMountedPath(boolean isFilterSdcard) {
        List<String> temp = new ArrayList<String>();
        for(UsbDevice device : DEVICE.values()) {
            //过滤内置SD卡
            if(isFilterSdcard && device.getRootPath().equals(ProviderHelper.USB_INNER_SD_PATH)) {
                continue;
            }
            if(device.isMount()) {
                temp.add(device.getRootPath());
            }
        }
        return temp;
    }

    /**
     * 获取挂载设备的路径
     */
    public static List<UsbDevice> getMountedDevices(boolean isFilterSdcard) {
        List<UsbDevice> temp = new ArrayList<UsbDevice>();
        for(UsbDevice device : DEVICE.values()) {
            //过滤内置SD卡
            if(isFilterSdcard && device.getRootPath().equals(ProviderHelper.USB_INNER_SD_PATH)) {
                continue;
            }
            if(device.isMount()) {
                temp.add(device);
            }
        }
        LogUtils.i("getMountedDevices size ->" + temp.size() + "|" + isFilterSdcard);
        return temp;
    }

    /**
     * 取得根目录
     */
    public static String getRootPath(String path) {
        if(path != null) {
            for(UsbDevice device : DEVICE.values()) {
                if(path.contains(device.getRootPath())) {
                    return device.getRootPath();
                }
            }
        }
        return null;
    }

    /**
     * 6.0取得挂载路径
     * @param context void
     * @author:
     * @createTime: 2017-3-2 上午11:47:09
     * @history:
     */
//    public static List<String> getMountedDevice(Context context) {
//        List<String> temp = new ArrayList<String>();
//        StorageManager mStorageManager = null;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mStorageManager = context.getSystemService(StorageManager.class);
//        }
//        List<VolumeInfo> volumes = mStorageManager.getVolumes();
//        for(VolumeInfo vol : volumes) {
//            if(vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                // 6.0中外置sd和usb标示为公共的
//                File usbFile = vol.getPath();
//                if(usbFile != null) {
//                    String usbPath = usbFile.getAbsolutePath();
//                    LogUtils.d("--- FileUtils_getMountedDevice_usbPath ---" + usbPath);
//                    temp.add(usbPath);
//                }
//            }
//        }
//        return temp;
//    }

    /**
     * @return 是否为6.0系统
     */
    public static boolean isDeviceVersion6() {
        int currentVersion = Build.VERSION.SDK_INT;
        if(currentVersion == Build.VERSION_CODES.M) {// 6.0系统
            return true;
        }
        return false;
    }

}
