package com.cookoo.musicsdk.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

import com.cookoo.musicsdk.utils.GlobalTool;
import com.cookoo.musicsdk.utils.LogUtils;

/**
 * @author lsf
 */
public class MediaAudioManager {

	private final String TAG = "carAudio";
	/**当前播放媒体音源**/
	private int currentPlaySource = -1;
	/**是否有焦点**/
	private boolean isFocus;
	/**声音焦点管理器**/
	private AudioManager audioManager;

	private MediaAudioManager() {
	}

	public static MediaAudioManager getInstance() {
		return CarMediaAudioManagerInstance.CAR_MEDIA_AUDIO_MANAGER;
	}

	private static class CarMediaAudioManagerInstance {
		private static final MediaAudioManager CAR_MEDIA_AUDIO_MANAGER = new MediaAudioManager();
	}

	public boolean isFocus() {
		return isFocus;
	}

	public void init(){
		audioManager = (AudioManager) GlobalTool.getInstance().getContext().getSystemService(Context.AUDIO_SERVICE);
	}

	public void setFocus(boolean isFocus) {
		this.isFocus = isFocus;
	}

	/**
	 * 焦点请求
	 * @return
	 */
	public void requestFocus(int mediaType) {
		LogUtils.print(TAG,"----->>requestFocus()" + " currentPlaySource: " + currentPlaySource + " isFocus:" + isFocus + " mediaType: " + mediaType);
		currentPlaySource = mediaType;
		if (isFocus) {
			handleGetFocus();
			return;
		}
		int result = audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		LogUtils.print(TAG, "--requestFocus2()--result->" + result);
//		if(AudioManager.AUDIOFOCUS_GAIN == result){
//			handleGetFocus();
//		}
	}

	/** 释放声音焦点 **/
	public void abandonFocus() {
		LogUtils.print(TAG, "====abandonFocus()========" + audioManager + ", focus = " + isFocus + ", audioFocusListener = " + audioFocusListener);
		if (audioManager != null && isFocus()) {
			audioManager.abandonAudioFocus(audioFocusListener);
			setFocus(false);
		}
	}

	private OnAudioFocusChangeListener audioFocusListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			LogUtils.print(TAG, "====onAudioFocusChange========" + focusChange + ", focus = " + isFocus);
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				isFocus = true;
				handleGetFocus();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				isFocus = false;
				handleFocusLoss();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				isFocus = false;
				handleFocusLoss();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				isFocus = true;
				handleGetFocus();
				break;
				default:
			}
		}
	};

	/**
	 * 主动请求焦点的情况下，直接进行播放，
	 * 被抢焦点后释放回来的，需要先判断播放状态再做播放处理
	 */
	private void handleGetFocus() {
		MusicManager.getInstance().startMusic();
	}

	protected void handleFocusLoss() {
		MusicManager.getInstance().pauseMusic();
	}

}
