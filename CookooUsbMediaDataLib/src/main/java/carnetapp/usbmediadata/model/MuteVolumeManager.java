package carnetapp.usbmediadata.model;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author: kobe
 * @date: 2018/8/8 9:17
 * @decribe:
 */

public class MuteVolumeManager {
    // action
    private static final int ACTION_RESTORE_VALUE = 1000;
    private static final int ACTION_STOP_VALUE = 1001;
    // 淡出与淡出时间：2秒
    private int restoreTime = 2000;
    // 静音 淡出时间为1秒
    private int stopTime = 1000;
    private float muteCurVolume = 0f;
    private float muteTargetVolume = 0f;
    private float unMuteCurVolume = 0f;
    private float unMuteTargetVolume = 0f;
    private boolean isMuting = false;
    private boolean isUnMuting = false;
    // listener
    private VolumeListener volumeListener;
    // handler control
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == ACTION_RESTORE_VALUE) {
                if(muteCurVolume < muteTargetVolume) {
                    muteCurVolume += 0.1f;
                    if(muteCurVolume > 1) {
                        muteCurVolume = 1;
                    }
                    Log.d("siven","实时恢复音量 " + muteCurVolume);
                    notifyVolumeChange(muteCurVolume);//最大1.0,是个百分比
                    handler.sendEmptyMessageDelayed(ACTION_RESTORE_VALUE,restoreTime / 10);//2秒恢复音量100%音量
                } else {
                    notifyVolumeChange(muteTargetVolume);//最大1.0,是个百分比
                    Log.d("siven","实时恢复音量 " + muteTargetVolume);
                    if(volumeListener != null && isUnMuting) {
                        volumeListener.onCompleteUnMute(muteCurVolume);
                    }
                    isUnMuting = false;
                }
            } else if(msg.what == ACTION_STOP_VALUE) {
                if(unMuteCurVolume > unMuteTargetVolume) {
                    unMuteCurVolume -= 0.1;
                    if(unMuteCurVolume < 0) {
                        unMuteCurVolume = 0;
                    }
                    notifyVolumeChange(unMuteCurVolume);
                    Log.d("siven","实时静音音量 " + unMuteCurVolume);
                    if(unMuteCurVolume > unMuteTargetVolume) {
                        handler.sendEmptyMessageDelayed(ACTION_STOP_VALUE,stopTime / 10);//0.5秒恢复音量100%音量
                    } else {
                        if(volumeListener != null && isMuting) {
                            volumeListener.onCompleteMute(unMuteCurVolume);
                        }
                        isMuting = false;
                    }
                } else {
                    notifyVolumeChange(0f);//静音
                    if(volumeListener != null && isMuting) {
                        volumeListener.onCompleteMute(unMuteCurVolume);
                    }
                    isMuting = false;
                }
            }
        }
    };

    public void setVolumeListener(VolumeListener volumeListener) {
        this.volumeListener = volumeListener;
    }

    /**
     * 恢复音量,淡入效果
     */
    public void unMute(float curVolume,float tagetVolume) {
        if(isUnMuting) {
            return;
        }
        isUnMuting = true;
        this.muteTargetVolume = tagetVolume;
        this.muteCurVolume = curVolume;
        handler.sendEmptyMessage(ACTION_RESTORE_VALUE);
    }

    /**
     * 静音,淡出效果
     */
    public void mute(float curVolume) {
        if(isMuting) {
            return;
        }
        isMuting = true;
        this.unMuteCurVolume = curVolume;
        this.unMuteTargetVolume = 0f;
        handler.sendEmptyMessage(ACTION_STOP_VALUE);
    }

    private void notifyVolumeChange(float volume) {
        if(volumeListener != null) {
            volumeListener.onVolumeChange(volume);
        }
    }

    public interface VolumeListener {
        public void onCompleteMute(float curVolume);

        public void onCompleteUnMute(float curVolume);

        public void onVolumeChange(float volume);
    }

}
