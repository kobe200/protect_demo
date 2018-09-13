package com.cookoo.musicsdk.imp;

import carnetapp.usbmediadata.bean.MediaData;

/**
 * Created by lsf on 2018/3/14.
 */

public interface IMusicMode {
    /**
     * 播放音乐
     * @param mediaData
     */
    void playMusic(MediaData mediaData);

    /**
     * 暂停音乐
     */
    void pauseMusic();

    /**
     * 开始播放
     */
    void startMusic();

    /**
     * 停止音乐，释放MediaPlayer
     */
    void stopMusic();

    /**
     * 播放或暂停
     * @return
     */
    void playOrPause();

    /**
     * 重新播放
     */
    void rePlayMusic();

    /**
     * 下一曲
     * @param isUser
     */
    void nextMusic(boolean isUser);

    /**
     * 上一个图片
     */
    void preMusic();

    /**
     * 快退
     */
    void backForward();

    /**
     * 快进
     */
    void fastForward();

    /**
     * 清除数据
     * @param usbPath
     */
    void clearUsbMusic(String usbPath);
}
