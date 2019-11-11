package com.khmer.fm.adnroid_recordd.record.mix;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;


import com.khmer.fm.adnroid_recordd.Constants;
import com.khmer.fm.adnroid_recordd.record.AudioMediaPlayer;
import com.khmer.fm.adnroid_recordd.record.AudioTrackPlayer;
import com.khmer.fm.adnroid_recordd.utils.AudioUtils;

import java.io.File;

/**
 * author : created by cui on 2019/10/9 17:29
 * Description:管理录音与混音
 */
public class MixManager {
    private final static String TAG = MixManager.class.getSimpleName();

    private final static int RECORD_STREAM = 0;
    private final static int TRACK_STREAM = 1;
    private final static int AUDIO_BUFFER_SIZE = 4 * 1024;

    private int mSampleRate = MixParams.SampleRateOptions.SAMPLERATE_44100;
    private int mChannels = MixParams.ChannelsOptions.CHANNEL_2;
    private int mBits = MixParams.BitsOptions.BITS_16;

    private VRecorder mRecorder;
    private AudioTrackPlayer mPlayer;//pcm 文件音频太烂音质
    private AudioMediaPlayer audioMediaPlayer;
    private BgMusicProcessor mBgMusicProcessor;
    private MixUtils mMixUtils;

    private InputThread mInputThread;
    private OutputThread mOutputThread;
    private boolean isPause = false;
    private int mVolume = 0;
    RecordVolumeListener mRecordVolumeListener;
    private long millisInFuture = 60 * 60 * 1000;//倒计时时间
    private long countDownInterval = 200;//200毫秒走一次Ontick
    private AudioUtils mAudioUtils;
    private int db = -4;
    private double factor = Math.pow(10, (double) db / 20);//音量值
    private RecordSaveUtils mRecordSaveUtils;
    private String mBgPath;

