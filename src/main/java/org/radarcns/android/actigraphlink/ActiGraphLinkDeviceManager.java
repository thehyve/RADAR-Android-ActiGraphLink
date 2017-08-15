package org.radarcns.android.actigraphlink;

import actigraph.deviceapi.AGDeviceLibrary;
import actigraph.deviceapi.AGDeviceLibraryListener;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.radarcns.actigraphlink.ActiGraphLinkAcceleration;
import org.radarcns.android.data.DataCache;
import org.radarcns.android.data.TableDataHandler;
import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceManager;
import org.radarcns.android.device.DeviceStatusListener;
import org.radarcns.key.MeasurementKey;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ActiGraphLinkDeviceManager implements DeviceManager {
    private static final String TAG = ActiGraphLinkDeviceManager.class.getSimpleName();
    private final ActiGraphLinkDeviceState deviceState = new ActiGraphLinkDeviceState();
    private final Context context;
    private final TableDataHandler dataHandler;
    private boolean started;
    private String deviceId;
    private AGDeviceLibrary agDeviceLibrary;
    private final DataCache<MeasurementKey, ActiGraphLinkAcceleration> accelerationTable;

    public ActiGraphLinkDeviceManager(Context context, TableDataHandler dataHandler) {
        this.context = context;
        this.dataHandler = dataHandler;
        this.accelerationTable = dataHandler.getCache(ActiGraphLinkTopics.getInstance().getAccelerationTopic());
    }

    @Override
    public void start(@NonNull final Set<String> acceptableIds) {
        if (!started) {
            agDeviceLibrary = AGDeviceLibrary.getInstance();
            agDeviceLibrary.EnumerateDevices();

            agDeviceLibrary.registerLibraryListener(context, new AGDeviceLibraryListener() {
                @Override
                public void OnDeviceData(String data) {
                    Log.d(TAG, "Device data: " + data);

                    if (deviceId != null) {
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(data);
                        if (json.has("device")) {
                            String device = json.getString("device");
                            if (acceptableIds.contains(device)) {
                                Log.i(TAG, "Connecting to device " + device);
                                deviceId = device;
                                deviceState.getId().setSourceId(deviceId);
                                deviceState.setStatus(DeviceStatusListener.Status.CONNECTING);
                                agDeviceLibrary.ConnectToDevice(deviceId);

                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + data, e);
                    }
                }

                @Override
                public void OnDeviceStatus(String data) {
                    Log.d(TAG, "Device status: " + data);

                    try {
                        processDeviceStatus(new JSONObject(data));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + data, e);
                    }
                }
            });
            started = true;
        }
    }

    private void processDeviceStatus(JSONObject json) throws JSONException {
        if (json.has("deviceConnected")) {
            String device = json.getString("deviceConnected");
            Log.i(TAG, "Device connected: " + device);

            if (Objects.equals(deviceId, device)) {
                String configureIMU = new JSONObject()
                        .put("imu", new JSONObject().put("accelerometer", true))
                        .toString();
                agDeviceLibrary.ConfigureDevice(configureIMU);

                String enableStream = new JSONObject()
                        .put("raw", new JSONObject().put("stream", true))
                        .toString();
                agDeviceLibrary.ConfigureDevice(enableStream);

                deviceState.setStatus(DeviceStatusListener.Status.CONNECTED);
            }
        }
        if (json.has("deviceDisconnected")) {
            String device = json.getString("deviceDisconnected");
            Log.i(TAG, "Device disconnected: " + device);

            if (Objects.equals(deviceId, device)) {
                deviceState.setStatus(DeviceStatusListener.Status.DISCONNECTED);
            }
        }
        if (json.has("error")) {
            JSONObject error = json.getJSONObject("error");
            Log.e(TAG, "Error: " + error.get("description") + (error.has("extra") ? ", " + error.get("extra") : ""));
        }
        if (json.has("raw")) {
            JSONObject raw = json.getJSONObject("raw");
            if (Objects.equals(deviceId, raw.getString("device"))) {
                double time = Long.parseLong(raw.getString("timestamp"));
                double timeReceived = TimeUnit.MICROSECONDS.toSeconds(System.currentTimeMillis());
                JSONArray accelerations = raw.getJSONArray("acceleration");
                for (int i = 0; i < accelerations.length(); i++) {
                    JSONObject acceleration = accelerations.getJSONObject(i);
                    float x = (float) acceleration.getDouble("x");
                    float y = (float) acceleration.getDouble("y");
                    float z = (float) acceleration.getDouble("z");
                    dataHandler.addMeasurement(accelerationTable, deviceState.getId(), new ActiGraphLinkAcceleration(time, timeReceived, x, y, z));
                    deviceState.setAcceleration(x, y, z);
                }
            }
        }
    }


    @Override
    public boolean isClosed() {
        return !started;
    }

    @Override
    public BaseDeviceState getState() {
        return deviceState;
    }

    @Override
    public String getName() {
        if (deviceId != null) {
            return deviceId;
        }
        return "No ActiGraph Link devices found";
    }

    @Override
    public void close() throws IOException {
        if (started) {
            if (agDeviceLibrary != null) {
                deviceState.setStatus(DeviceStatusListener.Status.DISCONNECTED);
                agDeviceLibrary.CancelEnumeration();
                agDeviceLibrary.DisconnectFromDevice();
            }
        }
    }
}
