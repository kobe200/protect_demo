package com.cookoo.musicsdk.manager;

import android.app.Application;
import android.content.Context;

import com.cookoo.musicsdk.modle.MusicInitParams;
import com.cookoo.musicsdk.utils.CacheDataUtil;
import com.cookoo.musicsdk.utils.GlobalTool;
import com.cookoo.musicsdk.utils.LogUtils;

/**
 * Created by lsf on 2018/4/13.
 * @author
 */
public class CookooMusicConfiguration {
    private static final String TAG = "CookooMusicConfiguration";

    private CookooMusicConfiguration() {

    }
    private static CookooMusicConfiguration instance = new CookooMusicConfiguration();

    public static CookooMusicConfiguration getInstance() {
        return instance;
    }

    private Context context;
    private MusicInitParams param;
    public void initMusicConfiguration(Context context, MusicInitParams params) {
        if (context == null || params == null) {
            throw new NullPointerException(
                    "Context or MusicInitParams  can not be null,make sure you have init correctly");
        }
        if (!(context instanceof Application)) {
            throw new RuntimeException("context must be an Application Context");
        }
        this.context = context;
        this.param = params;
        GlobalTool.getInstance().setContext(context);
        CacheDataUtil.getInstance().init(context);
        MusicManager.getInstance().init();
        LogUtils.print(TAG,"------>>> initMusicConfiguration() ");
    }

    public MusicInitParams getParam() {
        return param;
    }

    public void setParam(MusicInitParams param) {
        this.param = param;
    }
}
