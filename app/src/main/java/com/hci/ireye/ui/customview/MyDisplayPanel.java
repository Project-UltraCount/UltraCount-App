package com.hci.ireye.ui.customview;

//
// Created by Lithops on 2022/6/7, 14:22.
//

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hci.ireye.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MyDisplayPanel extends FrameLayout {

    private Context mContext;
    private static final int COLUMN_COUNT = 2;

    public MyDisplayPanel(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MyDisplayPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public MyDisplayPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    TableLayout mContainer;

    TextView mTitle;

    LinkedHashMap<TableRow, ArrayList<LinearLayout>> mRowToItems;
    LinkedHashMap<String, LinearLayout> mKeyToItems;

    TableRow mNextInsertionRow;

    private void init() {
        View.inflate(mContext, R.layout.layout_my_display_panel, this);

        mTitle = findViewById(R.id.tv_my_display_panel_title);
        mContainer = findViewById(R.id.tl_my_display_panel_container);
        mRowToItems = new LinkedHashMap<>();
        mKeyToItems = new LinkedHashMap<>();

        mNextInsertionRow = appendTableRow();
    }

    public MyDisplayPanel setTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public MyDisplayPanel setTitleVisibility(int visibility) {
        mTitle.setVisibility(visibility);
        View divider = findViewById(R.id.v_my_display_panel_title_divider);
        divider.setVisibility(visibility);
        return this;
    }

    public MyDisplayPanel appendEntry(String key, Object value) {
        LinearLayout newItem = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.item_my_display_panel, mNextInsertionRow, false);
        TextView tvKey = newItem.findViewById(R.id.tv_item_my_display_panel_key);
        TextView tvValue = newItem.findViewById(R.id.tv_item_my_display_panel_value);

        tvKey.setText(key);
        tvValue.setText(value.toString());

        mNextInsertionRow.addView(newItem);

        // save data to maps
        mRowToItems.get(mNextInsertionRow).add(newItem);
        mKeyToItems.put(key, newItem);

        if (mRowToItems.get(mNextInsertionRow).size() >= COLUMN_COUNT) {
            mNextInsertionRow = appendTableRow();
        }

        return this;
    }

    public MyDisplayPanel updateValue(String key, Object newValue) {
        LinearLayout item = mKeyToItems.get(key);

        TextView tvValue = item.findViewById(R.id.tv_item_my_display_panel_value);
        tvValue.setText(newValue.toString());
        return this;
    }

    private TableRow appendTableRow() {
        TableRow tableRow = (TableRow) LayoutInflater.from(mContext).inflate(R.layout.item_my_display_panel_tablerow, mContainer, false);

        mContainer.addView(tableRow);

        mRowToItems.put(tableRow, new ArrayList<>());

        return tableRow;
    }


}
