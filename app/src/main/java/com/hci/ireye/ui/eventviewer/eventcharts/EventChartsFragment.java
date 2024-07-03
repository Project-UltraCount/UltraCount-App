package com.hci.ireye.ui.eventviewer.eventcharts;

//
// Created by Lithops on 2020/6/4, 14:50.
//

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.EventsManager;

import java.util.List;

public class EventChartsFragment extends Fragment {

    private Context mContext;
    private LinearLayout mCvAggregate;
    private TableLayout mTlDevices;

    private static final int COLUMN_COUNT = 1;

    private SharedPreferences mUserPrefs, mDevicePrefs;

    private EventChartsManager eventChartsManager;
    //    private static long barChartInterval;

    private EventsManager.CountingEvent mEvent; // the event whose data is to be displayed

    public static EventChartsFragment newInstance(EventsManager.CountingEvent event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        EventChartsFragment fragment = new EventChartsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mDevicePrefs = mContext.getSharedPreferences(mContext.getString(R.string.device_prefs), Context.MODE_PRIVATE);
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);
        eventChartsManager = EventChartsManager.getInstance(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_charts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEvent = (EventsManager.CountingEvent) getArguments().getSerializable("event");

        mCvAggregate = view.findViewById(R.id.cv_chart);
        mTlDevices = view.findViewById(R.id.tl_device_charts_container);

        // add aggregate chart
        FrameLayout aggregateLineChartContainer = mCvAggregate.findViewById(R.id.fl_chart_container);
        aggregateLineChartContainer.addView(eventChartsManager.getNewAggregateLineChart(),
                new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        TextView aggregateTitle = mCvAggregate.findViewById(R.id.tv_chart_title);
        aggregateTitle.setText(R.string.tab_aggregate);

        List<String> deviceIds = mEvent.getDeviceIds();
        // add device charts
        int rowCount = (int) Math.ceil(deviceIds.size() / (double)COLUMN_COUNT);

        for (int row = 0; row < rowCount; row++) {
            TableRow tableRow = new TableRow(mContext);
//            tableRow.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MyUtil.dpToPx(320, mContext)));

            for (int column = 0; column < COLUMN_COUNT; column++) {
                int cnt = row * COLUMN_COUNT + column;
                String deviceId = cnt < deviceIds.size() ? deviceIds.get(cnt) : null;

                LinearLayout deviceCardView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_chart_card_view, tableRow, false);

                if (deviceId != null) {
                    // add chart
                    FrameLayout deviceLineChartContainer = deviceCardView.findViewById(R.id.fl_chart_container);
                    deviceLineChartContainer.addView(eventChartsManager.getNewDeviceLineChart(deviceId),
                            new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                    // set title
                    TextView deviceTitle = deviceCardView.findViewById(R.id.tv_chart_title);
                    deviceTitle.setText(deviceId);
                } else {
                    //deviceCardView.setVisibility(View.INVISIBLE);
                }

                tableRow.addView(deviceCardView);
            }
            mTlDevices.addView(tableRow);

            view.findViewById(R.id.ll_fragment_event_charts_root).requestLayout();
            view.findViewById(R.id.ll_fragment_event_charts_root).invalidate();
        }
    }

    public void update(EventsManager.CountingDataSet dataSet) {
        if (mContext == null) mContext = getContext();

        //update from preference (they may have changed)
        long lineChartInterval = mUserPrefs.getLong(mContext.getString(R.string.sp_charts_interval), 60000) / 1000;

        EventsManager.CountingDataSet newDataSet = dataSet.toUniformCountingDataSet(EventChartsManager.TIME_OFFSET, lineChartInterval, true);
        Log.d("我", "update: newdataset " + newDataSet);
        Log.d("我", "update: dataset" + dataSet);
        eventChartsManager.update(newDataSet);
    }

    public void stop() {

//        boolean success = true;
////        success &= mLcAggregate.saveToGallery("RecordLineChart" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis())), "IREye", "LineChartAggregate", Bitmap.CompressFormat.PNG, 100);
////        success &= mBcAggregate.saveToGallery("RecordBarChart" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis())), "IREye", "BarChartAggregate", Bitmap.CompressFormat.PNG, 100);
//        success &= mAggregate.saveToGallery("1", "", "LineChartAggregate", Bitmap.CompressFormat.PNG, 100);
//        success &= mBcAggregate.saveToGallery("2", "", "BarChartAggregate", Bitmap.CompressFormat.PNG, 100);
//        if (success) {
//            ToastUtil.makeToast(mContext, mContext.getString(R.string.save_to_gallery_successful_prompt));
//        } else {
//            ToastUtil.makeToast(mContext, mContext.getString(R.string.failed_to_save_to_gallery_prompt));
//        }
    }

//    private BarChart getCustomBarChart() {
//        BarChart ret = new BarChart(mContext);
//
//        XAxis xAxis = ret.getXAxis();
//        YAxis yAxisLeft = ret.getAxisLeft();
//        YAxis yAxisRight = ret.getAxisRight();
//
//        //xAxis.setEnabled(false);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.enableGridDashedLine(10,10000,0);
//        xAxis.setAxisLineWidth(1);
//
//        yAxisRight.setEnabled(false);
//        yAxisLeft.setAxisLineWidth(1);
//        yAxisLeft.setZeroLineWidth(1);
//
//        yAxisLeft.setDrawZeroLine(true);
//        //disable legends and description
//        ret.getLegend().setEnabled(false);
//        ret.getDescription().setEnabled(false);
//
//        ret.setMarker(new MyMarker(mContext, R.layout.layout_chart_marker, TIME_OFFSET));
//
//        xAxis.setValueFormatter(mXAxisTimeFormatter);
//        xAxis.setLabelRotationAngle(-30);
//
//        //set data padding
//        xAxis.setSpaceMin(30);
//        xAxis.setSpaceMax(30);
//
//        return ret;
//    }
//
//    static private BarData getCustomBarData(Context context, List<BarEntry> dataPos, List<BarEntry> dataNeg) {
//        BarDataSet barPosDataSet = new BarDataSet(dataPos, "pos"),
//                barNegDataSet = new BarDataSet(dataNeg, "neg");
//
//        BarData barData = new BarData(barPosDataSet, barNegDataSet);
//
//        barPosDataSet.setColor(context.getColor(R.color.colorMyTheme));
//        barNegDataSet.setColor(context.getColor(R.color.colorRed));
//        barPosDataSet.setValueTextColor(context.getColor(R.color.colorMyTheme));
//        barNegDataSet.setValueTextColor(context.getColor(R.color.colorRed));
//
//        barData.setBarWidth(barChartInterval / 2000f *0.8f);
//        return barData;
//    }
//
//    static private Map<String, List<BarEntry>> makeBarDataSet(Map<String, List<EventsManager.CountingEntry>> dataSet, long timeInterval) {
//        Map<String, List<BarEntry>> rets = new TreeMap<>();
//
//        long start = truncateTimeToMin(((List<EventsManager.CountingEntry>)dataSet.values().toArray()[0]).get(((List<EventsManager.CountingEntry>)dataSet.values().toArray()[0]).size() - 1).time);
//
//        for (String deviceId : dataSet.keySet()) {
//            List<EventsManager.CountingEntry> data = dataSet.get(deviceId);
//            List<BarEntry> ret = new ArrayList<>();
//            ListIterator<EventsManager.CountingEntry> it = data.listIterator(data.size());
//
//            //consume first entry
//            it.previous();
//
//            Map<Long, Integer> tmp = new LinkedHashMap<>();
//            while (it.hasPrevious()) {
//                EventsManager.CountingEntry cur = it.previous();
//                tmp.put(getStartOfInterval(truncateTimeToMin(cur.time), start, timeInterval), cur.count - data.get(data.size() - 1).count);
//            }
//
//            //bar-specific
//            List<Long> keyList = new LinkedList<>(tmp.keySet());
//            for (int i = tmp.size() - 1; i > 0; --i) {
//                tmp.put(keyList.get(i), tmp.get(keyList.get(i)) - tmp.get(keyList.get(i - 1)));
//            }
//
//            for (Map.Entry<Long, Integer> entry : tmp.entrySet()) {
//                ret.add(new BarEntry(compressTimeInSeconds(entry.getKey(), TIME_OFFSET), entry.getValue()));
//            }
//            rets.put(deviceId, ret);
//        }
//        return rets;
//    }
//
//
////    [0] for positive dataset, [1] for negative dataset
//    private List<List<BarEntry>> combineToAggregateBarDataSet(Map<String, List<BarEntry>> dataSet) {
//        Map<Float, Float> tmpPos = new TreeMap<>();
//        Map<Float, Float> tmpNeg = new TreeMap<>();
//
//
//        for (String deviceId : dataSet.keySet()) {
//            List<BarEntry> data = dataSet.get(deviceId);
//            boolean inOrOut = mDevicePrefs.getBoolean(deviceId, true);
//
//            for (Entry entry : data) {
//                if (inOrOut) {
//                    if (tmpPos.containsKey(entry.getX() + barChartInterval / 1000f * 0.3f)) {
//                        tmpPos.put(entry.getX() + barChartInterval / 1000f * 0.3f, tmpPos.get(entry.getX() + barChartInterval / 1000f * 0.3f) + entry.getY());
//                    }else {
//                        tmpPos.put(entry.getX() + barChartInterval / 1000f * 0.3f, entry.getY());
//                    }
//                } else {
//                    if (tmpNeg.containsKey(entry.getX() + barChartInterval / 1000f * 0.7f)) {
//                        tmpNeg.put(entry.getX() + barChartInterval / 1000f * 0.7f, tmpNeg.get(entry.getX() + barChartInterval / 1000f * 0.7f) - entry.getY());
//                    } else {
//                        tmpNeg.put(entry.getX() + barChartInterval / 1000f * 0.7f, -entry.getY());
//                    }
//                }
//            }
//        }
//        List<List<BarEntry>> ret = new ArrayList<>();
//        ret.add(new ArrayList<BarEntry>());
//        ret.add(new ArrayList<BarEntry>());
//
//        for (Map.Entry<Float, Float> entry : tmpPos.entrySet()) {
//            ret.get(0).add(new BarEntry(entry.getKey(), entry.getValue()));
//        }
//        for (Map.Entry<Float, Float> entry : tmpNeg.entrySet()) {
//            ret.get(1).add(new BarEntry(entry.getKey(), entry.getValue()));
//        }
//        return ret;
//    }

    //merge entries from different devices to one, while maintaining its descending chronological order.
    //didn't use
//    static private List<EventsManager.CountingEntry> mergeDataSets(Map<String, List<EventsManager.CountingEntry>> dataSet) {
//        List<EventsManager.CountingEntry> ret = new ArrayList<>();
//
//        for (List<EventsManager.CountingEntry> lists : dataSet.values()) {
//            List<EventsManager.CountingEntry> tmp = new ArrayList<>();
//            Iterator<EventsManager.CountingEntry> it = lists.iterator();
//            Iterator<EventsManager.CountingEntry> itret = ret.iterator();
//
//            if (it.hasNext() && itret.hasNext()) {
//                EventsManager.CountingEntry p = it.next(), pret = itret.next();
//
//                while (true) {
//                    if (p.compareTo(pret) > 0) {
//                        tmp.add(p);
//                        if (!it.hasNext()) {
//                            tmp.add(pret);
//                            break;
//                        }
//                        p = it.next();
//                    } else {
//                        tmp.add(pret);
//                        if (!itret.hasNext()) {
//                            tmp.add(p);
//                            break;
//                        }
//                        pret = itret.next();
//                    }
//                }
//            }
//
//            while (it.hasNext()) {
//                tmp.add(it.next());
//            }
//            while (itret.hasNext()) {
//                tmp.add(itret.next());
//            }
//
//            ret = new ArrayList<>(tmp);
//        }
//
//        return ret;
//    }

}
