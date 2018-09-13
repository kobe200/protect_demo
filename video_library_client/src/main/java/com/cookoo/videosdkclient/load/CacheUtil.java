package com.cookoo.videosdkclient.load;

/**
 * Created by lsf on 2018/3/30.
 */

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.cookoo.videosdkclient.manager.VideoAidlManager;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存工具类
 * @author lsf
 */
public class CacheUtil {
    private static final String TAG = "CacheUtil";
    private ImageCache imageCache;
    private Handler childHandler;
    private HandlerThread handlerThread;
    private ArrayList<String> filePaths;
	private Bitmap defaultBitmap;
    private static CacheUtil cacheUtil;

    public static CacheUtil getInstance(){
        if (cacheUtil == null){
            cacheUtil = new CacheUtil();
        }
        return cacheUtil;
    }

    private CacheUtil(){
        Map<String, SoftReference<Bitmap>> cacheMap = new HashMap<String, SoftReference<Bitmap>>();
        // Build.VERSION.SDK_INT:19,  Build.VERSION_CODES.HONEYCOMB_MR1:12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.imageCache = new ImageCache(cacheMap);
        }
        filePaths = new ArrayList<String>();
        handlerThread = new HandlerThread("downloadImage");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper(),new ChildCallback());
    }
    
    
    public void setDefaultBitmap(Bitmap defaultBitmap){
    	this.defaultBitmap = defaultBitmap;
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
        Log.i(TAG,"getBitmapFromLruCache() fileName: "+fileName);
        Bitmap bm = null;
        // SDK版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bm = imageCache.get(fileName);
            Log.i(TAG," ===111===getBitmapFromLruCache() bm: "+bm);
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
                Log.i(TAG," ===222===getBitmapFromLruCache() bm: "+bm);
            }
        }
        return bm;
    }

    private Bitmap getBitmapFromDiskCache(String fileName){
        Bitmap bm = null;
        byte[] data = DisCacheUtil.getInstance(VideoAidlManager.getInstance().getContext()).readBytesFromStorage(fileName);
        Log.i(TAG," ===333===getBitmapFromDiskCache() data: "+data);
        if (data != null && data.length > 0) {
            try {
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (OutOfMemoryError e) {
                bm = ParseBitmapUtils.byteToBitmap(data);
                Log.i(TAG, "==getBitmapFromDiskCache==OutOfMemoryError=ee==");
            }
            Log.i(TAG," ===444===getBitmapFromDiskCache() bm: "+bm);
            imageCache.put(fileName, bm);
        }
        return bm;
    }

    /**
     * 使用三级缓存为ImageView设置图片
     */
    public void loadBitmapToView(final String filePath, ImageView imageView,ImageCallback imageCallback) {
        Log.i(TAG,"  loadBitmapToView() path: "+filePath);
        Bitmap bm = getBitmapFromLruCache(filePath);
        Log.i(TAG,"  loadBitmapToView() bm: "+bm);
        if (bm != null) {
        	imageView.setImageBitmap(bm);
            imageCallback.imageLoaded(filePath, ParseState.PICTURE_PARSE_SUCCESS);
        } else {
        	if (!filePaths.contains(filePath)) {
        		Log.i(TAG, "---->>loadBitmapToView() contains == false");
        		Task task = new Task();
        		task.path = filePath;
        		task.imageView = imageView;
        		task.callback = imageCallback;
        		Message msg = Message.obtain();
        		filePaths.add(filePath);
        		msg.obj = task;
        		msg.what = CALLBACK_MSG;
        		childHandler.sendMessage(msg);
			}
        }
    }

    private static final int CALLBACK_MSG = 0x01;
    class ChildCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message message) {
            Task task = (Task) message.obj;
            if (task == null || TextUtils.isEmpty(task.path)){
                return false;
            }
            String currentUsbPath = VideoAidlManager.getInstance().getUsbRootPathByFilePath(task.path);
            Log.i(TAG, "===handleMessage=MSG_CALLBACK===isUsbMount=="+VideoAidlManager.getInstance().isCurrentUsbMount(currentUsbPath)+" currentUsbPath： "+currentUsbPath);
            if (!VideoAidlManager.getInstance().isCurrentUsbMount(currentUsbPath)){
                return false;
            }
            Log.i(TAG,"----->> ChildCallback  handleMessage() ");
            task.parseState = ParseState.PICTURE_PARSING;
            Bitmap bitmap = getBitmapFromDiskCache(task.path);
            if (bitmap == null){
            	task = loadBitmapFromDB(task);
            }else {
                task.parseState = ParseState.PICTURE_PARSE_SUCCESS;
                task.bitmap = bitmap;
            }
            filePaths.remove(task.path);
            Message msg = Message.obtain();
            msg.obj = task;
            msg.what=MSG_CALLBACK;
            mHandler.sendMessage(msg);
            return false;
        }
    }

    private static final byte MSG_CALLBACK = 0x01;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
        	switch(message.what){
        	case MSG_CALLBACK:
        		Task task = (Task) message.obj;
                String currentUsbPath = VideoAidlManager.getInstance().getUsbRootPathByFilePath(task.path);
                Log.i(TAG, "===handleMessage=MSG_CALLBACK===isUsbMount=="+VideoAidlManager.getInstance().isCurrentUsbMount(currentUsbPath)+" currentUsbPath： "+currentUsbPath);
        		if (task.callback != null && VideoAidlManager.getInstance().isCurrentUsbMount(currentUsbPath)) {
        			Log.i(TAG, "===handleMessage=MSG_CALLBACK===bitmap: "+task.bitmap);
        			if (task.bitmap != null){
        				task.imageView.setImageBitmap(task.bitmap);
        			}else if(defaultBitmap !=null){
        				task.imageView.setImageBitmap(defaultBitmap);
        			}
        			task.callback.imageLoaded(task.path,task.parseState);
        		}
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
    public void recycle(){
    	defaultBitmap = null;
    	filePaths.clear();
    	childHandler.removeMessages(CALLBACK_MSG);
    	mHandler.removeMessages(MSG_CALLBACK);
    }
    
	/**
	 * 拿到缩略图
	 * @param task
	 * @return
	 */
	public Task loadBitmapFromDB(Task task){
		Log.d(TAG, "---->> loadBitmapFromDB()  path: "+task.path);
		Bitmap thumb = null;
		try {
			thumb = ThumbnailUtils.createVideoThumbnail(task.path, MediaStore.Video.Thumbnails.MINI_KIND);
			task.parseState =  ParseState.PICTURE_PARSE_SUCCESS;
			task.bitmap = thumb;
			CacheUtil.getInstance().putBitmapIntoCache(task.path, thumb);
			DisCacheUtil.getInstance(VideoAidlManager.getInstance().getContext()).writeFileToStorage(task.path, Bitmap2Bytes(thumb));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (thumb == null) {
			Log.d(TAG, "isNull");
		}

		return task;

	}
	
	private byte[] Bitmap2Bytes(Bitmap bm) {  
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);  
		return baos.toByteArray();  
	}

    /**
     *图片解析加载状态
     */
    public interface ParseState {
        int PICTURE_PARSE_FAILURE = -1;
        int PICTURE_PARSING = 0;
        int PICTURE_PARSE_SUCCESS = 1;
    }


}
