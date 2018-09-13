package com.cookoo.imagesdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.cookoo.imagesdk.adapter.LoopPagerAdapter;
import com.cookoo.imagesdk.binder.ImageAidlBinder;
import com.cookoo.imagesdk.imp.IImageModule;
import com.cookoo.imagesdk.manager.CookooImageConfiguration;
import com.cookoo.imagesdk.manager.ImageManager;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;


/**
 * @author lsf
 */
public class ImageSdkService extends Service implements IImageModule {
    private static ImageSdkService carMediaService = null;
    private final String TAG = ImageSdkService.class.getSimpleName();
    private ImageAidlBinder binder = new ImageAidlBinder();
    private ImageManager mImageManager = ImageManager.getInstance();

    private Handler mHandler = new Handler();

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            nextImage();
            mHandler.postDelayed(this, CookooImageConfiguration.getInstance().getParam().getTimeStep());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.print(TAG, "----->>> onCreate()");
        carMediaService = this;
        binder.addImageListener(this);
        sendStartExtendImageBroadcast();
    }

    /**
     * 向外发送一个服务启动广播，通知远程服务端进行服务绑定操作
     **/
    private void sendStartExtendImageBroadcast() {
        Intent intent = new Intent();
        intent.setAction("start.extend.image.process.action");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.print(TAG, "----->>> onDestroy()");
        binder.removeImageListener();
    }

    @Override
    public void startSlide() {
        if (!mImageManager.isSlidePlay()) {
            mImageManager.setSlidePlay(true);
            mHandler.postDelayed(runnable, 5000);
        }
    }

    @Override
    public void endSlidePlay() {
        if (mImageManager.isSlidePlay()) {
            mImageManager.setSlidePlay(false);
            mHandler.removeCallbacks(runnable);
        }
    }

    @Override
    public void clearUsbImage(String usbPath) {
        mImageManager.setCurrentPlayMediaItem(null);
        mImageManager.setCurrentListPosition(-1);
        mImageManager.getPlayList().clear();
        LogUtils.print(TAG, "== clearUsbImage == " + usbPath);
        for(MediaListData md:mImageManager.getOriginalData()){
            LogUtils.print(TAG, "== clearUsbImage == " + md.getUsbRootPath());
            if(!TextUtils.isEmpty(md.getUsbRootPath()) && md.getUsbRootPath().startsWith(usbPath)){
                mImageManager.getOriginalData().remove(md);
                break;
            }
        }
    }

    @Override
    public void zoomOut() {
        if (mImageManager.getCurrentPhotoView() == null) {
            return;
        }
        float currentScale = mImageManager.getCurrentPhotoView().getScale();
        float minScale = mImageManager.getCurrentPhotoView().getMinScale();
        if (currentScale - CookooImageConfiguration.getInstance().getParam().getPerScale() <= minScale) {
            currentScale = minScale;
        } else {
            currentScale = currentScale - CookooImageConfiguration.getInstance().getParam().getPerScale();
        }
        mImageManager.getCurrentPhotoView().setScale(currentScale, true);
    }

    @Override
    public void zoomIn() {
        if (mImageManager.getCurrentPhotoView() == null) {
            return;
        }
        float currentScale = mImageManager.getCurrentPhotoView().getScale();
        float maxScale = mImageManager.getCurrentPhotoView().getMaxScale();
        if (currentScale + CookooImageConfiguration.getInstance().getParam().getPerScale() >= maxScale) {
            currentScale = maxScale;
        } else {
            currentScale = currentScale + CookooImageConfiguration.getInstance().getParam().getPerScale();
        }
        mImageManager.getCurrentPhotoView().setScale(currentScale, true);
    }

    @Override
    public void rotate(int angle) {
        if (mImageManager.getCurrentPhotoView() == null) {
            return;
        }
        mImageManager.getCurrentPhotoView().setRotationBy(angle);
    }

    @Override
    public void preImage() {
        if (mImageManager.getViewPager() == null) {
            return;
        }
        int position = mImageManager.getViewPager().getCurrentItem();
        int count = mImageManager.getViewPager().getAdapter().getCount();
        List<MediaData> dataList = ((LoopPagerAdapter) mImageManager.getViewPager().getAdapter()).getData();
        if (position == 0) {
            position = count - 1;
        } else {
            position = position - 1;
        }
        LogUtils.print(TAG, "===preImage====position: " + position + "  getCount: " + count);
        mImageManager.getViewPager().setCurrentItem(position);
    }

    @Override
    public void nextImage() {
        if (mImageManager.getViewPager() == null) {
            return;
        }
        int position = mImageManager.getViewPager().getCurrentItem();
        int count = mImageManager.getViewPager().getAdapter().getCount();
        List<MediaData> dataList = ((LoopPagerAdapter) mImageManager.getViewPager().getAdapter()).getData();
        if (position == count - 1) {
            position = 0;
        } else {
            position = position + 1;
        }
        LogUtils.print(TAG, "===nextImage====position: " + position + "  getCount: " + count);
        mImageManager.getViewPager().setCurrentItem(position);
    }

}
