package com.khmer.fm.adnroid_recordd.record.mix;

/**
 * author : created by cui on 2019/10/9 16:53
 * Description:混音错误码
 */
public class MixError {
    private final static String TAG = MixError.class.getSimpleName();

    public final static int MIXERROR_OK = 0;
    public final static int MIXERROR_FAILED = -1;

    public final static int MIXERROR_SAMPLE_RATE = -2;//采样率错误
    public final static int MIXERROR_BITS = -3;//采样率错误
    public final static int MIXERROR_CHANNELS = -4;//声道数错误
    public final static int MIXERROR_INPUTSTREAMCOUNT = -5;//音轨数错误
    public final static int MIXERROR_INPUTBUFFERSIZE = -6;//输入buffer size错误
    public final static int MIXERROR_OUTPUTBUFFERSIZE = -7;//输出buffer size错误
    public final static int MIXERROR_OUTPATH=-8;//获取写入混音文件的路径失败
    public final static int MIXERROR_NOSUCHSTREAM = -8;//无此输入流
    public final static int MIXERROR_BUFFERFULL = -9;//BUFFER已满
    public final static int MIXERROR_NO_NEET_BG = -10;//不需要混音

}
