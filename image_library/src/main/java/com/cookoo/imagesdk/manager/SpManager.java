package com.cookoo.imagesdk.manager;


import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.utils.CacheDataUtil;

import carnetapp.usbmediadata.bean.MediaData;


/**
 * Created by lsf on 2018/3/14.
 */

public class SpManager {

    /**
     * 播放图片文件路径
     **/
    public static final String IMAGE_PLAY_MEDIAITEM = "IMAGE_PLAY_MEDIAITEM ";

    private SpManager() {
    }

    public static SpManager getInstance() {
        return SpManagerInstance.spfManager;
    }


    public void savePlayMediaItem(MediaData mediaItemInfo) {
        CacheDataUtil.getInstance().saveDataFile(IMAGE_PLAY_MEDIAITEM,mediaItemInfo);
    }

    public MediaData getPlayMediaItem() {
        Object obj = CacheDataUtil.getInstance().getDataFile(IMAGE_PLAY_MEDIAITEM);
        if(obj != null) {
            return (MediaData) obj;
        }
        return null;
    }

    private static class SpManagerInstance {
        private static final SpManager spfManager = new SpManager();
    }


}
