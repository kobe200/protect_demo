//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package carnetapp.usbmediadata.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UsbDevice implements Parcelable {
	private boolean isMount = false;
	private boolean isScanFinished = false;
	private String rootPath = null;
	public static final Creator<UsbDevice> CREATOR = new Creator<UsbDevice>() {
		@Override
		public UsbDevice createFromParcel(Parcel in) {
			return new UsbDevice(in);
		}

		@Override
		public UsbDevice[] newArray(int size) {
			return new UsbDevice[size];
		}
	};

	public UsbDevice() {
	}

	public UsbDevice(String rootPath, boolean isMount) {
		this.rootPath = rootPath;
		this.isMount = isMount;
	}

	protected UsbDevice(Parcel in) {
		readFromParcel(in);
	}

	public boolean isMount() {
		return this.isMount;
	}

	public void setMount(boolean isMount) {
		this.isMount = isMount;
	}

	public String getRootPath() {
		return this.rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public boolean isScanFinished() {
		return this.isScanFinished;
	}

	public void setScanFinished(boolean scanFinished) {
		this.isScanFinished = scanFinished;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(this.rootPath);
		parcel.writeByte((byte) (isMount ? 1 : 0));
		parcel.writeByte((byte) (isScanFinished ? 1 : 0));
	}

	public void readFromParcel(Parcel source) {
		this.rootPath = source.readString();
		this.isMount = source.readByte() == 1;
		this.isScanFinished = source.readByte() == 1;
	}
}
