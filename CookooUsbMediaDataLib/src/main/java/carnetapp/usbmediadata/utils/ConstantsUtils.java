package carnetapp.usbmediadata.utils;


public class ConstantsUtils {

    public static final String USB_BIND_ACTION = "carnetos.usbservice.AIDL_SERVICE";
    public static final int MEDIA_SCAN_FINISHED = 101;
    public static final int MEDIA_METADATA_RETRIEVED = 102;
    public static final int MEDIA_USB_MOUNTED = 103;
    public static final int MEDIA_USB_DISMOUNTED = 104;

    public interface FileType {
        int MUSIC = 1;
        int VIDEO = 2;
        int IMAGE = 3;
        int OTHER = 4;
    }

    /**
     * 数据类型
     **/
    public interface ListType {
        /**
         * 全部数据
         **/
        int ALL_TYPE = 1;
        /**
         * 多级文件夹数据
         **/
        int TREE_FOLDER_TYPE = 2;
        /**
         * 两级文件夹根目录数据
         **/
        int TWO_CLASS_FOLDER_LEVEL1_TYPE = 3;
        /**
         * 两级文件夹数据
         **/
        int TWO_CLASS_FOLDER_LEVEL2_TYPE = 4;
        /**
         * 收藏数据
         **/
        int COLLECTION_TYPE = 5;
        /**
         * 专辑根目录数据
         **/
        int ALBUM_LEVEL1_TYPE = 6;
        /**
         * 专辑数据
         **/
        int ALBUM_LEVEL2_TYPE = 7;
        /**
         * 艺术家根目录数据
         **/
        int AUTHOR_LEVEL1_TYPE = 8;
        /**
         * 艺术家数据
         **/
        int AUTHOR_LEVEL2_TYPE = 9;
        /**
         * 本地sdcard数据
         **/
        int SDCARD_INNER_TYPE = 10;
        /**
         * 外置sdcard数据
         **/
        int SDCARD_OUT_TYPE = 11;
    }

}
