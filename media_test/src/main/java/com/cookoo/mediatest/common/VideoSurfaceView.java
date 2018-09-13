package com.cookoo.mediatest.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cookoo.mediatest.R;


/**
 * @author: kobe
 * @date: 2018/6/8 10:34
 * @decribe:
 */

public class VideoSurfaceView extends RelativeLayout {
    private RelativeLayout rootView ,defaultView;
    private TextView tip;
    private SurfaceView surfaceView;

    public VideoSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public VideoSurfaceView(Context context,AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    public VideoSurfaceView(Context context,AttributeSet attrs,int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        init(context);
    }

    public TextView getTip() {
        return tip;
    }

    public void setTip(TextView tip) {
        this.tip = tip;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    private void init(Context context){
        rootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.videosurfaceview,this);
        surfaceView = rootView.findViewById(R.id.videosurfaceview_surfaceview);
        defaultView = rootView.findViewById(R.id.videosurfaceview_default);
        tip = rootView.findViewById(R.id.videosurfaceview_default_tip);
    }

    public void showNoPlay(){
        defaultView.setVisibility(View.VISIBLE);
    }

    public void play(){
        defaultView.setVisibility(View.GONE);
    }


}
