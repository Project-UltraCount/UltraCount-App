package com.hci.ireye.ui.eventviewer.eventcharts;

//
// Created by Lithops on 2022/6/4, 22:46.
//

import static com.hci.ireye.ui.util.MyUtil.compressTimeInSeconds;
import static com.hci.ireye.ui.util.MyUtil.extractTimeInSeconds;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.EventsManager;
import com.hci.ireye.ui.customview.MyMarker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class EventChartsManager {
    private static EventChartsManager instance;
    private Context mContext;
    private LineChart mAggregateLineChart;

    private TreeMap<String, LineChart> mDeviceLineCharts = new TreeMap<>();

    public static final long TIME_OFFSET = System.currentTimeMillis() / 1000;
    private SharedPreferences mUserPrefs;

    public static EventChartsManager getInstance(Context context) {
        if (instance == null) instance = new EventChartsManager(context);
        return instance;
    }

    private EventChartsManager(Context context) {
        mContext = context;
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);
    }

    private final ValueFormatter mXAxisTimeFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            return SimpleDateFormat.getDateTimeInstance().format(new Date(extractTimeInSeconds(value, TIME_OFFSET) * 1000)).replace(" ", ",\n");
        }
    };

    public LineChart getNewAggregateLineChart() {
        return mAggregateLineChart = newCustomLineChartInstance();
    }

    public LineChart getNewDeviceLineChart(String deviceId) {
        Log.d("我", "getNewDeviceLineChart: new chart requested!" + deviceId);

        mDeviceLineCharts.put(deviceId, newCustomLineChartInstance());
        return mDeviceLineCharts.get(deviceId);
    }

    public void update(EventsManager.CountingDataSet dataSet) {
        Log.d("我", "dataset1 = " + dataSet);
        updateAggregateLineChart(dataSet.getCountingAggregateData());
        Log.d("我", "update: all device ids" + dataSet.getDeviceIds());
        for (String deviceId : dataSet.getDeviceIds()) {
            updateDeviceLineChart(dataSet.getCountingDeviceData(deviceId));
        }
    }

    private void updateAggregateLineChart(EventsManager.CountingDeviceData data) {
        if (mAggregateLineChart == null) {
            return;
        }

        LineData lineData = new LineData();
        if (data.getInflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getInflow()),
                            mContext.getColor(R.color.colorMyTheme)
                    )
            );
            Log.d("我", "updateAggregateLineChart: " + singleCountingDataToEntryList(data.getInflow()));
            Log.d("我", "updateAggregateLineChart: inflow" + data.getInflow());
        }
        if (data.getOutflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getOutflow(true)),
                            mContext.getColor(R.color.colorRed)
                    )
            );
            Log.d("我", "updateAggregateLineChart: " + singleCountingDataToEntryList(data.getOutflow()));
            Log.d("我", "updateAggregateLineChart: outflow " + data.getOutflow());
        }
        // only display netflow when both inflow and outflow data are present
        if (data.getInflow() != null && data.getOutflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getNetflow()),
                            mContext.getColor(R.color.colorBlack)
                    )
            );
            Log.d("我", "updateAggregateLineChart: " + singleCountingDataToEntryList(data.getNetflow()));
            Log.d("我", "updateAggregateLineChart: getNetflow" + data.getNetflow());
        }
        mAggregateLineChart.setData(lineData);
        mAggregateLineChart.invalidate();
    }

    private void updateDeviceLineChart(EventsManager.CountingDeviceData data) {
        Log.d("我", "updateDeviceLineChart: " + data.getDeviceId());
        // skips data that belongs to devices that do not yet own a chart
        if (!mDeviceLineCharts.containsKey(data.getDeviceId())) {
            return;
        }

        LineChart deviceLineChart = mDeviceLineCharts.get(data.getDeviceId());

        // for now same as aggregate
        LineData lineData = new LineData();
        if (data.getInflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getInflow()),
                            mContext.getColor(R.color.colorMyTheme)
                    )
            );
        }
        if (data.getOutflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getOutflow(true)),
                            mContext.getColor(R.color.colorRed)
                    )
            );
        }
        // only display netflow when both inflow and outflow data are present
        if (data.getInflow() != null && data.getOutflow() != null) {
            lineData.addDataSet(
                    newCustomLineDataSetInstance(
                            singleCountingDataToEntryList(data.getNetflow()),
                            mContext.getColor(R.color.colorBlack)
                    )
            );
        }
        Log.d("我", "updateDeviceLineChart: updated!");
        deviceLineChart.setData(lineData);
        deviceLineChart.invalidate();

    }

    static private List<Entry> singleCountingDataToEntryList(TreeMap<Long, Integer> data) {

        ArrayList<Entry> ret = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : data.entrySet()) {
            ret.add(new Entry(compressTimeInSeconds(entry.getKey(), TIME_OFFSET), entry.getValue()));
        }
        return ret;
    }


    private LineChart newCustomLineChartInstance() {
        LineChart ret = new LineChart(mContext);

        XAxis xAxis = ret.getXAxis();
        YAxis yAxisLeft = ret.getAxisLeft();
        YAxis yAxisRight = ret.getAxisRight();

        yAxisRight.setEnabled(false);

        // configure line appearance
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10, 10000, 0);
        xAxis.setGridLineWidth(1);
        xAxis.setAxisLineWidth(1);
        yAxisLeft.setAxisLineWidth(1);

        //disable legends and description
        ret.getLegend().setEnabled(false);
        ret.getDescription().setEnabled(false);

        //set pop up view on point selected
        ret.setMarker(new MyMarker(mContext, R.layout.layout_chart_marker, TIME_OFFSET));

        //format x-axis data to time
        xAxis.setValueFormatter(mXAxisTimeFormatter);

        xAxis.setLabelRotationAngle(-30);

        yAxisLeft.setSpaceBottom(0);
        yAxisLeft.setSpaceTop(30);

        //set data padding
        xAxis.setSpaceMin(30);
        xAxis.setSpaceMax(30);

        return ret;
    }

    private LineDataSet newCustomLineDataSetInstance(List<Entry> data, int color) {
        LineDataSet lineDataSet = new LineDataSet(data, "label");
        //set color
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setValueTextColor(color);
        lineDataSet.setLineWidth(1.5f);

//        lineDataSet.setDrawFilled(true);
//        lineDataSet.setFillDrawable(mContext.getResources().getDrawable(R.drawable.bg_line_chart_fill));

        lineDataSet.setMode(LineDataSet.Mode.LINEAR);

        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);

        return lineDataSet;
    }
}
