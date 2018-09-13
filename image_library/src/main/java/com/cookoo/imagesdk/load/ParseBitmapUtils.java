package com.cookoo.imagesdk.load;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.cookoo.imagesdk.ImageSdkConstants;
import com.cookoo.imagesdk.utils.GlobalTool;
import com.cookoo.imagesdk.utils.LogUtils;

/**
 * @author lsf
 */
public class ParseBitmapUtils {
	
	private static final String TAG = "ParseBitmapUtils";

	public static Task downloadBitmapByPath(Task task,int screenWidth,int screenHeight) {
		byte[] temp= decodeBitmap(task.path, screenWidth, screenHeight);
		LogUtils.print(TAG," --->> downloadBitmapByPath() temp: "+temp);
		if (temp != null && temp.length > 0) {
			//将图片写到文件中
			FileUtil.getInstance(GlobalTool.getInstance().getContext()).writeFileToStorage(task.path, temp);
			try {
				task.bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
			} catch (OutOfMemoryError e) {
				task.bitmap = ParseBitmapUtils.byteToBitmap(temp);
				LogUtils.print(TAG, "====OutOfMemoryError=ee==");
			}
		}else {
			task.parseState =  ImageSdkConstants.ImageParseState.PICTURE_PARSE_FAILURE;
		}
		if (task.bitmap != null) {
			//将图片保存到LruCache中
			CacheUtil.getInstance().putBitmapIntoCache(task.path, task.bitmap);
			task.parseState =  ImageSdkConstants.ImageParseState.PICTURE_PARSE_SUCCESS;
		}else{
			task.parseState =  ImageSdkConstants.ImageParseState.PICTURE_PARSE_FAILURE;
			task.bitmap = null;
		}
		return task;
	}

	public static byte[] decodeBitmap(String path,int width,int height) {
		LogUtils.print(TAG," decodeBitmap() width: "+width+"  height: "+height+"  path: "+path);
		if (path == null) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError e) {
			return null;
		}
		opts.inSampleSize = computeSampleSize(opts, -1, width * height);
		LogUtils.print(TAG, "decodeBitmap==computeSampleSize=="+computeSampleSize(opts, -1, width * height));
		opts.inJustDecodeBounds = false;
		opts.inTempStorage = new byte[16 * 1024];
		FileInputStream is = null;
		Bitmap bmp = null;
		Bitmap bmp2 = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			try {
				is = new FileInputStream(path);
				bmp = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
				double scale = getScaling(opts.outWidth * opts.outHeight,width * height);
				bmp2 = Bitmap.createScaledBitmap(bmp,(int) (opts.outWidth * scale),(int) (opts.outHeight * scale), true);
				bmp2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			} catch (OutOfMemoryError e) {
				decodeBitmap(path,width-100,height - 100);
			}
		} catch (Exception e) {
			try {
				is.close();
				baos.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return null;
		} finally {
			try {
				bmp.recycle();
				bmp2.recycle();
				is.close();
				baos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.gc();
		}
		return baos.toByteArray();
	}

	public static Bitmap byteToBitmap(byte[] imgByte) {
		InputStream input = null;
		 Bitmap bitmap = null; 
		 Options options = new Options();
		 options.inSampleSize = 8; 
		  input = new ByteArrayInputStream(imgByte); 
		 SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options)); 
		   bitmap = (Bitmap) softRef.get(); 
		    if (imgByte != null) { 
		     imgByte = null; 
		    } 
		    try { 
		      if (input != null) { 
		        input.close(); 
		      } 
		    } catch (IOException e) { 
		      e.printStackTrace(); 
		    } 
		    return bitmap; 
		  } 
	
	public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    if (height > reqHeight || width > reqWidth) {
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	    return inSampleSize;
	  }

	private static int computeSampleSize(Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	private static double getScaling(int src, int des) {
		double scale = Math.sqrt((double) des / (double) src);
		return scale;
	}
}