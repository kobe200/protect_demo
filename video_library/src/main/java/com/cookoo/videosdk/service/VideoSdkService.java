package com.cookoo.videosdk.service;

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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cookoo.videosdk.binder.VideoAidlBinder;
import com.cookoo.videosdk.imp.IVideoModule;
import com.cookoo.videosdk.manager.CookooVideoConfiguration;
import com.cookoo.videosdk.manager.MediaAudioManager;
import com.cookoo.videosdk.manager.SpManager;
import com.cookoo.videosdk.manager.VideoManager;
import com.cookoo.videosdk.modle.VideoInitParams;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.model.MuteVolumeManager;

/**
 * @author lsf
 * @date 2018/3/21
 */

public class VideoSdkService extends Service implements OnErrorListener, OnCompletionListener, OnPreparedListener, OnSeekCompleteListener, IVideoModule {
    private static final String TAG = VideoSdkService.class.getSimpleName();
    private VideoAidlBinder binder = new VideoAidlBinder();
    private VideoManager mVideoManager = VideoManager.getInstance();
    private VideoInitParams param = CookooVideoConfiguration.getInstance().getParam();
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
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
                case VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME:
                    handleUpdatePlayTime();
                    mVideoManager.sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME);
                    mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME,1000 - (mVideoManager.getCurrentPlayPosition() % 1000));
                    break;
                case VideoSdkConstants.VideoHandler.FAST_FORWARD:
                    handleFastForward();
                    break;
                case VideoSdkConstants.VideoHandler.BACK_FORWARD:
                    handleBackForward();
                    break;
                case VideoSdkConstants.VideoHandler.VIDEO_PLAY_ERROR:
                    mVideoManager.sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.VIDEO_PLAY_ERROR);
                    break;
                case VideoSdkConstants.VideoHandler.VIDEO_PREPARE_PLAY:
                    preparedPlay();
                    break;
                case VideoSdkConstants.VideoHandler.VIDEO_PLAY_NEXT:
                    nextVideo();
                    break;
                case VideoSdkConstants.VideoHandler.VIDEO_PLAY_START:
                    handleVideoStart();
                    break;
                case VideoSdkConstants.VideoHandler.VIDEO_PAUSE:
                    handleVideoPause();
                    break;
                default:
            }
        }
    };
    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            LogUtils.print(TAG,"  ---->> surfaceCreated()->" + mVideoManager.getMediaPlayer());
            createMediaPlayer();
            mVideoManager.getMediaPlayer().setDisplay(surfaceHolder);
            mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.IDLE);
            if(mVideoManager.getCurrentPlayMediaItem() != null) {
                preparedPlay();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder,int i,int i1,int i2) {
            LogUtils.print(TAG,"  ---->> surfaceChanged()");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            LogUtils.print(TAG,"  ---->> surfaceDestroyed()" + surfaceView);
            if(surfaceView != null) {
                stopVideo();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.print(TAG,"----->>> onCreate() ");
        binder.addVideoListener(this);
        sendStartExtendVideoBroadcast();
        init();
    }

    private void sendStartExtendVideoBroadcast() {
        Intent intent = new Intent();
        intent.setAction("start.extend.video.process.action");
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
                        mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PAUSE);
                        mHandler.sendEmptyMessage(VideoSdkConstants.VideoHandler.VIDEO_PAUSE);
                    }

                    @Override
                    public void onCompleteUnMute(float v) {
                        LogUtils.print("volume","====onCompleteUnMute====" + v);
                        if(mVideoManager.getMediaPlayer() != null && mVideoManager.getMediaPlayer().isPlaying()) {
                            mVideoManager.getMediaPlayer().setVolume(v,v);
                        }
                    }

                    @Override
                    public void onVolumeChange(float v) {
                        LogUtils.print("volume","==onVolumeChange==" + v);
                        if(mVideoManager.getMediaPlayer() != null && mVideoManager.getMediaPlayer().isPlaying()) {
                            mVideoManager.getMediaPlayer().setVolume(v,v);
                        }
                    }
                });
            }
        } else {
            LogUtils.print(TAG,"init param is null!");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binder.removeVideoListener();
    }

    @Override
    public void initSurfaceHolder(final SurfaceView surfaceView) {
        if(surfaceView == this.surfaceView){
            return;
        }
        LogUtils.print(TAG,"==initSurfaceHolder==" + surfaceView + "|" + this.surfaceView);
        this.surfaceView = surfaceView;
        this.holder = surfaceView.getHolder();
        holder.removeCallback(callback);
        holder.addCallback(callback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public MediaPlayer createMediaPlayer() {
        LogUtils.print(TAG,"===createMediaPlayer===" + mVideoManager.getMediaPlayer());
        if(mVideoManager.getMediaPlayer() == null) {
            mVideoManager.setMediaPlayer(new MediaPlayer());
            mVideoManager.getMediaPlayer().setOnPreparedListener(this);
            mVideoManager.getMediaPlayer().setOnCompletionListener(this);
            mVideoManager.getMediaPlayer().setOnErrorListener(this);
            mVideoManager.getMediaPlayer().setOnSeekCompleteListener(this);
        }
        return mVideoManager.getMediaPlayer();
    }

    @Override
    public void setVideoLayout() {
        if(mVideoManager.isAllUsbUnMount() || mVideoManager.getMediaPlayer() == null) {
            return;
        }
        int[] videoPosition = mVideoManager.getVideoPosition();
        int surfaceWidth = videoPosition[0];
        int surfaceHeight = videoPosition[1];
        int marginLeft = videoPosition[2];
        int marginTop = videoPosition[3];
        float requestRatio = SpManager.getInstance().getSavePlayFormat();
        LogUtils.print(TAG,"--->>> setVideoLayout() ==1111== requestRatio: " + requestRatio + " getVideoWidth: " + mVideoManager.getMediaPlayer().getVideoWidth() + " getVideoHeight: " + mVideoManager.getMediaPlayer().getVideoHeight());
        if(VideoSdkConstants.VideoPlayFormat.AUTO_SCALE == requestRatio) {
            requestRatio = mVideoManager.getMediaPlayer().getVideoWidth() / (float) mVideoManager.getMediaPlayer().getVideoHeight();
        }
        LogUtils.print(TAG,"--->>> setVideoLayout() ==2222== requestRatio: " + requestRatio);
        int realVideoWidth = (1 < requestRatio) ? surfaceWidth :(int) (requestRatio * surfaceHeight);
        int realVideoHeight = (1 > requestRatio) ? surfaceHeight :(int) (surfaceWidth / requestRatio);
        int marginRight = (surfaceWidth - realVideoWidth) / 2;
        int marginBottom = (surfaceHeight - realVideoHeight) / 2;
        marginLeft = (surfaceWidth - realVideoWidth) / 2 + marginLeft;
        marginTop = (surfaceHeight - realVideoHeight) / 2 + marginTop;
        surfaceView.getLayoutParams().width = realVideoWidth;
        surfaceView.getLayoutParams().height = realVideoHeight;
        if(surfaceView.getLayoutParams() instanceof AbsoluteLayout.LayoutParams) {
            LogUtils.print(TAG,"--->>> setVideoLayout() surfaceView parent layout the AbsoluteLayout is unsupported");
        } else if(surfaceView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) surfaceView.getLayoutParams()).leftMargin = marginLeft;
            ((LinearLayout.LayoutParams) surfaceView.getLayoutParams()).topMargin = marginTop;
            ((LinearLayout.LayoutParams) surfaceView.getLayoutParams()).rightMargin = marginRight;
            ((LinearLayout.LayoutParams) surfaceView.getLayoutParams()).bottomMargin = marginBottom;
        } else {
            ((RelativeLayout.LayoutParams) surfaceView.getLayoutParams()).setMargins(marginLeft,marginTop,marginRight,marginBottom);
        }
        surfaceView.setLayoutParams(surfaceView.getLayoutParams());
    }

    private void preparedPlay() {
        try {
            createMediaPlayer();
            File file = new File(mVideoManager.getCurrentPlayMediaItem().getFilePath());
            if(file == null || !file.exists()) {
                LogUtils.print(TAG," preparedPlay file is not exist!");
                return;
            }
            LogUtils.print(TAG," -------->>>preparedPlay()  getFilePath:  " + mVideoManager.getCurrentPlayMediaItem().getFilePath());
            mVideoManager.getMediaPlayer().reset();
            mVideoManager.getMediaPlayer().setDataSource(mVideoManager.getCurrentPlayMediaItem().getFilePath());
            mVideoManager.getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            mVideoManager.getMediaPlayer().setDisplay(surfaceView.getHolder());
            mVideoManager.getMediaPlayer().prepareAsync();
        } catch(Exception e) {
            LogUtils.print(TAG," -------->>>preparedPlay()  exception:  " + e.toString());
            onError(mVideoManager.getMediaPlayer(),MediaPlayer.MEDIA_ERROR_UNKNOWN,0);
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PLAY_START);
        mHandler.sendEmptyMessage(VideoSdkConstants.VideoHandler.VIDEO_PLAY_START);
    }

    private void handleVideoStart() {
        LogUtils.print(TAG,"------>>> start()  getCurrentPlayPosition: " + mVideoManager.getCurrentPlayPosition() + "  getCurrentPlayState: " + mVideoManager.getCurrentPlayState());
        if(mVideoManager.getMediaPlayer() == null) {
            LogUtils.print(TAG,"------>>> start()  mediaPlayer is null...  ");
            return;
        }
        if(mVideoManager.getMediaPlayer().isPlaying()) {
            LogUtils.print(TAG,"------>>> start()   Current is in playing...  ");
            return;
        }
        if(muteVolumeManager != null && mVideoManager.isFadeInNndOut()) {
            mVideoManager.getMediaPlayer().setVolume(0,0);
            mVideoManager.getMediaPlayer().start();
            muteVolumeManager.unMute(0f,1f);
        } else {
            mVideoManager.getMediaPlayer().start();
        }
        if(mVideoManager.getCurrentPlayPosition() != 0 ) {
            mVideoManager.getMediaPlayer().seekTo(mVideoManager.getCurrentPlayPosition());
        } else {
            mVideoManager.setCurrentPlayPosition(0);
        }
        removeFastAndBackEvent();
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.PLAY);
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        mHandler.sendEmptyMessage(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        mVideoManager.sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_STATE);
    }

    @Override
    public void playVideo(MediaData mediaItemInfo) {
        boolean isCurrentPlayItem = mVideoManager.isCurrentPlayItem(mediaItemInfo);
        LogUtils.print(TAG,"---->> playVideo() getCurrentPlayState: " + mVideoManager.getCurrentPlayState() + "  isCurrentPlayItem: " + isCurrentPlayItem);
        if(surfaceView == null){
            LogUtils.print(TAG,"playVideo surfaceView == null");
            return;
        }
        if(mediaItemInfo == null || TextUtils.isEmpty(mediaItemInfo.getFilePath())) {
            LogUtils.print(TAG,"playVideo filePath is null !");
            return;
        }
        File file = new File(mediaItemInfo.getFilePath());
        if(file == null || !file.exists()) {
            LogUtils.print(TAG,"    playVideo file is not exist!");
            return;
        }
        if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.PLAY && isCurrentPlayItem) {
            LogUtils.print(TAG,"    the file is playing !");
            mVideoManager.setCurrentPlayMediaItem(mediaItemInfo);
            return;
        }
        // 如果已经播放过并且处于暂停状态，则只需继续播放
        if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.PAUSE && isCurrentPlayItem) {
            LogUtils.print(TAG,"--->> playVideo()  continue !");
            MediaAudioManager.getInstance().requestFocus(VideoSdkConstants.MediaType.VIDEO);
            return;
        }
        // 如果已经播放过并且处于暂停状态，则只需继续播放
        if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.STOP && isCurrentPlayItem) {
            LogUtils.print(TAG,"--->> playVideo()  stop !");
        }else{
            mVideoManager.setCurrentPlayPosition(0);
        }
        LogUtils.print(TAG,"--->> playVideo()  Switch to another video play！");
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.SWITCH);
        mVideoManager.setCurrentPlayMediaItem(mediaItemInfo);
        LogUtils.print(TAG,"--->> playVideo() surfaceView ->" );
        if(surfaceView.getVisibility() == View.GONE) {
            //重新创建surfaceView视图
            surfaceView.setVisibility(View.VISIBLE);
        } else {
            LogUtils.print(TAG,"--->> playVideo PREPARE_PLAY ->");
            mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PREPARE_PLAY);
            mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.VIDEO_PREPARE_PLAY,200);
        }
    }

    @Override
    public void pauseVideo() {
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PAUSE);
        if(muteVolumeManager != null && mVideoManager.isFadeInNndOut()) {
            muteVolumeManager.mute(1f);
        } else {
            mHandler.sendEmptyMessage(VideoSdkConstants.VideoHandler.VIDEO_PAUSE);
        }
    }

    private void handleVideoPause() {
        LogUtils.print(TAG,"---->> pauseVideo()");
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.PLAY && mVideoManager.getMediaPlayer() != null) {
            LogUtils.print(TAG,"---->> pauseVideo()" + mVideoManager.getMediaPlayer().getCurrentPosition());
            mVideoManager.getMediaPlayer().pause();
            mVideoManager.setCurrentPlayPosition(mVideoManager.getMediaPlayer().getCurrentPosition());
            mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.PAUSE);
            mVideoManager.sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_STATE);
            if (CookooVideoConfiguration.getInstance().getParam().isAbandonFocusAfterPause()){
                MediaAudioManager.getInstance().abandonFocus();
            }
        }
        if(muteVolumeManager != null && mVideoManager.isFadeInNndOut() &&mVideoManager.getMediaPlayer() != null) {
            //暂停之后恢复媒体音量
            mVideoManager.getMediaPlayer().setVolume(1,1);
        }
    }

    @Override
    public void playOrPause() {
        LogUtils.print(TAG," ---->>> playOrPause() getMediaPlayer: " + mVideoManager.getMediaPlayer() + "  getCurrentPlayState: " + mVideoManager.getCurrentPlayState());
        if(mVideoManager.getMediaPlayer() == null || mVideoManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.IDLE) {
            playVideo(mVideoManager.getCurrentPlayMediaItem());
        } else if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.PAUSE) {
            MediaAudioManager.getInstance().requestFocus(VideoSdkConstants.MediaType.VIDEO);
        } else if(mVideoManager.getCurrentPlayState() == VideoSdkConstants.PlayState.PLAY) {
            pauseVideo();
        }
    }

    @Override
    public void rePlayVideo() {

    }

    @Override
    public void preVideo() {
        LogUtils.print(TAG,"---preVideo--->>>" + mVideoManager.getCurrentListPosition() + " size: " + mVideoManager.getPlayList().size());
        if(mVideoManager.getPlayList().size() < 1) {
            return;
        }
        int currentListPosition = mVideoManager.getCurrentListPosition();
        if(currentListPosition <= 0 || currentListPosition > mVideoManager.getPlayList().size() - 1) {
            currentListPosition = mVideoManager.getPlayList().size() - 1;
        } else {
            currentListPosition = mVideoManager.getCurrentListPosition() - 1;
        }
        MediaData playData = mVideoManager.getPlayList().get(currentListPosition);
        if(playData.isFolder()) {
            currentListPosition = mVideoManager.getPlayList().size() - 1;
        }
        mVideoManager.setCurrentListPosition(currentListPosition);
        LogUtils.print(TAG,"--preVideo-->>> " + mVideoManager.getCurrentListPosition());
        playVideo(mVideoManager.getPlayList().get(currentListPosition));
    }

    @Override
    public void nextVideo() {
        LogUtils.print(TAG,"----nextVideo-->>>" + mVideoManager.getCurrentListPosition() + " size: " + mVideoManager.getPlayList().size());
        if(mVideoManager.getPlayList().size() < 1) {
            return;
        }
        int currentListPosition = mVideoManager.getCurrentListPosition();
        if(currentListPosition < 0 || currentListPosition >= mVideoManager.getPlayList().size() - 1) {
            currentListPosition = getFirstNoFolderPosition(mVideoManager.getPlayList());
        } else {
            currentListPosition = mVideoManager.getCurrentListPosition() + 1;
        }
        mVideoManager.setCurrentListPosition(currentListPosition);
        MediaData playData = mVideoManager.getPlayList().get(mVideoManager.getCurrentListPosition());
        LogUtils.print(TAG,"----nextVideo--->>>" + mVideoManager.getCurrentListPosition() + "|" + playData.getName());
        playVideo(playData);
    }

    private int getFirstNoFolderPosition(List<MediaData> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            if(!list.get(i).isFolder()) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void fastForward() {
        LogUtils.print(TAG,"---->> fastForward isFasting = " + isFasting);
        if(isFasting) {
            removeFastAndBackEvent();
            return;
        }
        handleFastForward();
    }

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
        if(mVideoManager.getMediaPlayer() != null && mVideoManager.getMediaPlayer().isPlaying()) {
            isFasting = true;
            int position = mVideoManager.getCurrentPlayPosition() - moveTime;
            LogUtils.print(TAG,"---->> backForward  position: " + position);
            if(position < 0) {
                mVideoManager.seekTo(0);
                isFasting = false;
            } else {
                mVideoManager.seekTo(position);
                if(CookooVideoConfiguration.getInstance().getParam().getMoveType() == VideoSdkConstants.MoveType.KEEP_MOVING) {
                    mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.BACK_FORWARD,fastTimeInterval);
                }
            }
        }
    }

    /**
     * 处理快进事件
     */
    private void handleFastForward() {
        isFasting = false;
        if(mVideoManager.getMediaPlayer() != null && mVideoManager.getMediaPlayer().isPlaying()) {
            isFasting = true;
            int position = mVideoManager.getCurrentPlayPosition() + moveTime;
            int allTime = mVideoManager.getMediaPlayer().getDuration();
            LogUtils.print(TAG,"---->> fastForward  position: " + position + " , allTime: " + allTime);
            if(position >= allTime) {
                isFasting = false;
                nextVideo();
            } else {
                mVideoManager.seekTo(position);
                if(CookooVideoConfiguration.getInstance().getParam().getMoveType() == VideoSdkConstants.MoveType.KEEP_MOVING) {
                    mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.FAST_FORWARD,fastTimeInterval);
                }
            }
        }
    }

    /**
     * 移除快进快退事件
     */
    private void removeFastAndBackEvent() {
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.BACK_FORWARD);
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.FAST_FORWARD);
        isFasting = false;
    }

    /**
     * 处理时间更新事件
     */
    private void handleUpdatePlayTime() {
        //如果不是快进快退则按获取正常的播放时间，否则设置快进快退时间，避免两个时间同时更新导致的问题
        if(!isFasting) {
            int currentPlayPosition = mVideoManager.getMediaPlayer().getCurrentPosition() < 0 ? 0 :mVideoManager.getMediaPlayer().getCurrentPosition();
            mVideoManager.setCurrentPlayPosition(currentPlayPosition);
            SpManager.getInstance().savePlayProgress(mVideoManager.getCurrentPlayPosition());
        }
        mVideoManager.sendVideoStateEvent(VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME);
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        if(mVideoManager.getCurrentPlayState() != VideoSdkConstants.PlayState.SWITCH) {
            mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME,1000);
        }
    }

    @Override
    public void clearUsbVideo(String usbPath) {
        LogUtils.print(TAG,"---->> clearUsbVideo()" + usbPath);
        if(TextUtils.isEmpty(usbPath)) {
            return;
        }
        for(MediaListData ml : mVideoManager.getOriginalData()) {
            LogUtils.print(TAG,"  ----->> clearUsbVideo: ml.getUsbRootPath " + ml.getUsbRootPath());
            if(ml.getUsbRootPath() != null && ml.getUsbRootPath().startsWith(usbPath)) {
                mVideoManager.getOriginalData().remove(ml);
                break;
            }
        }
        LogUtils.print(TAG,"  ---clearUsbVideo PlayList size-->>  " + mVideoManager.getPlayList().size());
        //判断当前播放列表是否为卸载U盘内数据，如是则需要清除
        if(mVideoManager.getPlayList().size() > 0) {
            Iterator it = mVideoManager.getPlayList().iterator();
            while(it.hasNext()) {
                MediaData md = (MediaData) it.next();
                if(md != null && !TextUtils.isEmpty(md.getFilePath()) && md.getFilePath().startsWith(usbPath)) {
                    it.remove();
                }
            }
        }
        if(mVideoManager.getMediaPlayer() == null || mVideoManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        //先判断拔掉usb是否为当前播放对象所在的usb在做stopVideo处理
        if(mVideoManager.getCurrentPlayMediaItem().getFilePath().startsWith(usbPath)) {
            //当前播放文件所在USB设备卸载，无需处理播放完成事件
            mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PLAY_NEXT);
            LogUtils.print(TAG,"  ---clearUsbVideo remove completed-->>  ");
            stopVideo();
            mVideoManager.setCurrentPlayMediaItem(null);
            mVideoManager.setCurrentPlayPosition(0);
            mVideoManager.setCurrentListPosition(0);
            mVideoManager.sendVideoStateEvent((VideoSdkConstants.VideoStateEventId.IS_USB_UN_MOUNT_STOP_VIDEO));
        }
    }

    /**
     * 释放媒体播放器资源
     * 在播放进程退出时调用
     */
    @Override
    public void stopVideo() {
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.UPDATE_PLAY_TIME);
        if(mVideoManager.getMediaPlayer() == null || mVideoManager.getCurrentPlayMediaItem() == null) {
            return;
        }
        LogUtils.print(TAG,"---->>  stopVideo()  CurrentPosition: " + mVideoManager.getCurrentPlayPosition() + "|" + mVideoManager.getMediaPlayer().isPlaying());
        mVideoManager.setCurrentPlayPosition(mVideoManager.getCurrentPlayPosition());
        mVideoManager.getMediaPlayer().stop();
        LogUtils.print(TAG,"---->>  stopVideo()");
        mVideoManager.getMediaPlayer().reset();
        LogUtils.print(TAG,"---->>  reset()");
        mVideoManager.getMediaPlayer().release();
        LogUtils.print(TAG,"---->>  release()");
        mVideoManager.setMediaPlayer(null);
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.STOP);
        MediaAudioManager.getInstance().abandonFocus();
        if(surfaceView != null) {
            LogUtils.print(TAG,"---->> stopVideo 清除图像缓存");
            //清除图像缓存
            surfaceView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtils.print(TAG,"----->> onPrepared");
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.PREPARE);
        MediaAudioManager.getInstance().requestFocus(VideoSdkConstants.MediaType.VIDEO);
    }

    /**
     * 歌曲播放错误监听
     **/
    @Override
    public boolean onError(MediaPlayer mediaPlayer,int what,int extra) {
        LogUtils.print(TAG,"------->>> onError what: " + what + "  extra:" + extra);
        stopVideo();
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.ERROR);
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PLAY_ERROR);
        mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.VIDEO_PLAY_ERROR,200);
        return false;
    }

    /**
     * 视频播放完成监听
     **/
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtils.print(TAG,"----->>> onCompletion");
        mVideoManager.setCurrentPlayState(VideoSdkConstants.PlayState.COMPLETED);
        mHandler.removeMessages(VideoSdkConstants.VideoHandler.VIDEO_PLAY_NEXT);
        mHandler.sendEmptyMessageDelayed(VideoSdkConstants.VideoHandler.VIDEO_PLAY_NEXT,500);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        LogUtils.print(TAG,"----->>> onSeekComplete()  getCurrentPosition: " + mediaPlayer.getCurrentPosition());
    }
}
