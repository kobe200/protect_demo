package carnetapp.usbmediadata.base;

import android.content.Context;

import carnetapp.usbmediadata.bean.MediaItemInfo;

/**
 * @author: kobe
 * @date: 2018/5/8 9:33
 * @decribe:
 */

public interface MusicScanManagerApi {
    /**
     * 请求解析媒体文件 filePath 媒体文件路径
     * <p>
     * 当前使用的媒体文件如果还没有解析，调用该方法进行优先解析
     **/
    void requestMusicParse(String filePath);

    /**
     * 请求媒体返回数据
     * @param context 引用对象
     * @param folderPath 当前U盘目录
     * @param dataType 数据类型
     */
    void requestMusicData(final Context context, final String folderPath, final String columnContentStr, int dataType);

    /**
     * 取得特定路径的对象
     * @param context
     * @param folderPath
     */
    MediaItemInfo getMusicMediaItem(Context context, String folderPath);

}
