package com.hci.ireye.ui.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hci.ireye.R;

/**
 * related:
 * java: this class
 * layout: layout_custom_dialog
 * colors: colorMyTheme
 * colors: colorTransparent
 * styles: CustomDialog
 * drawable: bg_custom_dialog
 */
public class MyCustomDialog extends Dialog implements View.OnClickListener {
    // it is a widget that can be used in other activities (just like Toast)

    private TextView mTvTitle, mTvMsg, mTvCancel, mTvConfirm;
    private View mVDivider;
    private String title, msg, cancel, confirm;

    private IOnCancelListener cancelListener = null;
    private IOnConfirmListener confirmListener = null;
    public MyCustomDialog(@NonNull Context context) {
        super(context);
    }

    public MyCustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public MyCustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public MyCustomDialog setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public MyCustomDialog setCancel(String cancel, IOnCancelListener listener) {
        this.cancel = cancel;
        this.cancelListener = listener;
        return this;
    }

    public MyCustomDialog setConfirm(String confirm, IOnConfirmListener listener) {
        this.confirm = confirm;
        this.confirmListener = listener;
        return this;
    }// allows Builder Mode


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_custom_dialog);
        //设置宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p =getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int)(size.x * 0.75);
        getWindow().setAttributes(p);

        mTvTitle = findViewById(R.id.tv_title);
        mTvMsg = findViewById(R.id.tv_msg);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mVDivider = findViewById(R.id.v_horizontal_divider);

        if(!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }
        if(!TextUtils.isEmpty(msg)) {
            mTvMsg.setText(msg);
        }
        if(!TextUtils.isEmpty(cancel)) {
            mTvCancel.setText(cancel);
        }
        if(!TextUtils.isEmpty(confirm)) {
            mTvConfirm.setText(confirm);
        }
        mTvCancel.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);

        //hide button if its listener is not assigned
        setCancelable(false);
        if(cancel == null && cancelListener == null) {
            mTvCancel.setVisibility(View.GONE);
            mVDivider.setVisibility(View.GONE);
        }
        if(confirm == null && confirmListener == null) {
            mTvConfirm.setVisibility(View.GONE);
            mVDivider.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.tv_cancel:
                if(cancelListener != null) {
                    cancelListener.onCancel(this);
                }
                dismiss();
                break;
            case R.id.tv_confirm:
                if(confirmListener != null) {
                    confirmListener.onConfirm(this);
                }
                dismiss();
                break;
        }
    }

    public interface IOnCancelListener {// I- prefix: self-defined interfaces
        void onCancel(MyCustomDialog dialog);

    }

    public interface IOnConfirmListener {
        void onConfirm(MyCustomDialog dialog);
    }
}
