package carnetapp.usbmediadata.base;

import java.util.List;

/**
 * 扫描服务主动上发接口
 * <p>
 * Created by Administrator on 2018/3/12 0012.
 */

public interface IBaseListener {

    /**
     * 回调接口：UsbDisk挂载。
     *
     * @param mountedPath: 挂载设备的路径集合
     */
    void onUsbDiskMounted(List<String> mountedPath);

    /**
     * 回调接口：UsbDisk卸载。
     *
     * @param root: 卸载设备的路径
     */
    void onUsbDiskUnMounted(String root);

    /**
     * 回调接口：媒体文件扫描完成。
     */
    void onFileScanFinished(String root);

}
