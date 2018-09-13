package com.cookoo.imagesdkclient.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cookoo.imagesdkclient.load.CacheUtil;
import com.cookoo.imagesdkclient.manager.CookooImageConfiguration;
import com.cookoo.imagesdkclient.manager.ImageAidlManager;
import com.cookoo.imagesdkclient.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import uk.co.senab.photoview.PhotoView;

/**
 * @author lsf
 * @date 2018/3/31
 * 实现真正的无限循环效果
 */

public abstract class LoopPagerAdapter extends PagerAdapter {

    private static final String TAG = LoopPagerAdapter.class.getSimpleName();
    public int currentPosition = -1;
    protected Context mContext;
    protected List<View> views;
    protected List<MediaData> mMediaItems;
    protected LayoutInflater inflater;
    private CacheUtil mLoader;

    public LoopPagerAdapter(Context context,List<MediaData> mediaItems) {
        CookooImageConfiguration.getInstance().getParam().setCustomLoopPagerAdapter(true);
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
        mMediaItems = new ArrayList<>();
        this.mMediaItems.addAll(mediaItems);
        if(mMediaItems.size() == 1) {
            mMediaItems.add(mMediaItems.get(0));
        }
        views = new ArrayList<>();
        mLoader = CacheUtil.getInstance();
        for(MediaData item : mMediaItems) {
            views.add(null);
        }
    }

    public void updaterAdapter(List<MediaData> mediaItems) {
        this.mMediaItems.clear();
        this.mMediaItems.addAll(mediaItems);
        views.clear();
        if(mMediaItems.size() == 1) {
            mMediaItems.add(mMediaItems.get(0));
        }
        for(MediaData item : mMediaItems) {
            views.add(null);
        }
        LogUtils.print(TAG,"  updaterAdapter() size: " + mMediaItems.size());
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return currentPosition;
    }

    @Override
    public boolean isViewFromObject(View view,Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container,final int position) {
        LogUtils.print(TAG," instantiateItem() position: " + position + ", views.size(): " + views.size());
        if(position < 0 || position >= views.size()) {
            return null;
        }
        View view = setImageView(position);
        if(view == null) {
            return null;
        }
        container.removeView(view);
        container.addView(view);
        return view;
    }

    private View setImageView(final int position) {
        //查找缓存内是否已经存在该view
        View view = getCurrentView(position);
        //如果没有加载则加载一个view
        if(view == null) {
            views.remove(position);
            view = getItemView();
            LogUtils.print(TAG,"setImageView add View ");
            views.add(position,view);
        }
        MediaData mediaData = mMediaItems.get(position);
        if(mediaData != null && !TextUtils.isEmpty(mediaData.getFilePath())) {
            currentPosition = position;
            PhotoView photoView = getCurrentPhotoView();
            ImageAidlManager.getInstance().setCurrentPhotoView(photoView);
            mLoader.loadBitmapToView(mMediaItems.get(position).getFilePath(),photoView,new CacheUtil.ImageCallback() {
                @Override
                public void imageLoaded(String imagePath,int parseState) {
                    LogUtils.print(TAG,"setImageView imagePath: " + imagePath);
                    if(position == currentPosition) {
                        View view = getCurrentView(position);
                        imageLoadFinish(view);
                    }
                }
            });
        }
        LogUtils.print(TAG," setImageView() return ");
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container,int position,Object object) {
        if(position < 0 || position > views.size()) {
            return;
        }
        View view = getCurrentView(position);
        LogUtils.print(TAG," destroyItem() view=" + mMediaItems.get(position).getName());
        container.removeView(view);
        if(position < views.size()) {
            views.remove(position);
            views.add(position,null);
        }
    }

    @Override
    public void setPrimaryItem(View container,int position,Object object) {
        super.setPrimaryItem(container,position,object);
        if(currentPosition == position) {
            return;
        }
        setImageView(position);
        LogUtils.print(TAG,"setPrimaryItem position =" + position + "|" + currentPosition + "|" + mMediaItems.get(position).getName());
    }

    public View getCurrentView(int position) {
        if(views.size() < 1) {
            return null;
        }
        if(position < 0) {
            views.get(0);
        } else if(views.size() > position) {
            return views.get(position);
        } else if(views.size() <= position) {
            return views.get(views.size() - 1);
        }
        return null;
    }

    public void recycle() {
        LogUtils.print(TAG,"==recycle==" + mLoader);
        CookooImageConfiguration.getInstance().getParam().setCustomLoopPagerAdapter(false);
        if(mLoader != null) {
            mLoader.cancelLoadTask();
        }
    }

    /**
     * 获取当前显示的PhotoView
     * @return
     */
    protected abstract PhotoView getCurrentPhotoView();

    /**
     * 获取当前显示的PhotoView
     * @return
     */
    protected abstract View getItemView();

    /**
     * 图片开始加载
     * @param view
     */
    protected abstract void imageLoadStart(View view);

    /**
     * 获取当前显示的PhotoView
     * @param view 图片加载完成回调
     */
    protected abstract void imageLoadFinish(View view);


}
