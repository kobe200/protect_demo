package com.cookoo.imagesdk.imp;

/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public interface IImageModule {
    /**
     * 开始幻灯片播放
     */
    void startSlide();

    /**
     * 结束幻灯片播放
     */
    void endSlidePlay();

    /**
     * 清理图片数据
     * @param usbPath 清除对应usb的数据
     */
    void clearUsbImage(String usbPath);

    /**
     * 缩小图片
     */
    void zoomOut();

    /**
     * 放大图片
     */
    void zoomIn();

    /**
     * 旋转图片，
     * @param angle 旋转角度
     */
    void rotate(int angle);

    /**
     * 上一个图片
     */
    void preImage();

    /**
     * 下一个图片
     */
    void nextImage();

}
