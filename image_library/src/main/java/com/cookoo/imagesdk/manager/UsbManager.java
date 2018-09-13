package com.cookoo.imagesdk.manager;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.UsbDevice;

/**
 *
 * @author lsf
 * @date 2018/3/19
 */

public class UsbManager {

    private static UsbManager usbManager;
    private UsbManager(){

    }
    public static UsbManager getIntance(){
        if (usbManager == null){
            usbManager = new UsbManager();
        }
        return usbManager;
    }

    private List<UsbDevice> usbDeviceList;

    public List<UsbDevice> getUsbDeviceList(){
        if (usbDeviceList == null){
            usbDeviceList = new ArrayList<>();
        }
        return usbDeviceList;
    }

    public void addToDeviceList(UsbDevice usbDevice){
        getUsbDeviceList().add(usbDevice);
    }

    public void removeDevice(UsbDevice usbDevice){
        getUsbDeviceList().remove(usbDevice);
    }

}
