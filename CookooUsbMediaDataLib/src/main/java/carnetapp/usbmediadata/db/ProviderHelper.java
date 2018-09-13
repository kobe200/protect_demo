package carnetapp.usbmediadata.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.LogUtils;

public class ProviderHelper {

    /**
     * 设备路径
     */
    public static final String USB_MEDIA_ROOT_PATH = "storage/udisk";
    public static final String USB_INNER_SD_PATH = "storage/emulated/0";
    public static final String USB_OUTER_SD_PATH = "storage/extsd";
    private static final String AUTHORITY = "carnetos.usbservice.provider";
    /**
     * AUDIO URI
     */
    public static final Uri AUDIO_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AUDIO");
    /**
     * VIDEO URI
     */
    public static final Uri VIDEO_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/VIDEO");
    /**
     * IMAGE URI
     */
    public static final Uri IMAGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IMAGE");
    /**
     * IMAGE URI
     */
    public static final Uri OFFICE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/OFFICE");
    /**
     * 媒体表全部字段数组
     */
    private static final String MediaTableAllCols[] = {MediaTableCols.ID,MediaTableCols.MEDIA_ID,MediaTableCols.ALBUM_ID,MediaTableCols.MEDIA_TYPE,MediaTableCols.NAME,MediaTableCols.NAME_ALPHABET,MediaTableCols.TITLE,MediaTableCols.ALBUM,MediaTableCols.ARTIST,MediaTableCols.FILE_PATH,MediaTableCols.SIZE,MediaTableCols.LAST_MODIFIED,MediaTableCols.DURATION,MediaTableCols.POSITION,MediaTableCols.IS_PLAY_ITEM,MediaTableCols.PLAYLIST_INDEX,MediaTableCols.PARSE_STATUS,MediaTableCols.UPDATE_TIME,MediaTableCols.SCAN_INDEX,MediaTableCols.DELETE_FLAG};
    private static String ORDER = MediaTableCols.NAME_ALPHABET + " ASC";
    /**
     * 是播放列表中的
     */
    private static String WHERE_PL = " " + MediaTableCols.PLAYLIST_INDEX + "  >0" + "  ";
    /**
     * 非伪删除
     */
    private static String WHERE_DF = " " + MediaTableCols.DELETE_FLAG + " =0 " + " ";
    /**
     * 内置卡中的
     */
    private static String WHERE_PH_ISD = " " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + USB_INNER_SD_PATH + "%'" + " ";
    /**
     * 外置卡中的
     */
    private static String WHERE_PH_OSD = " " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + USB_OUTER_SD_PATH + "%'" + " ";

