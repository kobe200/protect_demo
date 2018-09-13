package carnetapp.usbmediadata.base;

import android.content.Context;

/**
 * @author: kobe
 * @date: 2018/5/8 9:33
 * @decribe:
 */

public interface ImageScanManagerApi {

    /**
     * 请求媒体返回数据
     * @param context 引用对象
     * @param folderPath 当前U盘目录
     * @param dataType 数据类型
     */
    void requestImageData(final Context context, final String folderPath, int dataType);

    /**
     * 取得特定路径的对象
     * @param context
     * @param folderPath
     */
    void getMusicMediaItem(Context context, String folderPath);

}
