package com.khmer.fm.adnroid_recordd;

import android.os.Environment;

/**
 * author : created by cui on 2019/10/14 14:55
 * Description:常用的常量
 */
public class Constants {
    //录音模块
    public static String RECORD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Record/";//录音文件夹
    public static final String RECORD_MIX_FILE_PATH = RECORD_PATH +"MixRecord.wav";//录音输出文件
    public static final String RECORD_MIX_MP3_FILE_PATH = RECORD_PATH +"MixRecord.mp3";//录音输出文件
    public static final String BG_MUSIC_WAV_PATH = RECORD_PATH +"bg_muisc.wav";//背景音乐WAV文件的录音
    public static final String BG_MUSIC_PCM_PATH = RECORD_PATH +"bg_muisc.pcm";//背景音乐PCM文件的录音
    public static boolean isHeadSet =false;//耳机是否有插

}
