package carnetapp.usbmediadata.base;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaListData;

/**
 * 扫描服务主动上发接口
 * <p>
 * Created by Administrator on 2018/3/12 0012.
 */
public interface IMediaChangeListener extends IBaseListener {
    /**
     * 回调接口：媒体文件数据改变，通知上层状态
     */
    void onMediaDataChanged();

    /**
     * 回调接口：返回查询数据列表
     * @param mediaListData 媒体对象集合
     */
    void onMediaDataCallback(List<MediaListData> mediaListData, final String folderPath, int dataType);

    /**
     * 回调接口：数据扫描完成监听
     * @param root 媒体对象集合
     */
    void onFileScanFinished(String root);

    /**
     * 单个媒体文件解析完毕后回调
     * @param path: 返回解析后的路径
     */
    void OnMediaParseBack(String path);
}
