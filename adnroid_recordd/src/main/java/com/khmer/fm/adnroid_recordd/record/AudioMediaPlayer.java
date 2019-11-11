package com.khmer.fm.adnroid_recordd.record;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * author : created by cui on 2019/10/10 11:46
 * MediaPlayer工具类
 */
public class AudioMediaPlayer {
    private String audioPath;
    private MediaPlayer mMediaPlayer;//负责播放音频
    private boolean isPause = false;
    private boolean isStart = false;//录音开始了后面加入背景音乐
    public void init(String path) {
        audioPath = path;
    }

    public void start() {
        release();
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPause = true;
        }
    }

    public void restart() {
        if (mMediaPlayer != null) {
            return;
        }
        if (isStart){
            start();
            isStart=false;

        }
        if (isPause) {
            mMediaPlayer.start();
            isPause = false;
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }
}
