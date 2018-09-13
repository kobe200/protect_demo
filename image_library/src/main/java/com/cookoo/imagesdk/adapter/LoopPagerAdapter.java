package com.cookoo.imagesdk.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cookoo.imagesdk.component.ImageViewPager;
import com.cookoo.imagesdk.load.CacheUtil;
import com.cookoo.imagesdk.manager.ImageManager;
import com.cookoo.imagesdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import uk.co.senab.photoview.PhotoView;

/**
 * @author lsf
 */
public abstract class LoopPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    private static final String TAG = LoopPagerAdapter.class.getSimpleName();
    protected Context mContext;
    protected List<View> views;
    protected List<MediaData> mMediaItems;
    protected LayoutInflater inflater;
    protected ImageViewPager mViewPager;
    protected int mPosition = -1;
    private CacheUtil mLoader;
    private ImageManager mImageManager = ImageManager.getInstance();

    public LoopPagerAdapter(Context context, List<MediaData> mediaItems, ImageViewPager viewPager) {
        this.mContext = context;
        this.mViewPager = viewPager;
        this.inflater = LayoutInflater.from(mContext);
        mViewPager.setOnPageChangeListener(this);
        mLoader = CacheUtil.getInstance();
        initData(mediaItems);
    }

    public void updateAdapter(List<MediaData> mediaItems) {
        initData(mediaItems);
        this.notifyDataSetChanged();
    }

    private void initData(List<MediaData> mediaItems){
        if (mMediaItems == null){
            mMediaItems = new ArrayList<>();
        }
        if (views == null){
            views = new ArrayList<>();
        }
        mMediaItems.clear();
        mMediaItems.addAll(mediaItems);
        views.clear();
        if (mMediaItems.size() <= 0) {
            return;
        }
        mMediaItems.add(0, mMediaItems.get(mMediaItems.size() - 1));
        mMediaItems.add(mMediaItems.get(1));
        for (MediaData item : mMediaItems) {
            views.add(null);
        }
        LogUtils.print(TAG, "  updaterAdapter() size: " + mMediaItems.size());
    }


    public void setCurrentItem(int position) {
        if (mViewPager != null) {
            LogUtils.print(TAG, "---->> setCurrentItem position: " + position + " size: " + views.size());
            mPosition = position + 1;
            mViewPager.setCurrentItem(mPosition);
            onPageSelected(mPosition);
        }
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LogUtils.print(TAG, "--->> instantiateItem() position: " + position + "  mPosition: " + mPosition);
        rePlaceView(position);
        container.addView(getCurrentView(position), 0);
        return getCurrentView(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onPageSelected(int position) {
        LogUtils.print(TAG, "--->> onPageSelected() position: " + position);
        mPosition = position;
        setImageBitmap(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        若viewpager滑动未停止，直接返回
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            return;
        }
//        若当前为第一张，设置页面为倒数第二张
        if (mPosition == 0) {
            mViewPager.setCurrentItem(views.size()-2,false);
        } else if (mPosition == views.size()-1) {
//        若当前为倒数第一张，设置页面为第二张
            mViewPager.setCurrentItem(1,false);
        }
    }

    private void rePlaceView(int position) {
        if (getCurrentView(position) == null) {
            views.remove(position);
            views.add(position, getItemView());
        }
    }

    public View getCurrentView(int position) {
        if (views.size() < 1) {
            return null;
        }
        if (position < 0) {
            views.get(0);
        } else if (views.size() > position) {
            return views.get(position);
        } else if (views.size() <= position) {
            return views.get(views.size() - 1);
        }
        return null;
    }

    public List<MediaData> getData() {
        return mMediaItems;
    }

    protected void setImageBitmap(final int position) {
        LogUtils.print(TAG, " setImageBitmap() position: " + position+" views.size: "+views.size());
        rePlaceView(position);
        if (position == 0) {
            mImageManager.setCurrentListPosition(mImageManager.getPlayList().size() - 1);
        } else if (position == views.size() - 1) {
            mImageManager.setCurrentListPosition(0);
        } else {
            mImageManager.setCurrentListPosition(position - 1);
        }
        mImageManager.setCurrentPhotoView(getCurrentPhotoView());
        LogUtils.print(TAG,"--->>> setImageBitmap() getFilePath: "+mMediaItems.get(position).getFilePath()+" getCurrentListPosition: "+mImageManager.getCurrentListPosition());
        mImageManager.setCurrentPlayMediaItem(mMediaItems.get(position));
        imageLoadStart(getCurrentView(position));
        mLoader.loadBitmapToView(mMediaItems.get(position).getFilePath(), getCurrentPhotoView(), new CacheUtil.ImageCallback() {
            @Override
            public void imageLoaded(String imagePath, int parseState) {
                LogUtils.print(TAG, " imageLoaded()  imagePath: " + imagePath + "  parseState: " + parseState);
                imageLoadFinish(getCurrentView(mPosition));
            }
        });
    }

    public void recycle() {
        LogUtils.print(TAG, "==recycle==" + mLoader);
        if (mLoader != null) {
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
