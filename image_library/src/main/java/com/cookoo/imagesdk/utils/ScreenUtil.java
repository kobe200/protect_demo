package com.cookoo.imagesdk.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class ScreenUtil {
  public static Pair<Integer,Integer> getResolution(Context ctx){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return getRealResolution(ctx);
    }
    else {
      return getRealResolutionOnOldDevice(ctx);
    }
  }

  private static Pair<Integer, Integer> getRealResolutionOnOldDevice(Context ctx) {
    try{
      WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      Method mGetRawWidth = Display.class.getMethod("getRawWidth");
      Method mGetRawHeight = Display.class.getMethod("getRawHeight");
      Integer realWidth = (Integer) mGetRawWidth.invoke(display);
      Integer realHeight = (Integer) mGetRawHeight.invoke(display);
      return new Pair<Integer, Integer>(realWidth, realHeight);
    }
    catch (Exception e) {
      DisplayMetrics disp = ctx.getResources().getDisplayMetrics();
      return new Pair<Integer, Integer>(disp.widthPixels, disp.heightPixels);
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private static Pair<Integer,Integer> getRealResolution(Context ctx) {
    WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getRealMetrics(metrics);
    return new Pair<Integer, Integer>(metrics.widthPixels, metrics.heightPixels);
  }
}