    /**
     * 将provider中的数据转化为MediaItme
     * @param cursor
     * @return MediaItemInfo
     * @author:
     * @createTime: 2016-11-29 下午4:07:42
     * @history:
     */
    private static MediaItemInfo cursorToMediaItem(Cursor cursor) {
        MediaItemInfo mediaItemInfo = null;
        if(cursor != null) {
            mediaItemInfo = new MediaItemInfo(MediaItemInfo.MediaType.getMediaType(cursor.getInt(cursor.getColumnIndex(MediaTableCols.MEDIA_TYPE))),cursor.getString(cursor.getColumnIndex(MediaTableCols.NAME)),cursor.getString(cursor.getColumnIndex(MediaTableCols.FILE_PATH)),cursor.getLong(cursor.getColumnIndex(MediaTableCols.SIZE)),cursor.getLong(cursor.getColumnIndex(MediaTableCols.LAST_MODIFIED)),cursor.getString(cursor.getColumnIndex(MediaTableCols.NAME_ALPHABET)));
            mediaItemInfo.setID(cursor.getLong(cursor.getColumnIndex(MediaTableCols.ID)));
            mediaItemInfo.setMediaID(cursor.getLong(cursor.getColumnIndex(MediaTableCols.MEDIA_ID)));
            mediaItemInfo.setTitle(cursor.getString(cursor.getColumnIndex(MediaTableCols.TITLE)));
            mediaItemInfo.setAlbumID(cursor.getLong(cursor.getColumnIndex(MediaTableCols.ALBUM_ID)));
            mediaItemInfo.setAlbum(cursor.getString(cursor.getColumnIndex(MediaTableCols.ALBUM)));
            mediaItemInfo.setArtist(cursor.getString(cursor.getColumnIndex(MediaTableCols.ARTIST)));
            mediaItemInfo.setDuration(cursor.getLong(cursor.getColumnIndex(MediaTableCols.DURATION)));
            mediaItemInfo.setPosition(cursor.getLong(cursor.getColumnIndex(MediaTableCols.POSITION)));
            mediaItemInfo.setPlayItem(cursor.getInt(cursor.getColumnIndex(MediaTableCols.IS_PLAY_ITEM)) == 1 ? true :false);
            mediaItemInfo.setPlaylistIndex(cursor.getLong(cursor.getColumnIndex(MediaTableCols.PLAYLIST_INDEX)));
            mediaItemInfo.setParseStatus(MediaItemInfo.ParseStatus.status(cursor.getInt(cursor.getColumnIndex(MediaTableCols.PARSE_STATUS))));
            mediaItemInfo.setUpdateTime(cursor.getLong(cursor.getColumnIndex(MediaTableCols.UPDATE_TIME)));
            mediaItemInfo.setScanIndex(cursor.getLong(cursor.getColumnIndex(MediaTableCols.SCAN_INDEX)));
            mediaItemInfo.setDelete(cursor.getInt(cursor.getColumnIndex(MediaTableCols.DELETE_FLAG)) == 1 ? true :false);
        }
        return mediaItemInfo;
    }

    /**
     * 将provider中的数据转化为MediaItme
     * @param cursor
     * @return MediaItemInfo
     * @author:
     * @createTime: 2016-11-29 下午4:07:42
     * @history:
     */
    private static MediaData cursorToMediaItemNew(Cursor cursor,int dataType) {
        MediaData mediaData = null;
        if(cursor != null) {
            mediaData = new MediaData();
            mediaData.setFilePath(cursor.getString(cursor.getColumnIndex(MediaTableCols.FILE_PATH)));
            mediaData.setName(cursor.getString(cursor.getColumnIndex(MediaTableCols.NAME)));
            mediaData.setTitle(cursor.getString(cursor.getColumnIndex(MediaTableCols.TITLE)));
            mediaData.setAlbum(cursor.getString(cursor.getColumnIndex(MediaTableCols.ALBUM)));
            mediaData.setArtist(cursor.getString(cursor.getColumnIndex(MediaTableCols.ARTIST)));
            mediaData.setDataType(dataType);
            boolean isCollected = cursor.getInt(cursor.getColumnIndex(MediaTableCols.IS_COLLECTED)) == 1 ? true :false;
            mediaData.setCollected(isCollected);
            mediaData.setmSize(cursor.getLong(cursor.getColumnIndex(MediaTableCols.SIZE)));
            mediaData.setmDuration(cursor.getLong(cursor.getColumnIndex(MediaTableCols.DURATION)));
        }
        return mediaData;
    }

    private static List<MediaData> queryNew(Context context,int dataType,Uri uri,String[] args) {
        String where = toWhere(args);
        where = where + "and 0=0) group by (" + MediaTableCols.FILE_PATH; //使用group by进行分组，也能达到去掉重复ID的效果
        List<MediaData> temp = new ArrayList<>();
        LogUtils.i("==queryNew2 ==" + context + "|" + uri);
        Cursor c = context.getContentResolver().query(uri,null,where,null,ORDER);
        LogUtils.i("==queryNew2 ==" + where + "|" + c);
        if(c != null) {
            LogUtils.i("==queryNew2 ==" + where + "|" + c.getCount() + "|" + c.getColumnCount());
        }
        while(c != null && c.moveToNext()) {
            MediaData mediaData = ProviderHelper.cursorToMediaItemNew(c,dataType);
            //            LogUtils.i("==queryNew fileDir==" + dataType + "|" + mediaData.getFilePath());
            temp.add(mediaData);
        }
        LogUtils.i("==queryNew2 ==" + dataType + "|" + temp.size());
        if(c != null) {
            c.close();
        }
        return temp;
    }

