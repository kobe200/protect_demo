package carnetapp.usbmediadata.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author kobe
 */
public class MediaData implements Parcelable {
    public static final Creator<MediaData> CREATOR = new Creator<MediaData>() {

        @Override
        public MediaData createFromParcel(Parcel source) {
            return new MediaData(source);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };
    private static final long INVALID = -1;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * 专辑
     */
    private String album;
    /**
     * 艺术家
     */
    private String artist;
    /**
     * 文件所在目录
     */
    private String filePath;
    /**
     * 文件类型：分为搜索、非搜索类型，具体参考 FileType
     **/
    private String fileType;
    /**
     * 数据类型：参考com.cookoo.musicsdk.MusicSdkConstants.ListType
     **/
    private int dataType;
    /**
     * 是否文件夹
     */
    private boolean isFolder;
    /**
     * 表示该文件是否已经被收藏
     */
    private boolean isCollected;
    /**
     * 媒体文件大小
     */
    private long mSize;
    /**
     * 时长
     */
    private long mDuration = INVALID;

    public MediaData() {
        super();
    }

    private MediaData(Parcel source) {
        readFromParcel(source);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean isSearch() {
        return FileType.SEARCH.equals(fileType);
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }

    public long getmSize() {
        return mSize;
    }

    public void setmSize(long mSize) {
        this.mSize = mSize;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    @Override
    public MediaData clone() {
        try {
            return (MediaData) super.clone();
        } catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p,int flags) {
        p.writeString(name);
        p.writeString(title);
        p.writeString(album);
        p.writeString(artist);
        p.writeString(filePath);
        p.writeString(fileType);
        p.writeInt(dataType);
        p.writeByte((byte) (isFolder ? 1 :0));
        p.writeByte((byte) (isCollected ? 1 :0));
        p.writeLong(mSize);
        p.writeLong(mDuration);
    }

    public void readFromParcel(Parcel source) {
        name = source.readString();
        title = source.readString();
        album = source.readString();
        artist = source.readString();
        filePath = source.readString();
        fileType = source.readString();
        dataType = source.readInt();
        isFolder = source.readByte() == 1;
        isCollected = source.readByte() == 1;
        mSize = source.readLong();
        mDuration = source.readLong();
    }

    public static class FileType {
        public static final String SEARCH = "SEARCH";
    }
}
