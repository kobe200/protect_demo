package com.cookoo.imagesdk.manager;

import com.cookoo.imagesdk.imp.ImageStateListener;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author: lsf
 * @date: 2018/2/7 14:16
 * @decribe:
 */
public abstract class BaseManager {

    private static final String TAG = "BaseManager";
    private static List<ImageStateListener> mediaStateListeners = new ArrayList<ImageStateListener>();

    protected BaseManager(){}

    public boolean bindMediaStateListener(ImageStateListener l) {
        LogUtils.print(TAG,"--->>>bindMediaStateListener  ImageStateListener: "+l);
        if(l == null) {
            return false;
        }
        if(mediaStateListeners.contains(l)) {
            return false;
        }
        mediaStateListeners.add(l);
        LogUtils.print(TAG,"  size: "+ mediaStateListeners.size());
        return true;
    }

    public boolean unbindMediaStateListener(ImageStateListener l) {
        if(l == null || mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return false;
        }
        mediaStateListeners.remove(l);
        return true;
    }

    public void sendImageStateEvent(int eventId) {
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        for(ImageStateListener msl : mediaStateListeners) {
            msl.onImageStateChanged(eventId);
        }
    }

    public void sendScanStateEvent(int eventId) {
        LogUtils.print(TAG,"  mediaStateListeners: "+ mediaStateListeners);
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        LogUtils.print(TAG,"  size: "+ mediaStateListeners.size());
        for(ImageStateListener msl : mediaStateListeners) {
            msl.onImageScanChanged(eventId);
        }
    }





}