    private static List<MediaData> queryNew(Context context,int dataType,String fileDir,Uri uri,String[] args) {
        String where = toWhere(args);
        where = where + "and 0=0) group by (" + MediaTableCols.FILE_PATH; //使用group by进行分组，也能达到去掉重复ID的效果
        List<MediaData> temp = new ArrayList<>();
        List<String> folderPaths = new ArrayList<>();
        Cursor c = context.getContentResolver().query(uri,null,where,null,ORDER);
        LogUtils.i("==queryNew ==" + where);
        LogUtils.i("==queryNew fileDir==" + fileDir + "|" + dataType);
        while(c != null && c.moveToNext()) {
            MediaData item = ProviderHelper.cursorToMediaItemNew(c,dataType);
            //            LogUtils.i("==queryNew item ==" + dataType + "|" + item.getFilePath());
            if(dataType == ConstantsUtils.ListType.TREE_FOLDER_TYPE) {
                //以树形文件结构返回数据
                String filePath = item.getFilePath();
                File itemFile = new File(filePath);
                String parentPath = itemFile.getParent();
                //是文件夹则需要处理，否则不需要处理
                if(parentPath.equals(fileDir)) {
                    temp.add(item);
                } else {
                    String fileDir2 = fileDir;
                    if(!fileDir2.endsWith("/")) {
                        fileDir2 += "/";
                    }
                    if(!filePath.contains(fileDir2)) {
                        continue;
                    }
                    int start = fileDir2.length();
                    String filePath2 = filePath.substring(start);
                    int end = filePath2.indexOf("/");
                    String folderName = filePath2.substring(0,end);
                    String folderPath = fileDir2 + folderName;
                    if(TextUtils.isEmpty(folderName)) {
                        continue;
                    }
                    //没有添加过该文件夹对象则将该对象添加到头部
                    if(!folderPaths.contains(folderName)) {
                        item.setFolder(true);
                        item.setName(folderName);
                        item.setFilePath(folderPath);
                        temp.add(0,item);
                        folderPaths.add(folderName);
                    }
                }
            } else if(dataType == ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL1_TYPE) {
                //以两级文件结构返回数据
                String filePath = item.getFilePath();
                File itemFile = new File(filePath);
                String parentPath = itemFile.getParent();
                String f1 = filePath.substring(0,filePath.lastIndexOf("/"));
                String folderName = f1.substring(f1.lastIndexOf("/") + 1);
                //没有添加过该文件夹对象则将该对象添加到头部
                if(!folderPaths.contains(folderName)) {
                    item.setFolder(true);
                    item.setName(folderName);
                    item.setFilePath(parentPath);
                    temp.add(item);
                    folderPaths.add(folderName);
                }
            } else if(dataType == ConstantsUtils.ListType.TWO_CLASS_FOLDER_LEVEL2_TYPE) {
                String filePath = item.getFilePath();
                File itemFile = new File(filePath);
                String parentPath = itemFile.getParent();
                if(fileDir.equals(parentPath)) {
                    temp.add(item);
                }
            }
        }
        LogUtils.i("==queryNew fileDir end size ==" + temp.size());
        if(c != null) {
            c.close();
        }
        return temp;
    }

    private static MediaItemInfo queryMediaItem(Context context,Uri uri,String[] args) {
        String where = toWhere(args);
        where = where + "and 0=0) group by (" + MediaTableCols.FILE_PATH; //使用group by进行分组，也能达到去掉重复ID的效果
        Cursor c = context.getContentResolver().query(uri,null,where,null,ORDER);
        while(c != null && c.moveToNext()) {
            MediaItemInfo temp = ProviderHelper.cursorToMediaItem(c);
            if(c != null) {
                c.close();
            }
            return temp;
        }
        if(c != null) {
            c.close();
        }
        return null;
    }

