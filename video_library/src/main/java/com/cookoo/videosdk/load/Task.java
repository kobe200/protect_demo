package com.cookoo.videosdk.load;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 *
 * @author lsf
 * @date 2018/3/30
 */

public class Task {
    /**
     * 解析图片路径
     */
    String path = "";
    /**
     * 解析图片路径
     */
    ImageView imageView;
    /**
     * 通过路径解析得到的图片
     */
    Bitmap bitmap;
    /**
     * 回掉给适配器
     */
    CacheUtil.ImageCallback callback;
    /**
     * 解析图片的状态
     */
    int parseState = -1;

    @Override
    public boolean equals(Object o) {
        boolean flag;
        if (o == null) {
            return false;
        }
        try {
            flag = ((Task) o).path.equals(path);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
}
