package com.cookoo.extendimage;

import android.content.Context;
import android.view.View;

import com.cookoo.imagesdkclient.adapter.LoopPagerAdapter;

import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import uk.co.senab.photoview.PhotoView;

/**
 * @author lsf
 * @date 2018/3/31
 */
public class ImagePageAdapter extends LoopPagerAdapter {
    private final String TAG = ImagePageAdapter.class.getSimpleName();

    public ImagePageAdapter(Context context,List<MediaData> mediaItems) {
        super(context,mediaItems);
    }

    @Override
    protected View getItemView() {
        return inflater.inflate(R.layout.pager_item,null);
    }

    @Override
    public PhotoView getCurrentPhotoView() {
        View view = getCurrentView(currentPosition);
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
