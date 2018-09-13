package com.cookoo.videosdk.manager;

import android.annotation.SuppressLint;


/**
 *
 * @author lsf
 * @date 2018/3/14
 */

public class SharedPreferenceManager {

    private SharedPreferenceManager() {
    }

    public static SharedPreferenceManager getInstance() {
        return MusicManagerInstance.SPF_MANAGER;
    }

    private static class MusicManagerInstance {
        private static final SharedPreferenceManager SPF_MANAGER = new SharedPreferenceManager();
    }

    @SuppressLint("WrongConstant")
    public void init (){
    }

}
