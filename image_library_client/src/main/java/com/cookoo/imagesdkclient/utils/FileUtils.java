package com.cookoo.imagesdkclient.utils;

import android.text.TextUtils;

import java.io.File;

/**
 * @author: kobe
 * @date: 2018/4/18 19:52
 * @decribe:
 */

public class FileUtils {

    /**
     * 判读文件是否存在
     * @param filePath ：文件路径
     * @return
     */
    public static boolean isFileExit(String filePath) {
        if(TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }


}