    private static List<MediaData> query(Context context,int dataType,Uri uri,String[] args) {
        String where = toWhere(args);
        where = where + "and 0=0) group by (" + MediaTableCols.FILE_PATH;//使用group by进行分组，也能达到去掉重复ID的效果
        List<MediaData> temp = new ArrayList<>();
        Cursor c = context.getContentResolver().query(uri,null,where,null,ORDER);
        LogUtils.i("query " + where);
        while(c != null && c.moveToNext()) {
            MediaData mediaData = ProviderHelper.cursorToMediaItemNew(c,dataType);
            //            LogUtils.i("query " + dataType + " name = " + mediaData.getFilePath());
            temp.add(mediaData);
        }
        LogUtils.i("query " + dataType + " size = " + temp.size());
        if(c != null) {
            c.close();
        }
        return temp;
    }

    private static List<MediaData> queryCollectedItem(Context context,Uri uri,String[] args) {
        String where = toWhere(args);
        where = where + " and " + MediaTableCols.IS_COLLECTED + " =1 " + " ";
        where = where + "and 0=0) group by (" + MediaTableCols.FILE_PATH; //使用group by进行分组，也能达到去掉重复ID的效果
        List<MediaData> temp = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(uri,null,where,null,ORDER);
        LogUtils.i("queryCollectedItem = " + where);
        while(cursor != null && cursor.moveToNext()) {
            MediaData mediaData = cursorToMediaItemNew(cursor,ConstantsUtils.ListType.COLLECTION_TYPE);
            //            LogUtils.i("queryCollectedItem = " + mediaData.getFilePath());
            temp.add(mediaData);
        }
        LogUtils.i("queryCollectedItem = " + temp.size());
        if(cursor != null) {
            cursor.close();
        }
        return temp;
    }

