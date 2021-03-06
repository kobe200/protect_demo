package com.cookoo.videosdk.load;

/**
 * Created by lsf on 2018/3/30.
 */

import java.lang.ref.SoftReference;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * 图片缓存
 * @author lsf
 */
public class ImageCache extends LruCache<String, Bitmap> {
    private static final String TAG = "ImageCache";
    private Map<String, SoftReference<Bitmap>> cacheMap;

    public ImageCache(Map<String, SoftReference<Bitmap>> cacheMap) {
        super((int) (Runtime.getRuntime ().maxMemory() / 3));
        int cacheSize = (int) (Runtime.getRuntime ().maxMemory() / 3);
        Log.i(TAG," ImageCache() cacheSize: "+cacheSize);
        this.cacheMap = cacheMap;
    }

    /**
     * 获取图片大小
     * @param key
     * @param value
     * @return
     */
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    /**
     * 当有图片从LruCache中移除时，将其放进软引用集合中
     * @param evicted
     * @param key
     * @param oldValue
     * @param newValue
     */
    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        Log.i(TAG," entryRemoved() oldValue: "+oldValue+" key: "+key);
        if (oldValue != null) {
            SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(oldValue);
            cacheMap.put(key, softReference);
        }
    }

    public Map<String, SoftReference<Bitmap>> getCacheMap() {
        return cacheMap;
    }
}