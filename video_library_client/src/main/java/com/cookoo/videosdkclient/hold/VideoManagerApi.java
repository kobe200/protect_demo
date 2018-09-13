package com.cookoo.videosdkclient.hold;

import java.util.List;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;

/**
 * Created by lsf on 2018/4/2
 * 音乐SDK中供集成方调用的API
 * @author lsf
 */

public interface VideoManagerApi {

    /**
     * 获取总时长
     *
     * @return
     */
    int getTotalTime();

    /**
     * 上一曲
     */
    void preVideo();

    /**
     * 下一曲
     */
    void nextVideo();

    /**
     * 播放或暂停
     * @return
     */
    void playOrPause();

    /**
     * 开始播放
     * @return
     */
    void start();

    /**
     * 退出视频资源
     * @return
     */
    void stopVideo();

    /**
     * 播放音乐
     * @param mediaItemInfo
     */
    void playVideo(MediaData mediaItemInfo);

    /**
     * 暂停
     */
    void pauseVideo();

    /**
     * 快退
     */
    void backForward();

    /**
     * 快进
     */
    void fastForward();

    /**
     * 移动到指定位置进行播放
     * @param position
     */
    void seekTo(int position);

    /**
     * 动态设置视频的layout
     */
    void setVideoLayout();

    /**
     * 清除数据
     * @param usbPath
     */
    void clearUsbVideo(String usbPath);

    /**
     * 判断是否为当前播放的对象
     * @param itemInfo
     * @return
     */
    boolean isCurrentPlayItem(MediaData itemInfo);

    /**
     * 判断当前usb是否挂载
     * @param usbPath
     * @return
     */
    boolean isCurrentUsbMount(String usbPath);

    /**
     * 判断当前机器是否没有usb设备
     * @return 返回所有usb是否卸载
     */
    boolean isAllUsbUnMount();

    /**
     * 判断所有usb是否扫描完毕
     * @param isFilterSdcard 是否过来sdk扫描
     * @return 返回所有usb是否扫描完成
     */
    boolean isAllDeviceScanFinished(boolean isFilterSdcard);

    /**
     * 判断某个usb是否扫描完毕
     * @param usbPath 当前usb路径
     * @return 放回当前usb是否扫描完成
     */
    boolean isCurrentDeviceScanFinished(String usbPath);

    /**
     * 设置优先加载音乐文件
     * @param isRefrain
     */
    void setCPURefrain(boolean isRefrain);

    /**
     * 当数据改变的时候，需要调用这个方法进行更新获取相应数据
     * @param usbPath 表示获取对应usb的相应数据
     */
    void handleVideoDataChange(String usbPath);

    /**
     * 获取所有的视频数据
     * @param path 如果为null，表示获取所有usb数据，如果为usb根目录，则获取对应usb的数据，否则获取对应文件夹的数据
     * @param dataType 请求数据类型
     */
    void requestVideoData(String path, int dataType);

    /**
     * 获取记忆保存的播放对象路径
     * @return 返回上次保存的记忆播放对象的路径
     */
    String getSavePlayMediaItemPath();
    /**
     * 获取记忆保存的播放对象的数据类型
     * @return 返回上次保存的记忆播放对象的数据类型
     */
    int getSavePlayMediaItemDataType();
    /**
     * 获取记忆保存的播放进度条
     * @return 返回上次保存的记忆播放进度条
     */
    int getSavePlayProgress();

    /**
     * 获取当前播放对象
     * @return
     */
    MediaData getCurrentPlayMediaItem() ;

    /**
     * 设置当前播放对象
     * @param currentPlayMediaItem
     */
    void setCurrentPlayMediaItem(MediaData currentPlayMediaItem);

    /**
     * 获取MediaItemInfo对象
     * @param filePath
     * @return
     */
    MediaItemInfo getMediaItemInfo(String filePath);

    /**
     * 更新音乐的名称
     * @param isCollection 是否更新收藏列表
     * @param itemInfo
     * @return
     */
    boolean updateMediaItemName(MediaData itemInfo, boolean isCollection);

    /**
     * 添加收藏
     *  mediaData的isCollected值 表示收藏，false表示移除收藏
     * @param mediaData
     * @return 返回是否删除成功
     */
    boolean collected(MediaData mediaData);

    /**
     * 取消收藏
     *  mediaData的isCollected值 表示收藏，false表示移除收藏
     * @param mediaData
     * @return 返回是否删除成功
     */
    boolean unCollected(MediaData mediaData);

    /**
     * 获取音乐当前播放状态
     * @return
     */
    int getCurrentPlayState();

    /**
     * 设置当前播放状态
     * @param currentPlayState
     */
    void setCurrentPlayState(int currentPlayState);

    /**
     * 获取音乐当前播放列表位置
     * @return
     */
    int getCurrentListPosition();

    /**
     * 设置当前列表位置
     * @param listPosition
     */
    void setCurrentListPosition(int listPosition);

    /**
     * 获取音乐当前播放位置
     * @return
     */
    int getCurrentPlayPosition();

    /**
     *设置当前播放位置
     * @param currentPlayPosition
     */
    void setCurrentPlayPosition(int currentPlayPosition);

    /**
     * 设置视频显示位置
     * @param videoPosition 确认视频显示在屏幕上的位置，长度为4，分别为：
     * surfaceWidth surfaceView需要显示的宽
     * surfaceHeight surfaceView需要显示的高
     * marginLeft surfaceView需要显示位置距离左边大小
     * marginTop surfaceView需要显示位置距离右边大小
     */
    void setVideoPosition(int[] videoPosition);

    /**
     * 获取当前列表高亮位置，如果当前播放的数据不再当前列表中，则默认是0
     * @return
     */
    int getHeightLightPosition();

    /**
     * 获取当前数据列表类型
     * @return
     */
    int getCurrentDataListType();

    /**
     * 获取指定文件所在的usb路径，在判断某个文件对应usb是否挂载用到
     * @param filePath 文件路径
     * @return 返回filePath所在的usb目录
     */
    String getUsbRootPathByFilePath(String filePath);

    /**
     * 获取当前显示的列表路径
     * @return  返回当前显示文件的路径
     */
    String getCurrentShowDataPath();

    /**
     * 获取当前显示的列表的上一级目录
     * @param currentPath 当前显示文件的路径
     * @return  返回当前显示文件的上一级目录
     */
    String getUpperLevelFolderPath(String currentPath);

    /**
     * 设置播放列表数据
     * @param playList
     */
    void setPlayList(List<MediaData> playList);

    /**
     * 获取播放列表数据
     * @return
     */
    List<MediaData> getPlayList();

    /**
     * 获取最新获取到的数据
     * @return
     */
    List<MediaData> getNewData();

    /**
     * 获取缓存的原始数据
     * @return
     **/
    List<MediaListData> getOriginalData();

    /**
     * 获取usb设备列表
     * @return
     */
    List<UsbDevice> getUsbDeviceList();

    /**
     * 返回上一级目录
     * @param usbPath 返回的请求usb路径，如果为null，则获取所有usb的数据
     */
    void upperLevel(String usbPath);

}
