package com.cookoo.imagesdk.manager;

import android.content.SharedPreferences;

/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public class SharedPreferenceManager {

    private SharedPreferenceManager() {
    }

    public static SharedPreferenceManager getInstance() {
        return SharedPreferenceManager.MusicManagerInstance.SPF_MANAGER;
    }

    private static class MusicManagerInstance {
        private static final SharedPreferenceManager SPF_MANAGER = new SharedPreferenceManager();
    }

    private SharedPreferences spf;
    public void init (){
    }

}
