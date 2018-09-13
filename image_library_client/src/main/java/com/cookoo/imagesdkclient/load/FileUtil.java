package com.cookoo.imagesdkclient.load;

/**
 * Created by lsf on 2018/3/30.
 */

import android.content.Context;

import com.cookoo.imagesdkclient.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 操作内存文件的工具类
 * @author lsf
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private static FileUtil instance;

    private Context context;

    private FileUtil(Context context) {
        this.context = context;
    }

    public static FileUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (FileUtil.class) {
                if (instance == null) {
                    instance = new FileUtil(context);
                }
            }
        }
        return instance;
    }

    /**
     * 将文件存储到内存中
     */
    public void writeFileToStorage(String filePath, byte[] b) {
        String fileName = replace(filePath);
        LogUtils.print(TAG," writeFileToStorage() fileName: "+fileName+" filePath: "+filePath);
        FileOutputStream fos = null;
        try {
            File file = new File(context.getFilesDir(), fileName);
            fos = new FileOutputStream(file);
            fos.write(b, 0, b.length);
        } catch (Exception e) {
            LogUtils.print(TAG," writeFileToStorage() eee: "+e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从内存中读取文件的字节码
     */
    public byte[] readBytesFromStorage(String filePath) {
        String fileName = replace(filePath);
        LogUtils.print(TAG," readBytesFromStorage() fileName: "+fileName+" fi   lePath: "+filePath);
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            return null;
        }
        byte[] b = null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = context.openFileInput(fileName);
            baos = new ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int len = 0;
            while ((len = fis.read(tmp)) != -1) {
                baos.write(tmp, 0, len);
            }
            b = baos.toByteArray();
        } catch (Exception e) {
            LogUtils.print(TAG," readBytesFromStorage() eee: "+e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    /**
     * 主要是将文件路径的"/"字符转换为"_" 后，作为保存到Files文件夹下的文件名
     * @param filePath
     * @return
     */
    public String replace(String filePath){
        return filePath.replace("/","_");
    }

    /**
     * 1 .每次打开图片进程先把之前缓存的图片数据清理
     * 2. 每次拔掉usb后把缓存的图片数据清理
     */
    public void removeCacheBitmap() {
        File file_File = new File(context.getFilesDir().getParent());
        LogUtils.print(TAG," removeCacheBitmap() file_file: "+file_File.toString());
        if (file_File.exists()) {
            File[] files = file_File.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }
}
