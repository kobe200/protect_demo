package com.cookoo.imagesdkclient.load;

/**
 * Created by lsf on 2018/3/30.
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 访问Http的工具类
 * @author lsf
 */
public class HttpUtil {
    private static HttpUtil instance;

    private HttpUtil() {
    }

    public static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 通过path（URL）访问网络获取返回的字节数组
     */
    public byte[] getByteArrayFromWeb(String path) {
        byte[] b = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                baos = new ByteArrayOutputStream();
                is = connection.getInputStream();
                byte[] tmp = new byte[1024];
                int length = 0;
                while ((length = is.read(tmp)) != -1) {
                    baos.write(tmp, 0, length);
                }
            }
            b = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
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
}