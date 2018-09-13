package com.cookoo.musicsdk.modle;

import com.cookoo.musicsdk.constants.MusicSdkConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import carnetapp.usbmediadata.model.MusicScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 * @date 2018/4/13
 */

public class MusicInitParams implements Serializable {
    /**
     * 扩展程序的包名
     **/
    private List<String> extendProcessPackageNameList;
    /**
     * 快进快退的时长
     */
    private int moveDuration = 10000;
    /**
     * 快进类型（只移动一次、不断移动）
     */
    private int moveType = MusicSdkConstants.MoveType.KEEP_MOVING;
    /**
     * 当前数据列表类型
     **/
    private int currentDataListType = ConstantsUtils.ListType.ALL_TYPE;
    /**
     * 是否设置淡入淡出
     */
    private boolean isFadeInNndOut = false;

    private boolean isAbandonFocusAfterPause = false;

    public MusicInitParams(int currentDataListType) {
        this.currentDataListType = currentDataListType;
    }

    public int getMoveDuration() {
        return moveDuration;
    }

    public void setMoveDuration(int moveDuration) {
        this.moveDuration = moveDuration;
    }

    public int getMoveType() {
        return moveType;
    }

    public void setMoveType(int moveType) {
        this.moveType = moveType;
    }

    public int getCurrentDataListType() {
        return currentDataListType;
    }

    public void setCurrentDataListType(int currentDataListType) {
        this.currentDataListType = currentDataListType;
    }

    public boolean isFadeInNndOut() {
        return isFadeInNndOut;
    }

    public void setFadeInNndOut(boolean fadeInNndOut) {
        isFadeInNndOut = fadeInNndOut;
    }

    public List<String> getExtendProcessPackageNameList() {
        return extendProcessPackageNameList;
    }

    public void setExtendProcessPackageNameList(String extendProcessPackageName) {
        if(extendProcessPackageNameList == null) {
            extendProcessPackageNameList = new ArrayList<>();
        }
        if(!extendProcessPackageNameList.contains(extendProcessPackageName)) {
            extendProcessPackageNameList.add(extendProcessPackageName);
        }
    }

    public boolean isAbandonFocusAfterPause() {
        return isAbandonFocusAfterPause;
    }

    public void setAbandonFocusAfterPause(boolean abandonFocusAfterPause) {
        isAbandonFocusAfterPause = abandonFocusAfterPause;
    }

    /**
     * 设置是否过滤sdcard数据
     * @param filterSdcard
     */
    public void setFilterSdcard(boolean filterSdcard) {
        MusicScanManager.getInstance().setFilterSdcard(filterSdcard);
    }

    /**
     * 设置是否区分不同usb
     * @param isDistinguishDifferentiatingUsb
     */
    public void setDistinguishDifferentiatingUsb(boolean isDistinguishDifferentiatingUsb){
        MusicScanManager.getInstance().setDifferentiatingUSB(isDistinguishDifferentiatingUsb);
    }

}
