package com.khmer.fm.adnroid_recordd.record.mix;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;

/**
 * author : created by cui on 2019/10/9 16:16
 * Description ：录音管理
 */
public class VRecorder {
    private final String TAG = VRecorder.class.getSimpleName();
    private AudioRecord mRecord;
    private boolean isRecording;//判断是否在录音

    private int mSampleRate;//采样率
    private int mChannels;//声道
    private int mBits;//位数

    private int mBufferSize; //根据参数获取音频缓存的大小
    private HandlerThread mThread;
    private Handler mHandler;
    public VRecorder(int sampleRate, int bits, int channels) {
        mSampleRate = sampleRate;
        mBits = bits;
        mChannels = channels;

        int pcmBit;
        if (mBits == 16){
            pcmBit = AudioFormat.ENCODING_PCM_16BIT;
        }else if(mBits == 8){
            pcmBit = AudioFormat.ENCODING_PCM_8BIT;
        }else {
            pcmBit = AudioFormat.ENCODING_PCM_16BIT;
        }
        mBits=pcmBit;
        int pcmChannel;
        if (mChannels == 1){
            pcmChannel = AudioFormat.CHANNEL_OUT_MONO;
        }else if(mChannels == 2){
            pcmChannel = AudioFormat.CHANNEL_OUT_STEREO;
        }else {
            pcmChannel = AudioFormat.CHANNEL_OUT_STEREO;
        }
        mChannels=pcmChannel;
        //根据参数创建record对象
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, pcmChannel, pcmBit);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mSampleRate,
                pcmChannel,
                pcmBit,
                mBufferSize);
        //创建线程 等待启动
        mThread = new HandlerThread(TAG, THREAD_PRIORITY_DEFAULT);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    /**
     * 启动线程 启动录音
     */
    public void start(){
        if (mRecord == null){
            Log.e(TAG, "record is null");
            return;
        }

        isRecording = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    mRecord.startRecording();
                }catch (Exception e){
                    Log.d("","打开record 失败");
                    //一些手机打开会失败 需要Release 和置空
                    releaseRecord();
                    isRecording=false;
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * release 和置空 record
     */
    private void releaseRecord(){
        if (mRecord!=null) {
            mRecord.release();
            mRecord=null;
        }
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mSampleRate,
                mChannels,
                mBits,
                mBufferSize);
        start();
    }
    /**
     * 获取录音的数据
     * @param outputData 准备装数据的数组
     * @param size 数组大小
     * @return 不等于-1 为成功 等于-1 为失败
     */
    public int getData(byte[] outputData, int size){

        if (!isRecording){
            return 0;
        }

        if (mRecord == null){
            return 0;
        }

        int ret = mRecord.read(outputData, 0, size);

        return ret;
    }

    /**
     * 关闭录音 释放资源
     */
    public void stop(){
        if (mRecord == null){
            return;
        }
        isRecording = false;
        try{
            mRecord.stop();
        }catch (Exception e){
            Log.d("","关闭record 失败");
            //一些手机打开会失败 需要Release 和置空
            mRecord.release();
            mRecord=null;
            e.printStackTrace();
        }
    }

}
