package carnetapp.usbmediadata.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.base.VideoScanManagerApi;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * Created by Administrator on 2018/3/12 0012.
 */

public class VideoScanManager extends ScanManager implements VideoScanManagerApi {

    private static VideoScanManager videlScanManager;
    private static Handler mHandler;

    static {
        mHandler = new Handler();
    }

    private Context context;
    /**
     * 数据类型
     */
    private int dataType;
    /**
     * 数据变化监听
     */
    private IMediaChangeListener videoChangeListener;
    /**
     * 是否过滤内置sdcard
     **/
    private boolean isFilterSdcard = false;
    /**
     * 是否区分USB
     **/
    private boolean isDifferentiatingUSB = true;

    private VideoScanManager() {
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public void setFilterSdcard(boolean filterSdcard) {
        isFilterSdcard = filterSdcard;
    }

    public void setDifferentiatingUSB(boolean differentiatingUSB) {
        isDifferentiatingUSB = differentiatingUSB;
    }


    protected IMediaChangeListener getVideoChangeListener() {
        return videoChangeListener;
    }

    /**
     * 获取MusicControlManager实例
     * @return
     */
    public static VideoScanManager getInstance() {
        if(videlScanManager == null) {
            videlScanManager = new VideoScanManager();
        }
        return videlScanManager;
    }

    public void initVideoInfo(Context context,IMediaChangeListener videoChangeListener,int dataType) {
        this.context = context;
        this.videoChangeListener = videoChangeListener;
        this.dataType = dataType;
        context.startService(new Intent(context,MediaUsbService.class));
    }

    public void requestVideoScan(String filePath) {
    }

    public MediaItemInfo getVideoMediaItem(Context context,String path) {
        return ProviderHelper.getVideoMediaItemByFilePath(context,path);
    }


    @Override
    public void requestVideoParse(String filePath) {
        if(MediaUsbService.iMediaService != null) {
            try {
                MediaUsbService.iMediaService.addPriorityMedaiItem(filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestVideoData(Context context,String folderPath,int dataType) {
        switch(dataType) {
            case ConstantsUtils.ListType.ALL_TYPE:
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                requestMediaData(context,ProviderHelper.VIDEO_CONTENT_URI,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                requestMediaData(context,ProviderHelper.VIDEO_CONTENT_URI,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
            case ConstantsUtils.ListType.SDCARD_OUT_TYPE:
                requestMediaData(context,ProviderHelper.VIDEO_CONTENT_URI,"SD",dataType);
                break;
        }
    }


    /**
     * 全部数据列表、收藏列表、两级文件夹列表、树形文件夹列表
     * @param context
     * @param folderPath
     * @param dataType
     */
    private void requestMediaData(final Context context,final Uri uri,final String folderPath,final int dataType) {
        this.dataType = dataType;
        ThreadPoolManager.getSinglePool("Video").execute(new MediaDataRunnable(context,uri,null,folderPath,dataType,mHandler,videoChangeListener,isFilterSdcard,
                isDifferentiatingUSB));
    }

    public void handleRequestData() {
        requestVideoData(context,null,dataType);
    }
}
