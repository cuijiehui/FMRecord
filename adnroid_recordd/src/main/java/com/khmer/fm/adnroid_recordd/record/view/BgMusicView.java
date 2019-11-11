package com.khmer.fm.adnroid_recordd.record.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * author : created by cui on 2019/10/10 17:01
 * Description:录音波动图
 */
public class BgMusicView extends View {
    private static final String TAG = BgMusicView.class.getSimpleName();
    private static final int INVALIDATE_VIEW = 0X1233;
    private float width;//控件宽度
    private float height;//空间高度
    private double rect_w;//柱状图的宽度
    private LinkedList<Integer> volumeData = new LinkedList<>();
    Paint mPaint, mPaint2;
    private float distance = 3f;//之间的距离
    private float fristX = 0;//记录最前面的X 用于绘画中间线
    private float heightPercent;//假设声量范围是0到100，将高度分为100份
    private float baseHeight = 2;//为0也会有高度
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == INVALIDATE_VIEW) {
                BgMusicView.this.invalidate();//重绘更新view
            }
        }

        ;
    };

    {

        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.RED);
        mPaint2.setStyle(Paint.Style.STROKE);

    }

    public BgMusicView(Context context) {
        super(context);
    }

    public BgMusicView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //拿到控件的宽高度,并动态计算柱子的宽度
        height = View.MeasureSpec.getSize(heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        heightPercent = height / 100;
        baseHeight = heightPercent * 2;
        rect_w = (float) (width / 200);
    }

    public LinkedList<Integer> getVolumeData() {
        return volumeData;
    }


    public void setVolumeData(LinkedList<Integer> list) {
        volumeData.clear();
        volumeData.addAll(list);
        handler.sendEmptyMessage(INVALIDATE_VIEW);

    }

    public void setVolumeData(int volume) {
        volumeData.addFirst(volume);
        handler.sendEmptyMessage(INVALIDATE_VIEW);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left;
        float top;
        float bottom;
        float volume;
        for (int i = 0; i < volumeData.size(); i++) {
            left = width - (distance * i);
            if (i == volumeData.size() - 1) {
                fristX = left;
            }
            if (left < 0) {
                continue;
            }
            volume = (volumeData.get(i) * heightPercent) + baseHeight;
            top = (float) (height * 0.5) - volume < 1 ? 1 : (float) (height * 0.5) - volume;
            bottom = ((float) (height * 0.5)) + volume;
//            canvas.drawRect(rectF, mPaint2);//画出来图像大小不一样
            canvas.drawLine(left, top, left, bottom, mPaint2);

        }
        if (fristX >= 0) {
            canvas.drawLine((float) (0), (float) (height * 0.5), (fristX), (float) (height * 0.5), mPaint);
        }
        if(volumeData.size()<=0){
            canvas.drawLine((float) (0), (float) (height * 0.5), (width), (float) (height * 0.5), mPaint);

        }
    }
}
