package com.hci.ireye.data.aliyun;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class DeviceManager implements Serializable {

    private static final String PRODUCT_KEY = "ha9aL7yJNmT";

    private static DeviceManager mDeviceManager = null;

    public static DeviceManager getInstance() {
        if (mDeviceManager == null) mDeviceManager = new DeviceManager();
        return mDeviceManager;
    }

    public CountingDevice getCountingDevice(String deviceId) {
        for (CountingDevice device : getAllCountingDevices()) {
            if (device.iotId.equals(deviceId)) return device;
        }
        return null;
    }


    public ArrayList<CountingDevice> getAllCountingDevices() {

        ArrayList<CountingDevice> devices = new ArrayList<>();
        try (Response response = new AliyunRequestExecutor("QueryDevice")
                .addParam("ProductKey", PRODUCT_KEY)
                .execute()) {
            // success
            Gson gson = new GsonBuilder().create();
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            Log.d("曹操", "getAllCountingDevices: "+json);
            JsonArray devicesInfo = json.getAsJsonObject("Data").getAsJsonArray("DeviceInfo");

            for (JsonElement x : devicesInfo) {
                JsonObject deviceInfo = x.getAsJsonObject();
                devices.add(
                        new CountingDevice(
                                deviceInfo.getAsJsonPrimitive("IotId").getAsString(),
                                deviceInfo.getAsJsonPrimitive("DeviceName").getAsString(),
                                deviceInfo.getAsJsonPrimitive("DeviceStatus").getAsString().equals("ONLINE")
                        )
                );
            }
        } catch (Exception e) {
            Log.d("我", "getAllCountingDevices: " + e);
        }
        return devices;
    }

    public static void main(String[] args) {

        List<CountingDevice> devices = DeviceManager.getInstance().getAllCountingDevices();
        for (CountingDevice device :devices) {
            System.out.println(device.getThingModelProperties());
        }
    }

    public class CountingDevice implements Serializable {
        private String iotId;
        private String deviceId;
        private boolean isOnline;

        public CountingDevice(String iotId, String deviceId, boolean isOnline) {
            this.iotId = iotId;
            this.deviceId = deviceId;
            this.isOnline = isOnline;
        }

        public String getIotId() {
            return iotId;
        }

        public String getDeviceId() {
            return deviceId;
        }


        public boolean activate(Integer inflowOutflowStatus, String eventId) {
            JsonObject json = new JsonObject();
            json.addProperty("RunningState", 1);
            if (inflowOutflowStatus != null) {
                json.addProperty("InflowOutflowStatus", inflowOutflowStatus);
            }
            if (eventId != null ) {
                json.addProperty("EventId", eventId);
            }
            return setCountingDeviceProperty(json.toString());
        }

        public boolean deactivate() {
            JsonObject json = new JsonObject();
            json.addProperty("RunningState", 0);
            return setCountingDeviceProperty(json.toString());
        }

        // updates online status, uses internet
        public boolean fetchOnlineStatus() {
            try (Response response = new AliyunRequestExecutor("GetDeviceStatus")
                    .addParam("IotId", iotId)
                    .execute()) {

                Gson gson = new GsonBuilder().create();
                JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);

                String status = jsonResponse.getAsJsonObject("Data").getAsJsonPrimitive("Status").getAsString();
                return isOnline = status.equals("ONLINE");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isOnline = false;
        }

        // returns stored status, does not go thru internet
        public boolean isOnline() {
           return isOnline;
        }


        public CountingDeviceProperties getThingModelProperties() {
            try (Response response = new AliyunRequestExecutor("QueryDevicePropertyStatus")
                    .addParam("IotId", iotId)
                    .execute()) {

                Gson gson = new GsonBuilder().create();
                JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray jsonProperties = jsonResponse.getAsJsonObject("Data")
                        .getAsJsonObject("List")
                        .getAsJsonArray("PropertyStatusInfo");

                return new CountingDeviceProperties(jsonProperties);


            } catch (Exception e) {
                Log.d("我", "getThingModelProperties: " + e);
                return null;
            }
        }

        private boolean setCountingDeviceProperty(String jsonProperties) {
            try (Response response = new AliyunRequestExecutor("SetDeviceProperty")
                    .addParam("IotId", iotId)
                    .addParam("Items", jsonProperties)
                    .execute()) {
                return true;
            } catch (Exception e) {
                Log.d("屮", "activateDevice: " + e.toString());
                return false;
            }
        }
    }

    public class CountingDeviceProperties {
        private String ipAddress, eventId;
        private int runningState, inflowOutflowStatus, ossConnectionState;

        private static final int COUNTING_INFLOW = 1;
        private static final int COUNTING_OUTFLOW = 2;
        private static final int COUNTING_INFLOW_OUTFLOW = 3;

        private static final int RUNNING_STATE_ACTIVATED = 1;
        private static final int RUNNING_STATE_DEACTIVATED = 0;

        private static final int OSS_CONNECTED = 1;
        private static final int OSS_DISCONNECTED = 0;

        @Override
        public String toString() {
            return "CountingDeviceProperties{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", eventId='" + eventId + '\'' +
                    ", runningState=" + runningState +
                    ", inflowOutflowStatus=" + inflowOutflowStatus +
                    ", ossConnectionState=" + ossConnectionState +
                    '}';
        }

        private CountingDeviceProperties(JsonArray jsonProperties) {


            for (JsonElement x : jsonProperties) {
                JsonObject property = x.getAsJsonObject();
                String propertyName = property.getAsJsonPrimitive("Identifier").getAsString();
                JsonPrimitive propertyValue = property.getAsJsonPrimitive("Value");
                if (propertyName.equals("IPAddress")) {
                    ipAddress = propertyValue.getAsString();
                } else if (propertyName.equals("RunningState")) {
                    runningState = propertyValue.getAsInt();
                } else if (propertyName.equals("EventId")) {
                    eventId = propertyValue.getAsString();
                } else if (propertyName.equals("InflowOutflowStatus")) {
                    inflowOutflowStatus = propertyValue.getAsInt();
                } else if (propertyName.equals("OssConnectionState")) {
                    ossConnectionState = propertyValue.getAsInt();
                }
            }
        }
        public int getInflowOutflowStatus() {
            return inflowOutflowStatus;
        }

        // get the id of the event this device is currently counting for.
        public String getEventId() {
            return eventId;
        }

        public int getRunningState() {
            return runningState;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getOssConnectionState() {
            return ossConnectionState;
        }

        // convenience methods
        public boolean isActivated() {
            return runningState == RUNNING_STATE_ACTIVATED;
        }

        public boolean isCountingInflow() {
            return inflowOutflowStatus == COUNTING_INFLOW || inflowOutflowStatus == COUNTING_INFLOW_OUTFLOW;
        }

        public boolean isCountingOutflow() {
            return inflowOutflowStatus == COUNTING_OUTFLOW || inflowOutflowStatus == COUNTING_INFLOW_OUTFLOW;
        }

        public boolean isOSSConnected() {
            return ossConnectionState == OSS_CONNECTED;
        }

    }


}
