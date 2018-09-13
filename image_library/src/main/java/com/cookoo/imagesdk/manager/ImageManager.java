package com.cookoo.imagesdk.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.binder.ImageAidlBinder;
import com.cookoo.imagesdk.imp.IImageModule;
import com.cookoo.imagesdk.imp.ImageManagerApi;
import com.cookoo.imagesdk.load.FileUtil;
import com.cookoo.imagesdk.service.ImageSdkService;
import com.cookoo.imagesdk.utils.GlobalTool;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.model.ImageScanManager;
import carnetapp.usbmediadata.model.MediaUsbService;
import carnetapp.usbmediadata.model.MusicScanManager;
import carnetapp.usbmediadata.utils.ConstantsUtils;
import uk.co.senab.photoview.PhotoView;

/**
 *
 * @author: lsf
 * @date: 2018/2/7 14:40
 * @decribe:
 */

public class ImageManager extends BaseManager implements ImageManagerApi{
	private final String TAG = ImageManager.class.getSimpleName();

	private ImageManager() {
	}

	public static ImageManager getInstance() {
		return MusicManagerInstance.MUSIC_MANAGER;
	}

	private static class MusicManagerInstance {
		private static final ImageManager MUSIC_MANAGER = new ImageManager();
	}

	/**当前播放文件对象**/
	private MediaData currentPlayMediaItem = null;
	/**当前播放文件位置**/
	private int currentListPosition = 0;
	private int currentShowListDataType = CookooImageConfiguration.getInstance().getParam().getCurrentDataListType();
	/**当前显示列表路径*/
	private String currentShowFilePath = null;
	/**当前播放列表路径*/
	private String currentPlayFilePath = "";
    private ImageAidlBinder imageBinder;
    /***幻灯片播放标记*/
	private boolean isSlidePlay =false;
	private ViewPager viewPager;
	private PhotoView currentPhotoView;
	private List<MediaData> playList = null;
	/**所有USB设备信息**/
	private List<UsbDevice> usbDeviceList;
	private IImageModule iImageModule;
	private List<MediaListData> originalData;


	@SuppressLint("WrongConstant")
	public void init(){
		LogUtils.print(TAG,"-------->> init()");
		SharedPreferenceManager.getInstance().init();
		ImageDataManager.getInstance().init();
		FileUtil.getInstance(GlobalTool.getInstance().getContext()).removeCacheBitmap();
		startImageService();
		startScanService();
	}