    /**
     * 倒计时60秒，一次1秒
     */
    CountDownTimer timer = new CountDownTimer(millisInFuture, countDownInterval) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mRecordVolumeListener != null && !isPause) {
                mRecordVolumeListener.backVolume(mVolume);
            }
        }

        @Override
        public void onFinish() {
//
        }
    };


    public MixManager(String bgPath) {
        init(bgPath);
    }

    /**
     * 初始化
     *
     * @param bgPath 可以为null
     */
    private void init(String bgPath) {
        mBgPath = bgPath;
        mRecorder = new VRecorder(mSampleRate, mBits, mChannels);
        mPlayer = new AudioTrackPlayer(mSampleRate, mBits, mChannels);//需要的就播放
        mBgMusicProcessor = new BgMusicProcessor(bgPath);
        audioMediaPlayer = new AudioMediaPlayer();//
        audioMediaPlayer.init(Constants.BG_MUSIC_WAV_PATH);
        mMixUtils = new MixUtils();
        mMixUtils.Create(mSampleRate, mBits, mChannels, 2, 10 * 1024, 10 * 1024);
        mAudioUtils = new AudioUtils();
        File file = new File(Constants.RECORD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

    }

    public RecordVolumeListener getmRecordVolumeListener() {
        return mRecordVolumeListener;
    }

    public void setmRecordVolumeListener(RecordVolumeListener mRecordVolumeListener) {
        this.mRecordVolumeListener = mRecordVolumeListener;
    }

    /**
     * 启动线程
     */
    public void start() {
        Log.d(TAG, "start=MixManager");

        mInputThread = new InputThread();//把录音数据和背景数据写入键值对保存
//        mOutputThread = new OutputThread();//把录音数据和背景数据写入键值对保存 输出给audioTrack播放
        mRecorder.start();//启动录音
        mBgMusicProcessor.start();//准备背景音乐
        mRecordSaveUtils = new RecordSaveUtils();//设置输出准备
        mRecordSaveUtils.createNewFile(Constants.RECORD_MIX_FILE_PATH);
        mInputThread.startInput();
        if (!TextUtils.isEmpty(mBgPath)) {
            audioMediaPlayer.start();
        }

        Log.d(TAG, "start=MixManager=mInputThread");

//        mOutputThread.startOutput();//边录边播
        timer.start();//一秒获取一次声量
    }

    /**
     * 设置背景音乐数据
     *
     * @param path
     */
    public void setBgMusic(String path) {
        audioMediaPlayer.init(Constants.BG_MUSIC_WAV_PATH);
        if (!TextUtils.isEmpty(mBgPath)) {
            audioMediaPlayer.setStart(true);
        }
        mBgPath=path;
        mBgMusicProcessor.setPath(path);
    }

    /**
     * 停止录音 释放资源
     */
    public void stop() {
        try {
            audioMediaPlayer.release();
            mInputThread.stopInput();
            recordSaveStop();
//            mOutputThread.stopOutput();
            mRecorder.stop();
            mPlayer.stop();
            mBgMusicProcessor.stop();
            mMixUtils.reset();

            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mInputThread = null;
            mOutputThread = null;
            mVolume = 0;

        }

    }

    /**
     * 设置录音释放有效
     *
     * @param isEnable
     */
    public void setRecorderEnable(boolean isEnable) {
        if (mMixUtils != null) {
            mMixUtils.SetStreamValid(RECORD_STREAM, isEnable);
        }
    }

    /**
     * 获取录音释放有效
     *
     * @return
     */
    public boolean isRecordEnable() {
        if (mMixUtils == null) {
            return false;
        }
        return mMixUtils.isValidStream(RECORD_STREAM);
    }

    /**
     * 设置背景音乐是否有效
     *
     * @param isEnable
     */
    public void setBgEnable(boolean isEnable) {
        if (mMixUtils != null) {
            mMixUtils.SetStreamValid(TRACK_STREAM, isEnable);
        }
    }

    /**
     * 停止录音关闭输出文件
     */
    public void recordSaveStop() {
        if (mRecordSaveUtils != null) {

            mRecordSaveUtils.onRecordingStopped();
        }
    }

    /**
     * 试听后再录音 重新设置输出文件 继续输出
     */
    public void recordSaveReset() {
        if (mRecordSaveUtils != null) {
            mRecordSaveUtils.resetInfo();
        }
    }

    /**
     * 获取背景音乐是否有效
     *
     * @return
     */
    public boolean isBgEnable() {
        if (mMixUtils == null) {
            return false;
        }
        return mMixUtils.isValidStream(TRACK_STREAM);
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    class InputThread extends Thread {
        private boolean isRunning = false;

        public InputThread() {

        }

        public void startInput() {
            isRunning = true;
            start();
        }

        public void stopInput() {
            isRunning = false;
        }

        @Override
        public void run() {
            byte[] recordBuffer = new byte[AUDIO_BUFFER_SIZE];//录音的数组
            byte[] trackBuffer = new byte[AUDIO_BUFFER_SIZE];//背景音乐的数组
            byte[] outputbuffer = new byte[AUDIO_BUFFER_SIZE];

//            mPlayer.start(); //启动播放器
            while (isRunning) {
                if (!isPause) {
                    int realRecordSize = mRecorder.getData(recordBuffer, AUDIO_BUFFER_SIZE);//写入录音数据
                    int realTrackSize = mBgMusicProcessor.getData(trackBuffer, AUDIO_BUFFER_SIZE);//写入背景音乐数据
//                    if (realTrackSize> 0) {
//                        mPlayer.play(trackBuffer, AUDIO_BUFFER_SIZE);
//                    }
                    if (!Constants.isHeadSet) {//根据耳机的情况 确定是否需要背景音乐
                        //处理背景音乐声量
                        trackBuffer = mAudioUtils.amplifyPCMData(trackBuffer, trackBuffer.length, 16, (float) factor);
                    }
                    setBgEnable(Constants.isHeadSet);
                    mMixUtils.SetData(RECORD_STREAM, recordBuffer, realRecordSize);//把录音数据写入mix中
                    mMixUtils.SetData(TRACK_STREAM, trackBuffer, realTrackSize);//把背景音乐写入mix中
                    int mixSize = mMixUtils.GetData(outputbuffer, AUDIO_BUFFER_SIZE);
                    calculateRealVolume(outputbuffer);
                    if (mRecordSaveUtils != null && mixSize > 0) {
                        mRecordSaveUtils.onDataReady(outputbuffer, mixSize);
                    }
                    if (!TextUtils.isEmpty(mBgPath)) {
                        audioMediaPlayer.restart();
                    }
                }else{
                    if (!TextUtils.isEmpty(mBgPath)) {
                        audioMediaPlayer.pause();
                    }
                }

            }
        }
    }

    private void calculateRealVolume(byte[] buffer) {
        mVolume = (int) doublecalculateVolume(buffer);
    }

    private double doublecalculateVolume(byte[] buffer) {

        double sumVolume = 0.0;

        double avgVolume = 0.0;

        double volume = 0.0;

        for (int i = 0; i < buffer.length; i += 2) {

            int v1 = buffer[i] & 0xFF;

            int v2 = buffer[i + 1] & 0xFF;

            int temp = v1 + (v2 << 8);// 小端

            if (temp >= 0x8000) {

                temp = 0xffff - temp;

            }

            sumVolume += Math.abs(temp);

        }

        avgVolume = sumVolume / buffer.length / 2;

        volume = Math.log10(1 + avgVolume) * 10;

        return volume;

    }

    class OutputThread extends Thread {
        private boolean isRunning = false;

        public OutputThread() {

        }

        public void startOutput() {
            isRunning = true;
            start();
        }

        public void stopOutput() {
            isRunning = false;
        }

        @Override
        public void run() {
            byte[] recordBuffer = new byte[AUDIO_BUFFER_SIZE];
            byte[] trackBuffer = new byte[AUDIO_BUFFER_SIZE];
            byte[] outputbuffer = new byte[AUDIO_BUFFER_SIZE];

            mPlayer.start(); //启动播放器
            while (isRunning) {
                if (!isPause) {
                    if (!isPause) {
                        int realRecordSize = mRecorder.getData(recordBuffer, AUDIO_BUFFER_SIZE);//获取录音数据
                        int realTrackSize = mBgMusicProcessor.getData(trackBuffer, AUDIO_BUFFER_SIZE);//获取背景数据
                        mMixUtils.SetData(RECORD_STREAM, recordBuffer, realRecordSize); //写入录音数据
                        mMixUtils.SetData(TRACK_STREAM, trackBuffer, realTrackSize);//写入背景数据

                        int realSize = mMixUtils.GetData(outputbuffer, AUDIO_BUFFER_SIZE);//获取混合输出流
                        if (realSize <= 0) {
                            continue;
                        }
                        calculateRealVolume(outputbuffer);
                        mPlayer.play(outputbuffer, realSize);//播放流音乐
                    }
                }

            }
        }


    }
}
