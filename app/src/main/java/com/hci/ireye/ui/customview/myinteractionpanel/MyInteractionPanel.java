package com.hci.ireye.ui.customview.myinteractionpanel;

//
// Created by Lithops on 2022/6/8, 18:23.
//

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hci.ireye.R;

import java.util.LinkedHashMap;

public class MyInteractionPanel extends FrameLayout {
    private Context mContext;
    private TableRow tr;
    private TableLayout tableLayout;

    private LinkedHashMap<String, LinearLayout> mItems = new LinkedHashMap<>();

    public MyInteractionPanel(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MyInteractionPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();

    }

    public MyInteractionPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();

    }


    private void init() {
        View.inflate(mContext, R.layout.layout_my_interaction_panel, this);

        tableLayout = findViewById(R.id.tl_my_interaction_panel_table);

        tr = new TableRow(mContext);
    }

    public MyInteractionPanel addItem(String id, Drawable img, String text, OnClickListener listener) {
        LinearLayout newItem = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.item_my_interaction_panel, tr, false);

        ImageButton ib = newItem.findViewById(R.id.ib_my_interaction_panel_item);
        TextView tv = newItem.findViewById(R.id.tv_my_interaction_panel_item);
        ib.setImageDrawable(img);
        tv.setText(text);
        ib.setOnClickListener(listener);

        tr.addView(newItem);

        mItems.put(id, newItem);

        if (tr.getParent() == null) tableLayout.addView(tr);

        return this;
    }

    public MyInteractionPanel updateImg(String id, Drawable img) {
        LinearLayout item = mItems.get(id);

        ImageButton ib = item.findViewById(R.id.ib_my_interaction_panel_item);
        ib.setImageDrawable(img);
        return this;
    }

    public MyInteractionPanel updateText(String id, String text) {
        LinearLayout item = mItems.get(id);

        TextView tv = item.findViewById(R.id.tv_my_interaction_panel_item);
        tv.setText(text);
        return this;
    }

    public MyInteractionPanel upateListener(String id, OnClickListener listener) {
        LinearLayout item = mItems.get(id);
        ImageButton ib = item.findViewById(R.id.ib_my_interaction_panel_item);
        ib.setOnClickListener(listener);
        return this;
    }
}
