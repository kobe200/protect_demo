package com.cookoo.videosdk.manager;

import android.app.Application;
import android.content.Context;

import com.cookoo.videosdk.modle.VideoInitParams;
import com.cookoo.videosdk.utils.CacheDataUtil;
import com.cookoo.videosdk.utils.GlobalTool;
import com.cookoo.videosdk.utils.LogUtils;


/**
 * Created by lsf on 2018/4/13.
 * @author
 */
public class CookooVideoConfiguration {
    private static final String TAG = "CookooVideoConfiguration";

    private CookooVideoConfiguration() {

    }
    private static CookooVideoConfiguration instance = new CookooVideoConfiguration();

    public static CookooVideoConfiguration getInstance() {
        return instance;
    }

    private Context context;
    private VideoInitParams param;
    public void initVideoConfiguration(Context context, VideoInitParams params) {
        if (context == null || params == null) {
            throw new NullPointerException(
                    "Context or VideoInitParams  can not be null,make sure you have init correctly");
        }
        if (!(context instanceof Application)) {
            throw new RuntimeException("context must be an Application Context");
        }
        this.context = context;
        this.param = params;
        GlobalTool.getInstance().setContext(context);
        CacheDataUtil.getInstance().init(context);
        VideoManager.getInstance().init();
        LogUtils.print(TAG,"------>>> initVideoConfiguration() ");
    }

    public VideoInitParams getParam() {
        return param;
    }

    public void setParam(VideoInitParams param) {
        this.param = param;
    }
}
