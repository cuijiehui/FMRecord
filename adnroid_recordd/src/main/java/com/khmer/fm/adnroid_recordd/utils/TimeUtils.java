package com.khmer.fm.adnroid_recordd.utils;

/**
 * author : created by cui on 2019/10/12 16:38
 * Description : 时间管理工具
 */
public class TimeUtils {
    /**
     * 秒转MM:SS模式
     * @param second 秒数
     * @return MM:SS格式的时间
     */
    public static String getMSTime(int second){
        if (second < 10) {
            return "00:0" + second;
        }
        if (second < 60) {
            return "00:" + second;
        }
        int minute = second / 60;
        second = second - minute * 60;
        if (minute < 10) {
            if (second < 10) {
                return "0" + minute + ":0" + second;
            }
            return "0" + minute + ":" + second;
        }
        if (second < 10) {
            return minute + ":0" + second;
        }
        return minute + ":" + second;
    }

    /**
     * 根据秒数转化为时分秒
     * @param second 秒数
     * @return HH:MM:SS 格式时间
     */
    public static String getHMSTime(int second) {
        if (second < 10) {
            return "00:0" + second;
        }
        if (second < 60) {
            return "00:" + second;
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;
            if (minute < 10) {
                if (second < 10) {
                    return "0" + minute + ":0" + second;
                }
                return "0" + minute + ":" + second;
            }
            if (second < 10) {
                return minute + ":0" + second;
            }
            return minute + ":" + second;
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (second < 10) {
                    return "0" + hour + ":0" + minute + ":0" + second;
                }
                return "0" + hour + ":0" + minute + ":" + second;
            }
            if (second < 10) {
                return "0" + hour + minute + ":0" + second;
            }
            return "0" + hour + minute + ":" + second;
        }
        if (minute < 10) {
            if (second < 10) {
                return hour + ":0" + minute + ":0" + second;
            }
            return hour + ":0" + minute + ":" + second;
        }
        if (second < 10) {
            return hour + minute + ":0" + second;
        }
        return hour + minute + ":" + second;
    }
}
