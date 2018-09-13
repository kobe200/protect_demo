package com.cookoo.imagesdk.load;

/**
 * Created by lsf on 2018/3/30.
 */

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.manager.CookooImageConfiguration;
import com.cookoo.imagesdk.manager.ImageManager;
import com.cookoo.imagesdk.utils.GlobalTool;
import com.cookoo.imagesdk.utils.LogUtils;
import com.cookoo.imagesdk.utils.ScreenUtil;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * 缓存工具类
 * @author lsf
 */
public class CacheUtil {
    private static final String TAG = "CacheUtil";
    private ImageCache imageCache;
    private int screenWidth;
    private int screenHeight;
    private Handler childHandler;
    private HandlerThread handlerThread;
    private CopyOnWriteArrayList<Task> taskQueue;
    /**
     * 正在异步解析的路径
     */
    private String currentPath = "";

    private static CacheUtil cacheUtil;

    public static CacheUtil getInstance(){
        if (cacheUtil == null){
            cacheUtil = new CacheUtil();
        }
        return cacheUtil;
    }

    private CacheUtil(){
        screenWidth = ScreenUtil.getResolution(GlobalTool.getInstance().getContext()).first;
        screenHeight = ScreenUtil.getResolution(GlobalTool.getInstance().getContext()).second;
        Map<String, SoftReference<Bitmap>> cacheMap = new HashMap<>();
        // Build.VERSION.SDK_INT:19,  Build.VERSION_CODES.HONEYCOMB_MR1:12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.imageCache = new ImageCache(cacheMap);
        }
        taskQueue = new CopyOnWriteArrayList<>();
        handlerThread = new HandlerThread("downloadImage");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper(),new ChildCallback());
    }

    /**
     * 将图片添加到缓存中
     */
    public void putBitmapIntoCache(String filePath ,Bitmap bitmap) {
        // 将图片存入强引用（LruCache）
        imageCache.put(filePath, bitmap);
    }

    /**
     * 从缓存中取出图片
     */
    private Bitmap getBitmapFromLruCache(String fileName) {
        // 从强引用（LruCache）中取出图片
        LogUtils.print(TAG,"getBitmapFromLruCache() fileName: "+fileName);
        Bitmap bm = null;
        // SDK版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bm = imageCache.get(fileName);
            LogUtils.print(TAG," ===111===getBitmapFromLruCache() bm: "+bm);
            if (bm == null) {
                // 如果图片不存在强引用中，则去软引用（SoftReference）中查找
                Map<String, SoftReference<Bitmap>> cacheMap = imageCache.getCacheMap();
                SoftReference<Bitmap> softReference = cacheMap.get(fileName);
                if (softReference != null) {
                    bm = softReference.get();
                    if (bm != null){
                        imageCache.put(fileName, bm);
                        return bm;
                    }
                }
                LogUtils.print(TAG," ===222===getBitmapFromLruCache() bm: "+bm);
            }
        }
        return bm;
    }

    private Bitmap getBitmapFromDiskCache(String fileName){
        Bitmap bm = null;
        byte[] data = FileUtil.getInstance(GlobalTool.getInstance().getContext()).readBytesFromStorage(fileName);
        LogUtils.print(TAG," ===333===getBitmapFromLruCache() data: "+data);
        if (data != null && data.length > 0) {
            try {
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (OutOfMemoryError e) {
                bm = ParseBitmapUtils.byteToBitmap(data);
                Log.i(TAG, "====OutOfMemoryError=ee==");
            }
            LogUtils.print(TAG," ===444===getBitmapFromLruCache() bm: "+bm);
            imageCache.put(fileName, bm);
        }
        return bm;
    }

    /**
     * 使用三级缓存为ImageView设置图片
     */
    public void loadBitmapToView(final String filePath, ImageView imageView, ImageCallback imageCallback) {
        LogUtils.print(TAG,"  loadBitmapToView() path: "+filePath+" imageView: "+imageView);
        if (imageView == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        Bitmap bm = getBitmapFromLruCache(filePath);
        LogUtils.print(TAG,"  loadBitmapToView() bm: "+bm);
        if (bm != null) {
            imageView.setImageBitmap(bm);
            imageCallback.imageLoaded(filePath, ImageSdkConstants.ImageParseState.PICTURE_PARSE_SUCCESS);
        } else {
            loadBitmap(imageView,filePath,imageCallback);
        }
    }

    public void loadBitmap(ImageView imageView, String imagePath, ImageCallback imageCallback){
        LogUtils.print(TAG, " loadBitmap() imagePath: "+imagePath +" currentPath: "+currentPath);
        if (currentPath.equals(imagePath)) {
            return;
        }
        Task task = new Task();
        task.path = imagePath;
        task.imageView = imageView;
        task.callback = imageCallback;
        Message msg = Message.obtain();
        msg.obj = task;
        msg.what= MSG_ADD_TASK;
        if (taskQueue != null){
            if (taskQueue.size() > CookooImageConfiguration.getInstance().getParam().getLoadCount()){
                mHandler.removeMessages(MSG_ADD_TASK);
            }
        }
        mHandler.sendMessage(msg);
    }

    private static final int CALLBACK_MSG = 0x01;
    class ChildCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message message) {
            LogUtils.print(TAG," handleMessage() isAllUsbUnMount: "+ImageManager.getInstance().isAllUsbUnMount() +" size: "+taskQueue.size());
            if (ImageManager.getInstance().isAllUsbUnMount() || taskQueue == null || taskQueue.size() <= 0){
                return false;
            }
            LogUtils.print(TAG,"----->> ChildCallback taskQueue.size(): "+taskQueue.size());
            Task task = taskQueue.remove(0);
            currentPath = task.path;
            task.parseState = ImageSdkConstants.ImageParseState.PICTURE_PARSING;
            Bitmap bitmap = getBitmapFromDiskCache(task.path);
            if (bitmap == null){
                task = ParseBitmapUtils.downloadBitmapByPath(task,screenWidth,screenHeight);
            }else {
                task.parseState = ImageSdkConstants.ImageParseState.PICTURE_PARSE_SUCCESS;
                task.bitmap = bitmap;
            }
            Message msg = Message.obtain();
            msg.obj = task;
            msg.what=MSG_CALLBACK;
            mHandler.sendMessage(msg);
            return false;
        }
    }

    private static final byte MSG_CALLBACK = 0x01;
    private static final byte MSG_ADD_TASK = MSG_CALLBACK+1;
    private static final byte MSG_NOTIFY = MSG_CALLBACK+2;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch(message.what){
                case MSG_CALLBACK:
                    Task task = (Task) message.obj;
                    LogUtils.print(TAG, "===handleMessage=MSG_CALLBACK===isAllUsbUnMount=="+!ImageManager.getInstance().isAllUsbUnMount());
                    if (task.callback != null && !ImageManager.getInstance().isAllUsbUnMount()) {
                        LogUtils.print(TAG, "===handleMessage=MSG_CALLBACK===bitmap: "+task.bitmap);
                        if (task.bitmap != null){
                            task.imageView.setImageBitmap(task.bitmap);
                        }
                        task.callback.imageLoaded(task.path,task.parseState);
                    }
                    currentPath = "";
                    mHandler.removeMessages(MSG_NOTIFY);
                    message.what = MSG_NOTIFY;
                    mHandler.sendEmptyMessageDelayed(MSG_NOTIFY, 300);
                    break;

                case MSG_ADD_TASK:
                    Task temp =(Task) message.obj;
                    LogUtils.print(TAG, "=11==handleMessage=MSG_ADD_TASK===temp: "+temp+"  taskQueue: "+taskQueue);
                    if(temp ==null) {
                        return;
                    }
                    if (taskQueue != null && !taskQueue.contains(temp)) {
                        if (taskQueue.size() > CookooImageConfiguration.getInstance().getParam().getLoadCount()) {
                            taskQueue.remove(taskQueue.size() - 1);
                        }
                        synchronized (taskQueue){
                            taskQueue.add(0, temp);
                        }
                        mHandler.sendEmptyMessage(MSG_NOTIFY);
                    }
                    break;
                case MSG_NOTIFY:
                    LogUtils.print(TAG, "===handleMessage=MSG_NOTIFY======");
                    //通知在wait中的thread
                    childHandler.removeMessages(CALLBACK_MSG);
                    childHandler.sendEmptyMessage(CALLBACK_MSG);
                    break;
                default:
            }

        }
    };

    public interface ImageCallback {
        /**
         * 图片加载完成后回调方法
         * @param imagePath
         * @param parseState
         */
        void imageLoaded(String imagePath, int parseState);
    }

    /**
     * 在退出图片界面只是把加载的任务移除，图片缓存不需要移除
     */
    public void cancelLoadTask(){
        handlerThread.quit();
        cacheUtil = null;
    }


}
