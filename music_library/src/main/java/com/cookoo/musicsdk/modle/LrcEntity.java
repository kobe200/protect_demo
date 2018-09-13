package com.cookoo.musicsdk.modle;

import android.text.TextUtils;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词解析基类
 * @author: kobe
 * @date: 2018/6/13 16:36
 * @decribe:
 */
public class LrcEntity implements Comparable<LrcEntity> {
    public String time;
    public long timeLong;
    public String text;

    public LrcEntity(String time,String text,long timeLong) {
        this.time = time;
        this.text = text;
        this.timeLong = timeLong;
    }

    public LrcEntity() {
    }

    /**
     * 解析歌词文本
     * @param lrcText
     * @return
     */
    private static List<LrcEntity> parseLrc(String lrcText) {
        if(TextUtils.isEmpty(lrcText)) {
            return null;
        }

        List<LrcEntity> entityList = new ArrayList<>();
        // 将字符串以换行符切割
        String[] array = lrcText.split("\\n");
        for(String line : array) {
            // 循环遍历按行解析
            List<LrcEntity> list = parseLine(line);
            if(list != null && !list.isEmpty()) {
                entityList.addAll(list);
            }
        }

        // 使序列按大小升序排序（由小到大）
        Collections.sort(entityList);
        return entityList;
    }

    /**
     * 针对每一句歌词解析，并存到LrcEntity中
     * @param line
     * @return
     */
    private static List<LrcEntity> parseLine(String line) {
        if(TextUtils.isEmpty(line)) {
            return null;
        }
        // 去除空格
        line = line.trim();
        // 正则表达式，判断line中是否有[01:10.60]格式的片段
        Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\])+)(.+)").matcher(line);
        // 如果没有，返回null
        if(!lineMatcher.matches()) {
            return null;
        }

        // 得到时间标签
        String times = lineMatcher.group(1);
        // 得到文本内容
        String text = lineMatcher.group(3);
        List<LrcEntity> entryList = new ArrayList<>();

        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\]").matcher(times);
        while(timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));// 分
            long sec = Long.parseLong(timeMatcher.group(2));// 秒
            long mil = Long.parseLong(timeMatcher.group(3));// 毫秒
            // 得到long型时间
            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
            // 最终解析得到一个list
            entryList.add(new LrcEntity(times,text,time));
        }
        return entryList;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimeLong() {
        return timeLong;
    }

    public void setTimeLong(long timeLong) {
        this.timeLong = timeLong;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int compareTo(LrcEntity entity) {
        if(entity == null) {
            return -1;
        }
        return (int) (timeLong - entity.getTimeLong());
    }


}
