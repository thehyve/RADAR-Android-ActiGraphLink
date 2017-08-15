package org.radarcns.android.actigraphlink;

import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceStateCreator;

public class ActiGraphLinkDeviceState extends BaseDeviceState {
    public static final Creator<ActiGraphLinkDeviceState> CREATOR = new DeviceStateCreator<>(ActiGraphLinkDeviceState.class);

    @Override
    public boolean hasAcceleration() {
        return true;
    }
}
