package com.cookoo.musicsdk.manager;

import com.cookoo.musicsdk.imp.MusicStateListener;
import com.cookoo.musicsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: lsf
 * @date: 2018/2/7 14:16
 * @decribe:
 */
public abstract class BaseManager {

    private static final String TAG = "BaseManager";
    private static List<MusicStateListener> mediaStateListeners = new ArrayList<MusicStateListener>();

    protected BaseManager() {
    }

    public boolean bindMediaStateListener(MusicStateListener l) {
        LogUtils.print(TAG,"--->>>bindMediaStateListener  MusicStateListener: " + l);
        if(l == null) {
            return false;
        }
        if(mediaStateListeners.contains(l)) {
            return false;
        }
        mediaStateListeners.add(l);
        LogUtils.print(TAG,"  size: " + mediaStateListeners.size());
        return true;
    }

    public boolean unbindMediaStateListener(MusicStateListener l) {
        if(l == null || mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return false;
        }
        mediaStateListeners.remove(l);
        return true;
    }

    public void sendMusicStateEvent(int eventId) {
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        for(MusicStateListener msl : mediaStateListeners) {
            msl.onMusicStateChanged(eventId);
        }
    }


    public void sendScanStateEvent(int eventId) {
        LogUtils.print(TAG,"  mediaStateListeners: " + mediaStateListeners);
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        for(MusicStateListener msl : mediaStateListeners) {
            msl.onMusicScanChanged(eventId);
        }
    }


}
