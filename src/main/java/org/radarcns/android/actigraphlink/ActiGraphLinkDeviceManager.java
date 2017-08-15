package org.radarcns.android.actigraphlink;

import android.content.Context;
import android.support.annotation.NonNull;
import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceManager;
import actigraph.deviceapi.*;

import java.io.IOException;
import java.util.Set;

public class ActiGraphLinkDeviceManager implements DeviceManager {
    private final ActiGraphLinkDeviceState deviceState = new ActiGraphLinkDeviceState();
    private final Context context;
    private boolean started;
    private AGDeviceLibrary agDeviceLibrary;

    public ActiGraphLinkDeviceManager(Context context) {
        this.context = context;
    }

    @Override
    public void start(@NonNull Set<String> acceptableIds) {
        if (!started) {
            agDeviceLibrary = AGDeviceLibrary.getInstance();
            agDeviceLibrary.registerLibraryListener(context, new AGDeviceLibraryListener() {
                @Override
                public void OnDeviceData(String s) {

                }

                @Override
                public void OnDeviceStatus(String s) {

                }
            });
            started = true;
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
        if (started && agDeviceLibrary != null) {
            String device = agDeviceLibrary.GetConnectedDevice();
            if (device != null) {
                return device;
            }
        }
        return "No ActiGraph Link devices found";
    }

    @Override
    public void close() throws IOException {
        if (started) {
            if (agDeviceLibrary != null) {
                agDeviceLibrary.CancelEnumeration();
                agDeviceLibrary.DisconnectFromDevice();
            }
        }
    }
}
