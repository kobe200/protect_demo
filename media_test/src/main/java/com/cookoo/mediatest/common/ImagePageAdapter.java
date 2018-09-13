package com.cookoo.mediatest.common;

import android.content.Context;
import android.view.View;

import com.cookoo.imagesdk.adapter.LoopPagerAdapter;
import com.cookoo.imagesdk.component.ImageViewPager;
import com.cookoo.mediatest.R;
import com.cookoo.musicsdk.utils.LogUtils;
import java.util.List;
import carnetapp.usbmediadata.bean.MediaData;
import uk.co.senab.photoview.PhotoView;

/**
 * @author lsf
 * @date 2018/3/31
 */
public class ImagePageAdapter extends LoopPagerAdapter {
    private final String TAG = ImagePageAdapter.class.getSimpleName();

    public ImagePageAdapter(Context context,List<MediaData> mediaItems,ImageViewPager viewPager) {
        super(context,mediaItems,viewPager);
    }

    @Override
    protected View getItemView() {
        return inflater.inflate(R.layout.pager_item,null);
    }

    @Override
    public PhotoView getCurrentPhotoView() {
        View view = getCurrentView(mPosition);
        LogUtils.print(TAG,"==getCurrentPhotoView()==" + view);
        if(view != null) {
            return (PhotoView) view.findViewById(R.id.iv_photo);
        }
        return null;
    }

    @Override
    protected void imageLoadStart(View view) {
    }


    @Override
    protected void imageLoadFinish(View view) {
    }
}
