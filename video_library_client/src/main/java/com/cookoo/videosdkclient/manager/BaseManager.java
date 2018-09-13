package com.cookoo.videosdkclient.manager;


import com.cookoo.videosdkclient.hold.VideoStateListener;
import com.cookoo.videosdkclient.utils.LogUtils;
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
    private static List<VideoStateListener> mediaStateListeners = new ArrayList<VideoStateListener>();

    protected BaseManager(){}

    public boolean bindMediaStateListener(VideoStateListener l) {
        LogUtils.print(TAG,"--->>>bindMediaStateListener  MusicStateListener: "+l);
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

    public boolean unbindMediaStateListener(VideoStateListener l) {
        if(l == null || mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return false;
        }
        mediaStateListeners.remove(l);
        return true;
    }


    public void sendVideoStateEvent(int eventId) {
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        for(VideoStateListener msl : mediaStateListeners) {
            msl.onVideoStateChanged(eventId);
        }
    }

    public void sendScanStateEvent(int eventId) {
        LogUtils.print(TAG,"  mediaStateListeners: "+ mediaStateListeners);
        if(mediaStateListeners == null || mediaStateListeners.size() == 0) {
            return;
        }
        LogUtils.print(TAG,"  size: "+ mediaStateListeners.size());
        for(VideoStateListener msl : mediaStateListeners) {
            msl.onVideoScanChanged(eventId);
        }
    }





}
