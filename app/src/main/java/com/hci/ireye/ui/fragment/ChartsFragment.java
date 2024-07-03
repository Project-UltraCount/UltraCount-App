package com.hci.ireye.ui.fragment;

//
// Created by Lithops on 2020/6/4, 14:50.
//

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.hci.ireye.R;
import com.hci.ireye.data.type.ArduinoEntry;
import com.hci.ireye.ui.customview.ChartsWindow;
import com.hci.ireye.ui.customview.MyMarker;
import com.hci.ireye.ui.customview.SubPage;
import com.hci.ireye.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import static com.hci.ireye.util.MyUtil.compressTimeInMillis;
import static com.hci.ireye.util.MyUtil.extractTimeInMillis;
import static com.hci.ireye.util.MyUtil.getStartOfInterval;
import static com.hci.ireye.util.MyUtil.truncateTimeToMin;

public class ChartsFragment extends Fragment {

    private Context mContext;
    private SubPage mRvSubPage;

    private LineChart mLcAggregate;
    private BarChart mBcAggregate;

    private SharedPreferences mUserPrefs, mDevicePrefs;

    private static long lineChartInterval;
    private static long barChartInterval;
    private static final long TIME_OFFSET = System.currentTimeMillis();

    private ValueFormatter mXAxisTimeFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            return new SimpleDateFormat("HH:mm:ss").format(new Date(extractTimeInMillis(value, TIME_OFFSET)));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvSubPage = view.findViewById(R.id.rv_charts_page);
        init();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mDevicePrefs = mContext.getSharedPreferences(mContext.getString(R.string.device_prefs), Context.MODE_PRIVATE);
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);
    }

    public void init() {

        ChartsWindow window = new ChartsWindow(mContext);

        lineChartInterval = mUserPrefs.getLong(mContext.getString(R.string.sp_charts_interval), 20000);
        barChartInterval = mUserPrefs.getLong(mContext.getString(R.string.sp_charts_interval), 20000);

        mLcAggregate = getCustomLineChart();
        mBcAggregate = getCustomBarChart();

        window.addEntry("lineChart", mLcAggregate, mContext.getString(R.string.chart_total_present));
        window.addEntry("barChart", mBcAggregate, mContext.getString(R.string.chart_guest_flow));

        mRvSubPage.addTab("aggregate", getResources().getDrawable(R.drawable.ic_menu_chart), mContext.getString(R.string.tab_aggregate),window);
    }

    public void update(Map<String, List<ArduinoEntry>> dataSet) {
        if (mContext == null) mContext = getContext();
        Map<String, Boolean> stillOnline = new HashMap<>();
        for (String tabId : mRvSubPage.getIds()) {
            if (tabId.equals("aggregate")) continue;
            stillOnline.put(tabId, false);
        }

        if (dataSet != null) {
            //update from preference (they may have changed)
            lineChartInterval = mUserPrefs.getLong(mContext.getString(R.string.sp_charts_interval), 20000);
            barChartInterval = mUserPrefs.getLong(mContext.getString(R.string.sp_charts_interval), 20000);

            //update linechart
            Map<String, List<Entry>> lineChartDataSets = makeLineDataSet(dataSet, lineChartInterval);
            mLcAggregate.setData(getCustomLineData(combineToAggregateLineDataSet(lineChartDataSets)));
            mLcAggregate.invalidate();

            //update barchart
            Map<String, List<BarEntry>> barChartDataSets = makeBarDataSet(dataSet, barChartInterval);
            List<List<BarEntry>> tmp = combineToAggregateBarDataSet(barChartDataSets);
            List<BarEntry> posAggregateBarDataSet = tmp.get(0), negAggregateBarDataSet = tmp.get(1);
            mBcAggregate.setData(getCustomBarData(mContext, posAggregateBarDataSet, negAggregateBarDataSet));
            mBcAggregate.invalidate();

            //update device linecharts
            for (String deviceId : lineChartDataSets.keySet()) {
                if (dataSet.get(deviceId).size() == 0) continue;

                if (mRvSubPage.getTab(deviceId) != null) {
                    stillOnline.put(deviceId, true);
                    Chart chart = ((ChartsWindow) mRvSubPage.getTab(deviceId).getWindowView()).getEntry("lineChart").getChart();
                    chart.setData(getCustomLineData(lineChartDataSets.get(deviceId)));
                    chart.invalidate();
                } else {
                    LineChart lineChart = getCustomLineChart();
                    lineChart.setData(getCustomLineData(lineChartDataSets.get(deviceId)));
                    ChartsWindow window = new ChartsWindow(mContext);
                    window.addEntry("lineChart", lineChart, mContext.getString(R.string.chart_device_count));
                    mRvSubPage.addTab(deviceId, getResources().getDrawable(R.drawable.ic_menu_chart), mContext.getString(R.string.device) + " " + deviceId, window);
                }
            }
        }
        for (String tabId : stillOnline.keySet()) {
            if (!stillOnline.get(tabId)) {
                mRvSubPage.removeTab(tabId);
            }
        }
    }

    public void stop() {

        boolean success = true;
//        success &= mLcAggregate.saveToGallery("RecordLineChart" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis())), "IREye", "LineChartAggregate", Bitmap.CompressFormat.PNG, 100);
//        success &= mBcAggregate.saveToGallery("RecordBarChart" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis())), "IREye", "BarChartAggregate", Bitmap.CompressFormat.PNG, 100);
        success &= mLcAggregate.saveToGallery("1", "", "LineChartAggregate", Bitmap.CompressFormat.PNG, 100);
        success &= mBcAggregate.saveToGallery("2", "", "BarChartAggregate", Bitmap.CompressFormat.PNG, 100);
        if (success) {
            ToastUtil.makeToast(mContext, mContext.getString(R.string.save_to_gallery_successful_prompt));
        } else {
            ToastUtil.makeToast(mContext, mContext.getString(R.string.failed_to_save_to_gallery_prompt));
        }
    }

    private LineChart getCustomLineChart() {
        LineChart ret = new LineChart(mContext);

        XAxis xAxis = ret.getXAxis();
        YAxis yAxisLeft = ret.getAxisLeft();
        YAxis yAxisRight = ret.getAxisRight();

        // configure line appearance
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);
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
        yAxisLeft.setAxisMinimum(0);

        yAxisLeft.setSpaceTop(30);

        //set data padding
        xAxis.setSpaceMin(30);
        xAxis.setSpaceMax(30);

        return ret;
    }

    private LineData getCustomLineData(List<Entry> data) {
        LineDataSet lineDataSet = new LineDataSet(data, "label");
        //set color
        lineDataSet.setColor(mContext.getColor(R.color.colorMyTheme));
        lineDataSet.setCircleColor(mContext.getColor(R.color.colorMyTheme));
        lineDataSet.setValueTextColor(mContext.getColor(R.color.colorMyTheme));
        lineDataSet.setLineWidth(1.5f);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(mContext.getResources().getDrawable(R.drawable.bg_line_chart_fill));

        lineDataSet.setMode(LineDataSet.Mode.LINEAR);

        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);

        LineData lineData  = new LineData(lineDataSet);
        return lineData;
    }

    private BarChart getCustomBarChart() {
        BarChart ret = new BarChart(mContext);

        XAxis xAxis = ret.getXAxis();
        YAxis yAxisLeft = ret.getAxisLeft();
        YAxis yAxisRight = ret.getAxisRight();

        //xAxis.setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10,10000,0);
        xAxis.setAxisLineWidth(1);

        yAxisRight.setEnabled(false);
        yAxisLeft.setAxisLineWidth(1);
        yAxisLeft.setZeroLineWidth(1);

        yAxisLeft.setDrawZeroLine(true);
        //disable legends and description
        ret.getLegend().setEnabled(false);
        ret.getDescription().setEnabled(false);

        ret.setMarker(new MyMarker(mContext, R.layout.layout_chart_marker, TIME_OFFSET));

        xAxis.setValueFormatter(mXAxisTimeFormatter);
        xAxis.setLabelRotationAngle(-30);

        //set data padding
        xAxis.setSpaceMin(30);
        xAxis.setSpaceMax(30);

        return ret;
    }

    static private BarData getCustomBarData(Context context, List<BarEntry> dataPos, List<BarEntry> dataNeg) {
        BarDataSet barPosDataSet = new BarDataSet(dataPos, "pos"),
                barNegDataSet = new BarDataSet(dataNeg, "neg");

        BarData barData = new BarData(barPosDataSet, barNegDataSet);

        barPosDataSet.setColor(context.getColor(R.color.colorMyTheme));
        barNegDataSet.setColor(context.getColor(R.color.colorRed));
        barPosDataSet.setValueTextColor(context.getColor(R.color.colorMyTheme));
        barNegDataSet.setValueTextColor(context.getColor(R.color.colorRed));

        barData.setBarWidth(barChartInterval / 2000f *0.8f);
        return barData;
    }

    static private Map<String, List<Entry>> makeLineDataSet(Map<String, List<ArduinoEntry>> dataSet, long timeInterval) {
        Map<String, List<Entry>> rets = new TreeMap<>();

        long start = truncateTimeToMin(((List<ArduinoEntry>)dataSet.values().toArray()[0]).get(((List<ArduinoEntry>)dataSet.values().toArray()[0]).size() - 1).time);

        for (String deviceId : dataSet.keySet()) {
            List<ArduinoEntry> data = dataSet.get(deviceId);
            List<Entry> ret = new ArrayList<>();
            ListIterator<ArduinoEntry> it = data.listIterator(data.size());

            //consume first entry
            it.previous();

            Map<Long, Integer> tmp = new LinkedHashMap<>();
            while (it.hasPrevious()) {
                ArduinoEntry cur = it.previous();
                tmp.put(getStartOfInterval(truncateTimeToMin(cur.time), start, timeInterval), cur.count - data.get(data.size() - 1).count);
            }

            for (Map.Entry<Long, Integer> entry : tmp.entrySet()) {
                ret.add(new Entry(compressTimeInMillis(entry.getKey(), TIME_OFFSET), entry.getValue()));
            }
            rets.put(deviceId, ret);
        }
        return rets;
    }

    static private Map<String, List<BarEntry>> makeBarDataSet(Map<String, List<ArduinoEntry>> dataSet, long timeInterval) {
        Map<String, List<BarEntry>> rets = new TreeMap<>();

        long start = truncateTimeToMin(((List<ArduinoEntry>)dataSet.values().toArray()[0]).get(((List<ArduinoEntry>)dataSet.values().toArray()[0]).size() - 1).time);

        for (String deviceId : dataSet.keySet()) {
            List<ArduinoEntry> data = dataSet.get(deviceId);
            List<BarEntry> ret = new ArrayList<>();
            ListIterator<ArduinoEntry> it = data.listIterator(data.size());

            //consume first entry
            it.previous();

            Map<Long, Integer> tmp = new LinkedHashMap<>();
            while (it.hasPrevious()) {
                ArduinoEntry cur = it.previous();
                tmp.put(getStartOfInterval(truncateTimeToMin(cur.time), start, timeInterval), cur.count - data.get(data.size() - 1).count);
            }

            //bar-specific
            List<Long> keyList = new LinkedList<>(tmp.keySet());
            for (int i = tmp.size() - 1; i > 0; --i) {
                tmp.put(keyList.get(i), tmp.get(keyList.get(i)) - tmp.get(keyList.get(i - 1)));
            }

            for (Map.Entry<Long, Integer> entry : tmp.entrySet()) {
                ret.add(new BarEntry(compressTimeInMillis(entry.getKey(), TIME_OFFSET), entry.getValue()));
            }
            rets.put(deviceId, ret);
        }
        return rets;
    }

    private List<Entry> combineToAggregateLineDataSet(Map<String, List<Entry>> dataSet) {
        Map<Float, Float> tmp = new TreeMap<>();

        for (String deviceId : dataSet.keySet()) {
            List<Entry> data = dataSet.get(deviceId);
            int inOrOut = mDevicePrefs.getBoolean(deviceId, true) ? 1 : -1;

            for (Entry entry : data) {
                if (tmp.containsKey(entry.getX())) {
                    tmp.put(entry.getX(), (tmp.get(entry.getX()) + inOrOut * entry.getY()));
                } else {
                    tmp.put(entry.getX(), (inOrOut * entry.getY()));
                }
            }
        }
        List<Entry> ret = new LinkedList<>();
        for (Map.Entry<Float, Float> entry : tmp.entrySet()) {
            ret.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    //[0] for positive dataset, [1] for negative dataset
    private List<List<BarEntry>> combineToAggregateBarDataSet(Map<String, List<BarEntry>> dataSet) {
        Map<Float, Float> tmpPos = new TreeMap<>();
        Map<Float, Float> tmpNeg = new TreeMap<>();


        for (String deviceId : dataSet.keySet()) {
            List<BarEntry> data = dataSet.get(deviceId);
            boolean inOrOut = mDevicePrefs.getBoolean(deviceId, true);

            for (Entry entry : data) {
                if (inOrOut) {
                    if (tmpPos.containsKey(entry.getX() + barChartInterval / 1000f * 0.3f)) {
                        tmpPos.put(entry.getX() + barChartInterval / 1000f * 0.3f, tmpPos.get(entry.getX() + barChartInterval / 1000f * 0.3f) + entry.getY());
                    }else {
                        tmpPos.put(entry.getX() + barChartInterval / 1000f * 0.3f, entry.getY());
                    }
                } else {
                    if (tmpNeg.containsKey(entry.getX() + barChartInterval / 1000f * 0.7f)) {
                        tmpNeg.put(entry.getX() + barChartInterval / 1000f * 0.7f, tmpNeg.get(entry.getX() + barChartInterval / 1000f * 0.7f) - entry.getY());
                    } else {
                        tmpNeg.put(entry.getX() + barChartInterval / 1000f * 0.7f, -entry.getY());
                    }
                }
            }
        }
        List<List<BarEntry>> ret = new ArrayList<>();
        ret.add(new ArrayList<BarEntry>());
        ret.add(new ArrayList<BarEntry>());

        for (Map.Entry<Float, Float> entry : tmpPos.entrySet()) {
            ret.get(0).add(new BarEntry(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<Float, Float> entry : tmpNeg.entrySet()) {
            ret.get(1).add(new BarEntry(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    //merge entries from different devices to one, while maintaining its descending chronological order.
    //didn't use
    static private List<ArduinoEntry> mergeDataSets(Map<String, List<ArduinoEntry>> dataSet) {
        List<ArduinoEntry> ret = new ArrayList<>();

        for (List<ArduinoEntry> lists : dataSet.values()) {
            List<ArduinoEntry> tmp = new ArrayList<>();
            Iterator<ArduinoEntry> it = lists.iterator();
            Iterator<ArduinoEntry> itret = ret.iterator();

            if (it.hasNext() && itret.hasNext()) {
                ArduinoEntry p = it.next(), pret = itret.next();

                while (true) {
                    if (p.compareTo(pret) > 0) {
                        tmp.add(p);
                        if (!it.hasNext()) {
                            tmp.add(pret);
                            break;
                        }
                        p = it.next();
                    } else {
                        tmp.add(pret);
                        if (!itret.hasNext()) {
                            tmp.add(p);
                            break;
                        }
                        pret = itret.next();
                    }
                }
            }

            while (it.hasNext()) {
                tmp.add(it.next());
            }
            while (itret.hasNext()) {
                tmp.add(itret.next());
            }

            ret = new ArrayList<>(tmp);
        }

        return ret;
    }

}
