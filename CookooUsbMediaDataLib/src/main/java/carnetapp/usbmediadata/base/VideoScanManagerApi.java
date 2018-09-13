package carnetapp.usbmediadata.base;

import android.content.Context;

import carnetapp.usbmediadata.bean.MediaItemInfo;

/**
 * @author: kobe
 * @date: 2018/5/8 9:33
 * @decribe:
 */

public interface VideoScanManagerApi {
    /**
     * 请求媒体返回数据
     * @param context 引用对象
     * @param folderPath 当前U盘目录
     * @param dataType 数据类型
     */
    void requestVideoData(final Context context, final String folderPath, int dataType);

    /**
     * 取得特定路径的对象
     * @param context
     * @param folderPath
     */
    MediaItemInfo getVideoMediaItem(Context context, String folderPath);

    /**
     * 请求解析媒体文件 filePath 媒体文件路径
     * <p>
     * 当前播放的媒体文件如果还没有解析，调用该方法进行优先解析
     **/
    void requestVideoParse(String filePath);


}
