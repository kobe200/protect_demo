package carnetapp.usbmediadata.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.base.MusicScanManagerApi;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.LogUtils;

/**
 * @author kobe
 * @date
 */
public class MusicScanManager extends ScanManager implements MusicScanManagerApi {
    private static MusicScanManager musicScanManager;
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
    private IMediaChangeListener musicChangeListener;
    /**
     * 是否过滤内置sdcard
     **/
    private boolean isFilterSdcard = false;
    /**
     * 是否区分USB
     **/
    private boolean isDifferentiatingUSB = true;

    private MusicScanManager() {
    }

    /**
     * 获取MusicControlManager实例
     * @return
     */
    public static MusicScanManager getInstance() {
        if(musicScanManager == null) {
            musicScanManager = new MusicScanManager();
        }
        return musicScanManager;
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

    protected IMediaChangeListener getMusicChangeListener() {
        return musicChangeListener;
    }

    /**
     * 设置数据类型 dataType: 请求指定类型文件列表，根据设置的数据类型 dataType决定以什么形式返回
     * 1、全部列表 2、文件分级列表 3、两级文件夹列表
     * @param context
     * @param musicChangeListener
     * @param dataType ConstantsUtils.REQUEST_DATA_TYPE_ALL: 全部列表
     * ConstantsUtils.REQUEST_DATA_TYPE_TREE_LIST: 文件分级列表
     * ConstantsUtils.REQUEST_DATA_TYPE_TWO_CALSS_LIST: 两级文件夹列表
     */
    public void initMusicInfo(Context context,IMediaChangeListener musicChangeListener,int dataType) {
        this.musicChangeListener = musicChangeListener;
        this.dataType = dataType;
        this.context = context;
        context.startService(new Intent(context,MediaUsbService.class));
    }

    @Override
    public void requestMusicParse(String filePath) {
        if(MediaUsbService.iMediaService != null) {
            try {
                MediaUsbService.iMediaService.addPriorityMedaiItem(filePath);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestMusicData(Context context,String folderPath,String columnContentStr,int dataType) {
        switch(dataType) {
            case ConstantsUtils.ListType.ALL_TYPE:
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                requestMediaData(context,ProviderHelper.AUDIO_CONTENT_URI,columnContentStr,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                requestMediaData(context,ProviderHelper.AUDIO_CONTENT_URI,columnContentStr,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
            case ConstantsUtils.ListType.SDCARD_OUT_TYPE:
                requestMediaData(context,ProviderHelper.AUDIO_CONTENT_URI,columnContentStr,"SD",dataType);
                break;
        }
    }

    /**
     * 记忆播放时，请求扫描媒体文件 filePath 媒体文件路径
     **/
    public void requestMusicScan(String filePath) {
    }

    /**
     * 全部数据列表、收藏列表、两级文件夹列表、树形文件夹列表
     * @param context
     * @param folderPath
     * @param dataType
     */
    private void requestMediaData(final Context context,final Uri uri,final String columnContentStr,final String folderPath,final int dataType) {
        this.dataType = dataType;
        ThreadPoolManager.getSinglePool("Music").execute(new MediaDataRunnable(context,uri,columnContentStr,folderPath,dataType,mHandler,musicChangeListener,isFilterSdcard,
                isDifferentiatingUSB));
    }

    public MediaItemInfo getMusicMediaItem(Context context,String folderPath) {
        LogUtils.i("getMusicMediaItem->" + folderPath);
        return ProviderHelper.getMusicMediaItemByFilePath(context,folderPath);
    }


    public void handleRequestData() {
        requestMusicData(context,null,null,dataType);
    }
}
