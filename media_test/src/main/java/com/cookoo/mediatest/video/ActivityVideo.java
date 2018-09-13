package com.cookoo.mediatest.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cookoo.mediatest.BaseActivity;
import com.cookoo.mediatest.R;
import com.cookoo.mediatest.Utils.AudioUtil;
import com.cookoo.mediatest.common.ListAdapter;
import com.cookoo.mediatest.common.SpacesItemDecoration;
import com.cookoo.mediatest.common.VideoSurfaceView;
import com.cookoo.videosdk.imp.VideoStateListener;
import com.cookoo.videosdk.manager.SpManager;
import com.cookoo.videosdk.manager.VideoManager;
import com.cookoo.videosdk.utils.FileUtils;
import com.cookoo.videosdk.utils.LogUtils;
import com.cookoo.videosdk.utils.VideoSdkConstants;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 */
public class ActivityVideo extends BaseActivity implements VideoStateListener, View.OnClickListener ,ListAdapter.OnClickEventListener {
    private static final String TAG = ActivityVideo.class.getSimpleName();
    private TextView usbScanState, showListDataType, showListFolderPath, playListCount;
    private VideoSurfaceView videoSurfaceView;
    private TextView mTitleTv, mAuthorTv, mPlayStateTv, mPlayTimeTv;
    private Button mPreviousBtn, mNextBtn, mPlayOrPauseBtn, mFastForwordBtn, mBackForwordBtn,  mBackBtn;
    private RecyclerView recyclerview;
    private GridLayoutManager gManager;
    private SpacesItemDecoration spacesItemDecoration;
    /**列表控件适配器**/
    private ListAdapter listAdapter = null;
    private VideoManager mVideoManager = VideoManager.getInstance();
    private MediaData mMediaData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.print(TAG," ------>>onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        recyclerview = findViewById(R.id.recyclerView);
        usbScanState = findViewById(R.id.usbScanState);
        showListFolderPath = findViewById(R.id.list_folder_path);
        showListDataType = findViewById(R.id.list_data_type);
        playListCount = findViewById(R.id.play_list_count);
        mAuthorTv = findViewById(R.id.tv_author);
        mTitleTv = findViewById(R.id.tv_title);
        mPlayStateTv = findViewById(R.id.tv_playstate);
        mPlayTimeTv = findViewById(R.id.tv_playtime);
        mPreviousBtn = findViewById(R.id.btn_prevideo);
        mPlayOrPauseBtn = findViewById(R.id.btn_playorpause);
        mNextBtn = findViewById(R.id.btn_nextvideo);
        mFastForwordBtn = findViewById(R.id.btn_fastworword);
        mBackForwordBtn = findViewById(R.id.btn_backForward);
        mBackBtn = findViewById(R.id.btn_back);
        videoSurfaceView = findViewById(R.id.videoSurfaceView) ;
        mVideoManager.initSurfaceHolder(videoSurfaceView.getSurfaceView());
    }

    private void initData() {
        int [] videoPosition = {384,300,0,0};
        mVideoManager.setVideoPosition(videoPosition);
        mVideoManager.requestVideoData(null,mVideoManager.getCurrentDataListType());
        initAdapter();
        initSurface();
    }

