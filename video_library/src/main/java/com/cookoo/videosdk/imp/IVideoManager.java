package com.cookoo.videosdk.imp;

/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public interface IVideoManager {
    /**
     * 播放音乐
     */
    void playVideo();

    /**
     * 暂停音乐
     */
    void pauseVideo();

    /**
     * 停止音乐，释放MediaPlayer
     */
    void stopVideo();

    /**
     * 重新播放
     */
    void rePlayVideo();

    /**
     * 下一个图片
     */
    void nextVideo();

    /**
     * 上一个图片
     */
    void preVideo();
}
