package com.khmer.fm.adnroid_recordd.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.khmer.fm.adnroid_recordd.R;


/**
 * author: created by cui
 * description: 对话框
 */
public class ConstomDialog extends Dialog {
    /**取消按钮*/
    private Button button_cancel;

    /**确认按钮*/
    private Button button_exit;

    /**标题文字*/
    private TextView tv;


    //构造方法
    public ConstomDialog(Context context) {
        super(context, R.style.mdialog);
        //通过LayoutInflater获取布局
        View view = LayoutInflater.from(getContext()).
                inflate(R.layout.dialog_layout, null);

        tv = (TextView) view.findViewById(R.id.title);
        button_cancel = (Button) view.findViewById(R.id.btn_cancel);
        button_exit = (Button) view.findViewById(R.id.btn_exit);

        //设置显示的视图
        setContentView(view);
    }

    /**
     * 设置显示的标题文字
     */
    public void setTv(String content) {
        tv.setText(content);
    }


    /**
     * 取消按钮监听
     * */
    public void setOnCancelListener(View.OnClickListener listener){
        button_cancel.setOnClickListener(listener);
    }

    /**
     * 退出按钮监听
     * */
    public void setOnExitListener(View.OnClickListener listener){
        button_exit.setOnClickListener(listener);
    }
}
