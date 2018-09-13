package com.cookoo.musicsdk.utils;

import android.content.Context;

import com.cookoo.musicsdk.modle.LrcEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: kobe
 * @date: 2018/6/13 16:33
 * @decribe:
 */

public class LrcReadUtil {

    /**
     * 根据文件名获取assets目录下的文件内容
     * @param fileName
     * @return
     */
    private String getLrcText(Context context,String fileName) {
        String lrcText = null;
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while((line = bufReader.readLine()) != null) {
                lrcText += line;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return lrcText;
    }

    /**
     * 针对每一句歌词解析，并存到LrcEntity中
     * @param line [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
     * @return
     */
    public List<LrcEntity> parseLine2(String line) {
        List<LrcEntity> entryList = new ArrayList<>();
        int pos1 = line.indexOf("[");//0
        int pos2 = line.indexOf("]");//9  indexof如果找不到返回-1
        if(pos1 == 0 && pos2 != -1) {
            //long数组用于存放时间戳，判断含有多少个时间标签
            String[] times = new String[getCount(line)];
            String strTime = line.substring(pos1,pos2 + 1);//[02:45.69]
            // 时间标签数组
            times[0] = strTime;
            //判断是否还有下一个
            String text = line;//[02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
            int i = 1;
            while(pos1 == 0 && pos2 != -1) {//判断是否有时间的显示，既歌词
                text = text.substring(pos2 + 1);//[02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
                pos1 = text.indexOf("[");//0
                pos2 = text.indexOf("]");//9
                if(pos2 != -1) {
                    strTime = text.substring(pos1,pos2 + 1);//[02:42.20]
                    times[i] = strTime;//将第二个时间戳添加到数组中
                    if(times[i] == "") {
                        return entryList;
                    }
                }
                i++;
            }

            LrcEntity lrcEntity = new LrcEntity();
            for(int j = 0; j < times.length; j++) {
                if(times[j] != null) {
                    lrcEntity.setText(text);
                    lrcEntity.setTimeLong(Str2Long(times[j]));
                    lrcEntity.setTime(times[j]);
                    entryList.add(lrcEntity);//将歌词信息添加到集合中
                    lrcEntity = new LrcEntity();
                }
            }
        }
        return entryList;
    }

    //将字符串转换为long类型
    private long Str2Long(String strTime) {
        long showTime = -1;
        try {
            strTime = strTime.substring(1,strTime.length() - 1);
            String[] s1 = strTime.split(":");
            String[] s2 = s1[1].split("\\.");
            long min = Long.parseLong(s1[0]);
            long second = Long.parseLong(s2[0]);
            long mil = Long.parseLong(s2[1]);
            showTime = min * 60 * 1000 + second * 1000 + mil * 10;
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
        return showTime;
    }

    /**
     * 判断当前行的歌词播放几次
     * @param line
     * @return
     */
    private int getCount(String line) {
        String[] split = line.split("\\]");
        return split.length;
    }
}