	private void startImageService(){
		Intent intent = new Intent(GlobalTool.getInstance().getContext(),ImageSdkService.class);
		intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
		GlobalTool.getInstance().getContext().bindService(intent, imageServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void startScanService(){
		Intent intent = new Intent(GlobalTool.getInstance().getContext(),MediaUsbService.class);
		intent.setPackage(GlobalTool.getInstance().getContext().getPackageName());
		GlobalTool.getInstance().getContext().startService(intent);
	}

	private ServiceConnection imageServiceConnection = new ServiceConnection() {
		@Override
        public void onServiceConnected(ComponentName name, IBinder service) {
			imageBinder = (ImageAidlBinder) service;
			LogUtils.print(TAG, "------>>> onServiceConnected  imageBinder: "+ imageBinder);
		}

		@Override
        public void onServiceDisconnected(ComponentName name) {
			LogUtils.print(TAG, "------>>> onServiceDisconnected");
			imageBinder = null;
		}
	};

	public void setImageModule(IImageModule iImageModule) {
		this.iImageModule = iImageModule;
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
	}

	public PhotoView getCurrentPhotoView() {
		return currentPhotoView;
	}

	public void setCurrentPhotoView(PhotoView currentPhotoView) {
		this.currentPhotoView = currentPhotoView;
	}


	public void setSlidePlay(boolean isSlidePlay){
		this.isSlidePlay = isSlidePlay;
	}

	public boolean isSlidePlay(){
		return isSlidePlay;
	}

	/**获取当前媒体播放对象**/
	@Override
	public void setCurrentPlayMediaItem(MediaData currentPlayMediaItem) {
		this.currentPlayMediaItem = currentPlayMediaItem;
		sendImageStateEvent(ImageSdkConstants.ImageStateEventId.UPDATE_PLAY_INFO);
	}

	@Override
	public boolean updateMediaItemName(MediaData itemInfo, boolean isCollection) {
		return false;
	}

	@Override
	public boolean collected(MediaData mediaData){
		if (mediaData == null || TextUtils.isEmpty(mediaData.getFilePath())){
			return false;
		}
		LogUtils.print(TAG,"---->> collected() isCollected: " + mediaData.isCollected());
		if (!mediaData.isCollected()){
			updateMediaCollected(true,mediaData.getFilePath());
		}
		return true;
	}

	@Override
	public boolean unCollected(MediaData mediaData){
		if (mediaData == null || TextUtils.isEmpty(mediaData.getFilePath())){
			return false;
		}
		LogUtils.print(TAG,"---->> unCollected() isCollected: " + mediaData.isCollected());
		if (mediaData.isCollected()){
			updateMediaCollected(false,mediaData.getFilePath());
		}
		return true;
	}

	private boolean updateMediaCollected(boolean isCollected,String filePath) {
		LogUtils.print(TAG,"---->>updateMediaCollected() isCollected: " +isCollected);
		return ImageScanManager.getInstance().updateMediaCollecte(isCollected, filePath);
	}

	/**获取当前媒体对象**/
	@Override
	public MediaData getCurrentPlayMediaItem() {
		return currentPlayMediaItem;
	}

	@Override
	public int getCurrentListPosition() {
		return currentListPosition;
	}

	@Override
	public void setCurrentListPosition(int currentListPosition) {
		this.currentListPosition = currentListPosition;
	}

	@Override
	public List<MediaListData> getOriginalData() {
		if (originalData  == null){
			originalData = new ArrayList<>();
		}
		return originalData;
	}

	@Override
	public List<MediaData> getPlayList() {
		if (playList  == null){
			playList = new ArrayList<>();
		}
		return playList;
	}

	@Override
	public List<MediaData> getNewData() {
		List<MediaData> list = new ArrayList<>();
		for(MediaListData ml : getOriginalData()) {
			if(ml == null || ml.getData() == null) {
				getOriginalData().remove(ml);
				continue;
			}
			list.addAll(ml.getData());
		}
		LogUtils.print(TAG,"getNewData()->" + list.size());
		return list;
	}

	@Override
	public void setPlayList(List<MediaData> playList) {
		this.playList = playList;
		if (playList == null || playList.size() <= 0){
			return;
		}
		LogUtils.print(TAG,"-------->> updatePlayListData() playList.size: "+playList.size()+" getDataType: "+playList.get(0).getDataType());
		if(ConstantsUtils.ListType.TREE_FOLDER_TYPE == playList.get(0).getDataType()) {
			//如果是文件列表需要过滤掉文件夹，否则图片会解析出错
			for(int i = playList.size() - 1; i >= 0; i--) {
				if(playList.get(i).isFolder()) {
					playList.remove(i);
				}
			}
			//重新获取当前文件位于列表中的位置
			if (getCurrentPlayMediaItem() == null){
				return;
			}
			LogUtils.print(TAG,"---->> setPlayList()===111=" );
			for(int i = 0; i < playList.size(); i++) {
				if(playList.get(i).getFilePath().equals(getCurrentPlayMediaItem().getFilePath())) {
					LogUtils.print(TAG,"---->> setPlayList() ===22==setCurrentListPosition==" + i);
					setCurrentListPosition(i);
					break;
				}
			}
		}
	}

	@Override
	public void nextImage(){
		if (iImageModule != null){
			iImageModule.nextImage();
			sendImageStateEvent(ImageSdkConstants.ImageStateEventId.UPDATE_PLAY_INFO);
		}
	}

	@Override
	public void preImage(){
		if (iImageModule != null){
			iImageModule.preImage();
		}
	}

	@Override
	public void zoomIn(){
		if (iImageModule != null){
			iImageModule.zoomIn();
		}
	}

	@Override
	public void zoomOut(){
		if (iImageModule != null){
			iImageModule.zoomOut();
		}
	}

	@Override
	public void rotate(int angle){
		if (iImageModule != null){
			iImageModule.rotate(angle);
		}
	}

	@Override
	public void endSlidePlay(){
		if (iImageModule != null){
			iImageModule.endSlidePlay();
		}
	}

	@Override
	public void startSlide() {
		if (iImageModule != null){
			iImageModule.startSlide();
		}
	}

	@Override
	public void clearUsbImage(String usbPath){
		if (iImageModule != null){
			iImageModule.clearUsbImage(usbPath);
		}
	}

	@Override
	public boolean isCurrentPlayItem(MediaData itemInfo){
		boolean isCurrentItem = false;
		if (itemInfo != null && currentPlayMediaItem != null){
			if (itemInfo.getFilePath() != null && currentPlayMediaItem.getFilePath() != null){
				if (itemInfo.getFilePath().equals(currentPlayMediaItem.getFilePath())){
					isCurrentItem = true;
				}
			}
		}
		return isCurrentItem;
	}

	@Override
	public boolean isCurrentUsbMount(String usbPath) {
		LogUtils.print(TAG," isCurrentUsbMount size: " + usbPath);
		if (usbPath == null){
			LogUtils.print(TAG,"--->>isCurrentUsbMount() Usb path is not null!");
			return false;
		}
		for(UsbDevice ud : getUsbDeviceList()) {
			if(ud.getRootPath() != null && usbPath.equals(ud.getRootPath())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAllUsbUnMount() {
		for(UsbDevice ud : getUsbDeviceList()) {
			if(ud.getRootPath() != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<UsbDevice> getUsbDeviceList() {
		if(usbDeviceList == null) {
			usbDeviceList = new ArrayList<>();
		}
		return usbDeviceList;
	}

	@Override
	public int getHeightLightPosition(){
		MediaData mediaData = getCurrentPlayMediaItem();
		List<MediaData> showDataList = getNewData();
		int playDataListType;
		int showDataListType = getCurrentDataListType();
		if (mediaData != null){
			playDataListType = mediaData.getDataType();
			if (playDataListType == showDataListType){
				return currentListPosition;
			}else {
				for (int i = 0; i < showDataList.size(); i++) {
					if (isCurrentPlayItem(showDataList.get(i))){
						return i;
					}
				}
			}
		}
		int heightLight = getFirstFilePosition(showDataList,0);
		LogUtils.print(TAG," ---->> getHeightLightPosition() heightLight"+heightLight);
		return heightLight;
	}

	public int getFirstFilePosition(List<MediaData> showDataList,int currentPosition){
		if (currentPosition >= showDataList.size()){
			return -1;
		}
		if (showDataList.get(currentPosition).isFolder()){
			currentPosition = currentPosition + 1;
			getFirstFilePosition(showDataList,currentPosition);
		}
		return currentPosition;
	}

	@Override
	public int getCurrentDataListType() {
		return currentShowListDataType;
	}

	public void setCurrentDataListType(int dataType){
		this.currentShowListDataType = dataType;
	}

	@Override
	public String getCurrentShowDataPath(){
		return currentShowFilePath;
	}

	@Override
	public String getUpperLevelFolderPath(String currentPath){
		if (TextUtils.isEmpty(currentPath)){
			return null;
		}
		return currentPath.substring(0, currentPath.lastIndexOf("/"));
	}

	@Override
	public void upperLevel(String usbPath) {
		LogUtils.print(TAG," == upperLevel() == " + getCurrentDataListType());
		switch(getCurrentDataListType()) {
			case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
				handleTreeClassicFolderUpperLevel(usbPath);
				break;
			case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
				handleTwoClassicFolderUpperLevel(usbPath);
				break;
			default:
		}
	}

	@Override
	public boolean isAllDeviceScanFinished(boolean isFilterSdcard){
		for(UsbDevice device: getUsbDeviceList()){
			if(!TextUtils.isEmpty(device.getRootPath()) && !isCurrentDeviceScanFinished(device.getRootPath())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isCurrentDeviceScanFinished(String usbPath){
		return ImageScanManager.getInstance().isUsbFileScanFinished(usbPath);
	}

	@Override
	public void setCPURefrain(boolean isRefrain){
		ImageScanManager.getInstance().setCPURefrain(isRefrain,3);
	}


	@Override
	public void requestImageData(String path, int dataType) {
		LogUtils.print(TAG,"---->> requestImageData() dataType: "+dataType+" path: "+path);
		this.currentShowListDataType = dataType;
		this.currentShowFilePath = path;
		ImageScanManager.getInstance().requestImageData(GlobalTool.getInstance().getContext(),path,dataType);
	}

	@Override
	public MediaData getSavePlayMediaItem() {
		return SpManager.getInstance().getPlayMediaItem();
	}

	private void handleTwoClassicFolderUpperLevel(String usbPath){
		requestImageData(null,ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE);
	}

	private void handleTreeClassicFolderUpperLevel(String usbPath) {
		LogUtils.print(TAG,"==handleTreeClassicFolderUpperLevel 1 ==" + currentShowFilePath);
		if(TextUtils.isEmpty(currentShowFilePath)) {
			return;
		}
		//如果当前显示文件的根目录就是usb的目录，直接返回总目录
		for(UsbDevice ud : getUsbDeviceList()) {
			if(currentShowFilePath.equals(ud.getRootPath())) {
				currentShowFilePath = null;
				requestImageData(currentShowFilePath,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
				return;
			}
		}
		String folderPath = getUpperLevelFolderPath(currentShowFilePath);
		LogUtils.print(TAG,"==handleTreeClassicFolderUpperLevel 2 ==" + folderPath);
		if(TextUtils.isEmpty(folderPath)) {
			return;
		}
		LogUtils.print(TAG," ---->> handleTreeFolderTypeUpperLevel  folderPath:" + folderPath);
		requestImageData(folderPath,ConstantsUtils.ListType.TREE_FOLDER_TYPE);
	}

	@Override
	public void handleImageDataChange(String usbPath){
		handleShowListDataChange(usbPath);
		handlePlayListDataChange(usbPath);
	}

	public void handleShowListDataChange(String usbPath){
		int showDataType = getCurrentDataListType();
		switch (showDataType){
			case ConstantsUtils.ListType.ALL_TYPE:
			case ConstantsUtils.ListType.COLLECTION_TYPE:
			case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
			case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
			case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
				requestImageData(usbPath,showDataType);
				break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
				requestImageData(null,showDataType);
                break;
			case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
			case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
				requestImageData(currentShowFilePath,showDataType);
				break;
			default:
		}
	}

	public void handlePlayListDataChange(String usbPath){
		int showDataType = getCurrentDataListType();
		if (getCurrentPlayMediaItem() == null || showDataType == getCurrentPlayMediaItem().getDataType()){
			return;
		}
		int playListDataType = getCurrentPlayMediaItem().getDataType();
		//更新播放列表数据
		switch (playListDataType){
			case ConstantsUtils.ListType.ALL_TYPE:
			case ConstantsUtils.ListType.COLLECTION_TYPE:
			case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
			case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
			case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
				ImageScanManager.getInstance().requestImageData(GlobalTool.getInstance().getContext(),usbPath,playListDataType);
				break;
			case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
				ImageScanManager.getInstance().requestImageData(GlobalTool.getInstance().getContext(),null,showDataType);
				break;
			case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
			case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
				ImageScanManager.getInstance().requestImageData(GlobalTool.getInstance().getContext(),currentShowFilePath,playListDataType);
				break;
			default:
		}
	}

}
