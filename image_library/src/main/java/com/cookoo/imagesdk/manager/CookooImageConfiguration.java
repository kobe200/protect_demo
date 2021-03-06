package com.cookoo.imagesdk.manager;

import android.app.Application;
import android.content.Context;

import com.cookoo.imagesdk.mode.ImageInitParams;
import com.cookoo.imagesdk.utils.GlobalTool;
import com.cookoo.imagesdk.utils.LogUtils;


/**
 * Created by lsf on 2018/4/13.
 * @author
 */
public class CookooImageConfiguration {
    private static final String TAG = "CookooImageConfiguration";

    private CookooImageConfiguration() {

    }
    private static CookooImageConfiguration instance = new CookooImageConfiguration();

    public static CookooImageConfiguration getInstance() {
        return instance;
    }

    private Context context;
    private ImageInitParams param;
    public void initImageConfiguration(Context context, ImageInitParams params) {
        if (context == null || params == null) {
            throw new NullPointerException(
                    "Context or ImageInitParams  can not be null,make sure you have init correctly");
        }
        if (!(context instanceof Application)) {
            throw new RuntimeException("context must be an Application Context");
        }
        this.context = context;
        this.param = params;
        GlobalTool.getInstance().setContext(context);
        ImageManager.getInstance().init();
        LogUtils.print(TAG,"------>>> initImageConfiguration() ");
    }

    public ImageInitParams getParam() {
        return param;
    }

    public void setParam(ImageInitParams param) {
        this.param = param;
    }
}
