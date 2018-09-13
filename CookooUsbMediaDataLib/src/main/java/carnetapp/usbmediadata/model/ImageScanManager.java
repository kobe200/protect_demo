package carnetapp.usbmediadata.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.base.ImageScanManagerApi;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * Created by Administrator on 2018/3/12 0012.
 */

public class ImageScanManager extends ScanManager implements ImageScanManagerApi {
    private static ImageScanManager imageScanManager;
    private static Handler mHandler;

    static {
        mHandler = new Handler();
    }

    private final String TAG = ImageScanManager.class.getSimpleName();
    private Context context;
    /**
     * 数据类型
     */
    private int dataType;
    /**
     * 数据变化监听
     */
    private IMediaChangeListener imageChangeListener;
    /**
     * 是否过滤内置sdcard
     **/
    private boolean isFilterSdcard = false;
    /**
     * 是否区分USB
     **/
    private boolean isDifferentiatingUSB = true;

    private ImageScanManager() {
    }

    /**
     * 获取MusicControlManager实例
     * @return
     */
    public static ImageScanManager getInstance() {
        if(imageScanManager == null) {
            imageScanManager = new ImageScanManager();
        }
        return imageScanManager;
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

    protected IMediaChangeListener getImageChangeListener() {
        return imageChangeListener;
    }

    public void setImageChangeListener(IMediaChangeListener imageChangeListener) {
        this.imageChangeListener = imageChangeListener;
    }

    /**
     * 设置数据类型 dataType: 请求指定类型文件列表，根据设置的数据类型 dataType决定以什么形式返回
     * 1、全部列表 2、文件分级列表 3、两级文件夹列表
     * @param context
     * @param imageChangeListener
     * @param dataType ConstantsUtils.REQUEST_DATA_TYPE_ALL: 全部列表
     * ConstantsUtils.REQUEST_DATA_TYPE_TREE_LIST: 文件分级列表
     * ConstantsUtils.REQUEST_DATA_TYPE_TWO_CALSS_LIST: 两级文件夹列表
     */
    public void initImageInfo(Context context,IMediaChangeListener imageChangeListener,int dataType) {
        this.context = context;
        this.imageChangeListener = imageChangeListener;
        this.dataType = dataType;
        context.startService(new Intent(context,MediaUsbService.class));
    }

    /**
     * 请求扫描媒体文件 filePath 媒体文件路径
     **/
    public void requestImageScan(String filePath) {
    }

    @Override
    public void requestImageData(Context context,String folderPath,int dataType) {
        switch(dataType) {
            case ConstantsUtils.ListType.ALL_TYPE:
                requestMediaData(context,ProviderHelper.IMAGE_CONTENT_URI,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                requestMediaData(context,ProviderHelper.IMAGE_CONTENT_URI,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                requestMediaData(context,ProviderHelper.IMAGE_CONTENT_URI,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                requestMediaData(context,ProviderHelper.IMAGE_CONTENT_URI,folderPath,dataType);
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
        ThreadPoolManager.getSinglePool("Image").execute(new MediaDataRunnable(context,uri,null,folderPath,dataType,mHandler,imageChangeListener,isFilterSdcard,
                isDifferentiatingUSB));
    }

    @Override
    public void getMusicMediaItem(Context context,String folderPath) {
        ProviderHelper.getImageMediaItemByFilePath(context,folderPath);
    }

    public void handleRequestData() {
        requestImageData(context,null,dataType);
    }
}
