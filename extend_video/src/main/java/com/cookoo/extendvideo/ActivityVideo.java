package com.cookoo.extendvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.cookoo.extendvideo.adapter.BaseViewHolder;
import com.cookoo.extendvideo.adapter.CommonAdapter;
import com.cookoo.extendvideo.adapter.ListAdapter;
import com.cookoo.extendvideo.adapter.SpacesItemDecoration;
import com.cookoo.videosdkclient.hold.VideoStateListener;
import com.cookoo.videosdkclient.manager.VideoAidlManager;
import com.cookoo.videosdkclient.utils.FileUtils;
import com.cookoo.videosdkclient.utils.LogUtils;
import com.cookoo.videosdkclient.utils.VideoSdkConstants;
import java.util.List;
import carnetapp.usbmediadata.bean.MediaData;

/**
 * @author lsf
 */
public class ActivityVideo extends BaseActivity implements VideoStateListener, View.OnClickListener,ListAdapter.OnClickEventListener {
	private static final String TAG = "ActivityVideo";
	private TextView usbScanState,showListDataType,showListFolderPath,playListCount;
	/**列表控件**/
	private RecyclerView recyclerview;
	private GridLayoutManager gManager;
	private SpacesItemDecoration spacesItemDecoration;
	/**列表控件适配器**/
	private ListAdapter listAdapter = null;
	private VideoAidlManager mVideoManager = VideoAidlManager.getInstance();
	private boolean mIsCurrentUI;

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
	}

	private void initData() {
		mIsCurrentUI = true;
		mVideoManager.requestVideoData(null,mVideoManager.getCurrentDataListType());
		initAdapter();
	}

	private void initListener() {
		listAdapter.setOnClickEventListener(this);
		findViewById(R.id.btn_list_all).setOnClickListener(this);
		findViewById(R.id.btn_two_class_folder).setOnClickListener(this);
		findViewById(R.id.btn_multiple_class_folder).setOnClickListener(this);
		findViewById(R.id.btn_playlist_collected).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_switch_style).setOnClickListener(this);
		findViewById(R.id.btn_exit).setOnClickListener(this);
		mVideoManager.bindMediaStateListener(this);
	}

	private void initAdapter() {
		List<MediaData> newData = mVideoManager.getNewData();
		LogUtils.print(TAG,"initAdapter()  listViewAdapter:  " + listAdapter + "|" + newData.size());
		listAdapter = new ListAdapter(this,newData);
		listAdapter.setOnClickEventListener(this);
		gManager = new GridLayoutManager(this,3);
		spacesItemDecoration = new SpacesItemDecoration();
		recyclerview.addItemDecoration(spacesItemDecoration);
		recyclerview.setLayoutManager(gManager);
		recyclerview.setAdapter(listAdapter);
	}

	/**
	 * 更新播放列表
	 */
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
		mVideoManager.setCPURefrain(false);
        updateListAdapter();
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
		mVideoManager.unbindMediaStateListener(this);
	}


	private void startPlayActivity(MediaData mediaData){
		LogUtils.print(TAG,"---->> startPlayActivity()  mIsCurrentUI: "+mIsCurrentUI+ " size: "+mVideoManager.getPlayList().size());
		if (!mIsCurrentUI || mVideoManager.getPlayList().size() <= 0) {
			return;
		}
		Intent intent = new Intent("start.video.play.action");
		intent.setPackage("com.cookoo.mediatest");
		intent.putExtra("mediaData",mediaData);
		startActivity(intent);
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
		switch (view.getId()) {
			case R.id.btn_list_all:
				mVideoManager.requestVideoData(null,VideoSdkConstants.ListType.ALL_TYPE);
				break;
			case R.id.btn_playlist_collected:
				mVideoManager.requestVideoData(null,VideoSdkConstants.ListType.COLLECTION_TYPE);
				break;
			case R.id.btn_two_class_folder:
				mVideoManager.requestVideoData(null,VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
				break;
			case R.id.btn_multiple_class_folder:
				mVideoManager.requestVideoData(null,VideoSdkConstants.ListType.TREE_FOLDER_TYPE);
				break;
			case R.id.btn_switch_style:
				switchShowListStyle();
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
	}

	@Override
	public void onVideoScanChanged(int eventId) {
		LogUtils.print(TAG, "===mediaStateChanged===" + eventId);
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
				updateListAdapter();
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
		LogUtils.print(TAG,"---->> handleClickCollectEvent() isCollected: "+isCollected);
		if(mVideoManager.updateMediaCollected(mi)) {
			updateListAdapter();
			//收藏状态更新成功，显示UI
			if(isCollected){
				Toast.makeText(ActivityVideo.this,"成功从收藏列表中移除",Toast.LENGTH_SHORT).show();
			}else{
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
			if(mediaData.getDataType() != md.getDataType() || !clickItemParentPath.equals(playItemParentPath)) {
				LogUtils.print(TAG,"--->> handleClickFileEvent() the PlayList is change... ");
				mVideoManager.setPlayList(mVideoManager.getNewData());
			}
		}
		mVideoManager.setCurrentListPosition(position);
		startPlayActivity(listAdapter.getData().get(position));

	}

	@Override
	public void handleClickFolderEvent(MediaData md) {
		LogUtils.print(TAG,"--->> handleClickFolderEvent() getDataType " + md.getDataType());
		switch (md.getDataType()) {
			case VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
				mVideoManager.requestVideoData(md.getFilePath(), VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE);
				break;
			case VideoSdkConstants.ListType.TREE_FOLDER_TYPE:
				mVideoManager.requestVideoData(md.getFilePath(),VideoSdkConstants.ListType.TREE_FOLDER_TYPE);
				break;
			default:
		}
	}

	private void handleUsbDisMounted() {
		updateUsbState();
		LogUtils.print(TAG,"handleUsbDisMounted getCurrentPlayMediaItem: " + mVideoManager.getCurrentPlayMediaItem());
		if(mVideoManager.getCurrentPlayMediaItem() == null) {
			//获取最后记录播放文件
			String saveFilePath = mVideoManager.getSavePlayMediaItemPath();
			boolean isFileExit = FileUtils.isFileExit(saveFilePath);
			LogUtils.print(TAG,"handleUsbDisMounted  saveFilePath: " + saveFilePath+"  isFileExit: "+isFileExit);
			if(isFileExit) {
				mVideoManager.requestVideoData(saveFilePath,VideoSdkConstants.ListType.ALL_TYPE);
			}
		}
	}

	/**
	 * 处理USB设备卸载Ui显示
	 */
	private void handleUsbDiskUnMount() {
		LogUtils.print(TAG," --->> handleUsbDiskUnMount()" );
		//刷新列表数据
		if(listAdapter != null) {
			listAdapter.updateAdapter(mVideoManager.getPlayList());
		}
		playListCount.setText("");
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
			case VideoSdkConstants.ListType.ALL_TYPE:
				return "全部数据列表";
			case VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
			case VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
				return "两级文件夹列表";
			case VideoSdkConstants.ListType.TREE_FOLDER_TYPE:
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
		LogUtils.print(TAG,"----->>> getShowListRootPath()  getDataType: "+mediaData.getDataType());
		switch(mediaData.getDataType()) {
			case VideoSdkConstants.ListType.ALL_TYPE:
			case VideoSdkConstants.ListType.COLLECTION_TYPE:
				break;
			case VideoSdkConstants.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
			case VideoSdkConstants.ListType.TREE_FOLDER_TYPE:
				path = mVideoManager.getUpperLevelFolderPath(mVideoManager.getCurrentShowDataPath());
				break;
			default:
		}
		return path;
	}


}
