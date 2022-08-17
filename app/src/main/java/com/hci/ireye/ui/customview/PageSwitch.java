package com.hci.ireye.ui.customview;

//
// Created by Lithops on 2022/6/3, 22:52.
//

import android.content.Context;
import android.util.AttributeSet;

class PageSwitch extends androidx.appcompat.widget.AppCompatButton {

    Context mContext;

    public PageSwitch(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public PageSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public PageSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {

    }

}
