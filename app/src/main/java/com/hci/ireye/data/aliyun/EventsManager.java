package com.hci.ireye.data.aliyun;

//
// Created by Lithops on 2022/6/2, 14:49.
//

import static com.hci.ireye.ui.util.MyUtil.getKey;
import static com.hci.ireye.ui.util.MyUtil.getStartOfInterval;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.hci.ireye.R;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.util.ToastUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class EventsManager {
    private static final String EVENT_METADATA_FILENAME = "metadata.json";

    private static EventsManager mEventManager = null;

//    private final SharedPreferences mEventManagerPrefs;

    private EventsManager() {
//        mEventManagerPrefs = context.getSharedPreferences(context.getString(R.string.event_manager_prefs), Context.MODE_PRIVATE);
    }

    public static EventsManager getInstance() {
        if (mEventManager == null) mEventManager = new EventsManager();
        return mEventManager;
    }

    public CountingEvent getEvent(Context context, String eventId) {
        for (CountingEvent countingEvent : getAllEvents(context)) {
            if (countingEvent.eventId.equals(eventId)) return countingEvent;
        }
        return null;
    }

    public ArrayList<CountingEvent> getAllEvents(Context context) {
        ArrayList<CountingEvent> ret = new ArrayList<>();

        OSSManager ossManager = OSSManager.getInstance(context);

        List<String> activityFolderPaths = ossManager.listFolders("");
        for (String activityFolderPath : activityFolderPaths) {
            String metadataPath = activityFolderPath + EVENT_METADATA_FILENAME;
            try {
                String metadata = ossManager.readFile(metadataPath);
                ret.add(new Gson().fromJson(metadata, CountingEvent.class));
            } catch (Exception e) {
                Log.d("我", "error trying to find event metadata: " + e);
            }
        }


        return ret;
    }

    // uses internet
    public CountingEvent startNewEvent(Context context, String eventName, Map<DeviceManager.CountingDevice, Integer> devicesToIOStatus) {
        OSSManager ossManager = OSSManager.getInstance(context);
        try {
            ossManager.uploadFile(eventName + "/", "");
            ossManager.uploadFile(eventName + "/metadata.json", generateEventMetadata(eventName, new ArrayList<>(devicesToIOStatus.keySet())).toString());
        } catch (ServiceException | ClientException e) {
            Log.d("我", "startNewEvent: " + e);
        }

        for (DeviceManager.CountingDevice device : devicesToIOStatus.keySet()) {
            device.activate(devicesToIOStatus.get(device), eventName);
        }

        return getEvent(context, eventName);
    }

    private JsonObject generateEventMetadata(String eventName, List<DeviceManager.CountingDevice> devices) {
        JsonArray deviceIds = new JsonArray();
        for (DeviceManager.CountingDevice device : devices) {
            deviceIds.add(device.getDeviceId());
        }

        JsonObject json = new JsonObject();
        json.addProperty("eventName", eventName);
        json.addProperty("startTime", System.currentTimeMillis() / 1000);
        json.add("endTime", JsonNull.INSTANCE);
        json.addProperty("eventId", eventName); // todo eventId should be different from eventName?
        json.addProperty("isOngoing", true);
        json.add("deviceIds", deviceIds);

        return json;

    }

    // all epoch time in seconds!
    public static class CountingEvent implements Serializable {
        private String eventName, eventId;
        private Integer startTime, endTime;
        private ArrayList<String> deviceIds;
        private boolean isOngoing;

        private CountingEvent(String eventName, String eventId, int startTime, Integer endTime, ArrayList<String> deviceIds, boolean isOngoing) {
            this.eventName = eventName;
            this.eventId = eventId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.deviceIds = deviceIds;
            this.isOngoing = isOngoing;
        }

        public String getEventName() {
            return eventName;
        }

        public String getEventId() {
            return eventId;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public List<String> getDeviceIds() {
            return Collections.unmodifiableList(deviceIds);
        }

        public boolean isOngoing() {
            return isOngoing;
        }

        // uses internet
        public void stop(Context context) {
            isOngoing = false;

            try {
                // update metadata
                OSSManager ossManager = OSSManager.getInstance(context);
                JsonObject metadata = new Gson().fromJson(ossManager.readFile(eventId + "/metadata.json"), JsonObject.class);
                metadata.addProperty("endTime", System.currentTimeMillis() / 1000);
                metadata.addProperty("isOngoing", false);
                ossManager.uploadFile(eventId + "/metadata.json", metadata.toString());

                // deactivate all devices
                for (DeviceManager.CountingDevice device : DeviceManager.getInstance().getAllCountingDevices()) {
                    if (deviceIds.contains(device.getDeviceId())) {
                        device.deactivate();
                    }
                }


            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "CountingEvent{" +
                    "eventName='" + eventName + '\'' +
                    ", eventId='" + eventId + '\'' +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", deviceIds=" + deviceIds +
                    ", isOngoing=" + isOngoing +
                    '}';
        }

        private String getDeviceFolderPath(String deviceId) {
            return eventId + "/" + deviceId;
        }

        private String getInflowPath(String deviceId) {
            return getDeviceFolderPath(deviceId) + "/write_inflow.txt";
        }

        private String getOutflowPath(String deviceId) {
            return getDeviceFolderPath(deviceId) + "/write_outflow.txt";
        }

        private TreeMap<Long, Integer> parseRawCountingDeviceData(String data) {

            TreeMap<Long, Integer> ret = new TreeMap<>();

            BufferedReader reader = new BufferedReader(new StringReader(data));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] v = line.split(" ");
                    try {
                        long time = Long.parseLong(v[0]) / 1000; // time in millis on server, convert to seconds locally
                        int count = Integer.parseInt(v[1]);
                        ret.put(time, count);
                    } catch (NumberFormatException e) {
                        Log.d("我", "parseCountingData: " + e);
                    }

                }
            } catch (IOException e) {
                Log.d("我", "parseCountingData: " + e);

            }
            return ret;
        }

        public CountingDataSet getCountingDataSet(Context context) {
            OSSManager ossManager = OSSManager.getInstance(context);

//            TreeMap<String, List<CountingEntry>> ret = new TreeMap<>();

            CountingDataSet ret = new CountingDataSet(
                    eventId,
                    null,
                    null);

            TreeMap<Long, Integer> parsedInflowData = null, parsedOutflowData = null;
            for (String deviceId : deviceIds) {
                try {
                    parsedInflowData = parseRawCountingDeviceData(ossManager.readFile(getInflowPath(deviceId)));
                } catch (Exception ex) {
                    Log.d("我", "getCountingDataSet: " + ex);
                }
                try {
                    parsedOutflowData = parseRawCountingDeviceData(ossManager.readFile(getOutflowPath(deviceId)));
                } catch (Exception ex) {
                    Log.d("我", "getCountingDataSet: " + ex);

                }
                Log.d("我", "getCountingDataSet: countingdevicedata constructed with devicejd = " + deviceId);
                CountingDeviceData deviceData = new CountingDeviceData(
                        deviceId,
                        null,
                        null,
                        parsedInflowData,
                        parsedOutflowData
                );
                ret.addRawCountingData(deviceId, deviceData);
            }

            Log.d("我", "DataGetter: data retrieved successfully");
            ThreadUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.makeToast(context, context.getString(R.string.data_fetch_successful_prompt));
                }
            });
            return ret;
        }

    }

    // data generated by all devices of an event
    public static class CountingDataSet {
        private String eventId;
        Long timeInterval;
        Boolean isCumulative;
        private TreeMap<String, CountingDeviceData> dataset;

        public CountingDataSet(String eventId, Long timeInterval, Boolean isCumulative) {
            this.eventId = eventId;
            this.timeInterval = timeInterval;
            this.isCumulative = isCumulative;
            this.dataset =new TreeMap<>();
        }

        private CountingDataSet addRawCountingData(String deviceId, CountingDeviceData countingData) {
            dataset.put(deviceId, countingData);
            return this;
        }

        @Override
        public String toString() {
            return "CountingDataSet{" +
                    "eventId='" + eventId + '\'' +
                    ", dataset=" + dataset +
                    '}';
        }

        public String getEventId() {
            return eventId;
        }

        public CountingDeviceData getCountingDeviceData(String deviceId, long timeInterval, boolean isCumulative) {
            return dataset.get(deviceId).toUniformCountingDeviceData(timeInterval, isCumulative);
        }

        public CountingDeviceData getCountingDeviceData(String deviceId) {
            if (timeInterval == null) {
                throw new RuntimeException("non-uniform device data is not exposed for use.");
            }
            return dataset.get(deviceId);
        }


        public CountingDeviceData getCountingAggregateData() {
            if (timeInterval == null) throw new RuntimeException("this device data is not uniform! Call its toUniformCountingDeviceDataSet() first.");

            TreeMap<Long, Integer> aggregateInflow = new TreeMap<>();
            TreeMap<Long, Integer> aggregateOutflow = new TreeMap<>();

            for (CountingDeviceData deviceData : dataset.values()) {
                if (deviceData.inflow != null) {
                    for (Map.Entry<Long, Integer> entry : deviceData.inflow.entrySet()) {
                        Long time = entry.getKey();
                        Integer count = entry.getValue();
                        aggregateInflow.merge(time, count, Integer::sum);
                    }
                }
                if (deviceData.outflow != null) {
                    for (Map.Entry<Long, Integer> entry : deviceData.outflow.entrySet()) {
                        Long time = entry.getKey();
                        Integer count = entry.getValue();
                        aggregateOutflow.merge(time, count, Integer::sum);
                    }
                }
            }

            return new CountingDeviceData(
                    null,
                    timeInterval,
                    isCumulative,
                    aggregateInflow,
                    aggregateOutflow
                    );
        }

        public Set<String> getDeviceIds() {
            return dataset.keySet();
        }

        public int getTotalInflow() {
            int ret = 0;
            for (String deviceId : dataset.keySet()) {
                ret += dataset.get(deviceId).getTotalInflow();
            }
            return ret;
        }

        public int getTotalInflow(String deviceId) {
            return dataset.get(deviceId).getTotalInflow();
        }

        public int getTotalOutflow() {
            int ret = 0;
            for (String deviceId : dataset.keySet()) {
                ret += dataset.get(deviceId).getTotalOutflow();
            }
            return ret;
        }
        public int getTotalOutflow(String deviceId) {
            return dataset.get(deviceId).getTotalOutflow();
        }


        public int getTotalNetflow() {
            return getTotalInflow() - getTotalOutflow();
        }

        public int getTotalNetflow(String deviceId) {
            return dataset.get(deviceId).getTotalNetflow();
        }

        public CountingDataSet toUniformCountingDataSet(long timeInterval, boolean isCumulative) {
            CountingDataSet newDataset = new CountingDataSet(eventId, timeInterval, isCumulative);

            for (Map.Entry<String, CountingDeviceData> entry : dataset.entrySet()) {
                String deviceId = entry.getKey();
                CountingDeviceData data = entry.getValue();
                newDataset.addRawCountingData(deviceId, data.toUniformCountingDeviceData(timeInterval, isCumulative));
            }
            return newDataset;
        }
    }


    // data generated by a single device
    public static class CountingDeviceData {
        private String deviceId;
        private Long timeInterval;
        private Boolean isCumulative;
        // timeInSeconds->count,
        private TreeMap<Long, Integer> inflow, outflow;

        private CountingDeviceData(String deviceId, Long timeInterval, Boolean isCumulative, TreeMap<Long, Integer> inflow, TreeMap<Long, Integer> outflow) {
            this.deviceId = deviceId;
            this.timeInterval = timeInterval;
            this.isCumulative = isCumulative;
            this.inflow = inflow;
            this.outflow = outflow;
        }

        @Override
        public String toString() {
            return "CountingDeviceData{" +
                    "deviceId='" + deviceId + '\'' +
                    ", inflow=" + inflow +
                    ", outflow=" + outflow +
                    '}';
        }

        public String getDeviceId() {
            return deviceId;
        }

        public Long getTimeInterval() {
            return timeInterval;
        }

        public Boolean getCumulative() {
            return isCumulative;
        }

        public int getTotalInflow() {
            if (inflow == null) return 0;
            int ret = 0;
            for (int count : inflow.values()) {
                ret += count;
            }
            return ret;
        }

        public int getTotalOutflow() {
            if (outflow == null) return 0;
            int ret = 0;
            for (int count : outflow.values()) {
                ret += count;
            }
            return ret;
        }

        public int getTotalNetflow() {
            return getTotalInflow() - getTotalOutflow();
        }


        public TreeMap<Long, Integer> getInflow() {
            return inflow;
        }

        public TreeMap<Long, Integer> getOutflow(boolean negative) {
            if (!negative) return outflow;
            TreeMap<Long, Integer> copy = (TreeMap<Long, Integer>) outflow.clone();
            copy.replaceAll((k, v) -> -v);
            return copy;
        }

        public TreeMap<Long, Integer> getOutflow() {
            return getOutflow(false);
        }

        public TreeMap<Long, Integer> getNetflow() {
            if (timeInterval == null) throw new RuntimeException("this device data is not uniform! Call its toUniformCountingDeviceData() first.");

            TreeMap<Long, Integer> ret = new TreeMap<>();

            if (inflow != null) {
                for (Map.Entry<Long, Integer> entry : inflow.entrySet()) {
                    long time = entry.getKey();
                    int count = entry.getValue();
                    ret.merge(time, count, Integer::sum);
                }
            }
            if (outflow != null) {
                for (Map.Entry<Long, Integer> entry : outflow.entrySet()) {
                    long time = entry.getKey();
                    int count = entry.getValue();
                    ret.merge(time, -count, Integer::sum);
                }
            }
            return ret;
        }

        public CountingDeviceData toUniformCountingDeviceData(long timeInterval, boolean isCumulative) {
            CountingDeviceData newData = new CountingDeviceData(
                    deviceId,
                    timeInterval,
                    isCumulative,
                    toUniformCountingDeviceDataSingle(inflow, timeInterval, isCumulative),
                    toUniformCountingDeviceDataSingle(outflow, timeInterval, isCumulative)
            );
            return newData;

        }

        private TreeMap<Long, Integer> toUniformCountingDeviceDataSingle(TreeMap<Long, Integer> data, long timeInterval, boolean isCumulative) {
            if (data == null) return null;

            TreeMap<Long, Integer> newData = new TreeMap<>();

            if (data.isEmpty()) {
                return newData;
            }

            long start = getKey(data, 0);

            for (Map.Entry<Long, Integer> entry : data.entrySet()) {
                long time = entry.getKey();
                int count = entry.getValue();

                newData.merge(getStartOfInterval(time, start, timeInterval), count, Integer::sum);
            }

            if (isCumulative) {
                int cumulativeCount = 0;
                for (Map.Entry<Long, Integer> entry : newData.entrySet()) {
                    long time = entry.getKey();
                    int count = entry.getValue();
                    cumulativeCount += count;
                    newData.replace(time, cumulativeCount);
                }
            }
            return newData;
        }
    }


    // a single line of counting entry
    @Deprecated
    public static class CountingEntry implements Comparable<CountingEntry> {
        public long time;
        public int count;

        @Override
        public int compareTo(@NonNull CountingEntry o) {
            return time < o.time ? -1 : 1;
        }

        public CountingEntry(long time, int count) {
            this.time = time;
            this.count = count;
        }
    }

    public static void main(String[] args) {

        TreeMap<Long, Integer> inflow = new TreeMap<>();
        TreeMap<Long, Integer> outflow = new TreeMap<>();

        inflow.put(1654359020517L, 0);
        inflow.put(1654359034172L, 11);
        inflow.put(1654359044263L, 4);
        inflow.put(1654359054352L, 4);
        inflow.put(1654359064428L, 0);
        inflow.put(1654359074512L, 2);
        inflow.put(1654359084592L, 9);
        inflow.put(1654359094672L, 9);
        inflow.put(1654359104752L, 14);
        inflow.put(1654359114842L, 17);

        outflow.put(1654359759800L, 0);
        outflow.put(1654359773457L, 19);
        outflow.put(1654359783542L, 24);
        outflow.put(1654359793622L, 5);
        outflow.put(1654359803702L, 12);
        outflow.put(1654359813781L, 6);
        outflow.put(1654359823863L, 4);
        outflow.put(1654359833965L, 5);
        outflow.put(1654359844042L, 1);
        outflow.put(1654359854122L, 9);
        outflow.put(1654359864221L, 3);
        outflow.put(1654359874353L, 3);
        outflow.put(1654359884442L, 7);
        outflow.put(1654359894616L, 0);
        outflow.put(1654359904721L, 0);
        outflow.put(1654359914802L, 10);
        outflow.put(1654359924892L, 0);
        outflow.put(1654359934972L, 0);
        outflow.put(1654359945052L, 0);
        outflow.put(1654359955182L, 0);
        outflow.put(1654359965254L, 5);
        outflow.put(1654359975331L, 0);
        outflow.put(1654359985492L, 5);
        outflow.put(1654359995567L, 15);
        outflow.put(1654360005642L, 23);
        outflow.put(1654360015883L, 21);
        outflow.put(1654360025947L, 9);
        outflow.put(1654360036055L, 0);
        outflow.put(1654360046132L, 1);
        outflow.put(1654360056202L, 3);
        outflow.put(1654360066308L, 3);
        outflow.put(1654360076385L, 1);
        outflow.put(1654360086472L, 3);
        outflow.put(1654360096542L, 0);
        outflow.put(1654360106782L, 1);
        outflow.put(1654360116847L, 0);
        outflow.put(1654360126932L, 0);
        outflow.put(1654360137020L, 7);
        outflow.put(1654360147092L, 0);
        outflow.put(1654360157172L, 0);
        outflow.put(1654360167322L, 0);
        outflow.put(1654360177402L, 0);
        outflow.put(1654360187482L, 0);
        outflow.put(1654360197575L, 0);
        outflow.put(1654360207668L, 0);
        outflow.put(1654360217770L, 0);
        outflow.put(1654360228042L, 0);
        outflow.put(1654360238142L, 0);
        outflow.put(1654360248232L, 0);
        outflow.put(1654360258372L, 0);
        outflow.put(1654360268461L, 0);
        outflow.put(1654360278532L, 0);
        outflow.put(1654360288733L, 0);
        outflow.put(1654360298808L, 0);
        outflow.put(1654360308872L, 0);
        outflow.put(1654360319042L, 1);
        outflow.put(1654360329108L, 0);
        outflow.put(1654360339190L, 0);
        outflow.put(1654360349432L, 0);
        outflow.put(1654360359526L, 0);
        outflow.put(1654360369608L, 0);
        outflow.put(1654360379704L, 0);
        outflow.put(1654360389788L, 0);
        outflow.put(1654360399872L, 0);
        outflow.put(1654360410042L, 0);
        outflow.put(1654360420142L, 0);
        outflow.put(1654360430231L, 0);
        outflow.put(1654360440312L, 0);
        outflow.put(1654360450402L, 0);
        outflow.put(1654360460492L, 0);
        outflow.put(1654360470582L, 0);
        outflow.put(1654360480672L, 0);
        outflow.put(1654360490752L, 0);
        outflow.put(1654360500842L, 0);
        outflow.put(1654360510937L, 0);
        outflow.put(1654360521022L, 0);
        outflow.put(1654360531101L, 0);
        outflow.put(1654360541198L, 0);
        outflow.put(1654360551272L, 0);
        outflow.put(1654360561353L, 0);
        outflow.put(1654360571432L, 0);
        outflow.put(1654360581522L, 0);
        outflow.put(1654360591596L, 0);
        outflow.put(1654360601688L, 0);
        outflow.put(1654360611762L, 0);
        outflow.put(1654360621848L, 0);
        outflow.put(1654360631927L, 0);
        outflow.put(1654360642020L, 0);
        outflow.put(1654360652092L, 0);
        outflow.put(1654360662192L, 0);
        outflow.put(1654360672279L, 0);
        outflow.put(1654360682371L, 0);
        outflow.put(1654360692442L, 0);
        outflow.put(1654360702548L, 0);
        outflow.put(1654360712630L, 0);
        outflow.put(1654360722712L, 0);
        outflow.put(1654360732787L, 0);
        outflow.put(1654360742872L, 0);
        outflow.put(1654360752972L, 0);
        outflow.put(1654360763061L, 0);
        outflow.put(1654360773132L, 0);
        outflow.put(1654360783229L, 0);
        outflow.put(1654360793312L, 0);
        outflow.put(1654360803413L, 0);
        outflow.put(1654360813490L, 0);
        outflow.put(1654360823569L, 0);
        outflow.put(1654360833642L, 0);
        outflow.put(1654360843730L, 0);
        outflow.put(1654360853832L, 0);
        outflow.put(1654360863912L, 0);
        outflow.put(1654360873998L, 0);
        outflow.put(1654360884078L, 0);
        outflow.put(1654360894170L, 0);
        outflow.put(1654360904242L, 0);
        outflow.put(1654360914322L, 0);
        outflow.put(1654360924412L, 0);
        outflow.put(1654360934505L, 0);
        outflow.put(1654360944609L, 0);
        outflow.put(1654360954689L, 0);
        outflow.put(1654360964782L, 0);
        outflow.put(1654360974862L, 0);
        outflow.put(1654360984952L, 0);
        outflow.put(1654360995082L, 0);
        outflow.put(1654361005152L, 0);
        outflow.put(1654361015248L, 0);
        outflow.put(1654361025338L, 0);
        outflow.put(1654361035427L, 0);
        outflow.put(1654361045502L, 0);
        outflow.put(1654361055582L, 0);
        outflow.put(1654361065672L, 0);
        outflow.put(1654361075746L, 0);
        outflow.put(1654361085822L, 0);
        outflow.put(1654361095896L, 0);
        outflow.put(1654361105985L, 0);
        outflow.put(1654361116058L, 0);
        outflow.put(1654361126156L, 0);
        outflow.put(1654361136236L, 0);
        outflow.put(1654361146339L, 0);
        outflow.put(1654361156416L, 0);
        outflow.put(1654361166510L, 0);
        outflow.put(1654361176596L, 0);
        outflow.put(1654361186774L, 0);
        outflow.put(1654361196862L, 0);
        outflow.put(1654361206936L, 0);
        outflow.put(1654361217036L, 0);
        outflow.put(1654361227127L, 0);
        outflow.put(1654361237322L, 0);
        outflow.put(1654361247444L, 0);
        outflow.put(1654361257533L, 0);
        outflow.put(1654361267862L, 0);
        outflow.put(1654361277937L, 0);
        outflow.put(1654361288208L, 0);
        outflow.put(1654361298293L, 0);
        outflow.put(1654361308582L, 0);
        outflow.put(1654361318672L, 0);
        outflow.put(1654361328762L, 0);
        outflow.put(1654361338843L, 0);
        outflow.put(1654361348934L, 0);
        outflow.put(1654361359022L, 0);
        outflow.put(1654361369258L, 0);
        outflow.put(1654361379337L, 0);
        outflow.put(1654361389412L, 0);
        outflow.put(1654361399502L, 0);
        outflow.put(1654361409592L, 0);
        outflow.put(1654361419679L, 0);
        outflow.put(1654361429913L, 0);
        outflow.put(1654361439992L, 0);
        outflow.put(1654361450058L, 0);
        outflow.put(1654361460144L, 0);
        outflow.put(1654361470222L, 0);
        outflow.put(1654361480316L, 0);
        outflow.put(1654361490382L, 0);
        outflow.put(1654361500472L, 0);
        outflow.put(1654361510542L, 0);
        outflow.put(1654361520627L, 0);
        outflow.put(1654361530712L, 1);
        outflow.put(1654361540802L, 0);



        CountingDeviceData data = new CountingDeviceData("ee", null, null, inflow, outflow);

        CountingDeviceData newData = data.toUniformCountingDeviceData(120, true);

        for (Map.Entry<Long, Integer> i : newData.inflow.entrySet()) {
            System.out.println(i);
        }



    }
}
