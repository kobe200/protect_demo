package com.cookoo.imagesdkclient.mode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.model.ImageScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 *
 * @author lsf
 * @date 2018/4/13
 */

public class ImageInitParams implements Serializable {
    private int folderType;
    /**
     * 扩展程序的包名
     */
    private List<String> extendProcessPackageNameList;
    /**
     * 图片每次缩放的比例
     */
    private float perScale = (float) 0.4;
    /**
     * 表示在AsyncImageLoaderByPath加载图片里面能够同时加载几张图片
     */
    private int loadCount = 3;
    /**
     * 是否使用自定义的无限循环适配器
     */
    private boolean isCustomLoopPagerAdapter;
    private int timeStep = 5000;

    /**
     * 当前数据列表类型
     **/
    private int currentDataListType = ConstantsUtils.ListType.ALL_TYPE;

    public ImageInitParams(int folderType) {
        this.folderType = folderType;
    }

    public int getFolderType() {
        return folderType;
    }

    public void setFolderType(int folderType) {
        this.folderType = folderType;
    }

    public List<String> getExtendProcessPackageNameList() {
        return extendProcessPackageNameList;
    }

    public void setExtendProcessPackageNameList(String extendProcessPackageName) {
        if (extendProcessPackageNameList == null) {
            extendProcessPackageNameList = new ArrayList<>();
        }
        if (!extendProcessPackageNameList.contains(extendProcessPackageName)){
            extendProcessPackageNameList.add(extendProcessPackageName);
        }
    }

    public void setExtendProcessPackageNameList(List<String> extendProcessPackageNameList) {
        this.extendProcessPackageNameList = extendProcessPackageNameList;
    }

    public float getPerScale() {
        return perScale;
    }

    public void setPerScale(float perScale) {
        this.perScale = perScale;
    }

    public int getLoadCount() {
        return loadCount;
    }

    public void setLoadCount(int loadCount) {
        this.loadCount = loadCount;
    }

    public boolean isCustomLoopPagerAdapter() {
        return isCustomLoopPagerAdapter;
    }

    public void setCustomLoopPagerAdapter(boolean customLoopPagerAdapter) {
        isCustomLoopPagerAdapter = customLoopPagerAdapter;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public int getCurrentDataListType() {
        return currentDataListType;
    }

    public void setCurrentDataListType(int currentDataListType) {
        this.currentDataListType = currentDataListType;
    }

    public void setFilterSdcard(boolean filterSdcard) {
        ImageScanManager.getInstance().setFilterSdcard(filterSdcard);
    }
}