    /**
     * 查询某个字段中，不重复的所有数据
     * @param context
     * @param uri
     * @param args
     * @param columnName：列名
     * @return
     */
    private static List<MediaData> queryColumnContent(Context context,int dataType,Uri uri,String[] args,String columnName) {
        String where = toWhere(args);
        where = where + "and 0=0) group by (" + columnName; //使用group by进行分组，也能达到去掉重复ID的效果
        String[] projection = new String[]{columnName};
        List<MediaData> temp = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(uri,projection,where,null,ORDER);
        while(cursor != null && cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(columnName));
            //            LogUtils.i("queryColumnContent " + columnName + " = " + name);
            if(TextUtils.isEmpty(name)) {
                continue;
            }
            MediaData mediaData = new MediaData();
            mediaData.setDataType(dataType);
            mediaData.setName(name);
            mediaData.setFolder(true);
            temp.add(mediaData);
        }
        LogUtils.i("queryColumnContent " + columnName + " , " + dataType + " size = " + temp.size());
        if(cursor != null) {
            cursor.close();
        }
        return temp;
    }

    /**
     * 获取某个字段中，不重复的所有数据
     * @param context
     * @param columnName：列名
     * @return
     */
    public static List<MediaData> getMusicColumnContent(Context context,int dataType,Uri uri,String columnName,String folderPath) {
        if(TextUtils.isEmpty(folderPath)) {
            return queryColumnContent(context,dataType,uri,new String[]{WHERE_DF},columnName);
        } else {
            folderPath = folderPath.replaceAll("'","''");
            return queryColumnContent(context,dataType,uri,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + folderPath + "%'" + " "},columnName);
        }
    }

    /**
     * 获取收藏的对象
     * @param context
     * @param folderPath
     * @return
     */
    public static List<MediaData> getImageCollectedItem(Context context,String folderPath) {
        if(TextUtils.isEmpty(folderPath)) {
            return queryCollectedItem(context,ProviderHelper.IMAGE_CONTENT_URI,new String[]{WHERE_DF});
        } else {
            folderPath = folderPath.replaceAll("'","''");
            return queryCollectedItem(context,ProviderHelper.IMAGE_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + folderPath + "%'" + " "});
        }
    }

    /**
     * 获取音乐频收藏列表数据
     * @param context
     * @param folderPath 当前查询路径，未NULL则查询全部数据
     * @return
     */
    public static List<MediaData> getMusicCollectedItem(Context context,String folderPath) {
        if(TextUtils.isEmpty(folderPath)) {
            return queryCollectedItem(context,ProviderHelper.AUDIO_CONTENT_URI,new String[]{WHERE_DF});
        } else {
            folderPath = folderPath.replaceAll("'","''");
            return queryCollectedItem(context,ProviderHelper.AUDIO_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + folderPath + "%'" + " "});
        }
    }

    public static List<MediaData> getCollectedItem(Context context,String folderPath,Uri uri) {
        if(TextUtils.isEmpty(folderPath)) {
            return queryCollectedItem(context,uri,new String[]{WHERE_DF});
        } else {
            folderPath = folderPath.replaceAll("'","''");
            return queryCollectedItem(context,uri,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + folderPath + "%'" + " "});
        }
    }

    /**
     * 获取视频收藏列表数据
     * @param context
     * @param folderPath 当前查询路径，未NULL则查询全部数据
     * @return
     */
    public static List<MediaData> getVideoCollectedItem(Context context,String folderPath) {
        if(TextUtils.isEmpty(folderPath)) {
            return queryCollectedItem(context,ProviderHelper.VIDEO_CONTENT_URI,new String[]{WHERE_DF});
        } else {
            folderPath = folderPath.replaceAll("'","''");
            return queryCollectedItem(context,ProviderHelper.VIDEO_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + folderPath + "%'" + " "});
        }
    }

    /**
     * 取得特定路径的对象
     **/
    public static MediaItemInfo getImageMediaItemByFilePath(Context context,String path) {
        if(TextUtils.isEmpty(path)) {
            return null;
        }
        path = path.replaceAll("'","''");
        return queryMediaItem(context,ProviderHelper.IMAGE_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + path + "%'" + " "});
    }

    public static MediaItemInfo getMusicMediaItemByFilePath(Context context,String path) {
        if(TextUtils.isEmpty(path)) {
            return null;
        }
        path = path.replaceAll("'","''");
        return queryMediaItem(context,ProviderHelper.AUDIO_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + path + "%'" + " "});
    }

    public static MediaItemInfo getVideoMediaItemByFilePath(Context context,String path) {
        if(TextUtils.isEmpty(path)) {
            return null;
        }
        path = path.replaceAll("'","''");
        return queryMediaItem(context,ProviderHelper.VIDEO_CONTENT_URI,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + path + "%'" + " "});
    }

    /**
     * 获取与某个字段相符的所有行
     * @param context
     * @param columnName：列名
     * @param columnContentStr：需要查询的字段
     * @return
     */
    public static List<MediaData> getCertainColumnContentList(Context context,int dataType,Uri uri,String columnName,String columnContentStr,String filePath) {
        if(TextUtils.isEmpty(columnContentStr)) {
            return null;
        }
        columnContentStr = columnContentStr.replaceAll("'","''");
        if(TextUtils.isEmpty(filePath)) {
            return query(context,dataType,uri,new String[]{WHERE_DF," " + columnName + " LIKE " + "'%" + columnContentStr + "%'"});
        } else {
            return query(context,dataType,uri,new String[]{WHERE_DF," " + columnName + " LIKE " + "'%" + columnContentStr + "%'"," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + filePath + "%'" + " "});
        }
    }

    private static String toWhere(String[] args) {
        String where = null;
        if(args != null) {
            where = new String();
            for(int i = 0; i < args.length; i++) {
                where = where + args[i];
                if(i != args.length - 1) {
                    where = where + "  AND   ";
                }
            }
        }
        try {
            where = new String(where.getBytes(),"UTF-8");
            Log.i("ProviderHelper","=toWhere====where====" + where);
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return where;
    }

    /**
     * 获取播放列表
     * @param context
     * @param uri
     * @param dataType
     * @return
     */
    public static List<MediaData> getMediaPlayList(Context context,Uri uri,int dataType) {
        return query(context,dataType,uri,new String[]{WHERE_DF,WHERE_PL});
    }

    /**
     * 取得内置SD卡中的媒体数据
     * @param context
     * @param uri
     * @param dataType
     * @return
     */
    public static List<MediaData> getMediaListFromISD(Context context,Uri uri,int dataType) {
        return query(context,dataType,uri,new String[]{WHERE_DF,WHERE_PH_ISD});
    }

    /**
     * 取得外置SD卡中的媒体数据
     * @param context
     * @param uri
     * @param dataType
     * @return
     */
    public static List<MediaData> getMediaListFromOSD(Context context,Uri uri,int dataType) {
        return query(context,dataType,uri,new String[]{WHERE_DF,WHERE_PH_OSD});
    }

    /**
     * 取得U盘的
     */
    public static List<MediaData> getAudioAllFromDB(Context context,Uri uri,String path,int dataType) {
        path = path.replaceAll("'","''");
        return queryNew(context,dataType,uri,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + path + "%'" + " "});
    }

    /**
     * 取得U盘的
     **/
    public static List<MediaData> getMediaFolderDataFromDB(Context context,Uri uri,String path,int dataType) {
        path = path.replaceAll("'","''");
        return queryNew(context,dataType,path,uri,new String[]{WHERE_DF," " + MediaTableCols.FILE_PATH + " LIKE " + "'%" + path + "%'" + " "});
    }

    /**
     * 取得所有音频文件
     */
    public static Cursor getAllAudioCursor(Context context) {
        return context.getContentResolver().query(ProviderHelper.AUDIO_CONTENT_URI,null,WHERE_DF,null,null);
    }

    /**
     * 从列表中删除
     * @param context
     * @param mediaItemInfo mediaItemInfo.getFilePath()必须为有效
     * @return
     * @throws Exception
     */
    public static int delete(Context context,MediaItemInfo mediaItemInfo) throws Exception {
        if(context == null || mediaItemInfo == null || mediaItemInfo.getFilePath() == null || mediaItemInfo.getFilePath() == "") {
            throw new Exception("delete ERR! 参数不正确！");
        }
        switch(mediaItemInfo.getMediaType()) {
            case AUDIO:
                Log.d("tt","AUDIO delete");
                ContentValues values = mediaItemToContentValues(mediaItemInfo);
                values.put("action","del");
                mediaItemInfo.setPlaylistIndex(0);
                return context.getContentResolver().update(ProviderHelper.AUDIO_CONTENT_URI,values," where 1 = 1 ",null);
            default:
                break;
        }
        return -1;
    }

    /**
     * 将音乐添加到播放列表（收藏列表）
     * @param context
     * @param mediaItemInfo
     * @return 如果添加成功则返回原对象 否则 返回空
     * @throws Exception
     */
    public static MediaItemInfo addToPlayList(Context context,MediaItemInfo mediaItemInfo) throws Exception {
        if(context == null || mediaItemInfo == null || mediaItemInfo.getFilePath() == null || mediaItemInfo.getFilePath() == "") {
            throw new Exception("delete ERR! 参数不正确！");
        }
        int resualt = -1;
        switch(mediaItemInfo.getMediaType()) {
            case AUDIO:
                Log.d("tt","AUDIO addToPlayList");
                ContentValues values = mediaItemToContentValues(mediaItemInfo);
                values.put("action","addPlayList");
                resualt = context.getContentResolver().update(ProviderHelper.AUDIO_CONTENT_URI,values," where " + WHERE_DF,null);
                Log.d("test","AUDIO addToPlayList resualt " + resualt);
            default:
                break;
        }
        if(resualt < 0) {
            return null;
        } else {
            mediaItemInfo.setPlaylistIndex(resualt);
            return mediaItemInfo;
        }
    }

    /**
     * 暂时不用该方法
     * @param context 只能更新多媒体的状态
     * @param mediaItemInfo
     * @throws Exception
     */
    public static void update(Context context,MediaItemInfo mediaItemInfo) throws Exception {
        if(context == null || mediaItemInfo == null || mediaItemInfo.getMediaType() == null || mediaItemInfo.getMediaType().getId() == -1 || mediaItemInfo.getID() == -1 || mediaItemInfo.getParseStatus() == null || mediaItemInfo.getParseStatus().value() == -1) {
            throw new Exception("update ERR! 参数不正确！");
        }
        ContentValues values = mediaItemToContentValues(mediaItemInfo);
        context.getContentResolver().update(ProviderHelper.AUDIO_CONTENT_URI,values,null,null);
    }

    /**
     * 将媒体对象内容转存到ContentValues对象中
     * @param mediaItemInfo 媒体对象
     * @return 媒体对象全字段对应的ContentValues对象
     */
    private static ContentValues mediaItemToContentValues(MediaItemInfo mediaItemInfo) {
        ContentValues values = new ContentValues();
        values.put(MediaTableCols.MEDIA_ID,mediaItemInfo.getMediaID());
        values.put(MediaTableCols.ALBUM_ID,mediaItemInfo.getAlbumID());
        values.put(MediaTableCols.MEDIA_TYPE,mediaItemInfo.getMediaType().getId());
        values.put(MediaTableCols.NAME,mediaItemInfo.getName());
        values.put(MediaTableCols.TITLE,mediaItemInfo.getTitle());
        values.put(MediaTableCols.ALBUM,mediaItemInfo.getAlbum());
        values.put(MediaTableCols.ARTIST,mediaItemInfo.getArtist());
        values.put(MediaTableCols.FILE_PATH,mediaItemInfo.getFilePath());
        values.put(MediaTableCols.SIZE,mediaItemInfo.getSize());
        values.put(MediaTableCols.LAST_MODIFIED,mediaItemInfo.getLastModified());
        values.put(MediaTableCols.DURATION,mediaItemInfo.getDuration());
        values.put(MediaTableCols.POSITION,mediaItemInfo.getPosition());
        values.put(MediaTableCols.IS_PLAY_ITEM,mediaItemInfo.isPlayItem() ? 1 :0);
        values.put(MediaTableCols.PLAYLIST_INDEX,mediaItemInfo.getPlaylistIndex());
        values.put(MediaTableCols.PARSE_STATUS,mediaItemInfo.getParseStatus().value());
        values.put(MediaTableCols.UPDATE_TIME,mediaItemInfo.getUpdateTime());
        values.put(MediaTableCols.SCAN_INDEX,mediaItemInfo.getScanIndex());
        values.put(MediaTableCols.DELETE_FLAG,mediaItemInfo.isDelete() ? 1 :0);
        return values;
    }

    /**
     * 取得SQL文中LIMIT语句对应的内容
     * @param startIndex 开始位置
     * @param count 取得数量，小于0时LIMIT语句返回null
     * @return LIMIT语句中的内容
     */
    private String getLimitSQL(int startIndex,int count) {
        String limit = null;
        if(startIndex >= 0 && count > 0) {
            limit = startIndex + ", " + count;
        }
        return limit;
    }

    public static class MediaTableCols {

        /* 自增字段 */
        public static final String ID = "_id";
        /* 系统媒体库中媒体ID */
        public static final String MEDIA_ID = "mediaID";
        /* 系统媒体库中专辑ID */
        public static final String ALBUM_ID = "albumID";

        public static final String MEDIA_TYPE = "mediaType";

        public static final String NAME = "name";
        public static final String NAME_ALPHABET = "nameAlphabet";

        public static final String TITLE = "title";

        public static final String ALBUM = "album";

        public static final String ARTIST = "artist";

        public static final String FILE_PATH = "filePath";

        public static final String SIZE = "size";

        public static final String LAST_MODIFIED = "lastModified";

        public static final String DURATION = "duration";

        public static final String POSITION = "position";

        public static final String IS_PLAY_ITEM = "isPlayItem";

        public static final String PLAYLIST_INDEX = "playlistIndex";

        public static final String PARSE_STATUS = "parseStatus";

        public static final String UPDATE_TIME = "updateTime";

        public static final String SCAN_INDEX = "scanIndex";

        public static final String DELETE_FLAG = "deleteFlag";

        public static final String IS_COLLECTED = "isCollected";

    }
}