    private void initListener() {
        listAdapter.setOnClickEventListener(this);
        findViewById(R.id.btn_list_all).setOnClickListener(this);
        findViewById(R.id.btn_two_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_multiple_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_playlist_collected).setOnClickListener(this);
        findViewById(R.id.btn_start_extend_video).setOnClickListener(this);
        findViewById(R.id.btn_auto).setOnClickListener(this);
        findViewById(R.id.btn_small).setOnClickListener(this);
        findViewById(R.id.btn_large).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_switch_style).setOnClickListener(this);
        mVideoManager.bindMediaStateListener(this);
        mPreviousBtn.setOnClickListener(this);
        mPlayOrPauseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mFastForwordBtn.setOnClickListener(this);
        mBackForwordBtn.setOnClickListener(this);
        mBackForwordBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
    }

    private void initAdapter() {
        LogUtils.print(TAG,"initAdapter()  listViewAdapter:  " + listAdapter + "|" + mVideoManager.getPlayList().size());
        listAdapter = new ListAdapter(this,mVideoManager.getNewData());
        listAdapter.setDataType(1);
        gManager = new GridLayoutManager(this,2);
        spacesItemDecoration = new SpacesItemDecoration();
        recyclerview.addItemDecoration(spacesItemDecoration);
        recyclerview.setLayoutManager(gManager);
        recyclerview.setAdapter(listAdapter);
    }

    private void updateListAdapter(){
        if (listAdapter == null){
            initAdapter();
            return;
        }
        List<MediaData> mediaDates = mVideoManager.getNewData();
        LogUtils.print(TAG,"mediaDates size: " + mediaDates.size());
        if(mediaDates.size() < 1) {
            if(mVideoManager.isAllDeviceScanFinished(true)) {
                LogUtils.print(TAG,"   no video file!");
            } else {
                LogUtils.print(TAG,"   video data loading...");
            }
            listAdapter.updateAdapter(mediaDates);
            return;
        }
        listAdapter.updateAdapter(mediaDates);
        //列表更新后，需要更新列表显示类型,和浏览目录
        showListDataType.setText(getShowListDataType());
        showListFolderPath.setText(getShowListRootPath());
        playListCount.setText(""+mVideoManager.getPlayList().size());
        if (mVideoManager.getCurrentPlayMediaItem() == null){
            mVideoManager.setCurrentListPosition(0);
            mVideoManager.setPlayList(mediaDates);
            mVideoManager.playVideo(mediaDates.get(0));
        }
    }

    /**
     * 更新播放列表
     */
    private void updatePlayList() {
        LogUtils.print(TAG,"---->> updatePlayList() listShowStyle: "+listAdapter.getShowListStyle());
        updateListAdapter();
    }

    @Override
    public Activity getChildContext() {
        return this;
    }

    /**
     * 在播放异常后，需要重新创建SurfaceView
     */
    private void initSurface(){
        if (videoSurfaceView == null){
            videoSurfaceView = findViewById(R.id.videoSurfaceView);
        }
        mVideoManager.initSurfaceHolder(videoSurfaceView.getSurfaceView());
    }

    @Override
    protected void onRestart() {
        LogUtils.print(TAG," ------>>onRestart()");
        super.onRestart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.print(TAG," ------>>onNewIntent()");
        mMediaData = getMediaData(intent);
        if (mMediaData != null){
            initSurface();
        }
        mVideoManager.playVideo(mMediaData);
    }

    private MediaData getMediaData(Intent intent){
        return intent.getParcelableExtra("mediaData");
    }

    @Override
    protected void onResume() {
        LogUtils.print(TAG," ------>>onResume()");
        super.onResume();
        mVideoManager.setCPURefrain(false);
        updatePlayList();
    }

    @Override
    protected void onPause() {
        LogUtils.print(TAG," ------>>onPause()");
        super.onPause();
        mVideoManager.pauseVideo();
    }

    @Override
    protected void onStop() {
        LogUtils.print(TAG," ------>>onStop()");
        super.onStop();
        mVideoManager.stopVideo();
    }

    @Override
    protected void onDestroy() {
        LogUtils.print(TAG," ------>>onDestroy()");
        super.onDestroy();
        mVideoManager.unbindMediaStateListener(this);
    }

    private void switchShowListStyle(){
        recyclerview.removeAllViewsInLayout();
        if (listAdapter.getShowListStyle() == 0){
            listAdapter.setShowListStyle(1);
        }else{
            listAdapter.setShowListStyle(0);
        }
        updateListAdapter();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_list_all:
                mVideoManager.requestVideoData(null,ConstantsUtils.ListType.ALL_TYPE);
                break;
            case R.id.btn_playlist_collected:
                mVideoManager.requestVideoData(null,ConstantsUtils.ListType.COLLECTION_TYPE);
                break;
            case R.id.btn_two_class_folder:
                mVideoManager.requestVideoData(null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
                break;
            case R.id.btn_multiple_class_folder:
                mVideoManager.requestVideoData(null,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                break;
            case R.id.btn_prevideo:
                mVideoManager.preVideo();
                break;
            case R.id.btn_playorpause:
                mVideoManager.playOrPause();
                break;
            case R.id.btn_nextvideo:
                mVideoManager.nextVideo();
                break;
            case R.id.btn_fastworword:
                mVideoManager.fastForward();
                break;
            case R.id.btn_backForward:
                mVideoManager.backForward();
                break;
            case R.id.btn_start_extend_video:
                Intent intent = new Intent("start.extend.video.action");
                intent.setPackage("com.cookoo.extendvideo");
                startActivity(intent);
                break;
            case R.id.btn_switch_style:
                switchShowListStyle();
                break;
            case R.id.btn_auto:
                SpManager.getInstance().savePlayFormat(VideoSdkConstants.VideoPlayFormat.AUTO_SCALE);
                mVideoManager.setVideoLayout();
                break;
            case R.id.btn_small:
                SpManager.getInstance().savePlayFormat(VideoSdkConstants.VideoPlayFormat.SMALL_SCALE);
                mVideoManager.setVideoLayout();
                break;
            case R.id.btn_large:
                SpManager.getInstance().savePlayFormat(VideoSdkConstants.VideoPlayFormat.LARGE_SCALE);
                mVideoManager.setVideoLayout();
                break;
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_back:
                mVideoManager.upperLevel(null);
                break;
            default:
        }
    }

    @Override
    public void onVideoStateChanged(int eventId) {
        if(eventId != VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME) {
            LogUtils.print(TAG,"===mediaStateChanged===" + eventId);
        }
        switch(eventId) {
            case VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_TIME:
                updatePlayProgress();
                break;
            case VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_INFO:
                updatePlayInfo();
                break;
            case VideoSdkConstants.VideoStateEventId.UPDATE_PLAY_STATE:
                updatePlayState();
                break;
            case VideoSdkConstants.VideoStateEventId.VIDEO_PLAY_ERROR:
                LogUtils.print(TAG,"---->>> VIDEO_PLAY_ERROR mSurfaceView: " + videoSurfaceView);
                videoSurfaceView = null;
                initSurface();
                mVideoManager.nextVideo();
                break;
            default:
        }
    }

    @Override
    public void onVideoScanChanged(int eventId) {
        LogUtils.print(TAG,"===mediaStateChanged===" + eventId);
        switch(eventId) {
            case VideoSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED:
                updateUsbState();
                break;
            case VideoSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED:
                handleUsbDiskUnMount();
                break;
            case VideoSdkConstants.ScanStateEventId.USB_DISK_MOUNTED:
                handleUsbDisMounted();
                break;
            case VideoSdkConstants.ScanStateEventId.VIDEO_DATA_CHANGE:
                mVideoManager.handleVideoDataChange(null);
                break;
            case VideoSdkConstants.ScanStateEventId.VIDEO_ALL_DATA_BACK:
            case VideoSdkConstants.ScanStateEventId.VIDEO_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK:
            case VideoSdkConstants.ScanStateEventId.VIDEO_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK:
            case VideoSdkConstants.ScanStateEventId.VIDEO_TREE_FOLDER_DATA_BACK:
            case VideoSdkConstants.ScanStateEventId.VIDEO_COLLECTED_DATA_BACK:
                updatePlayList();
                showListFolderPath.setText(getShowListRootPath());
                break;
            case VideoSdkConstants.ScanStateEventId.VIDEO_PARSE_BACK:
                break;
            default:
        }
    }

    @Override
    public void handleClickCollectEvent(MediaData mi) {
        //获取对象当前收藏状态
        boolean isCollected = mi.isCollected();
        LogUtils.print(TAG,"---->> handleClickCollectEvent() isCollected: " + isCollected);
        if(mVideoManager.updateMediaCollected(mi)) {
            updatePlayList();
            //收藏状态更新成功，显示UI
            if(isCollected) {
                Toast.makeText(ActivityVideo.this,"成功从收藏列表中移除",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityVideo.this,"成功添加到收藏列表",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void handleClickFileEvent(int position, MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType() + " position: " + position);
        MediaData mediaData = mVideoManager.getCurrentPlayMediaItem();
        if(mediaData == null) {
            mVideoManager.setPlayList(mVideoManager.getNewData());
        } else {
            //需要判断当前播放对象的数据类型和点击的对象是不是同一种类型，不是同一种类型就需要更新播放列表数据,并且判断是不是同一级列表数据
            LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType() + " PlayMediaItem().getDataType: " + mVideoManager.getCurrentPlayMediaItem().getDataType());
            String playItemParentPath = mVideoManager.getUpperLevelFolderPath(mediaData.getFilePath());
            String clickItemParentPath = mVideoManager.getUpperLevelFolderPath(md.getFilePath());
            if(mediaData.getDataType() != md.getDataType() || !playItemParentPath.equals(clickItemParentPath)) {
                LogUtils.print(TAG,"--->> handleClickFileEvent() the PlayList is change... ");
                mVideoManager.setPlayList(mVideoManager.getNewData());
            }
        }
        mVideoManager.setCurrentListPosition(position);
        updatePlayList();
        mVideoManager.playVideo(md);
    }

    @Override
    public void handleClickFolderEvent(MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFolderEvent() getDataType " + md.getDataType());
        recyclerview.removeAllViewsInLayout();
        switch(md.getDataType()) {
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
                mVideoManager.requestVideoData(md.getFilePath(),ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                mVideoManager.requestVideoData(md.getFilePath(),ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                break;
            default:
        }
    }

    private void handleUsbDisMounted() {
        updateUsbState();
        if(mVideoManager.getCurrentPlayMediaItem() == null) {
            //获取最后记录播放文件
            String saveFilePath = mVideoManager.getSavePlayMediaItemPath();
            boolean isFileExit = FileUtils.isFileExit(saveFilePath);
            LogUtils.print(TAG,"handleUsbDisMounted  saveFilePath: " + saveFilePath + "  isFileExit: " + isFileExit);
            if(isFileExit) {
                mVideoManager.requestVideoData(saveFilePath,ConstantsUtils.ListType.ALL_TYPE);
            }
        }
    }

    /**
     * 处理USB设备卸载Ui显示
     */
    private void handleUsbDiskUnMount() {
        LogUtils.print(TAG," --->> handleUsbDiskUnMount()");
        //刷新列表数据
        if(listAdapter != null) {
            listAdapter.updateAdapter(mVideoManager.getPlayList());
        }
        playListCount.setText("");
        showListFolderPath.setText("");
        showListDataType.setText("");
        updateUsbState();
        if(videoSurfaceView != null){
            videoSurfaceView.showNoPlay();
        }
    }

    private void updateUsbState() {
        usbScanState.setText(getUsbState());
    }

    private void updatePlayProgress() {
        mPlayTimeTv.setText(AudioUtil.formatDurationInt(mVideoManager.getTotalTime()) + "|" + AudioUtil.formatDurationInt(mVideoManager.getCurrentPlayPosition()));
    }

    private void updatePlayState() {
        mPlayStateTv.setText(getPlayState());
    }

    public String getPlayState() {
        String mode = "播放";
        switch(mVideoManager.getCurrentPlayState()) {
            case VideoSdkConstants.PlayState.PLAY:
                mode = "播放";
                if(videoSurfaceView != null){
                    videoSurfaceView.play();
                }
                break;
            case VideoSdkConstants.PlayState.PAUSE:
                mode = "暂停";
                break;
            case VideoSdkConstants.PlayState.PREPARE:
                mode = "准备播放...";
                break;
            default:
        }
        return mode;
    }

    private void updatePlayInfo() {
        mTitleTv.setText(getTitleName());
        mAuthorTv.setText(getAuthor());
        playListCount.setText(getPlayListPosition());
        updatePlayState();
        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
    }

    private String getTitleName() {
        return mVideoManager.getCurrentPlayMediaItem() == null ? "未知" :mVideoManager.getCurrentPlayMediaItem().getTitle();
    }

    private String getAuthor() {
        return mVideoManager.getCurrentPlayMediaItem() == null ? "未知" :mVideoManager.getCurrentPlayMediaItem().getArtist();
    }

    private String getPlayListPosition() {
        return mVideoManager.getPlayList().size() + "|" + mVideoManager.getCurrentListPosition();
    }

    /**
     * 获取当前USB设备挂载、扫描信息
     **/
    private String getUsbState() {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i < mVideoManager.getUsbDeviceList().size(); i++) {
            if(mVideoManager.getUsbDeviceList().get(i).getRootPath() == null) {
                stringBuffer.append("USB" + i + "  ：卸载|未连接 ");
            } else {
                stringBuffer.append("USB" + i + "  ：挂载| ");
                if(mVideoManager.getUsbDeviceList().get(i).isScanFinished()) {
                    stringBuffer.append("扫描完成  ");
                } else {
                    stringBuffer.append("扫描中  ");
                }
            }
        }
        return stringBuffer.toString();
    }

    private String getShowListDataType() {
        LogUtils.print(TAG,"---->> getShowListDataType() getCurrentDataListType: " + mVideoManager.getCurrentDataListType());
        switch(mVideoManager.getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
                return "全部数据列表";
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                return "两级文件夹列表";
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                return "文件夹列表";
            default:
        }
        return "";
    }

    private String getShowListRootPath() {
        if(mVideoManager.isAllUsbUnMount() || listAdapter == null || listAdapter.getData().size() <= 0) {
            return "";
        }
        String path = "USB根目录";
        MediaData mediaData = listAdapter.getData().get(0);
        LogUtils.print(TAG,"----->>> getShowListRootPath()  getDataType: " + mediaData.getDataType());
        switch(mediaData.getDataType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                path = mVideoManager.getUpperLevelFolderPath(mVideoManager.getCurrentShowDataPath());
                break;
            default:
        }
        return path;
    }


}
