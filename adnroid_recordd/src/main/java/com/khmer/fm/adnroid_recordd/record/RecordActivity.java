package com.khmer.fm.adnroid_recordd.record;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.khmer.fm.adnroid_recordd.Constants;
import com.khmer.fm.adnroid_recordd.R;
import com.khmer.fm.adnroid_recordd.eventbus.BgMusicBackEvent;
import com.khmer.fm.adnroid_recordd.eventbus.EditTextEventbus;
import com.khmer.fm.adnroid_recordd.ffmpeg.FFmpegCmd;
import com.khmer.fm.adnroid_recordd.record.bean.BgMusicBean;
import com.khmer.fm.adnroid_recordd.record.mix.MixManager;
import com.khmer.fm.adnroid_recordd.record.mix.RecordVolumeListener;
import com.khmer.fm.adnroid_recordd.record.view.BgMusicView;
import com.khmer.fm.adnroid_recordd.record.view.LyricView;
import com.khmer.fm.adnroid_recordd.utils.AudioUtils;
import com.khmer.fm.adnroid_recordd.utils.ConstomDialog;
import com.khmer.fm.adnroid_recordd.utils.TimeUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import pub.devrel.easypermissions.EasyPermissions;

public class RecordActivity extends Activity implements EasyPermissions.PermissionCallbacks, RecordVolumeListener, View.OnClickListener {
    public static final String TAG = RecordActivity.class.getSimpleName();
    private static final int PERMISSION_REQ = 222;
    private static final int START_RECORD_TIME = 456;
    private static final int STOP_RECORD_TIME = 654;
    private final static int FFMPEG_MSG_BEGIN = 11;
    private final static int FFMPEG_MSG_FINISH = 12;
    public final static int AUDIO_TOPCM_SUCCEED = 13;
    public final static int AUDIO_TOPCM_ERROR = -13;

    private LinearLayout mLlToBgMusic;//选择配乐
    private BgMusicView mBMVVolume;//动态录音图
    private LyricView mLvLyric;//文稿view
    private ImageView titleBack;//返回键
    private ImageView editTest;//写文稿
    private TextView recordTime;//时间view
    private ImageView recordControl;//录音控制
    private ImageView recordFinish;//录音结束
    private TextView selectMusic;//背景音乐文本
    private ImageView tryPlay;
    private String mBgPath;//转成pcm的背景音乐文件地址
    private MixManager mMixManager;//录音管理类

    private boolean isPlaying = false;//是否录音
    private boolean isPause = false;//是否暂停
    private boolean isTryPlaying = false; //是否再播放试听
    private boolean isFFMPEG = false; //是否正在转码
    private boolean isRecord = false;//是否开始过录音

