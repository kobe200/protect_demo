package com.cookoo.extendimage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cookoo.extendimage.adapter.BaseViewHolder;
import com.cookoo.extendimage.adapter.CommonAdapter;
import com.cookoo.imagesdkclient.ImageSdkConstants;
import com.cookoo.imagesdkclient.iface.ImageStateListener;
import com.cookoo.imagesdkclient.manager.ImageAidlManager;
import com.cookoo.imagesdkclient.utils.FileUtils;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 */
public class ActivityImage extends BaseActivity implements ImageStateListener, View.OnClickListener, OnItemClickListener ,ViewPager.OnPageChangeListener{
    private static final String TAG = ActivityImage.class.getSimpleName();
    private TextView usbScanState, showListDataType, showListFolderPath, playListPath,playListCount;
    /**
     * 列表控件
     **/
    private ListView playLv;
    /**
     * 列表控件适配器
     **/
    private CommonAdapter playListAdapter = null;
    private ImageAidlManager mImageManager = ImageAidlManager.getInstance();
    private boolean mIsCurrentUI;
    private MyViewPager mViewPager;
    private TextView mTitleTv, mPlayStateTv;
    private Button mPreviousBtn, mNextBtn, mPlayOrPauseBtn, mZoomInBtn, mZoomOutBtn, mRotateBtn, mBackBtn;
    private ImagePageAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.print(TAG," ------>>onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        playLv = findViewById(R.id.playlist);
        usbScanState = findViewById(R.id.usbScanState);
        showListFolderPath = findViewById(R.id.list_folder_path);
        showListDataType = findViewById(R.id.list_data_type);
        playListPath = findViewById(R.id.list_play_path);
        playListCount = findViewById(R.id.play_list_count);
        mViewPager = findViewById(R.id.viewpager);
        mTitleTv = findViewById(R.id.tv_title);
        mPlayStateTv = findViewById(R.id.tv_playstate);
        mPreviousBtn = findViewById(R.id.btn_preimage);
        mPlayOrPauseBtn = findViewById(R.id.btn_playorpause);
        mNextBtn = findViewById(R.id.btn_nextimage);
        mZoomInBtn = findViewById(R.id.btn_zoom_in);
        mZoomOutBtn = findViewById(R.id.btn_zoom_out);
        mRotateBtn = findViewById(R.id.btn_rotate);
        mBackBtn = findViewById(R.id.btn_back);
    }

    private void initData() {
        mIsCurrentUI = true;
        mImageManager.setViewPager(mViewPager);
        initAdapter();
        setImagePageAdapter();
        updateImageInfo();
    }

    private void initListener() {
        playLv.setOnItemClickListener(this);
        findViewById(R.id.btn_list_all).setOnClickListener(this);
        findViewById(R.id.btn_two_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_multiple_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_playlist_collected).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mPlayOrPauseBtn.setOnClickListener(this);
        mPreviousBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mZoomInBtn.setOnClickListener(this);
        mZoomOutBtn.setOnClickListener(this);
        mRotateBtn.setOnClickListener(this);
        mImageManager.bindMediaStateListener(this);
        mBackBtn.setOnClickListener(this);
        mImageManager.bindMediaStateListener(this);
    }

