package carnetapp.usbmediadata.model;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.base.IMediaChangeListener;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
import carnetapp.usbmediadata.db.ProviderHelper;
import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.DeviceUtils;
import carnetapp.usbmediadata.utils.LogUtils;

/**
 * @author: kobe
 * @date: 2018/6/13 14:52
 * @decribe:
 */

public class MediaDataRunnable implements Runnable {
    private Context context;
    private Uri uri;
    private String columnContentStr;
    private String folderPath;
    private int dataType;
    private Handler mHandler;
    private IMediaChangeListener mediaChangeListener;
    private boolean isFilterSdcard = false;
    private boolean isDifferentiatingUSB = false;

    public MediaDataRunnable(Context context,Uri uri,String columnContentStr,String folderPath,int dataType,Handler mHandler,
                             IMediaChangeListener mediaChangeListener,boolean isFilterSdcard,boolean isDifferentiatingUSB) {
        LogUtils.i("--MediaDataRunnable->" + uri + "|" + folderPath + "|" + dataType + "|" + columnContentStr + "|" + isFilterSdcard + "|" + isDifferentiatingUSB);
        this.context = context;
        this.uri = uri;
        this.columnContentStr = columnContentStr;
        this.folderPath = folderPath;
        this.dataType = dataType;
        this.mHandler = mHandler;
        this.mediaChangeListener = mediaChangeListener;
        this.isFilterSdcard = isFilterSdcard;
        this.isDifferentiatingUSB = isDifferentiatingUSB;
    }

    @Override
    public void run() {
        final List<MediaListData> mediaListDataList = new ArrayList<>();
        //如果查询路径为空则查询所有的USB数据
        if(TextUtils.isEmpty(folderPath)) {
            getAllData(mediaListDataList);
        } else {
            getFilePathData(mediaListDataList);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mediaChangeListener != null) {
                    LogUtils.i("--MediaDataRunnable size ->" + mediaListDataList.size());
                    mediaChangeListener.onMediaDataCallback(mediaListDataList,folderPath,dataType);
                }
            }
        });
    }

    private boolean getFilePathData(List<MediaListData> mediaListDataList) {
        List<MediaData> items = null;
        switch(dataType) {
            case ConstantsUtils.ListType.ALL_TYPE:
                items = ProviderHelper.getAudioAllFromDB(context,uri,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                items = ProviderHelper.getCollectedItem(context,"",uri);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                items = ProviderHelper.getMediaFolderDataFromDB(context,uri,folderPath,dataType);
                break;
            case ConstantsUtils.ListType.SDCARD_INNER_TYPE:
                items = ProviderHelper.getMediaListFromISD(context,uri,dataType);
                break;
            case ConstantsUtils.ListType.SDCARD_OUT_TYPE:
                items = ProviderHelper.getMediaListFromOSD(context,uri,dataType);
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,folderPath);
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,columnContentStr,folderPath);
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,folderPath);
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,columnContentStr,folderPath);
                break;
        }
        if(items == null) {
            return true;
        }
        final MediaListData mediaListData = new MediaListData();
        mediaListData.setDataType(dataType);
        mediaListData.setUsbRootPath(folderPath);
        mediaListData.setData(items);
        mediaListDataList.add(mediaListData);
        return false;
    }

    private void getAllData(List<MediaListData> mediaListDataList) {
        LogUtils.i("==getAllData isDifferentiatingUSB ==" + isDifferentiatingUSB);
        if(!isDifferentiatingUSB) {
            getAllDataNoDifferentiatingUsb(mediaListDataList);
            return;
        }
        for(String filePath : DeviceUtils.getMountedPath(isFilterSdcard)) {
            LogUtils.i("==getAllData==" + filePath);
            if(TextUtils.isEmpty(filePath)) {
                continue;
            }
            List<MediaData> items = null;
            switch(dataType) {
                case ConstantsUtils.ListType.ALL_TYPE:
                    items = ProviderHelper.getAudioAllFromDB(context,uri,filePath,dataType);
                    break;
                case ConstantsUtils.ListType.COLLECTION_TYPE:
                    items = ProviderHelper.getCollectedItem(context,"",uri);
                    break;
                case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
                case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
                case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                    items = ProviderHelper.getMediaFolderDataFromDB(context,uri,filePath,dataType);
                    break;
                case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                    items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,filePath);
                    break;
                case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                    items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,columnContentStr,filePath);
                    break;
                case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                    items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,filePath);
                    break;
                case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                    items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,columnContentStr,filePath);
                    break;
            }
            if(items == null) {
                LogUtils.i("==items is null==");
                continue;
            }
            final MediaListData mediaListData = new MediaListData();
            mediaListData.setDataType(dataType);
            mediaListData.setUsbRootPath(filePath);
            mediaListData.setData(items);
            mediaListDataList.add(mediaListData);
        }
    }

    /**
     * 获取媒体数据，不区分U盘路径
     * @param mediaListDataList
     */
    private void getAllDataNoDifferentiatingUsb(List<MediaListData> mediaListDataList) {
        LogUtils.i("==getAllDataNoDifferentiatingUsb ==" + dataType + "|" + folderPath);
        List<MediaData> items = null;
        switch(dataType) {
            case ConstantsUtils.ListType.ALL_TYPE:
                items = ProviderHelper.getAudioAllFromDB(context,uri,"",dataType);
                break;
            case ConstantsUtils.ListType.COLLECTION_TYPE:
                items = ProviderHelper.getCollectedItem(context,"",uri);
                break;
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE:
            case ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE:
            case ConstantsUtils.ListType.TREE_FOLDER_TYPE:
                items = ProviderHelper.getMediaFolderDataFromDB(context,uri,"",dataType);
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL1_TYPE:
                items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,"");
                break;
            case ConstantsUtils.ListType.ALBUM_LEVEL2_TYPE:
                items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ALBUM,columnContentStr,"");
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL1_TYPE:
                items = ProviderHelper.getMusicColumnContent(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,"");
                break;
            case ConstantsUtils.ListType.AUTHOR_LEVEL2_TYPE:
                items = ProviderHelper.getCertainColumnContentList(context,dataType,uri,ProviderHelper.MediaTableCols.ARTIST,columnContentStr,"");
                break;
        }
        if(items == null) {
            LogUtils.i("==items is null==");
        }
        MediaListData mediaListData = new MediaListData();
        mediaListData.setDataType(dataType);
        mediaListData.setUsbRootPath(folderPath);
        mediaListData.setData(items);
        mediaListDataList.add(mediaListData);
    }
}
