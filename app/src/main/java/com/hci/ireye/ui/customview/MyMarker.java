package com.hci.ireye.ui.customview;

//
// Created by Lithops on 2020/6/23, 20:06.
//

import android.content.Context;
import android.widget.TextView;

import com.hci.ireye.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hci.ireye.ui.util.MyUtil.extractTimeInSeconds;

public class MyMarker extends MarkerView {

    private TextView mTvTime, mTvCount;
    private long mTimeOffset;
    public MyMarker(Context context, int layoutResource, long offset) {
        super(context, layoutResource);
        mTvTime = findViewById(R.id.tv_chart_marker_time_data);
        mTvCount = findViewById(R.id.tv_chart_marker_count_data);
        mTimeOffset = offset;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);
        mTvTime.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(extractTimeInSeconds(highlight.getX(), mTimeOffset) * 1000)).replace(' ', '\n'));
        mTvCount.setText((int)highlight.getY() + "");

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED);
        measure(widthMeasureSpec, heightMeasureSpec);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getMeasuredWidth() / 2.0f), -getMeasuredHeight() - 20);
        }

        return mOffset;
    }
}
