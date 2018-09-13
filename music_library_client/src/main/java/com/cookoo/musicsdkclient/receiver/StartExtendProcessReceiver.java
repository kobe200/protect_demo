package com.cookoo.musicsdkclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cookoo.musicsdkclient.utils.LogUtils;

/**
 *
 * @author lsf
 * @date 2018/4/13
 */

public class StartExtendProcessReceiver extends BroadcastReceiver {
    private static final String TAG = "StartExtendProcessReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.print(TAG,"-------->>> onReceive() getAction: "+intent.getAction());
    }
}
