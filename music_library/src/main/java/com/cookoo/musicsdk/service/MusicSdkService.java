package com.cookoo.musicsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.cookoo.musicsdk.binder.MusicAidlBinder;
import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.imp.IMusicMode;
import com.cookoo.musicsdk.manager.CookooMusicConfiguration;
import com.cookoo.musicsdk.manager.MediaAudioManager;
import com.cookoo.musicsdk.manager.MusicManager;
import com.cookoo.musicsdk.manager.SpManager;
import com.cookoo.musicsdk.modle.MusicInitParams;
import com.cookoo.musicsdk.utils.LogUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.model.MuteVolumeManager;

/**
 * @author
 */
public class MusicSdkService extends Service implements IMusicMode, OnErrorListener, OnCompletionListener, OnPreparedListener, OnSeekCompleteListener {
    private final String TAG = MusicSdkService.class.getSimpleName();
    private MusicManager mMusicManager = MusicManager.getInstance();

    private MusicInitParams param = CookooMusicConfiguration.getInstance().getParam();
    /**
     * 远程服务交互对象
     **/
    private MusicAidlBinder binder = new MusicAidlBinder();
    /**
     * 创建随机参数对象
     **/
    private Random random = new Random();
    /**
     * 快进快退步进时间：XX毫秒
     **/
    private int moveTime;
    /**
     * 快进快退步进时间：XX毫秒
     **/
    private long fastTimeInterval = 1000;
    /**
     * 是否快进快退中
     **/
    private boolean isFasting = false;
    /**
     * 媒体音量管理器，控制音量静音、淡入淡出、音量变化
     **/
    private MuteVolumeManager muteVolumeManager;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME:
                    handleUpdatePlayTime();
                    break;
                case MusicSdkConstants.MusicHandler.FAST_FORWARD:
                    handleFastForward();
                    break;
                case MusicSdkConstants.MusicHandler.BACK_FORWARD:
                    handleBackForward();
                    break;
                case MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY:
                    preparedPlay();
                    break;
                case MusicSdkConstants.MusicHandler.MUSIC_START_EXTEND_PROCESS:
                    sendStartExtendMusicBroadcast();
                    break;
                case MusicSdkConstants.MusicHandler.MUSIC_PLAY_START:
                    handleMusicStart();
                    break;
                case MusicSdkConstants.MusicHandler.MUSIC_PAUSE:
                    handleMusicPause();
                    break;
                default:
            }
        }
    };

    private void handleUpdatePlayTime() {
        //如果不是快进快退则按获取正常的播放时间，否则或者快进快退时间
        if(!isFasting && mMusicManager.getMediaPlayer().isPlaying()) {
            int currentPlayPosition = mMusicManager.getMediaPlayer().getCurrentPosition() < 0 ? 0 :mMusicManager.getMediaPlayer().getCurrentPosition();
            mMusicManager.setCurrentPlayPosition(currentPlayPosition);
            SpManager.getInstance().savePlayProgress(currentPlayPosition);
        }
        mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_TIME);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME,1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.print(TAG," ---->>onBind() binder: " + binder);
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.print(TAG,"onCreate()");
        binder.addMusicListener(this);
        mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_START_EXTEND_PROCESS,0);
        init();
    }

    /**
     * 向外发送一个服务启动广播，通知远程服务端进行服务绑定操作
     **/
    private void sendStartExtendMusicBroadcast() {
        Intent intent = new Intent();
        intent.setAction("start.extend.music.process.action");
        sendBroadcast(intent);
    }

    private void init() {
        if(param != null) {
            moveTime = param.getMoveDuration();
            if(param.isFadeInNndOut()) {
                muteVolumeManager = new MuteVolumeManager();
                muteVolumeManager.setVolumeListener(new MuteVolumeManager.VolumeListener() {
                    @Override
                    public void onCompleteMute(float v) {
                        LogUtils.print("volume","====onCompleteMute====" + v);
                        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PAUSE);
                        mHandler.sendEmptyMessage(MusicSdkConstants.MusicHandler.MUSIC_PAUSE);
                    }

                    @Override
                    public void onCompleteUnMute(float v) {
                        LogUtils.print("volume","====onCompleteUnMute====" + v);
                        if(mMusicManager.getMediaPlayer() != null && mMusicManager.getMediaPlayer().isPlaying()) {
                            mMusicManager.getMediaPlayer().setVolume(v,v);
                        }
                    }

                    @Override
                    public void onVolumeChange(float v) {
                        LogUtils.print("volume","==onVolumeChange==" + v);
                        if(mMusicManager.getMediaPlayer() != null && mMusicManager.getMediaPlayer().isPlaying()) {
                            mMusicManager.getMediaPlayer().setVolume(v,v);
                        }
                    }
                });
            }
        } else {
            LogUtils.print(TAG,"init param is null!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binder.removeMusicListener();
    }

    public MediaPlayer createMediaPlayer() {
        LogUtils.print(TAG,"===createMediaPlayer===" + mMusicManager.getMediaPlayer());
        if(mMusicManager.getMediaPlayer() == null) {
            mMusicManager.setMediaPlayer(new MediaPlayer());
            mMusicManager.getMediaPlayer().setOnPreparedListener(this);
            mMusicManager.getMediaPlayer().setOnCompletionListener(this);
            mMusicManager.getMediaPlayer().setOnErrorListener(this);
            mMusicManager.getMediaPlayer().setOnSeekCompleteListener(this);
        }
        return mMusicManager.getMediaPlayer();
    }

    /**
     * 准备播放
     */
    private void preparedPlay() {
        try {
            createMediaPlayer();
            LogUtils.print(TAG," ----preparedPlay()--->>>" + mMusicManager.getCurrentPlayMediaItem().getFilePath());
            mMusicManager.getMediaPlayer().reset();
            mMusicManager.getMediaPlayer().setDataSource(mMusicManager.getCurrentPlayMediaItem().getFilePath());
            mMusicManager.getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMusicManager.getMediaPlayer().prepareAsync();
        } catch(Exception e) {
            LogUtils.print(TAG," -------->>>preparedPlay()  eeee:  " + e.toString());
            onError(mMusicManager.getMediaPlayer(),MediaPlayer.MEDIA_ERROR_IO,0);
            e.printStackTrace();
        }
    }

    @Override
    public void playMusic(MediaData mediaData) {
        if(mediaData == null || TextUtils.isEmpty(mediaData.getFilePath())) {
            LogUtils.print(TAG,"playMusic filePath is null !");
            return;
        }
        LogUtils.print(TAG," ---->>> playMusic() ===mediaItem：" + mediaData.getFilePath());
        File file = new File(mediaData.getFilePath());
        if(file == null || !file.exists()) {
            nextMusic(false);
            LogUtils.print(TAG,"____" + mediaData.getFilePath() + "____playMusic file is not exist!");
            return;
        }
        // 当前播放的是相同的媒体文件则无需重新进行播放
        boolean isCurrentPlayItem = mMusicManager.isCurrentPlayItem(mediaData);
        if(mMusicManager.getCurrentPlayState() == MusicSdkConstants.PlayState.PLAY && isCurrentPlayItem) {
            mMusicManager.setCurrentPlayMediaItem(mediaData);
            mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_INFO);
            LogUtils.print(TAG,"the file is playing !");
            return;
        }
        // 如果已经播放过并且处于暂停状态，则只需继续播放
        if(mMusicManager.getCurrentPlayState() == MusicSdkConstants.PlayState.PAUSE && isCurrentPlayItem) {
            MediaAudioManager.getInstance().requestFocus(MusicSdkConstants.MediaType.MUSIC);
            return;
        }
        LogUtils.print(TAG,"prepare play music...");
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        mMusicManager.setCurrentPlayMediaItem(mediaData);
        mMusicManager.setCurrentPlayPosition(0);
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.SWITCH);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY);
        mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY,200);
    }

    @Override
    public void playOrPause() {
        LogUtils.print(TAG," ---->>> playOrPause() getMediaPlayer: " + mMusicManager.getMediaPlayer() + "  getCurrentPlayState: " + mMusicManager.getCurrentPlayState());
        if(mMusicManager.getMediaPlayer() == null || mMusicManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        if(mMusicManager.getCurrentPlayState() == MusicSdkConstants.PlayState.PAUSE) {
            MediaAudioManager.getInstance().requestFocus(MusicSdkConstants.MediaType.MUSIC);
        } else if(mMusicManager.getCurrentPlayState() == MusicSdkConstants.PlayState.PLAY) {
            pauseMusic();
        }
    }

    @Override
    public void rePlayMusic() {
        //TODO
    }

    /**
     * 暂停播放
     **/
    @Override
    public void pauseMusic() {
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PAUSE);
        if(muteVolumeManager != null && MusicManager.getInstance().isFadeInNndOut()) {
            muteVolumeManager.mute(1f);
        } else {
            mHandler.sendEmptyMessage(MusicSdkConstants.MusicHandler.MUSIC_PAUSE);
        }
    }

    private void handleMusicPause() {
        LogUtils.print(TAG,"  pauseMusic  CurrentPlayState:  " + mMusicManager.getCurrentPlayState());
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        if(mMusicManager.getCurrentPlayState() == MusicSdkConstants.PlayState.PLAY && mMusicManager.getMediaPlayer() != null) {
            mMusicManager.getMediaPlayer().pause();
            LogUtils.print("mediaplay","==pauseMusic pause==");
            mMusicManager.setCurrentPlayPosition(mMusicManager.getMediaPlayer().getCurrentPosition());
            LogUtils.print("mediaplay","==pauseMusic==" + mMusicManager.getMediaPlayer().getCurrentPosition());
            mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.PAUSE);
            mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_STATE);
            if (CookooMusicConfiguration.getInstance().getParam().isAbandonFocusAfterPause()){
                MediaAudioManager.getInstance().abandonFocus();
            }
        }
        if(muteVolumeManager != null && mMusicManager.isFadeInNndOut() && mMusicManager.getMediaPlayer() != null) {
            //暂停之后恢复媒体音量
            mMusicManager.getMediaPlayer().setVolume(1,1);
        }
    }

    @Override
    public void startMusic() {
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PLAY_START);
        mHandler.sendEmptyMessage(MusicSdkConstants.MusicHandler.MUSIC_PLAY_START);
    }

    /**
     * 处理音乐开始播放事件
     */
    private void handleMusicStart() {
        LogUtils.print(TAG,"------>>> start()  getCurrentPlayPosition: " + mMusicManager.getCurrentPlayPosition() + "  getCurrentPlayState: " + mMusicManager.getCurrentPlayState());
        isFasting = false;
        if(mMusicManager.getMediaPlayer() == null) {
            LogUtils.print(TAG,"------>>> start()  mediaPlayer is null...  ");
            return;
        }
        if(mMusicManager.getMediaPlayer().isPlaying()) {
            LogUtils.print(TAG,"------>>> start()   Current is in playing...  ");
            return;
        }
        if(muteVolumeManager != null && mMusicManager.isFadeInNndOut()) {
            mMusicManager.getMediaPlayer().setVolume(0,0);
            mMusicManager.getMediaPlayer().start();
            muteVolumeManager.unMute(0f,1f);
        } else {
            mMusicManager.getMediaPlayer().start();
        }
        if(mMusicManager.getCurrentPlayPosition() != 0 && mMusicManager.getCurrentPlayState() != MusicSdkConstants.PlayState.PAUSE) {
            mMusicManager.seekTo(mMusicManager.getCurrentPlayPosition());
            LogUtils.print(TAG,"------>>> start() seekTo...  ");
        } else {
            mMusicManager.setCurrentPlayPosition(0);
        }
        removeFastAndBackEvent();
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.PLAY);
        mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_STATE);
        mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_INFO);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        mHandler.sendEmptyMessage(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
    }

    @Override
    public void stopMusic() {
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        if(mMusicManager.getMediaPlayer() == null || mMusicManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        mMusicManager.setCurrentPlayPosition(mMusicManager.getMediaPlayer().getCurrentPosition());
        mMusicManager.getMediaPlayer().stop();
        LogUtils.print("mediaplay","==MediaPlayer stop==");
        mMusicManager.getMediaPlayer().reset();
        LogUtils.print("mediaplay","==MediaPlayer reset==");
        mMusicManager.getMediaPlayer().release();
        LogUtils.print("mediaplay","==MediaPlayer release==");
        mMusicManager.setMediaPlayer(null);
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.STOP);
        MediaAudioManager.getInstance().abandonFocus();
    }

    /**
     * 上一曲
     */
    @Override
    public void preMusic() {
        LogUtils.print(TAG,"-------->>>>previous getCurrentPlayMode: " + mMusicManager.getCurrentPlayMode());
        switch(mMusicManager.getCurrentPlayMode()) {
            case MusicSdkConstants.PlayMode.ALL_LOOP:
                allLoopPlay(0);
                break;
            case MusicSdkConstants.PlayMode.RANDOM_LOOP:
                randomPlay(mMusicManager.getPlayList());
                break;
            case MusicSdkConstants.PlayMode.SINGLE_LOOP:
                allLoopPlay(0);
                break;
            default:
        }
    }

    /**
     * 下一曲
     **/
    @Override
    public void nextMusic(boolean isUser) {
        LogUtils.print(TAG,"------->>>>nextMusic()  isUser: " + isUser + " getCurrentPlayMode:  " + mMusicManager.getCurrentPlayMode());
        if(isUser) {
            switch(mMusicManager.getCurrentPlayMode()) {
                case MusicSdkConstants.PlayMode.ALL_LOOP:
                    allLoopPlay(1);
                    break;
                case MusicSdkConstants.PlayMode.RANDOM_LOOP:
                    randomPlay(mMusicManager.getPlayList());
                    break;
                case MusicSdkConstants.PlayMode.SINGLE_LOOP:
                    allLoopPlay(1);
                    break;
                default:
            }
        } else {
            switch(mMusicManager.getCurrentPlayMode()) {
                case MusicSdkConstants.PlayMode.ALL_LOOP:
                    allLoopPlay(1);
                    break;
                case MusicSdkConstants.PlayMode.RANDOM_LOOP:
                    randomPlay(mMusicManager.getPlayList());
                    break;
                case MusicSdkConstants.PlayMode.SINGLE_LOOP:
                    singlePlay();
                    break;
                default:

            }
        }
    }

    /**
     * 快进
     **/
    @Override
    public void fastForward() {
        LogUtils.print(TAG,"---->> fastForward isFasting = " + isFasting);
        if(isFasting) {
            removeFastAndBackEvent();
            return;
        }
        handleFastForward();
    }

    /**
     * 快退
     **/
    @Override
    public void backForward() {
        LogUtils.print(TAG,"---->> backForward isFasting = " + isFasting);
        if(isFasting) {
            removeFastAndBackEvent();
            return;
        }
        handleBackForward();
    }

    /**
     * 处理快退事件
     */
    private void handleBackForward() {
        isFasting = false;
        if(mMusicManager.getMediaPlayer() != null && mMusicManager.getMediaPlayer().isPlaying()) {
            isFasting = true;
            int position = mMusicManager.getCurrentPlayPosition() - moveTime;
            LogUtils.print(TAG,"---->> backForward  position: " + position);
            if(position < 0) {
                mMusicManager.seekTo(0);
                isFasting = false;
            } else {
                mMusicManager.seekTo(position);
                if(param != null && param.getMoveType() == MusicSdkConstants.MoveType.KEEP_MOVING) {
                    mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.BACK_FORWARD,fastTimeInterval);
                }
            }
        }
    }

    /**
     * 处理快进事件
     */
    private void handleFastForward() {
        isFasting = false;
        if(mMusicManager.getMediaPlayer() != null && mMusicManager.getMediaPlayer().isPlaying()) {
            isFasting = true;
            int position = mMusicManager.getCurrentPlayPosition() + moveTime;
            int allTime = mMusicManager.getMediaPlayer().getDuration();
            LogUtils.print(TAG,"---->> fastForward  position: " + position + " , allTime: " + allTime);
            if(position >= allTime) {
                nextMusic(false);
            } else {
                mMusicManager.seekTo(position);
            }
            if(param != null && param.getMoveType() == MusicSdkConstants.MoveType.KEEP_MOVING) {
                mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.FAST_FORWARD,fastTimeInterval);
            }
        }
    }

    /**
     * 移除快进快退事件
     */
    private void removeFastAndBackEvent() {
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.BACK_FORWARD);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.FAST_FORWARD);
        isFasting = false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtils.print(TAG,"----->> onPrepared");
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.PREPARE);
        MediaAudioManager.getInstance().requestFocus(MusicSdkConstants.MediaType.MUSIC);
    }

    /**
     * 歌曲播放错误监听
     **/
    @Override
    public boolean onError(MediaPlayer mp,int what,int extra) {
        LogUtils.print(TAG,"------->>> onError what: " + what + "  extra:" + extra);
        stopMusic();
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.ERROR);
        mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_INFO);
        mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.MUSIC_PLAY_ERROR);
        return false;
    }

    /**
     * 歌曲播放完成监听
     **/
    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtils.print(TAG,"----->>> onCompletion");
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.COMPLETED);
        nextMusic(false);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        LogUtils.print(TAG,"----->>> onSeekComplete");
    }

    /**
     * 封装了所有循环逻辑
     * @param type 上一曲 : 0 , 下一曲 : 1
     */
    private void allLoopPlay(int type) {
        LogUtils.print(TAG,"------>>>>allLoopPlay type:  " + type);
        switch(type) {
            case 0:
                allLoopPreMusic(mMusicManager.getPlayList());
                break;
            case 1:
                allLoopNextMusic(mMusicManager.getPlayList(),mMusicManager.getCurrentPlayMediaItem());
                break;
            default:
        }
    }

    private void allLoopPreMusic(List<MediaData> items) {
        LogUtils.print(TAG,"---------->>>allLoopPreMusic getListPosition111: " + mMusicManager.getCurrentListPosition());
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        int listPosition = 0;
        if(items.size() > 0) {
            for(int i = 0; i < items.size(); i++) {
                if(mMusicManager.isCurrentPlayItem(items.get(i))) {
                    listPosition = i;
                    break;
                }
            }
            if(listPosition == 0) {
                listPosition = items.size() - 1;
            } else {
                listPosition = listPosition - 1;
            }
            if(items.get(listPosition).isFolder()) {
                listPosition = mMusicManager.getPlayList().size() - 1;
            }
            mMusicManager.setCurrentListPosition(listPosition);
            mMusicManager.setCurrentPlayMediaItem(items.get(listPosition));
            mMusicManager.setCurrentPlayPosition(0);
            LogUtils.print(TAG,"---------->>>allLoopPreMusic getListPosition222: " + listPosition);
            mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.SWITCH);
            mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY);
            mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY,200);
        }
    }

    private void allLoopNextMusic(List<MediaData> items,MediaData currentMediaData) {
        LogUtils.print(TAG,"---------->>>allLoopNextMusic getListPosition111: " + mMusicManager.getCurrentListPosition() + " items.size()：" + items.size() + "|" + currentMediaData);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.UPDATE_PLAY_TIME);
        if(currentMediaData == null) {
            return;
        }
        int listPosition = 0;
        if(items.size() <= 0) {
            return;
        }
        for(int i = 0; i < items.size(); i++) {
            if(items.get(i) != null && items.get(i).getFilePath() != null && items.get(i).getFilePath().equals(currentMediaData.getFilePath())) {
                listPosition = i;
                LogUtils.print(TAG,"---------->>>allLoopNextMusic getListPosition333: " + listPosition);
                break;
            }
        }
        LogUtils.print(TAG,"---------->>>allLoopNextMusic getListPosition444: " + listPosition);
        if(listPosition == items.size() - 1) {
            listPosition = 0;
        } else {
            listPosition = listPosition + 1;
        }
        mMusicManager.setCurrentListPosition(listPosition);
        boolean isFolder = items.get(listPosition).isFolder();
        LogUtils.print(TAG,"---------->>>allLoopNextMusic getListPosition222: " + mMusicManager.getCurrentListPosition() + "|" + isFolder);
        if(isFolder) {
            allLoopNextMusic(mMusicManager.getPlayList(),items.get(listPosition));
            return;
        }
        mMusicManager.setCurrentPlayPosition(0);
        mMusicManager.setCurrentPlayMediaItem(items.get(mMusicManager.getCurrentListPosition()));
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.SWITCH);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY);
        mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY,200);
    }

    private void randomPlay(List<MediaData> items) {
        LogUtils.print(TAG,"-------->>>>randomPlay");
        if(items != null && items.size() > 0) {
            if(items.size() > 1) {
                int randomPosition = random.nextInt(items.size() - 1);
                if(randomPosition == mMusicManager.getCurrentListPosition()) {
                    mMusicManager.setCurrentListPosition(randomPosition + 1);
                } else {
                    mMusicManager.setCurrentListPosition(randomPosition);
                }
                if(mMusicManager.getCurrentListPosition() > items.size() - 1) {
                    mMusicManager.setCurrentListPosition(0);
                }
                if(mMusicManager.getCurrentListPosition() < 0) {
                    mMusicManager.setCurrentListPosition(items.size() - 1);
                }
                mMusicManager.setCurrentPlayMediaItem(items.get(mMusicManager.getCurrentListPosition()));
                mMusicManager.setCurrentPlayPosition(0);
            }
            mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.SWITCH);
            mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY);
            mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY,200);
        }
    }

    private void singlePlay() {
        mMusicManager.setCurrentPlayPosition(0);
        mMusicManager.setCurrentPlayState(MusicSdkConstants.PlayState.SWITCH);
        mHandler.removeMessages(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY);
        mHandler.sendEmptyMessageDelayed(MusicSdkConstants.MusicHandler.MUSIC_PREPARE_PLAY,200);
    }

    /**
     * 拔掉usb后把所有数据清除
     * 清理usbPath的Usb的数据
     */
    @Override
    public void clearUsbMusic(String usbPath) {
        if(TextUtils.isEmpty(usbPath)) {
            return;
        }
        //判断当前浏览列表是否为卸载U盘内数据，如是则需要清除
        for(MediaListData ml : mMusicManager.getOriginalData()) {
            LogUtils.print(TAG,"  ----->> clearUsbMusic: ml.getUsbRootPath " + ml.getUsbRootPath() + "|" + usbPath);
            if(TextUtils.isEmpty(ml.getUsbRootPath())) {
                continue;
            }
            if(ml.getUsbRootPath().startsWith(usbPath)) {
                mMusicManager.getOriginalData().remove(ml);
                break;
            }
        }
        //判断当前播放列表是否为卸载U盘内数据，如是则需要清除
        LogUtils.print(TAG,"---111->> clearUsbMusic() size: " + mMusicManager.getOriginalData().size() + "|" + mMusicManager.getPlayList().size());
        if(mMusicManager.getPlayList().size() > 0) {
            Iterator it = mMusicManager.getPlayList().iterator();
            while(it.hasNext()) {
                MediaData md = (MediaData) it.next();
                if(md != null && !TextUtils.isEmpty(md.getFilePath()) && md.getFilePath().startsWith(usbPath)) {
                    it.remove();
                }
            }
        }
        if(mMusicManager.getMediaPlayer() == null || mMusicManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        LogUtils.print(TAG,"--222-->> clearUsbMusic() size: " + mMusicManager.getPlayList().size());
        //先判断拔掉usb是否为当前播放对象所在的usb在做stopMusic处理，同时清除当前播放歌曲
        if(mMusicManager.getCurrentPlayMediaItem().getFilePath().startsWith(usbPath)) {
            LogUtils.print("mediaplay","==clearUsbMusic isPlaying==" + mMusicManager.getMediaPlayer().isPlaying() + "|" + mMusicManager.getCurrentPlayState());
            stopMusic();
            mMusicManager.setCurrentPlayMediaItem(null);
            mMusicManager.setCurrentPlayPosition(0);
            mMusicManager.setCurrentListPosition(0);
            mMusicManager.sendMusicStateEvent(MusicSdkConstants.MusicStateEventId.IS_USB_UNMOUNT_STOPMUSIC);
        }
    }
}
