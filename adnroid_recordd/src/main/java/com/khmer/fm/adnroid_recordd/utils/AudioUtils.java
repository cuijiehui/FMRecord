package com.khmer.fm.adnroid_recordd.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.khmer.fm.adnroid_recordd.record.RecordActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author : created by cui on 2019/10/14 15:35
 * Description: 音频文件工具类
 */
public class AudioUtils {

    private String wavToPcmPath = "";
    private String outputPath;
    private FileOutputStream mOutputWav;
    private File mOutputFile;
    private Handler mHandler;

    public void startWavToPcm(String inputPath, String outputPath, Handler handler) {
        this.wavToPcmPath = inputPath;
        this.outputPath = outputPath;
        this.mHandler = handler;
        wavToPcmThread.start();
    }

    private Thread wavToPcmThread = new Thread(new Runnable() {
        @Override
        public void run() {
            wavToPcmRun(wavToPcmPath, outputPath);
        }
    });

    private void wavToPcmRun(String inputPath, String outputPaths) {
        String outputPath;
        Message message = new Message();
        outputPath = outputPaths;
        mOutputFile = new File(outputPath);
        if (mOutputFile.exists()) {
            mOutputFile.delete();
        }
        try {
            mOutputWav = new FileOutputStream(mOutputFile);
            FileInputStream fileInputStream = new FileInputStream(new File(inputPath));
            //todo 去掉wav的头部
            byte[] buffer = new byte[1024];
            byte[] headBuffer = new byte[44];
            fileInputStream.read(headBuffer);
            while (fileInputStream.read(buffer) != -1) {
                mOutputWav.write(buffer);
            }
            headBuffer = null;
            buffer = null;
            mOutputWav.close();
            message.what = RecordActivity.AUDIO_TOPCM_SUCCEED;
            Log.d("wavToPcmRun", "wavToPcmRun=ok");
//            saveFileDetails(length);
        } catch (IOException e) {
            // TODO: 4/9/17 handle this
            message.what = RecordActivity.AUDIO_TOPCM_ERROR;
            e.printStackTrace();
        } finally {
            if (mHandler != null) {
                mHandler.sendMessage(message);
            }

        }
    }

    //调节PCM数据音量
    //pData原始音频byte数组，nLen原始音频byte数组长度，data2转换后新音频byte数组，nBitsPerSample采样率，multiple表示Math.pow()返回值
    public byte[] amplifyPCMData(byte[] pData, int nLen, int nBitsPerSample, float multiple) {
        Log.d("amplifyPCMData","multiple:"+multiple);
        int nCur = 0;
        byte[] data2 = new byte[nLen];
        if (16 == nBitsPerSample) {
            while (nCur < nLen) {
                short volum = getShort(pData, nCur);

                volum = (short) (volum * multiple);

                data2[nCur] = (byte) (volum & 0xFF);
                data2[nCur + 1] = (byte) ((volum >> 8) & 0xFF);
                nCur += 2;
            }

        }
        return data2;
    }

    private short getShort(byte[] data, int start) {
        return (short) ((data[start] & 0xFF) | (data[start + 1] << 8));
    }

}