    private LinkedList<Integer> volumeData = new LinkedList<>(); //音频声量数据
    private int mRecordTime = 0;//录音时间 秒
    private int mVolumeCount = 0;//传递给切割界面 现在还没使用
    private ProgressDialog mProgressDialog;//解码时用的加载框
    private String mInputPath;//传递过来的背景音频文件
    private String mMP3OutputPath;
    private HeadSetReceiver mReceiver;//耳机插入监听广播
    private MediaPlayer mMediaPlayer;//负责播放音频
    private boolean isTryplay = false;
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};//录音和文件权限
    private int FFMPEG_TYPE = 0;//用于区分使用ffmpeg的使用类型 0为mp3转wav 1为wav转mp3
    private BgMusicBean mBgMusicBean;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@Nonnull Message msg) {
            switch (msg.what) {
                case START_RECORD_TIME: //计时器 1秒
                    mRecordTime++;
                    recordTime.setText(TimeUtils.getMSTime(mRecordTime));
                    Message message = new Message();
                    message.what = START_RECORD_TIME;
                    mHandler.sendMessageDelayed(message, 1000);
                    break;
                case STOP_RECORD_TIME://取消计时器
                    this.removeMessages(START_RECORD_TIME);
                    break;
                case FFMPEG_MSG_BEGIN://解码开始
                    Log.d(TAG, "FFMPEG_MSG_BEGIN");
                    showLoadingDialog();
                    break;
                case FFMPEG_MSG_FINISH://解码结束 去转pcm
                    Log.d(TAG, "FFMPEG_MSG_FINISH");
                    switch (FFMPEG_TYPE) {
                        case 0://mp3转wav
                            wavToPcm();
                            break;
                        case 1://wav转mp3
                            startRNActivity(Constants.RECORD_MIX_MP3_FILE_PATH);
                            hideDialog();
                            isFFMPEG=false;
                            break;
                        default:
                            break;
                    }
                    break;
                case AUDIO_TOPCM_SUCCEED://转pcm成功 设置背景音乐
                    Log.d(TAG, "FFMPEG_MSG_FINISH");
                    hideDialog();
                    setBGMusicView();
                    isFFMPEG = false;
                    break;
                case AUDIO_TOPCM_ERROR:
                    Log.d(TAG, "FFMPEG_MSG_FINISH");
                    Toast.makeText(RecordActivity.this, "解释背景音乐失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private double language;
    private ConstomDialog mConstopDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        findView();
        initListener();
        initReceiver();
        Constants.RECORD_PATH =getFilePath(this,"Record");
        mBgMusicBean = new BgMusicBean();
        language = getIntent().getDoubleExtra("language",0);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
        stopRecord();
        isRecord=false;
    }

    /**
     * 注册耳机使用情况的广播
     */
    private void initReceiver() {
        mReceiver = new HeadSetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 文案信息
     *
     * @param editTextEventbus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEditTextEvent(EditTextEventbus editTextEventbus) {
        ArrayList<String> strings = new ArrayList<>();
        String[] contents = editTextEventbus.getContent().split("\n");
        for (String content : contents) {
            Log.d("onMessageEvent", "content=" + content);
            strings.add(content);
        }
        Log.d("onMessageEvent", "strings=" + strings.size());
        mLvLyric.setLrcRows(strings);

    }

    /**
     * 背景音乐信息
     *
     * @param bgMusicBackEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBgMusicBackEvent(BgMusicBackEvent bgMusicBackEvent) {
        mBgMusicBean = bgMusicBackEvent.getBgMusicBean();
        Log.d(TAG, "BgMusicBean = " + mBgMusicBean.toString());
        if (!TextUtils.isEmpty(mBgMusicBean.getPath())) {
            mp3ToWav(mBgMusicBean.getPath(), Constants.BG_MUSIC_WAV_PATH);
        }
    }

    /**
     * 设置控件点击事件
     */
    private void initListener() {
        titleBack.setOnClickListener(this);
        editTest.setOnClickListener(this);
        recordControl.setOnClickListener(this);
        recordFinish.setOnClickListener(this);
        mLlToBgMusic.setOnClickListener(this);
        tryPlay.setOnClickListener(this);
    }

    /**
     * 初始化控件对象
     */
    private void findView() {
        mLlToBgMusic = findViewById(R.id.ll_to_bg_music);
        mBMVVolume = findViewById(R.id.bmv_volume);
        mLvLyric = findViewById(R.id.lv_lyric);
        titleBack = findViewById(R.id.iv_title_back);
        editTest = findViewById(R.id.iv_edit_test);
        recordTime = findViewById(R.id.tv_record_time);
        recordControl = findViewById(R.id.iv_record_control);
        recordFinish = findViewById(R.id.iv_record_finish);
        selectMusic = findViewById(R.id.tv_select_music);
        tryPlay = findViewById(R.id.iv_try_play);
    }


    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @Nonnull String[] permissions,
                                           @Nonnull int[] grantResults) {
        if (PERMISSION_REQ == requestCode) {
            for (int x : grantResults) {
                if (x == PackageManager.PERMISSION_DENIED) {
                    //权限拒绝了
                    return;
                }
            }
            controlRecord();
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 允许权限成功后触发
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    /**
     * 禁止权限后触发
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            showRationale();
            return;
        }
    }

    /**
     * 显示权限框
     */
    private void showRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
        builder.setTitle("Permissions Required")
                .setCancelable(false)
                .setMessage(
                        getString(R.string.permissions_required))
                .setPositiveButton(R.string.dialog_action_ok, (dialog, which) -> {
                    openSettingsPage();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_action_cancel,
                        (dialog, which) -> onBackPressed())
                .show();
    }

    /**
     * 打开系统权限框
     */
    private void openSettingsPage() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PERMISSION_REQ);
    }

    /**
     * 初始化record
     */
    public void initRecord() {
        mMixManager = new MixManager(mBgPath);
        mMixManager.setmRecordVolumeListener(this);
    }
    public static String getFilePath(Context context, String dir) {
        String directoryPath="";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ) {//判断外部存储是否可用
            directoryPath =context.getExternalFilesDir(dir).getAbsolutePath();
        }else{//没外部存储就使用内部存储
            directoryPath=context.getFilesDir()+ File.separator+dir;
        }
        File file = new File(directoryPath);
        if(!file.exists()){//判断文件目录是否存在
            file.mkdirs();
        }
        return directoryPath;
    }

    private void pauseTryRecord(){
        if (mMediaPlayer != null) {
            Log.i("Debug ", "Stopping");
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    /**
     * 去试听界面
     *
     *
     */
    public void tryRecord() {
        if (mMixManager != null) {
            isTryPlaying = true;
            mMixManager.recordSaveStop();
        }

        try {
            if (mMediaPlayer != null) {
                Log.i("Debug ", "Stopping");
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(Constants.RECORD_MIX_FILE_PATH);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
//            mMediaPlayer.setOnCompletionListener(mp -> { //进度条
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        initRecord();
        if (mMixManager == null) {
            Log.d(TAG, "mMixManager=null");
            return;
        }
        recordControl.setBackgroundResource(R.drawable.record_srop_icon);
        recordTime.setTextColor(getResources().getColor(R.color.color_ff4a3f));
        mMixManager.start();
        isPlaying = true;
        isRecord=true;
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        if (mMixManager == null) {
            return;
        }
        mMixManager.stop();
        isPlaying = false;
        volumeData.clear();
        mVolumeCount = 0;
        recordControl.setBackgroundResource(R.drawable.start_record_icon);

    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        isPause = true;
        Message message = new Message();
        message.what = STOP_RECORD_TIME;
        mHandler.sendMessage(message);
        if (mMixManager == null) {
            return;
        }
        mMixManager.setPause(true);
        recordControl.setBackgroundResource(R.drawable.start_record_icon);

    }

    /**
     * 重新打开录音
     */
    public void restartRecord() {
        isPause = false;
        Message message = new Message();
        message.what = START_RECORD_TIME;
        mHandler.sendMessageDelayed(message, 1000);
        if (mMixManager == null) {
            return;
        }
        if (isTryPlaying) {
            mMixManager.recordSaveReset();
        }
        mMixManager.setPause(false);
        recordControl.setBackgroundResource(R.drawable.record_srop_icon);

    }

    /**
     * 获取声量更新UI
     *
     * @param volume 音量  一秒5条数据
     */
    @Override
    public void backVolume(int volume) {
        Log.d(TAG, "Volume = " + volume);
        if (mBMVVolume != null) {
            mBMVVolume.setVolumeData(volume);
        }
    }

    /**
     * 各控件的点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();//返回键
//去写文稿界面
//去选择配乐界面
//控制录音开始暂停
//控制录音结束
        if (i == R.id.iv_title_back) {
            stopRecord();
            if (isRecord) {
                showDialog("是否放弃这次录音?", v1 -> {
                    //如果对话框处于显示状态
                    if (mConstopDialog != null && mConstopDialog.isShowing()) {
                        //关闭对话框
                        mConstopDialog.dismiss();
                        //可能要删除文件
                        finish();

                        //关闭当前界面
                    }

                });
            }
        } else if (i == R.id.iv_edit_test) {
            if (isPlaying && !isPause) {
                pauseRecord();
            }
            Intent intent = new Intent(this, EditTextActivity.class);
            StringBuffer editText = new StringBuffer();
            for (String s : mLvLyric.getLrcRowList()) {
                editText.append(s);
                editText.append("\n");
            }
            intent.putExtra("EditText", editText.toString());
            startActivity(intent);
        } else if (i == R.id.ll_to_bg_music) {
            if (isPlaying && !isPause) {
                pauseRecord();
            }
            try {
                Class toActivity = Class.forName("com.khmer.fm.RNActivity");
                Intent intentToRN = new Intent();
                intentToRN.putExtra("type", "addMusic");
                intentToRN.setClass(this, toActivity);
                intentToRN.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentToRN);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (i == R.id.iv_record_control) {//去检查权限
            //权限判断，第一次弹出系统的授权提示框
            if (EasyPermissions.hasPermissions(this, permissions)) {
                controlRecord();
            } else {
                //没有权限的话，先提示，点确定后弹出系统的授权提示框
                EasyPermissions.requestPermissions(this, getString(R.string.permissions_required),
                        PERMISSION_REQ, permissions);
            }
        } else if (i == R.id.iv_record_finish) {
            stopRecord();
            if (isRecord) {
                showDialog("是否完成录音", v1 -> {
                    //如果对话框处于显示状态
                    if (mConstopDialog != null && mConstopDialog.isShowing()) {
                        //关闭对话框
                        mConstopDialog.dismiss();
                        wavToMp3();
                        //关闭当前界面
                    }

                });
            } else {
                showDialog("你没有录音！", v1 -> {
                    if (mConstopDialog != null && mConstopDialog.isShowing()) {
                        //关闭对话框
                        mConstopDialog.dismiss();
                        //关闭当前界面
                    }
                });
            }
        } else if (i == R.id.iv_try_play) {
            if (isPlaying && !isPause) {
                pauseRecord();
            }
            if (isTryplay) {
                isTryplay = false;
                pauseTryRecord();
                tryPlay.setBackgroundResource(R.drawable.record_try_play_icon);
            } else {
                tryRecord();
                isTryplay = true;
                tryPlay.setBackgroundResource(R.drawable.pause_record_icon);
            }
        }
    }

    private void startRNActivity(String lacalUrl) {
        try {
            Class toActivity = Class.forName("com.khmer.fm.RNActivity");
            Intent intentToRN = new Intent();
            intentToRN.putExtra("type", "complete");
            intentToRN.putExtra("localUrl", lacalUrl);
            intentToRN.setClass(this, toActivity);
            intentToRN.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentToRN);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 操作录音 开始 暂停 restart
     */
    private void controlRecord() {
        if (!isPlaying) {//开始录音
            mRecordTime = 0;
            Message message = new Message();
            message.what = START_RECORD_TIME;
            mHandler.sendMessageDelayed(message, 1000);
            startRecord();
        } else {
            if (isPause) {
                restartRecord();//打开录音
            } else {
                pauseRecord();//暂停录音
            }
        }
    }

    /**
     * 获取需要转换的类型 发送给ffmpeg
     */
    private void startTransform(String inputPath, String outputPath) {
        executeFFmpegCmd(transformAudio(inputPath, outputPath));

    }

    /**
     * mp3 转 WAV
     *
     * @param inputPath
     * @param outputPath
     */
    private void mp3ToWav(String inputPath, String outputPath) {
        if (!isFFMPEG) {
            isFFMPEG = true;
            FFMPEG_TYPE = 0;
            mInputPath = inputPath;
            startTransform(mInputPath, outputPath);
        }
    }

    /**
     * wav转 MP3
     */
    private void wavToMp3() {
        if (!isFFMPEG) {
            isFFMPEG=true;
            FFMPEG_TYPE = 1;
            startTransform(Constants.RECORD_MIX_FILE_PATH, Constants.RECORD_MIX_MP3_FILE_PATH);
        }
    }

    /**
     * 执行ffmpeg命令行
     *
     * @param commandLine commandLine
     */
    private void executeFFmpegCmd(final String[] commandLine) {
        if (commandLine == null) {
            return;
        }
        FFmpegCmd.execute(commandLine, new FFmpegCmd.OnHandleListener() {
            @Override
            public void onBegin() {
                Log.i(TAG, "handle audio onBegin...");
                mHandler.obtainMessage(FFMPEG_MSG_BEGIN).sendToTarget();
            }

            @Override
            public void onEnd(int result) {
                Log.i(TAG, "handle audio onEnd...");
                mHandler.obtainMessage(FFMPEG_MSG_FINISH).sendToTarget();
            }
        });
    }

    /**
     * 使用ffmpeg命令行进行音频转码
     *
     * @param srcFile    源文件
     * @param targetFile 目标文件（后缀指定转码格式）
     * @return 转码后的文件
     */
    public static String[] transformAudio(String srcFile, String targetFile) {
        String transformAudioCmd = "ffmpeg -i %s %s";
        transformAudioCmd = String.format(transformAudioCmd, srcFile, targetFile);
        return transformAudioCmd.split(" ");//以空格分割为字符串数组
    }

    /**
     * 圆圈加载进度的 dialog
     */
    private void showLoadingDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIcon(R.drawable.ic_launcher);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(true);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
        mProgressDialog.setCancelable(false);//点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
        mProgressDialog.show();
    }

    /**
     * 隐藏dialog
     */
    private void hideDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * wav转PCM
     */
    private void wavToPcm() {
        AudioUtils audioTrackUtils = new AudioUtils();
        audioTrackUtils.startWavToPcm(Constants.BG_MUSIC_WAV_PATH, Constants.BG_MUSIC_PCM_PATH, mHandler);
    }

    /**
     * 设置背景音乐
     */
    private void setBGMusicView() {
        if (mMixManager != null) {

            mMixManager.setBgMusic(Constants.BG_MUSIC_PCM_PATH);
        } else {
            mBgPath = Constants.BG_MUSIC_PCM_PATH;
        }
        if (!TextUtils.isEmpty(mBgMusicBean.getTitle())) {
            selectMusic.setText(mBgMusicBean.getTitle());
        }
    }

    /**
     * 显示对话框
     * @param title 需要提示的内容
     * @param listener 确认按钮的点击事件
     */
    public void showDialog(String title, View.OnClickListener listener) {
        //实例化自定义对话框
        if (mConstopDialog==null) {
            mConstopDialog = new ConstomDialog(this);
        }
        mConstopDialog.setTv(title);
        //对话框中退出按钮事件
        mConstopDialog.setOnExitListener(listener);
        //对话框中取消按钮事件
        mConstopDialog.setOnCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConstopDialog != null && mConstopDialog.isShowing()) {
                    //关闭对话框
                    mConstopDialog.dismiss();
                }
            }
        });
        mConstopDialog.show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当按下返回键时所执行的命令
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 此处写你按返回键之后要执行的事件的逻辑
            showDialog("是否放弃这次录音?", v1 -> {
                //如果对话框处于显示状态
                if (mConstopDialog!=null &&mConstopDialog.isShowing()) {
                    //关闭对话框
                    mConstopDialog.dismiss();
                    //可能要删除文件
                    finish();
                    //关闭当前界面
                }

            });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
