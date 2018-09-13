package com.cookoo.mediatest.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.imp.ImageStateListener;
import com.cookoo.imagesdk.manager.ImageManager;
import com.cookoo.mediatest.BaseActivity;
import com.cookoo.mediatest.R;
import com.cookoo.mediatest.common.ImagePageAdapter;
import com.cookoo.mediatest.common.ListAdapter;
import com.cookoo.mediatest.common.MyViewPager;
import com.cookoo.mediatest.common.SpacesItemDecoration;
import com.cookoo.videosdk.utils.LogUtils;
import java.util.List;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 */
public class ActivityImage extends BaseActivity implements ImageStateListener, View.OnClickListener,ListAdapter.OnClickEventListener{
    private static final String TAG = ActivityImage.class.getSimpleName();
    private TextView usbScanState, showListDataType, showListFolderPath, playListPath,playListCount;
    private RecyclerView recyclerview;
    private LinearLayoutManager gManager;
    private SpacesItemDecoration spacesItemDecoration;
    /**列表控件适配器**/
    private ListAdapter listAdapter = null;
    private ImageManager mImageManager = ImageManager.getInstance();
    private boolean mIsCurrentUI;
    private MyViewPager mViewPager;
    private TextView mTitleTv, mPlayStateTv;
    private Button mPreviousBtn, mNextBtn, mPlayOrPauseBtn, mZoomInBtn, mZoomOutBtn, mRotateBtn, mStartExtendImageBtn, mBackBtn;
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
        recyclerview = findViewById(R.id.recyclerView);
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
        mStartExtendImageBtn = findViewById(R.id.btn_start_extend_image);
        mBackBtn = findViewById(R.id.btn_back);
    }

    private void initData() {
        mIsCurrentUI = true;
        mImageManager.setViewPager(mViewPager);
        mImageManager.requestImageData(null,mImageManager.getCurrentDataListType());
        initAdapter();
        setImagePageAdapter(mImageManager.getCurrentListPosition());
        updateImageInfo();
    }

    private void initListener() {
        listAdapter.setOnClickEventListener(this);
        findViewById(R.id.btn_list_all).setOnClickListener(this);
        findViewById(R.id.btn_two_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_multiple_class_folder).setOnClickListener(this);
        findViewById(R.id.btn_playlist_collected).setOnClickListener(this);
        findViewById(R.id.btn_start_extend_image).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_switch_style).setOnClickListener(this);
        mPlayOrPauseBtn.setOnClickListener(this);
        mPreviousBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mZoomInBtn.setOnClickListener(this);
        mZoomOutBtn.setOnClickListener(this);
        mStartExtendImageBtn.setOnClickListener(this);
        mRotateBtn.setOnClickListener(this);
        mImageManager.bindMediaStateListener(this);
        mBackBtn.setOnClickListener(this);
    }

    private void initAdapter() {
        LogUtils.print(TAG,"initAdapter()1==1111===  listViewAdapter:  " + listAdapter + "|" + mImageManager.getPlayList().size());
        listAdapter = new ListAdapter(this, mImageManager.getNewData());
        listAdapter.setDataType(0);
        gManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
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
        List<MediaData> mediaDates = mImageManager.getNewData();
        LogUtils.print(TAG,"mediaDates size: " + mediaDates.size());
        if(mediaDates.size() < 1) {
            if(mImageManager.isAllDeviceScanFinished(true)) {
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
        playListCount.setText(""+mImageManager.getPlayList().size());
//        if (mImageManager.getCurrentPlayMediaItem() == null){
//            mImageManager.setCurrentListPosition(0);
//            mImageManager.setPlayList(mediaDates);
//        }
    }

    private void setImagePageAdapter(int selectPosition) {
        LogUtils.print(TAG," ----->>>setAdapter() mIsCurrentUI: "+  mIsCurrentUI+" getPlayList: " + mImageManager.getPlayList().size() + " getCurrentListPosition: " + mImageManager.getCurrentListPosition());
        if(!mIsCurrentUI || mImageManager.getPlayList().size() <= 0) {
            return;
        }
        if(mPagerAdapter == null) {
            mPagerAdapter = new ImagePageAdapter(this,mImageManager.getPlayList(),mViewPager);
            mViewPager.setAdapter(mPagerAdapter);
        } else {
            mPagerAdapter.updateAdapter(mImageManager.getPlayList());
        }
        mPagerAdapter.setCurrentItem(selectPosition);
    }

    private void updateImageInfo() {
        mTitleTv.setText(getCurrentPlayFilePath());
        playListCount.setText(getPlayListPosition());
        showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
        playListPath.setText(getListPathText(getCurrentPlayFilePath()));
        showListDataType.setText(getShowListDataType());
        updatePlayState();
        updateUsbState();
    }

    private void updatePlayState() {
        mPlayStateTv.setText(getPlayState());
    }

    private String getCurrentPlayFilePath() {
        return mImageManager.getCurrentPlayMediaItem() == null ? "未知" :mImageManager.getCurrentPlayMediaItem().getFilePath();
    }

    private String getShowListRootPath() {
        if(mImageManager.isAllUsbUnMount() || listAdapter == null || listAdapter.getData().size() <= 0) {
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
                path = mImageManager.getUpperLevelFolderPath(mImageManager.getCurrentShowDataPath());
                break;
            default:
        }
        return path;
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
//    private void updatePlayList() {
//        if(listAdapter == null) {
//            initAdapter();
//            return;
//        }
//        List<MediaData> mediaDates = mImageManager.getNewData();
//        LogUtils.print(TAG,"mediaDates size: " + mediaDates.size());
//        if(mediaDates.size() < 1) {
//            if(mImageManager.isAllDeviceScanFinished(true)) {
//                LogUtils.print(TAG,"   no video file!");
//            } else {
//                LogUtils.print(TAG,"   video data loading...");
//            }
//            listAdapter.updateAdapter(mediaDates);
//            return;
//        }
//        listAdapter.updateAdapter(mediaDates);
//        //列表更新后，需要更新列表显示类型,和浏览目录
//        showListDataType.setText(getShowListDataType());
//        showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
//        playListCount.setText(getPlayListPosition());
//    }

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
        LogUtils.print(TAG," ---111--->>onResume()");
        super.onResume();
        mIsCurrentUI = true;
        mImageManager.setCPURefrain(false);
        if(mImageManager.isSlidePlay()) {
            mImageManager.startSlide();
        }
        LogUtils.print(TAG," --222---->>onResume()");
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
            case R.id.btn_start_extend_image:
                mImageManager.endSlidePlay();
                openExtendView();
                this.finish();
                break;
            case R.id.btn_back:
                mImageManager.upperLevel(mImageManager.getCurrentShowDataPath());
                break;
            case R.id.btn_preimage:
                mImageManager.endSlidePlay();
                mImageManager.preImage();
                break;
            case R.id.btn_nextimage:
                mImageManager.endSlidePlay();
                mImageManager.nextImage();
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
            case R.id.btn_switch_style:
                switchShowListStyle();
                break;
            case R.id.btn_exit:
                mImageManager.endSlidePlay();
                finish();
                break;
            default:
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
                updateListAdapter();
                showListFolderPath.setText(getListPathText(mImageManager.getCurrentShowDataPath()));
                break;
            case ImageSdkConstants.ScanStateEventId.IMAGE_PARSE_BACK:
//                setImagePageAdapter();
                break;
            default:
        }
    }

    @Override
    public void handleClickCollectEvent(MediaData mi) {
        //获取对象当前收藏状态
        boolean isCollected = mi.isCollected();
        LogUtils.print(TAG,"---->> handleClickCollectEvent() isCollected: " + isCollected);
        if(mImageManager.updateMediaCollected(mi)) {
            updateListAdapter();
            //收藏状态更新成功，显示UI
            if(isCollected) {
                Toast.makeText(ActivityImage.this,"成功从收藏列表中移除",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityImage.this,"成功添加到收藏列表",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void handleClickFileEvent(int position, MediaData md) {
        mImageManager.endSlidePlay();
        List<MediaData> newData = mImageManager.getNewData();
        MediaData playMediaItem = mImageManager.getCurrentPlayMediaItem();
        LogUtils.print(TAG,"--->> handleClickFileEvent() getDataType " + md.getDataType()+" position: "+position);
        if(playMediaItem != null){
            LogUtils.print(TAG,"--->> handleClickFileEvent() md.getDataType " + md.getDataType()+" playMediaItem.getDataType: "+playMediaItem.getDataType()+" playMediaItem: "+
                    mImageManager.getUpperLevelFolderPath(playMediaItem.getFilePath())+" md: "+mImageManager.getUpperLevelFolderPath(md.getFilePath()));
        }
        //需要判断当前播放对象的数据类型和点击的对象是不是同一种类型，不是同一种类型就需要更新播放列表数据,并且判断是不是同一级列表数据
        if(mImageManager.getPlayList().size() == 0
                || playMediaItem == null
                || playMediaItem.getDataType() != md.getDataType()
                || !mImageManager.getUpperLevelFolderPath(playMediaItem.getFilePath()).equals(mImageManager.getUpperLevelFolderPath(md.getFilePath()))) {
            mImageManager.setCurrentPlayMediaItem(md);
            mImageManager.setCurrentListPosition(position);
            mImageManager.setPlayList(newData);
            setImagePageAdapter(mImageManager.getCurrentListPosition());
        }else{
            if(ConstantsUtils.ListType.TREE_FOLDER_TYPE == md.getDataType()) {
                //重新获取当前文件位于列表中的位置
                List<MediaData> playList = mImageManager.getPlayList();
                for(int i = 0; i < playList.size(); i++) {
                    if(playList.get(i).getFilePath().equals(md.getFilePath())) {
                        LogUtils.print(TAG,"==setCurrentListPosition==222==" + i);
                        mImageManager.setCurrentListPosition(i);
                        break;
                    }
                }
            } else {
                mImageManager.setCurrentListPosition(position);
            }
            mImageManager.setCurrentPlayMediaItem(md);
            if(mPagerAdapter != null){
                mPagerAdapter.setCurrentItem(mImageManager.getCurrentListPosition());
            }
        }
        LogUtils.print(TAG,"--->> handleClickFileEvent()==111= -> " + mImageManager.getCurrentListPosition() + "|" + mImageManager.getPlayList().size() + "|" + mImageManager.getCurrentPlayMediaItem().getFilePath());
    }

    @Override
    public void handleClickFolderEvent(MediaData md) {
        LogUtils.print(TAG,"--->> handleClickFolderEvent() getDataType " + md.getDataType());
        recyclerview.removeAllViewsInLayout();
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
            if(mi != null && com.cookoo.imagesdk.utils.FileUtils.isFileExit(mi.getFilePath())) {
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
        if(listAdapter != null) {
            listAdapter.updateAdapter(mImageManager.getPlayList());
        }
        if(mPagerAdapter != null){
            mPagerAdapter.updateAdapter(mImageManager.getPlayList());
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
        com.cookoo.musicsdk.utils.LogUtils.print(TAG,"getListPathText--> " + mImageManager.getCurrentDataListType() + "|" + path);
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

}
