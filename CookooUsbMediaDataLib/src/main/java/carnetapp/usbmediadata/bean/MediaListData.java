package carnetapp.usbmediadata.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * @author kobe
 */
public class MediaListData implements Parcelable, Serializable {

    private static final long serialVersionUID = -1098339961306327435L;
    /**
     * 预留
     */
    private String key;
    /**
     * U盘路径：表示哪个U盘
     **/
    private String usbRootPath;
    /**
     * 数据类型：参考carnetapp.usbmediadata.utils.ConstantsUtils.ListType
     **/
    private int dataType;
    /**
     * U盘下指定数据
     */
    private List<MediaData> data;

    public MediaListData() {
        super();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsbRootPath() {
        return usbRootPath;
    }

    public void setUsbRootPath(String usbRootPath) {
        this.usbRootPath = usbRootPath;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public List<MediaData> getData() {
        return data;
    }

    public void setData(List<MediaData> data) {
        this.data = data;
    }

    public static final Creator<MediaListData> CREATOR = new Creator<MediaListData>() {

        @Override
        public MediaListData createFromParcel(Parcel source) {
            MediaListData mediaListData = new MediaListData();
            mediaListData.setKey(source.readString());
            mediaListData.setUsbRootPath(source.readString());
            mediaListData.setDataType(source.readInt());
            mediaListData.setData(source.readArrayList(MediaData.class.getClassLoader()));
            return mediaListData;
        }

        @Override
        public MediaListData[] newArray(int size) {
            return new MediaListData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel,int i) {
        parcel.writeString(key);
        parcel.writeString(usbRootPath);
        parcel.writeInt(dataType);
        parcel.writeList(data);
    }
}
