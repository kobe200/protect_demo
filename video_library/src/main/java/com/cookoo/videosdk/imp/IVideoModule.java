package com.cookoo.videosdk.imp;

import android.view.SurfaceView;

import carnetapp.usbmediadata.bean.MediaData;

/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public interface IVideoModule {
    /**
     * 播放音乐
     * @param mediaItemInfo
     */
    void playVideo(MediaData mediaItemInfo);

    /**
     * 暂停音乐
     */
    void pauseVideo();

    /**
     * 开始播放
     */
    void start();

    /**
     * 停止音乐，释放MediaPlayer
     */
    void stopVideo();

    /**
     * 播放或暂停
     * @return
     */
    void playOrPause();

    /**
     * 重新播放
     */
    void rePlayVideo();

    /**
     * 下一个视频
     */
    void nextVideo();

    /**
     * 上一个图片
     */
    void preVideo();

    /**
     * 快退
     */
    void backForward();

    /**
     * 快进
     */
    void fastForward();

    /**
     * 动态设置视频播放显示位置以及缩放格式
     */
    void setVideoLayout();

    /**
     * 清除数据
     * @param usbPath
     */
    void clearUsbVideo(String usbPath);
    /**
     * 清除数据
     * @param surfaceView
     */
    void initSurfaceHolder(SurfaceView surfaceView);
}
