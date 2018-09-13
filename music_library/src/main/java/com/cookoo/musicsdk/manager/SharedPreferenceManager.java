package com.cookoo.musicsdk.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.cookoo.musicsdk.constants.MusicSdkConstants;
import com.cookoo.musicsdk.utils.GlobalTool;


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

    public static final String MUSIC_PLAY_MODE = "music_play_mode";
    private SharedPreferences spf;
    @SuppressLint("WrongConstant")
    public void init (){
        spf = GlobalTool.getInstance().getContext().getSharedPreferences(MUSIC_PLAY_MODE, Context.MODE_PRIVATE);
    }

    public void savePlayMode(int position) {
        SharedPreferences.Editor editor = spf.edit();
        editor.putInt(MUSIC_PLAY_MODE, position);
        editor.commit();
    }

    public int getPlayMode(){
        return spf.getInt(MUSIC_PLAY_MODE, MusicSdkConstants.PlayMode.ALL_LOOP);
    }
}
