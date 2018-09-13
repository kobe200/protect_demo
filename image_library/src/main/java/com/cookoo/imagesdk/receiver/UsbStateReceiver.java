package com.cookoo.imagesdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cookoo.imagesdk.manager.ImageDataManager;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.List;


/**
 *
 * @author lsf
 * @date 2018/3/21
 */

public class UsbStateReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.print(TAG,"onReceive()  getAction: "+intent.getAction());
        if(Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())){
            ImageDataManager.getInstance().onUsbDiskMounted((List<String>) null);
        }else if(Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())){
            ImageDataManager.getInstance().onUsbDiskUnMounted(null);
        }
    }
}
