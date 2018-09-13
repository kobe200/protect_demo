package com.cookoo.musicsdk.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import carnetapp.usbmediadata.bean.MediaItemInfo;

/**
 * @author
 */
public class ThumbnailUtil {

    private static final String TAG = "ThumbnailUtil";

    private static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    @SuppressWarnings("unused")
    private static Bitmap mCachedBit = null;

    /**
     * 获取缩略图。适用媒体类型为Audio的媒体。
     * @param context 上下文
     * @param mediaItem 媒体对象
     * @param w 期望的宽度
     * @param h 期望的高度
     * @return Bitmap
     * @throws Exception
     */
    public static Bitmap getThumbnailBitmap(Context context,MediaItemInfo mediaItem,int w,int h) throws Exception {
        if(mediaItem == null){
            return null;
        }
        Log.d(TAG,String.format("DBID %d MediaID:%d AlbumID:%d",mediaItem.getID(),mediaItem.getMediaID(),mediaItem.getAlbumID()));
        Bitmap bitmap = null;
        switch(mediaItem.getMediaType()) {
            case IMAGE:
            case VIDEO:
                throw new Exception("Not Supported. use getThumbnailBitmap(Context context, MediaItem mediaItem, int w, int h) instead.");

            case AUDIO:
                if(mediaItem.getAlbumID() == -1) {
                    return bitmap;
                }
                bitmap = getArtworkQuick(context,mediaItem.getAlbumID(),w,h);
                if(bitmap == null) {
                    Log.d(TAG,"getArtworkQuick returned null.try getArtwork");
                    bitmap = getArtwork(context,mediaItem.getMediaID(),mediaItem.getAlbumID());
                    Log.d(TAG,"bitmap:" + bitmap);
                }
                break;
            default:
                break;
        }
        return bitmap;
    }

    /**
     * 获取缩略图。适用媒体类型为Video和Image的媒体。
     * @param context 上下文
     * @param mediaItem 媒体对象
     * @param kind 对于Image,kind
     * 为MediaStore.Images.Thumbnails.MICRO_KIND或MediaStore
     * .Images.Thumbnails.MINI_KIND。 对于Video,kind
     * 为MediaStore.Video.Thumbnails
     * .MICRO_KIND或MediaStore.Images.Thumbnails.MINI_KIND。
     * @return
     * @throws Exception
     */
    public static Bitmap getThumbnailBitmap(Context context,MediaItemInfo mediaItem,int kind) throws Exception {
        Log.d(TAG,"item id:" + mediaItem.getMediaID());
        Bitmap bitmap = null;
        switch(mediaItem.getMediaType()) {
            case IMAGE:
                if(mediaItem.getMediaID() != -1 && context != null) {
                    if(kind == MediaStore.Images.Thumbnails.MICRO_KIND) {

                        Bitmap origBitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),mediaItem.getMediaID(),MediaStore.Images.Thumbnails.MINI_KIND,null);
                        bitmap = extractMicroImage(origBitmap);

                    } else {
                        bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),mediaItem.getMediaID(),kind,null);
                    }
                }
                break;
            case VIDEO:

                if(mediaItem.getMediaID() != -1 && context != null) {
                    if(kind == MediaStore.Video.Thumbnails.MICRO_KIND) {
                        Bitmap origBitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),mediaItem.getMediaID(),MediaStore.Video.Thumbnails.MINI_KIND,null);
                        bitmap = extractMicroImage(origBitmap);
                    } else {
                        bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),mediaItem.getMediaID(),kind,null);
                    }

                }
                break;
            case AUDIO:
                throw new Exception("Not Supported. use getThumbnailBitmap(Context context, MediaItem mediaItem, int w, int h) instead.");

            default:
                break;
        }
        return bitmap;

    }

    private static Bitmap extractMicroImage(Bitmap origBitmap) {
        Bitmap bitmap = null;
        if(origBitmap != null) {
            bitmap = android.media.ThumbnailUtils.extractThumbnail(origBitmap,TARGET_SIZE_MICRO_THUMBNAIL,TARGET_SIZE_MICRO_THUMBNAIL);
            if(origBitmap.isRecycled()) {
                origBitmap.recycle();
                origBitmap = null;
            }
        }
        return bitmap;
    }

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    private static Bitmap getArtwork(Context context,long song_id,long album_id) {

        if(album_id < 0) {
            // This is something that is not in the database, so get the album
            // art directly
            // from the file.
            if(song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context,song_id,-1);
                if(bm != null) {
                    return bm;
                }
            }

            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri,album_id);
        Log.d(TAG,"getArtwork uri:" + uri);
        if(uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in,null,sBitmapOptions);
            } catch(FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the
                // user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context,song_id,album_id);
                if(bm != null) {
                    if(bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565,false);
                    }
                }
                return bm;
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch(IOException ex) {
                    Log.d(TAG,"getArtwork IOException:" + ex);
                }
            }
        }

        return null;
    }

    // get album art for specified file
    // private static final String sExternalMediaUri =
    // MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    // .toString();

    private static Bitmap getArtworkFromFile(Context context,long songid,long albumid) {
        Bitmap bm = null;

        if(albumid < 0 && songid < 0) {
            Log.d(TAG,"getArtworkFromFile Must specify an album or a song id");
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if(albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri,"r");
                if(pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri,albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri,"r");
                if(pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch(IllegalStateException ex) {
            Log.d(TAG,"getArtworkFromFile bitmap:" + ex);
        } catch(FileNotFoundException ex) {
            Log.d(TAG,"getArtworkFromFile bitmap:" + ex);
        }
        if(bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    private static Bitmap getArtworkQuick(Context context,long album_id,int w,int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri,album_id);
        Log.d(TAG,"getArtworkQuick uri:" + uri);
        if(uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri,"r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,sBitmapOptionsCache);
                Log.d(TAG,"decodeFileDescriptor:");
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while(nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,sBitmapOptionsCache);
                Log.d(TAG,"b:" + b);
                if(b != null) {
                    // finally rescale to exactly the size we need
                    if(sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b,w,h,true);
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if(tmp != b) {
                            b.recycle();
                        }
                        b = tmp;
                    }
                }

                return b;
            } catch(FileNotFoundException e) {
                Log.d(TAG,"getArtworkQuick FileNotFoundException:" + e);
            } finally {
                try {
                    if(fd != null) {
                        fd.close();
                    }
                } catch(IOException e) {
                    Log.d(TAG,"getArtworkQuick IOException:" + e);
                }
            }
        }
        return null;
    }

    public static Bitmap setArtwork(Context context,String url,int w,int h) {
        Uri selectedAudio = Uri.parse(url);
        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        byte[] artwork = null;
        try {
            myRetriever.setDataSource(context,selectedAudio);
            artwork = myRetriever.getEmbeddedPicture();
        } catch(Exception e) {
            Log.i(TAG,"Exception:" + e);
        } finally {
            myRetriever.release();
            Log.i(TAG,"setArtwork release");
            if(artwork != null) {
                return Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(artwork,0,artwork.length),w,h,true);
            }
        }
        return null;
    }
}
