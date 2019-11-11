package com.khmer.fm.adnroid_recordd.record.mix;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;


import com.khmer.fm.adnroid_recordd.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * author : created by cui on 2019/10/9 16:49
 * Description: 混音工具
 */
public class MixUtils {
    private final static String TAG = MixUtils.class.getSimpleName();

    private int mSampleRate;
    private int mBits;
    private int mChannels;
    private int mInputStreamCount;
    private int mMaxInputBufferSize;
    private int mMaxOutputBufferSize;
    private int blockCount = 0;//用于计算有多少个的数据

    private SparseArray<ByteRingBuffer> mInputStreamBuffers;//键值对数据，保存了录音和背景音乐
    private SparseBooleanArray mInputStreamValid;

    private ByteRingBuffer mOutputStreamBuffer;


    public MixUtils() {
    }

    /**
     * 传参数准备混音
     * @param sampleRate 波特率
     * @param bits 传输
     * @param channels 通道
     * @param inputStreamCount 混音流的个数
     * @param maxInputBufferSize 写入的最大
     * @param maxOutputBufferSize 写出的最大
     * @return 0为成功
     */
    public int Create(int sampleRate, int bits, int channels, int inputStreamCount, int maxInputBufferSize, int maxOutputBufferSize){
        Log.d(TAG,"Create=MixUtils=");

        if (!MixParams.SampleRateOptions.isValidSampleRate(sampleRate)){
            return MixError.MIXERROR_SAMPLE_RATE;
        }

        if (!MixParams.BitsOptions.isValidBits(bits)){
            return MixError.MIXERROR_BITS;
        }

        if (!MixParams.ChannelsOptions.isValidChannels(channels)){
            return MixError.MIXERROR_CHANNELS;
        }

        if (inputStreamCount <= 0 || inputStreamCount > MixParams.BoundaryValue.MAX_STREAMCOUNT){
            return MixError.MIXERROR_INPUTSTREAMCOUNT;
        }

        if (maxInputBufferSize <= 0 || maxInputBufferSize > MixParams.BoundaryValue.MAX_BUFFERSIZE){
            return MixError.MIXERROR_INPUTBUFFERSIZE;
        }

        if (maxOutputBufferSize <= 0 || maxOutputBufferSize > MixParams.BoundaryValue.MAX_BUFFERSIZE){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        mSampleRate = sampleRate;
        mBits = bits;
        mChannels = channels;
        mInputStreamCount = inputStreamCount;
        mMaxInputBufferSize = maxInputBufferSize;
        mMaxOutputBufferSize = maxOutputBufferSize;

        //input stream buffer
        mInputStreamBuffers = new SparseArray<>();
        mInputStreamValid = new SparseBooleanArray();

        for (int i = 0; i < inputStreamCount; i++){
            ByteRingBuffer inputBuf = new ByteRingBuffer(maxInputBufferSize);
            mInputStreamBuffers.put(i, inputBuf);
            mInputStreamValid.put(i, true);
        }

        //output stream buffer
        mOutputStreamBuffer = new ByteRingBuffer(maxOutputBufferSize);
        Log.d(TAG,"Create=return=");

        return MixError.MIXERROR_OK;
    }

    /**
     *  把数据写于mixutils使用
     * @param streamNO 数据的类型 0 为录音 1 为背景音乐
     * @param data 数据
     * @param size 数据大小
     * @return
     */
    public int SetData(int streamNO, byte[] data, int size){
        if (!isValidStream(streamNO)){
            return MixError.MIXERROR_NOSUCHSTREAM;
        }

        if (size <= 0 || size > MixParams.BoundaryValue.MAX_BUFFERSIZE){
            return MixError.MIXERROR_INPUTBUFFERSIZE;
        }
        ByteRingBuffer inputBuf = mInputStreamBuffers.get(streamNO);
        if (inputBuf.getFree() < size){
            return MixError.MIXERROR_BUFFERFULL;
        }

        int ret = inputBuf.write(data);

        return ret;
    }

    /**
     * 获取混音后的数据
     * @param outputData 混音后的数据
     * @param size 数据大小
     * @return -1为失败
     */
    public int GetData(short[] outputData, int size){

        if (size > MixParams.BoundaryValue.MAX_BUFFERSIZE || size <= 0){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        if ((outputData.length * 2) < size){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        byte[] readBuf = new byte[size];

        int ret = GetData(readBuf, size);

        if (ret < 0){
            return ret;
        }

        short[] outputBuf = ByteBuffer.wrap(readBuf, 0, ret).asShortBuffer().array();

        System.arraycopy(outputBuf, 0, outputData, 0, outputBuf.length);

        return ret;
    }

    /**
     * 获取混音后的数据
     * @param outputData 混音后的数据
     * @param size 数据大小
     * @return -1为失败
     */
    public int GetData(byte[] outputData, int size){
        blockCount ++;
        if (size > MixParams.BoundaryValue.MAX_BUFFERSIZE || size <= 0){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        if (outputData.length < size){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        Mix2(mInputStreamBuffers.get(0), mInputStreamBuffers.get(1));//混合两个流

//        Mix();

        if (mOutputStreamBuffer.getUsed() < size){
            return MixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        int ret = mOutputStreamBuffer.read(outputData, 0, size); //把混合的数据传出去

        return ret;
    }

    /**
     * 设置流的声量
     * @param streamNO 流的类型 0为录音 1为背景
     * @param vol 声量
     * @return 还没实现
     */
    public int SetStreamVolume(int streamNO, int vol){
        return MixError.MIXERROR_OK;
    }

    /**
     * 设置流是否有效
     * @param streamNo 流的类型 0为录音 1为背景
     * @param isValid
     * @return
     */
    public int SetStreamValid(int streamNo, boolean isValid){
        if (mInputStreamValid == null){
            return MixError.MIXERROR_FAILED;
        }

        if (streamNo < 0 || streamNo >= mInputStreamValid.size()){
            return MixError.MIXERROR_NOSUCHSTREAM;
        }

        mInputStreamValid.put(streamNo, isValid);

        return MixError.MIXERROR_OK;
    }



    /**
     * 每次mix，是取出所有valid的stream，比较出最小buffer size，取这个size的长度buffer进行相加并输出
     */
    private void Mix(){
        int minValidSize = MixParams.BoundaryValue.MAX_BUFFERSIZE;
        SparseArray<ByteRingBuffer> validStreams = new SparseArray<>();
        //取出可用的流
        for (int i = 0; i < mInputStreamCount; ++i){
            if (!isValidStream(i)){
                continue;
            }
            ByteRingBuffer buffer = mInputStreamBuffers.get(i);
            if (buffer == null){
                continue;
            }
            validStreams.put(i, buffer);
            if (minValidSize > buffer.getUsed()){
                minValidSize = buffer.getUsed();
            }
        }

        if (minValidSize <= 0){
            return;
        }

        if (validStreams.size() <= 0){
            return;
        }

        //对每个流取出minValidSize的byte[]转为short[],再相加输出到outputStream

        short[] outputBuffer = new short[minValidSize/2 + minValidSize % 2];//初始化为0

        int minSize = 0;
        for (int i = 0; i < validStreams.size(); ++i){
            ByteRingBuffer buffer = validStreams.valueAt(i);
            if (buffer == null){
                continue;
            }

            byte[] readBuf = new byte[minValidSize];
            int readSize = buffer.read(readBuf);
            if (readSize <= 0){
                continue;
            }

//            short[] tempBuffer = ByteBuffer.wrap(readBuf).asShortBuffer().array();
            short[] tempBuffer = new short[minValidSize/2 + minValidSize%2];
            ByteBuffer.wrap(readBuf).asShortBuffer().get(tempBuffer);
            minSize = outputBuffer.length > tempBuffer.length ? tempBuffer.length : outputBuffer.length;

            for (int j = 0; j < minSize; ++j){
                int add1 = Short.valueOf(outputBuffer[j]).intValue();
                int add2 = Short.valueOf(tempBuffer[j]).intValue();
                int sum = (add1 + add2);
//                int sum = 0;
//                if (add1 < 0 && add2 < 0){
//                    sum = add1 + add2 - (int)(add1 * add2 / -(Math.pow(2.0, 15.0) - 1));
//                }else {
//                    sum = add1 + add2 - (int)(add1 * add2 / (Math.pow(2.0, 15.0) - 1));
//                }

                if (sum > Short.MAX_VALUE){
                    sum = Short.MAX_VALUE;
                }

                if (sum < Short.MIN_VALUE){
                    sum = Short.MIN_VALUE;
                }

                //这种地方就不要打日志了
//                Logger.i(TAG, "sum: [%d] + [%d] = [%d]", add1, add2, sum);

                outputBuffer[j] = Integer.valueOf(sum).shortValue();
            }
        }

        if (minSize <= 0){
            return;
        }

        ByteBuffer bb = ByteBuffer.allocate(minSize * 2);
        bb.asShortBuffer().put(outputBuffer);

        if (mOutputStreamBuffer.getFree() < bb.array().length){
            return;
        }

        mOutputStreamBuffer.write(bb.array());

    }

    /**
     * 混合两个音频文件的数据
     * @param stream1 录音
     * @param stream2 背景音乐
     */
    private void Mix2(ByteRingBuffer stream1, ByteRingBuffer stream2){

        if (stream1 == null || stream2 == null){
            return;
        }

        //两个都没有数据
        if ((stream1.getUsed() + stream2.getUsed()) == 0){
            return;
        }

        //只有stream1
        if (stream1.getUsed() > 0 && stream2.getUsed() == 0 || Constants.isHeadSet){
            if (stream1.getUsed() <= mOutputStreamBuffer.getFree()){
                byte[] buffer = new byte[stream1.getUsed()];
                stream1.read(buffer);
                if (blockCount % 1500 == 0){
                    if (buffer.length > 40){
                        for (int i = 0; i < 20; ++i){
                            buffer[i*2] = 0;
                            buffer[i*2 + 1] = 0x40;
                        }
                        for (int i = 20; i < 40; ++i){
                            buffer[i*2] = 0;
                            buffer[i*2 + 1] = -0x40;
                        }
                    }
                }

                mOutputStreamBuffer.write(buffer);
            }
            return;
        }
        //只有stream2 只有背景就不用写入去
        if (stream1.getUsed() == 0 && stream2.getUsed() > 0){
//            if (stream2.getUsed() <= mOutputStreamBuffer.getFree()){
//                byte[] buffer = new byte[stream2.getUsed()];
//                stream2.read(buffer);
//                mOutputStreamBuffer.write(buffer);
//            }
            return;
        }
        //两个都有数据
        //对每个流取出minValidSize的byte[]转为short[],再相加输出到outputStream
        int minValidSize = stream1.getUsed() > stream2.getUsed() ? stream2.getUsed() : stream1.getUsed();

        byte[] bBuffer1 = new byte[minValidSize];
        byte[] bBuffer2 = new byte[minValidSize];

        int readSize1 = stream1.read(bBuffer1);
        int readSize2 = stream2.read(bBuffer2);

        int sSize = minValidSize/2 + minValidSize%2;
        short[] sBuffer1 = new short[sSize];
        short[] sBuffer2 = new short[sSize];
        short[] outputBuffer = new short[sSize];//初始化为0

        ByteBuffer.wrap(bBuffer1).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer1);
        ByteBuffer.wrap(bBuffer2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer2);

        for (int i = 0; i < sSize; ++i){
            //mix1
//            int add1 = Short.valueOf(sBuffer1[i]).intValue();
//            int add2 = Short.valueOf(sBuffer2[i]).intValue();
//            int sum = (add1 + add2);
            //mix2
//            int sum = 0;
//            if (add1 < 0 && add2 < 0){
//                sum = add1 + add2 - (int)(add1 * add2 / -(Math.pow(2.0, 15.0) - 1));
//            }else {
//                sum = add1 + add2 - (int)(add1 * add2 / (Math.pow(2.0, 15.0) - 1));
//            }

            //mix3
            int sum = (sBuffer1[i] + sBuffer2[i])/2;

            if (sum > Short.MAX_VALUE){
                sum = Short.MAX_VALUE;
            }

            if (sum < Short.MIN_VALUE){
                sum = Short.MIN_VALUE;
            }

            //这种地方就不要打日志了
//          Logger.i(TAG, "sum: [%d] + [%d] = [%d]", add1, add2, sum);

            outputBuffer[i] = Integer.valueOf(sum).shortValue();
        }

        ByteBuffer bb = ByteBuffer.allocate(sSize * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outputBuffer);

        if (mOutputStreamBuffer.getFree() < bb.array().length){
            return;
        }

        mOutputStreamBuffer.write(bb.array());

    }

    /**
     * 获取是否有效
     * @param streamNO 流类型 0为录音 1为背景
     * @return
     */
    public boolean isValidStream(int streamNO){
        if (mInputStreamValid == null){
            return false;
        }

        return mInputStreamValid.get(streamNO);
    }

    /**
     * 释放资源
     */
    public void reset(){
        //input stream buffer
        blockCount = 0;
        for (int i = 0; i < mInputStreamBuffers.size(); ++i){
            mInputStreamBuffers.valueAt(i).clear();
        }

        for (int i = 0; i < mInputStreamValid.size(); ++i){
            mInputStreamValid.put(i, true);
        }
        //output stream buffer
        mOutputStreamBuffer.clear();
    }
}
