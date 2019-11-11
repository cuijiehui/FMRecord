package com.khmer.fm.adnroid_recordd.record.mix;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;

/**
 * author : created by cui on 2019/10/9 16:38
 * Description ：背景音乐管理器
 */
public class BgMusicProcessor {
    private final static String TAG = BgMusicProcessor.class.getSimpleName();
    private final static int TRACK_BUFFER_SIZE = 10 * 1024;

    private String mPath ;//背景音乐路径
    private FileInputStream mFileInputStream; //背景音乐流
    private boolean isProcessing = false; //是否播放背景音乐
    private ByteRingBuffer mTrackBuffer; //用于管理真正处理的流

    private HandlerThread mThread;
    private Handler mHandler;

    /**
     * 创建背景音乐管理对象 准备线程
     * @param mPath 背景音乐路径
     */
    public BgMusicProcessor(String mPath) {
        this.mPath = mPath;
        mThread = new HandlerThread(TAG, THREAD_PRIORITY_DEFAULT);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    /**
     * 设置背景音乐
     */
    public void start(){
        if (TextUtils.isEmpty(mPath)){
            Log.e(TAG, "track path is empty");
            return;
        }

        try {
            isProcessing = true;
            mFileInputStream = new FileInputStream(mPath); //设置背景音乐
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    public void setPath(String path ){
        isProcessing=false;
        mPath=path;
        start();
    }
    /**
     * 背景音乐暂停
     */
    public void pause(){
        isProcessing = false;
    }

    /**
     * 背景音乐重新开始
     */
    public void restart(){
        isProcessing = true;
    }

    /**
     * 停止背景音乐 释放资源
     */
    public void stop(){
        isProcessing = false;

        if (mFileInputStream != null) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }
    }

    /**
     * 设置背景音乐路径
     * @param path 背景音乐路径
     */
    public void setTrackPath(String path){
        mPath = path;
    }

    /**
     * 获取背景音乐的数据
     * @param outputData 准备装取数据的数组
     * @param size 数组大小
     * @return -1失败
     */
    public int getData(byte[] outputData, int size){

        if (!isProcessing){
            return 0;
        }

        if (mFileInputStream == null){
            return 0;
        }
//        int ret = mTrackBuffer.read(outputData, 0, size);
        int ret;
        try {
            ret = mFileInputStream.read(outputData, 0, size);

            if (ret == -1){
                Log.e(TAG, "track file end....");
                stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        return ret;
    }

}
