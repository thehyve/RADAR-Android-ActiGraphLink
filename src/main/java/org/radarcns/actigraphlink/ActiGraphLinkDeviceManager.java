/*
 * Copyright 2017 The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.actigraphlink;

import actigraph.deviceapi.AGDeviceLibrary;
import actigraph.deviceapi.AGDeviceLibraryListener;
import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.radarcns.android.data.DataCache;
import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceManager;
import org.radarcns.key.MeasurementKey;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.radarcns.android.device.DeviceStatusListener.Status.*;

public class ActiGraphLinkDeviceManager implements DeviceManager {
    private static final String TAG = ActiGraphLinkDeviceManager.class.getSimpleName();
    private final ActiGraphLinkDeviceState deviceState = new ActiGraphLinkDeviceState();
    private final ActiGraphLinkService service;
    private final DataCache<MeasurementKey, ActiGraphLinkAcceleration> accelerationTable;

    private AGDeviceLibrary agDeviceLibrary;

    public ActiGraphLinkDeviceManager(ActiGraphLinkService service) {
        this.service = service;
        this.accelerationTable = service.getDataHandler().getCache(ActiGraphLinkTopics.getInstance().getAccelerationTopic());
        this.deviceState.getId().setUserId(service.getUserId());
    }

    @Override
    public synchronized void start(@NonNull final Set<String> acceptableIds) {
        Log.i(TAG, "Starting. Acceptable devices: " + acceptableIds);
        if (agDeviceLibrary == null) {
            agDeviceLibrary = AGDeviceLibrary.getInstance();

            agDeviceLibrary.registerLibraryListener(service, new AGDeviceLibraryListener() {
                @Override
                public void OnDeviceData(String data) {
                    Log.d(TAG, "Device data: " + data);

                    try {
                        processDeviceData(acceptableIds, new JSONObject(data));
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
        }

        deviceState.setStatus(READY);
        agDeviceLibrary.EnumerateDevices();
    }

    private synchronized void processDeviceData(@NonNull final Set<String> acceptableIds, JSONObject json) throws JSONException {
        if (json.has("device")) {
            String device = json.getString("device");
            if (acceptableIds.contains(device)) {
                switch (deviceState.getStatus()) {
                    case READY: {
                        Log.i(TAG, "Connecting to device " + device);
                        deviceState.getId().setSourceId(device);
                        deviceState.setStatus(CONNECTING);
                        agDeviceLibrary.ConnectToDevice(device);
                        break;
                    }
                    case CONNECTING: {
                        if (!isActiveDevice(device)) {
                            Log.w(TAG,"Another acceptable device: " + device);
                        }
                        break;
                    }
                }
            }
        }
    }

    private synchronized void processDeviceStatus(JSONObject json) throws JSONException {
        if (json.has("deviceConnected")) {
            String device = json.getString("deviceConnected");
            Log.i(TAG, "Device connected: " + device);

            if (isActiveDevice(device)) {
                String configureIMU = new JSONObject()
                        .put("imu", new JSONObject().put("accelerometer", true))
                        .toString();
                agDeviceLibrary.ConfigureDevice(configureIMU);

                String enableStream = new JSONObject()
                        .put("raw", new JSONObject().put("stream", true))
                        .toString();
                agDeviceLibrary.ConfigureDevice(enableStream);

                deviceState.setStatus(CONNECTED);
            }
        }
        if (json.has("deviceDisconnected")) {
            String device = json.getString("deviceDisconnected");
            Log.i(TAG, "Device disconnected: " + device);

            if (isActiveDevice(device)) {
                deviceState.setStatus(DISCONNECTED);
            }
        }
        if (json.has("error")) {
            JSONObject error = json.getJSONObject("error");
            Log.e(TAG, "Error: " + error.get("description") + (error.has("extra") ? ", " + error.get("extra") : ""));
        }
        if (json.has("raw")) {
            JSONObject raw = json.getJSONObject("raw");
            if (isActiveDevice(raw.getString("device"))) {
                double time = raw.getLong("timestamp");
                double timeReceived = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                JSONArray accelerations = raw.getJSONArray("acceleration");
                for (int i = 0; i < accelerations.length(); i++) {
                    JSONObject acceleration = accelerations.getJSONObject(i);
                    float x = (float) acceleration.getDouble("x");
                    float y = (float) acceleration.getDouble("y");
                    float z = (float) acceleration.getDouble("z");
                    service.getDataHandler().addMeasurement(accelerationTable, deviceState.getId(), new ActiGraphLinkAcceleration(time, timeReceived, x, y, z));
                    deviceState.setAcceleration(x, y, z);
                }
            }
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return agDeviceLibrary == null;
    }

    @Override
    public synchronized BaseDeviceState getState() {
        return deviceState;
    }

    @Override
    public synchronized String getName() {
        if (deviceState.getId().getSourceId() != null) {
            return deviceState.getId().getSourceId();
        }
        return "No ActiGraph Link devices found";
    }

    @Override
    public synchronized void close() throws IOException {
        if (agDeviceLibrary != null) {
            deviceState.setStatus(DISCONNECTED);
            agDeviceLibrary.CancelEnumeration();
            agDeviceLibrary.DisconnectFromDevice();
        }
    }

    private boolean isActiveDevice(String device) {
        return Objects.equals(deviceState.getId().getSourceId(), device);
    }
}