    private void initAdapter() {
        LogUtils.print(TAG,"initAdapter()  playListAdapter:  " + playListAdapter + "|" + mImageManager.getPlayList().size());
        List<MediaData> data = mImageManager.getNewData();
        mImageManager.requestImageData(null,mImageManager.getCurrentDataListType());
        playListAdapter = new CommonAdapter(this,data,R.layout.playlistitem,null) {

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
                    artist.setVisibility(View.GONE);
                    album.setVisibility(View.GONE);
                    if(mi.getDataType() == ConstantsUtils.ListType.COLLECTION_TYPE) {
                        addCollect.setBackground(getResources().getDrawable(R.mipmap.del_collect_list));
                    } else {
                        addCollect.setBackground(getResources().getDrawable(R.mipmap.add_collect_list));
                    }
                    fileLayout.setBackgroundColor(getResources().getColor(R.color.playListItemBg_normal));
                    if(mImageManager.isCurrentPlayItem(mi)) {
                        fileLayout.setBackgroundColor(getResources().getColor(R.color.playListItemBg_select));
                        playLv.smoothScrollToPosition(position);
                    }
                    addCollect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClickCollectEvent(mi);
                        }
                    });
                }
            }
        };
        playLv.setAdapter(playListAdapter);
        playLv.setSelection(mImageManager.getCurrentListPosition());
    }

    private void setImagePageAdapter() {
        LogUtils.print(TAG,"  ---->>> setAdapter() mIsCurrentUI: " + mIsCurrentUI);
        if(!mIsCurrentUI) {
            return;
        }
        LogUtils.print(TAG," ----->>>setAdapter() getPlayList: " + mImageManager.getPlayList().size() + "|" + mImageManager.getCurrentListPosition());
        if(mPagerAdapter == null) {
            if(mImageManager.getPlayList().size() > 0) {
                mPagerAdapter = new ImagePageAdapter(this,mImageManager.getPlayList());
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setCurrentItem(mImageManager.getCurrentListPosition());
                LogUtils.print(TAG,"   ----->>>setAdapter()  getCurrentListPosition: " + mImageManager.getCurrentListPosition());
            }
        } else {
            mPagerAdapter.updaterAdapter(mImageManager.getPlayList());
        }
        mViewPager.setOnPageChangeListener(this);
    }

    private void updateImageInfo() {
        mTitleTv.setText(getFilePath());
        playListCount.setText(getPlayListPosition());
        showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
        playListPath.setText(getListPathText(mImageManager.getCurrentPlayFilePath()));
        showListDataType.setText(getShowListDataType());
        updatePlayState();
        updateUsbState();
        if(playListAdapter != null){
            playListAdapter.notifyDataSetChanged();
        }
    }

    private void updatePlayState() {
        mPlayStateTv.setText(getPlayState());
    }

    private String getFilePath() {
        return mImageManager.getCurrentPlayMediaItem() == null ? "未知" :mImageManager.getCurrentPlayMediaItem().getFilePath();
    }

    private String getPlayListPosition() {
        return mImageManager.getPlayList().size() + "|" + (mImageManager.getCurrentListPosition());
    }

    public String getPlayState() {
        String mode = "暂停";
        if(mImageManager.isSlidePlay()) {
            mode = "播放";
        }
        return mode;
    }

    /**
     * 更新显示列表
     */
    private void updatePlayList() {
        if(playListAdapter == null) {
            initAdapter();
            return;
        }
        List<MediaData> mediaDates = mImageManager.getNewData();
        LogUtils.print(TAG,"mediaDates size: " + mediaDates.size());
        if(mediaDates.size() < 1) {
            if(mImageManager.isAllDeviceScanFinished(true)) {
                LogUtils.print(TAG,"   no video file!");
            } else {
                LogUtils.print(TAG,"   video data loading...");
            }
            playListAdapter.updateAdapter(mediaDates);
            return;
        }
        playListAdapter.updateAdapter(mediaDates);
        //列表更新后，需要更新列表显示类型,和浏览目录
        showListDataType.setText(getShowListDataType());
        showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
        playListCount.setText(getPlayListPosition());
    }

    @Override
    public Activity getChildContext() {
        return this;
    }

    @Override
    protected void onRestart() {
        LogUtils.print(TAG," ------>>onRestart()");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        LogUtils.print(TAG," ------>>onResume()");
        super.onResume();
        mIsCurrentUI = true;
        mImageManager.setCPURefrain(false);
        if(mImageManager.isSlidePlay()) {
            mImageManager.startSlide();
        }
    }

    @Override
    protected void onPause() {
        LogUtils.print(TAG," ------>>onPause()");
        super.onPause();
        mIsCurrentUI = false;
    }

    @Override
    protected void onStop() {
        LogUtils.print(TAG," ------>>onStop()");
        super.onStop();
        mIsCurrentUI = false;
    }

    @Override
    protected void onDestroy() {
        LogUtils.print(TAG," ------>>onDestroy()");
        super.onDestroy();
        mImageManager.endSlidePlay();
        mImageManager.unbindMediaStateListener(this);
        if(mPagerAdapter != null){
            mPagerAdapter.recycle();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_list_all:
                mImageManager.requestImageData(null,ConstantsUtils.ListType.ALL_TYPE);
                break;
            case R.id.btn_playlist_collected:
                mImageManager.requestImageData(null,ConstantsUtils.ListType.COLLECTION_TYPE);
                break;
            case R.id.btn_two_class_folder:
                mImageManager.requestImageData(null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
                break;
            case R.id.btn_multiple_class_folder:
                mImageManager.requestImageData(null,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                break;
            case R.id.btn_back:
                mImageManager.upperLevel(mImageManager.getCurrentShowDataPath());
                break;
            case R.id.btn_preimage:
                mImageManager.endSlidePlay();
                mImageManager.preImage();
                if(mViewPager != null){
                    mViewPager.setCurrentItem(mImageManager.getCurrentListPosition());
                }
                break;
            case R.id.btn_nextimage:
                mImageManager.endSlidePlay();
                mImageManager.nextImage();
                if(mViewPager != null){
                    mViewPager.setCurrentItem(mImageManager.getCurrentListPosition());
                }
                break;
            case R.id.btn_rotate:
                mImageManager.rotate(90);
                break;
            case R.id.btn_playorpause:
                if(mImageManager.isSlidePlay()) {
                    mImageManager.endSlidePlay();
                } else {
                    mImageManager.startSlide();
                }
                break;
            case R.id.btn_zoom_in:
                mImageManager.endSlidePlay();
                mImageManager.zoomIn();
                break;
            case R.id.btn_zoom_out:
                mImageManager.endSlidePlay();
                mImageManager.zoomOut();
                break;
            case R.id.btn_exit:
                mImageManager.endSlidePlay();
                finish();
                break;
        }
        updatePlayState();
    }

    private void openExtendView() {
        try {
            Intent intent = new Intent("start.extend.image.action");
            intent.setPackage("com.cookoo.extendimage");
            startActivity(intent);
        } catch(Exception e) {
            LogUtils.print(TAG,"openExtendView fail !!!");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView,View view,int position,long l) {
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

    @Override
    public void onImageStateChanged(int eventId) {
        switch(eventId) {
            case ImageSdkConstants.ImageStateEventId.UPDATE_PLAY_INFO:
                updateImageInfo();
                break;
            default:
        }
    }

    @Override
    public void onImageScanChanged(int eventId) {
        LogUtils.print(TAG,"===mediaStateChanged===" + eventId);
        switch(eventId) {
            case ImageSdkConstants.ScanStateEventId.FILE_SCAN_FINISHED:
                updateUsbState();
                break;
            case ImageSdkConstants.ScanStateEventId.USB_DISK_UNMOUNTED:
                handleUsbDiskUnMount();
                break;
            case ImageSdkConstants.ScanStateEventId.USB_DISK_MOUNTED:
                handleUsbDisMounted();
                break;
            case ImageSdkConstants.ScanStateEventId.IMAGE_DATA_CHANGE:
                mImageManager.handleImageDataChange(mImageManager.getCurrentShowDataPath());
                break;
            case ImageSdkConstants.ScanStateEventId.IMAGE_ALL_DATA_BACK:
            case ImageSdkConstants.ScanStateEventId.IMAGE_TWO_CLASS_FOLDER_LEVEL1_DATA_BACK:
            case ImageSdkConstants.ScanStateEventId.IMAGE_TWO_CLASS_FOLDER_LEVEL2_DATA_BACK:
            case ImageSdkConstants.ScanStateEventId.IMAGE_TREE_FOLDER_DATA_BACK:
            case ImageSdkConstants.ScanStateEventId.IMAGE_COLLECTED_DATA_BACK:
                updatePlayList();
                showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
                break;
            case ImageSdkConstants.ScanStateEventId.IMAGE_PARSE_BACK:
//                setImagePageAdapter();
                break;
            default:
        }
    }

    private void handleClickCollectEvent(MediaData mi) {
        //获取对象当前收藏状态
        boolean isCollected = mi.isCollected();
        LogUtils.print(TAG,"---->> handleClickCollectEvent() isCollected: " + isCollected);
        if(mImageManager.updateMediaCollected(mi)) {
            updatePlayList();
            //收藏状态更新成功，显示UI
            if(isCollected) {
                Toast.makeText(ActivityImage.this,"成功从收藏列表中移除",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityImage.this,"成功添加到收藏列表",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleClickFileEvent(int position,MediaData md) {
        mImageManager.endSlidePlay();
        LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType() + " position: " + position);
        List<MediaData> newData = mImageManager.getNewData();
        MediaData currentMedia = mImageManager.getCurrentPlayMediaItem();
        LogUtils.print(TAG,"--->> handleClickFileEvent() 1 -> " + newData.size());
        //1、如果处于初始状态，列表没有存在数据则直接添加当前播放数据
        //2、不同数据类型之间的数据切换
        //3、不同路径下数据切换
        if(mImageManager.getPlayList().size() == 0 || currentMedia == null || currentMedia.getDataType() != md.getDataType() || (!TextUtils.isEmpty(mImageManager.getCurrentShowDataPath()) && !mImageManager.getCurrentShowDataPath().equals(mImageManager.getCurrentPlayFilePath()))) {
            if(ConstantsUtils.ListType.TREE_FOLDER_TYPE == md.getDataType()) {
                //如果是文件列表需要过滤掉文件夹，否则图片会解析出错
                for(int i = newData.size() - 1; i >= 0; i--) {
                    if(newData.get(i).isFolder()) {
                        newData.remove(i);
                    }
                }
                //重新获取当前文件位于列表中的位置
                for(int i = 0; i < newData.size(); i++) {
                    if(newData.get(i).getFilePath().equals(md.getFilePath())) {
                        LogUtils.print(TAG,"==setCurrentListPosition==" + i);
                        mImageManager.setCurrentListPosition(i);
                        break;
                    }
                }
            }else{
                mImageManager.setCurrentListPosition(position);
            }
            LogUtils.print(TAG,"--->> handleClickFileEvent() 2 -> " + newData.size() + "|" + mImageManager.getCurrentListPosition());
            mImageManager.setPlayList(newData);
            mImageManager.setCurrentPlayFilePath(mImageManager.getCurrentShowDataPath());
            mImageManager.setCurrentPlayMediaItem(md);
            setImagePageAdapter();
        } else {
            if(ConstantsUtils.ListType.TREE_FOLDER_TYPE == md.getDataType()) {
                //重新获取当前文件位于列表中的位置
                List<MediaData> playList = mImageManager.getPlayList();
                for(int i = 0; i < playList.size(); i++) {
                    if(playList.get(i).getFilePath().equals(md.getFilePath())) {
                        LogUtils.print(TAG,"==setCurrentListPosition==" + i);
                        mImageManager.setCurrentListPosition(i);
                        break;
                    }
                }
            } else {
                mImageManager.setCurrentListPosition(position);
            }
            mImageManager.setCurrentPlayMediaItem(md);
            if(mViewPager != null){
                mViewPager.setCurrentItem(mImageManager.getCurrentListPosition());
            }
        }
        LogUtils.print(TAG,"--->> handleClickFileEvent() 3 -> " + mImageManager.getCurrentListPosition() + "|" + mImageManager.getPlayList().size() + "|" + mImageManager.getCurrentPlayMediaItem().getFilePath());
    }

    private void handleClickFolderEvent(MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFolderEvent() getDataType " + md.getDataType());
        switch(md.getDataType()) {
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
                mImageManager.requestImageData(md.getFilePath(),ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                mImageManager.requestImageData(md.getFilePath(),ConstantsUtils.ListType.TREE_FOLDER_TYPE);
                break;
            default:
        }
    }

    private void handleUsbDisMounted() {
        updateUsbState();
        if(mImageManager.getCurrentPlayMediaItem() == null) {
            //获取最后记录播放文件
            MediaData mi = mImageManager.getSavePlayMediaItem();
            LogUtils.print(TAG,"handleUsbDisMounted  mi: " + mi);
            if(mi != null && FileUtils.isFileExit(mi.getFilePath())) {
                mImageManager.requestImageData(mi.getFilePath(),ConstantsUtils.ListType.ALL_TYPE);
            }
        }
    }

    /**
     * 处理USB设备卸载Ui显示
     */
    private void handleUsbDiskUnMount() {
        LogUtils.print(TAG," --->> handleUsbDiskUnMount()");
        //刷新列表数据
        if(playListAdapter != null) {
            playListAdapter.updateAdapter(mImageManager.getPlayList());
        }
        playListCount.setText(getPlayListPosition());
        showListFolderPath.setText("");
        showListDataType.setText("");
        updateUsbState();
    }

    private void updateUsbState() {
        LogUtils.print(TAG,"  updateUsbState()");
        usbScanState.setText(getUsbState());
    }

    /**
     * 获取当前USB设备挂载、扫描信息
     **/
    private String getUsbState() {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i < mImageManager.getUsbDeviceList().size(); i++) {
            if(mImageManager.getUsbDeviceList().get(i).getRootPath() == null) {
                stringBuffer.append("USB" + i + "  ：卸载|未连接 ");
            } else {
                if(ProviderHelper.USB_INNER_SD_PATH.equals(mImageManager.getUsbDeviceList().get(i).getRootPath())) {
                    stringBuffer.append("SD" + "  ：挂载| ");
                } else {
                    stringBuffer.append("USB" + i + "  ：挂载| ");
                }
                if(mImageManager.getUsbDeviceList().get(i).isScanFinished()) {
                    stringBuffer.append("扫描完成  ");
                } else {
                    stringBuffer.append("扫描中  ");
                }
            }
        }
        return stringBuffer.toString();
    }


    private String getShowListDataType() {
        LogUtils.print(TAG,"---->> getShowListDataType() getCurrentDataListType: " + mImageManager.getCurrentDataListType());
        switch(mImageManager.getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
                return "全部数据列表";
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                return "两级文件夹列表";
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                return "文件夹列表";
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                return "收藏列表";
            default:
        }
        return "";
    }

    private String getListPathText(String path) {
        LogUtils.print(TAG,"getListPathText--> " + mImageManager.getCurrentDataListType() + "|" + path);
        switch(mImageManager.getCurrentDataListType()) {
            case ConstantsUtils.ListType.ALL_TYPE:
                path = "全部数据列表目录 ";
                break;
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                path = "收藏列表目录 ";
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                path = "专辑目录";
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                path = "艺术家目录";
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


    @Override
    public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        LogUtils.print(TAG,"onPageSelected->" + position + "|" + mImageManager.getPlayList().size());
        if(position >= mImageManager.getPlayList().size()){
            return;
        }
        mImageManager.setCurrentListPosition(position);
        mImageManager.setCurrentPlayMediaItem(mImageManager.getPlayList().get(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
