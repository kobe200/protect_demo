package com.cookoo.extendmusic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cookoo.extendmusic.Utils.AudioUtil;
import com.cookoo.musicsdkclient.MusicSdkConstants;
import com.cookoo.musicsdkclient.iface.MusicStateListener;
import com.cookoo.musicsdkclient.manager.MusicAidlManager;
import com.cookoo.musicsdkclient.utils.FileUtils;
import com.cookoo.musicsdkclient.utils.LogUtils;
import com.cookoo.musicsdkclient.utils.ThumbnailUtil;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author kobe
 */
public class ActivityMusic extends BaseActivity implements MusicStateListener, OnClickListener, OnItemClickListener {
    private static final String TAG = "ActivityMusic";
    private TextView name, album, artist, time, state, mode, playListPath, showListDataType, showListFolderPath, playListFolderPath, playListCount, usbScanState;
    private ImageView mediaImage;
    /**
     * 列表控件
     **/
    private ListView playLv;
    /**
     * 列表控件适配器
     **/
    private CommonAdapter playListAdapter = null;
    private MusicAidlManager musicManager = MusicAidlManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicManager.setCPURefrain(false);
    }

    private void initView() {
        name = findViewById(R.id.name);
        album = findViewById(R.id.album);
        artist = findViewById(R.id.artist);
        time = findViewById(R.id.time);
        state = findViewById(R.id.play_state);
        mode = findViewById(R.id.play_mode);
        playListPath = findViewById(R.id.play_list_path);
        playListCount = findViewById(R.id.play_list_count);
        usbScanState = findViewById(R.id.usbScanState);
        mediaImage = findViewById(R.id.mediaImage);
        playLv = findViewById(R.id.playlist);
        showListFolderPath = findViewById(R.id.list_folder_path);
        playListFolderPath = findViewById(R.id.list_play_folder_path);
        showListDataType = findViewById(R.id.list_data_type);
    }

    private void initData() {
        initAdapter();
        updateUsbState();
        updatePlayInfo();
    }

    private void initListener() {
        playLv.setOnItemClickListener(this);
        findViewById(R.id.btn_playorpause).setOnClickListener(this);
        findViewById(R.id.btn_last).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_backForward).setOnClickListener(this);
        findViewById(R.id.btn_fastForward).setOnClickListener(this);
        findViewById(R.id.btn_singleModle).setOnClickListener(this);
        findViewById(R.id.btn_randomModle).setOnClickListener(this);
        findViewById(R.id.btn_allModle).setOnClickListener(this);
        findViewById(R.id.btn_list_all).setOnClickListener(this);
        findViewById(R.id.btn_playlist_ablum).setOnClickListener(this);
        findViewById(R.id.btn_playlist_artist).setOnClickListener(this);
        findViewById(R.id.btn_two_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_multiple_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_playlist_collected).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_sdcard_inner_data).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        musicManager.bindMediaStateListener(this);
    }

    public void initAdapter() {
        LogUtils.print(TAG,"setAdapter()  playListAdapter:  " + playListAdapter + "|" + musicManager.getPlayList().size());
        if(musicManager.getPlayList().size() < 1) {
            musicManager.setPlayList(musicManager.getNewData());
        }
        if(musicManager.getCurrentPlayMediaItem() == null && musicManager.getPlayList().size() > 0) {
            //获取最后记录播放文件
            musicManager.playMusic(musicManager.getPlayList().get(0));
        }
        playListAdapter = new CommonAdapter(this,musicManager.getPlayList(),R.layout.playlistitem,null) {

            @Override
            protected void afterGetView(int position,BaseViewHolder holder) {
                if(getData() == null || getData().size() == 0) {
                    return;
                }
                final Button addCollect = holder.getView(R.id.btn_add_collect);
                final MediaData mi = getData().get(position);
                if(mi == null){
                    return;
                }
                RelativeLayout fileLayout = holder.getView(R.id.playlistitem_file_layout);
                RelativeLayout folderLayout = holder.getView(R.id.playlistitem_folder_layout);
                if(mi.isFolder()) {
                    addCollect.setVisibility(View.GONE);
                    fileLayout.setVisibility(View.GONE);
                    folderLayout.setVisibility(View.VISIBLE);
                    TextView folderName = holder.getView(R.id.playlistitem_folder_layout_name);
                    folderName.setText(mi.getName());
                } else {
                    addCollect.setVisibility(View.VISIBLE);
                    fileLayout.setVisibility(View.VISIBLE);
                    folderLayout.setVisibility(View.GONE);
                    TextView name = holder.getView(R.id.playlistitem_name);
                    TextView artist = holder.getView(R.id.playlistitem_artist);
                    TextView album = holder.getView(R.id.playlistitem_album);
                    TextView no = holder.getView(R.id.playlistitem_no);
                    no.setText(position + "");
                    name.setText(mi.getName());
                    artist.setText(TextUtils.isEmpty(mi.getArtist()) ? "未知" :mi.getArtist());
                    album.setText(TextUtils.isEmpty(mi.getAlbum()) ? "未知" :mi.getAlbum());
                    if(mi.getDataType() == ConstantsUtils.ListType.COLLECTION_TYPE) {
                        addCollect.setBackground(getResources().getDrawable(R.mipmap.del_collect_list));
                    } else {
                        addCollect.setBackground(getResources().getDrawable(R.mipmap.add_collect_list));
                    }
                    fileLayout.setBackgroundColor(getResources().getColor(R.color.playListItemBg_normal));
                    if(musicManager.isCurrentPlayItem(mi)) {
                        fileLayout.setBackgroundColor(getResources().getColor(R.color.playListItemBg_select));
                        playLv.smoothScrollToPosition(position);
                    }
                    addCollect.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClickCollectEvent(mi);
                        }
                    });
                }
            }
        };
        playLv.setAdapter(playListAdapter);
        playLv.setSelection(musicManager.getCurrentListPosition());
    }

    /**
     * 更新播放列表
     */
    private void updatePlayList() {
        if(playListAdapter == null) {
            initAdapter();
            return;
        }
        List<MediaData> mediaDates = musicManager.getNewData();
        LogUtils.print(TAG,"mediaDates size: " + mediaDates.size());
        if(mediaDates.size() < 1) {
            musicManager.requestMusicData(musicManager.getCurrentShowDataPath(),musicManager.getColumnContentStr(),musicManager.getCurrentDataListType());
            if(musicManager.isAllDeviceScanFinished(true)) {
                LogUtils.print(TAG,"   no music file!");
            } else {
                LogUtils.print(TAG,"   music data loading...");
            }
        }
        playListAdapter.updateAdapter(mediaDates);
        playLv.setSelection(musicManager.getHeightLightPosition());
        //列表更新后，需要更新列表显示类型,和浏览目录
        showListDataType.setText(getShowListDataType());
        showListFolderPath.setText(getShowListRootPath());
        if(musicManager.getPlayList().size() < 1 && mediaDates.size() > 0) {
            musicManager.setPlayList(mediaDates);
        }
        if(musicManager.getCurrentPlayMediaItem() == null && musicManager.getPlayList().size() > 0) {
            //获取最后记录播放文件
            musicManager.playMusic(musicManager.getPlayList().get(0));
            musicManager.setCurrentPlayPath(getShowListRootPath());
            updatePlayInfo();
        }
    }

    @Override
    public void onMusicStateChanged(int eventId) {
        switch(eventId) {
            case MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_TIME:
                updatePlayProgress();
                break;
            case MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_INFO:
                LogUtils.print(TAG,"------>>>musicStateChanged() UPDATE_PLAY_INFO");
                updatePlayInfo();
                break;
            case MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_STATE:
                LogUtils.print(TAG,"------>>>musicStateChanged() UPDATE_PLAY_STATE");
                updatePlayState();
                break;
            case MusicSdkConstants.MusicStateEventId.UPDATE_PLAY_LIST_DATA:
                LogUtils.print(TAG,"------>>>musicStateChanged() UPDATE_PLAY_LIST_DATA");
                playListAdapter.updateAdapter(musicManager.getPlayList());
                break;
            case MusicSdkConstants.MusicStateEventId.MUSIC_PLAY_ERROR:
                LogUtils.print(TAG,"------>>>musicStateChanged() MUSIC_PLAY_ONERRROR");
                musicManager.nextMusic(false);
                break;
            case MusicSdkConstants.MusicStateEventId.IS_USB_UNMOUNT_STOPMUSIC:
                LogUtils.print(TAG,"------>>>musicStateChanged() IS_USB_UNMOUNT_STOPMUSIC");
                if(musicManager.getPlayList().size() > 0) {
                    for(MediaData md : musicManager.getPlayList()) {
                        if(!md.isFolder()) {
                            musicManager.playMusic(md);
                            break;
                        }
                    }
                }
                break;
            default:
        }
    }

    @Override
    public void onMusicScanChanged(int eventId) {
        LogUtils.print(TAG,"===musicScanChanged===" + eventId);
        switch(eventId) {
            case MusicSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED:
                updateUsbState();
                break;
            case MusicSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED:
                handleUsbDiskUnMount();
                break;
            case MusicSdkConstants.ScanStateEventId.USB_DISK_MOUNTED:
                handleUsbDisMounted();
                break;
            case MusicSdkConstants.ScanStateEventId.MUSIC_DATA_CHANGE:
                musicManager.handleMusicDataChange(null);
                break;
            case MusicSdkConstants.ScanStateEventId.MUSIC_ALL_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_ALBUM_LEVEL1_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_ALBUM_LEVEL2_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_ARTIST_LEVEL1_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_ARTIST_LEVEL2_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_TREE_FOLDER_DATA_BACK:
            case MusicSdkConstants.ScanStateEventId.MUSIC_COLLECTED_DATA_BACK:
                updatePlayList();
                showListFolderPath.setText(getShowListRootPath());
                break;
            case MusicSdkConstants.ScanStateEventId.MUSIC_PARSE_BACK:
                updatePlayInfo();
                break;
            default:
        }
    }

    private void handleUsbDisMounted() {
        musicManager.setCPURefrain(false);
        updateUsbState();
        LogUtils.print(TAG,"handleUsbDisMounted getCurrentPlayMediaItem: " + musicManager.getCurrentPlayMediaItem());
        musicManager.handleMemoryPlayback();
    }

    /**
     * 处理USB设备卸载Ui显示
     */
    private void handleUsbDiskUnMount() {
        //有设备卸载时，更新当前列表数据
        updatePlayList();
        //刷新播放信息
        MediaData mi = musicManager.getCurrentPlayMediaItem();
        //如果当前播放对象被移除则清除相关信息
        if(mi == null || !FileUtils.isFileExit(mi.getFilePath())) {
            name.setText("");
            artist.setText("");
            album.setText("");
            playListPath.setText("");
            playListCount.setText("");
            mode.setText("");
            time.setText("");
            state.setText("");
        }
        //刷新USB状态信息
        updateUsbState();
    }

    private void updatePlayMode() {
        mode.setText(getPlayMode());
    }

    @SuppressLint("SetTextI18n")
    private void updatePlayProgress() {
        time.setText(AudioUtil.formatDurationInt(musicManager.getTotalTime()) + "|" + AudioUtil.formatDurationInt(musicManager.getCurrentTime()));
    }

    private void updatePlayInfo() {
        if(musicManager.getCurrentPlayMediaItem() != null) {
            name.setText(getFileName());
            artist.setText(getArtist());
            album.setText(getAlbum());
            playListPath.setText(getCurrentPlayFilePath());
            playListCount.setText(getPlayListPosition());
            showListFolderPath.setText(getShowListRootPath());
            playListFolderPath.setText(getPlayListRootPath());
            showListDataType.setText(getShowListDataType());
            updatePlayMode();
            updatePlayProgress();
            updatePlayState();
            loadAlbumImage();
        }
        updatePlayList();
    }

    private String getShowListRootPath() {
        String path = musicManager.getCurrentShowDataPath();
        LogUtils.print(TAG,"----->>> getShowListRootPath()  getDataType: " + musicManager.getCurrentDataListType() + "|" + path);
        switch(musicManager.getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
                path = "全部数据列表目录 ";
                break;
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                path = "收藏列表目录 ";
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                path = "专辑目录";
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                path = "专辑数据 - " + musicManager.getColumnContentStr();
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                path = "艺术家目录";
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                path = "艺术家数据 - " + musicManager.getColumnContentStr();
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
                path = "内置SD卡";
                break;
            case ConstantsUtils.ListType.SDCARD_OUT_TYPE:
                path = "外置SD卡";
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                if(TextUtils.isEmpty(path)) {
                    path = "USB根目录";
                }
                break;
            default:
        }
        return path;
    }

    private String getPlayListRootPath() {
        return musicManager.getCurrentPlayPath();
    }

    private void updateUsbState() {
        LogUtils.print(TAG,"  updateUsbState()");
        usbScanState.setText(getUsbState());
    }

    private String getShowListDataType() {
        LogUtils.print(TAG,"---->> getShowListDataType() getCurrentDataListType: " + musicManager.getCurrentDataListType());
        switch(musicManager.getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
                return "全部数据列表";
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                return "专辑列表";
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                return "艺术家列表";
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                return "两级文件夹列表";
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                return "文件夹列表";
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
                return "内置SD卡";
            case ConstantsUtils.ListType.SDCARD_OUT_TYPE:
                return "外置SD卡";
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                return "收藏列表";
            default:
        }
        return "";
    }

    private void updatePlayState() {
        LogUtils.print(TAG,"  播放状态：" + getPlayState());
        state.setText(getPlayState());
    }

    private void loadAlbumImage() {
        try {
            if(musicManager.getCurrentPlayMediaItem() == null) {
                return;
            }
            Bitmap bm = ThumbnailUtil.getThumbnailBitmap(this,musicManager.getMediaItemInfo(musicManager.getCurrentPlayMediaItem().getFilePath()),401,401);
            LogUtils.print("==loadAlbumImage==" + bm);
            if(bm != null) {
                mediaImage.setImageBitmap(bm);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_playorpause:
                musicManager.playOrPause();
                break;
            case R.id.btn_last:
                musicManager.preMusic();
                break;
            case R.id.btn_next:
                musicManager.nextMusic(true);
                break;
            case R.id.btn_backForward:
                musicManager.backForward();
                break;
            case R.id.btn_fastForward:
                musicManager.fastForward();
                break;
            case R.id.btn_allModle:
                musicManager.savePlayMode(MusicSdkConstants.PlayMode.ALL_LOOP);
                updatePlayMode();
                break;
            case R.id.btn_singleModle:
                musicManager.savePlayMode(MusicSdkConstants.PlayMode.SINGLE_LOOP);
                updatePlayMode();
                break;
            case R.id.btn_randomModle:
                musicManager.savePlayMode(MusicSdkConstants.PlayMode.RANDOM_LOOP);
                updatePlayMode();
                break;
            case R.id.btn_list_all:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.ALL_TYPE);
                break;
            case R.id.btn_two_class_folder:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
                break;
            case R.id.btn_multiple_class_folder:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                break;
            case R.id.btn_playlist_ablum:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE);
                break;
            case R.id.btn_playlist_artist:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE);
                break;
            case R.id.btn_sdcard_inner_data:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.SDCARD_INNER_TYPE);
                break;
            case R.id.btn_sdcard_out_data:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.SDCARD_OUT_TYPE);
                break;
            case R.id.btn_playlist_collected:
                musicManager.requestMusicData(null,null,ConstantsUtils.ListType.COLLECTION_TYPE);
                break;
            case R.id.btn_back:
                musicManager.upperLevel(null);
                break;
            case R.id.btn_exit:
                finish();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicManager.unbindMediaStateListener(this);
    }

    @Override
    public Activity getChildContext() {
        return this;
    }

    public String getPlayMode() {
        String mode = "未知";
        switch(musicManager.getCurrentPlayMode()) {
            case MusicSdkConstants.PlayMode.ALL_LOOP:
                mode = "全部循环";
                break;
            case MusicSdkConstants.PlayMode.RANDOM_LOOP:
                mode = "随机循环";
                break;
            case MusicSdkConstants.PlayMode.SINGLE_LOOP:
                mode = "单曲循环";
                break;
            default:
        }
        return mode;
    }

    public String getPlayState() {
        String mode = "播放";
        switch(musicManager.getCurrentPlayState()) {
            case MusicSdkConstants.PlayState.PLAY:
                mode = "播放";
                break;
            case MusicSdkConstants.PlayState.PAUSE:
                mode = "暂停";
                break;
            case MusicSdkConstants.PlayState.PREPARE:
                mode = "准备播放...";
                break;
            default:
        }
        return mode;
    }

    /**
     * 获取当前USB设备挂载、扫描信息
     **/
    private String getUsbState() {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i < musicManager.getUsbDeviceList().size(); i++) {
            if(musicManager.getUsbDeviceList().get(i).getRootPath() == null) {
                stringBuffer.append("USB" + i + "  ：卸载|未连接 ");
            } else {
                if(ProviderHelper.USB_INNER_SD_PATH.equals(musicManager.getUsbDeviceList().get(i).getRootPath())) {
                    stringBuffer.append("SD" + "  ：挂载| ");
                } else {
                    stringBuffer.append("USB" + i + "  ：挂载| ");
                }
                if(musicManager.getUsbDeviceList().get(i).isScanFinished()) {
                    stringBuffer.append("扫描完成  ");
                } else {
                    stringBuffer.append("扫描中  ");
                }
            }
        }
        return stringBuffer.toString();
    }

    public String getUpperFilePath(String currentPath) {
        return TextUtils.isEmpty(currentPath) ? "未知" :musicManager.getUpperLevelFolderPath(musicManager.getCurrentShowDataPath());
    }

    private String getCurrentPlayFilePath() {
        return musicManager.getCurrentPlayMediaItem() == null ? "未知" :musicManager.getCurrentPlayMediaItem().getFilePath();
    }

    private String getFileName() {
        return musicManager.getCurrentPlayMediaItem() == null ? "未知" :musicManager.getCurrentPlayMediaItem().getName();
    }

    private String getAlbum() {
        return musicManager.getCurrentPlayMediaItem() == null ? "未知" :musicManager.getCurrentPlayMediaItem().getAlbum();
    }

    private String getArtist() {
        return musicManager.getCurrentPlayMediaItem() == null ? "未知" :musicManager.getCurrentPlayMediaItem().getArtist();
    }

    private String getPlayListPosition() {
        return musicManager.getPlayList().size() + "|" + musicManager.getCurrentListPosition();
    }

    @Override
    public void onItemClick(AdapterView<?> adapter,View view,int position,long arg3) {
        LogUtils.print(TAG," position: " + position);
        if(playListAdapter != null && playListAdapter.getData() != null && playListAdapter.getData().size() > position) {
            MediaData md = playListAdapter.getData().get(position);
            if(md.isFolder()) {
                handleClickFolderEvent(md);
            } else {
                handleClickFileEvent(position,md);
            }
        }
    }

    private void handleClickCollectEvent(MediaData mi) {
        //获取对象当前收藏状态
        boolean isCollected = mi.isCollected();
        if(musicManager.updateMediaCollected(mi)) {
            updatePlayList();
            //收藏状态更新成功，显示UI
            if(isCollected) {
                Toast.makeText(ActivityMusic.this,"成功从收藏列表中移除",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityMusic.this,"成功添加到收藏列表",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleClickFileEvent(int position,MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType() + " position: " + position);
        MediaData mediaData = musicManager.getCurrentPlayMediaItem();
        if(mediaData == null) {
            musicManager.setPlayList(musicManager.getNewData());
        } else {
            //需要判断当前播放对象的数据类型和点击的对象是不是同一种类型，不是同一种类型就需要更新播放列表数据,并且判断是不是同一级列表数据
            LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType() + " PlayMediaItem().getDataType: " + musicManager.getCurrentPlayMediaItem().getDataType());
            String playItemParentPath = musicManager.getUpperLevelFolderPath(mediaData.getFilePath());
            String clickItemParentPath = musicManager.getUpperLevelFolderPath(md.getFilePath());
            if(mediaData.getDataType() != md.getDataType() || !playItemParentPath.equals(clickItemParentPath)) {
                LogUtils.print(TAG,"--->> handleClickFileEvent() the PlayList is change... ");
                musicManager.setPlayList(musicManager.getNewData());
                musicManager.setCurrentPlayPath(getShowListRootPath());
            }
        }
        musicManager.setCurrentListPosition(position);
        musicManager.playMusic(md);

    }

    private void handleClickFolderEvent(MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFolderEvent() getDataType " + md.getDataType());
        switch(md.getDataType()) {
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                musicManager.requestMusicData(null,md.getName(),ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE);
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                musicManager.requestMusicData(null,md.getName(),ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
                musicManager.requestMusicData(md.getFilePath(),null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                musicManager.requestMusicData(md.getFilePath(),null,md.getDataType());
                break;
            default:
        }
    }

}
