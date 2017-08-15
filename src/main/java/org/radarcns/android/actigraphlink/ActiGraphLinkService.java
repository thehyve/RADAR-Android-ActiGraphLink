package org.radarcns.android.actigraphlink;

import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceManager;
import org.radarcns.android.device.DeviceService;
import org.radarcns.android.device.DeviceTopics;

public class ActiGraphLinkService extends DeviceService {
    @Override
    protected DeviceManager createDeviceManager() {
        return new ActiGraphLinkDeviceManager(this, getDataHandler());
    }

    @Override
    protected BaseDeviceState getDefaultState() {
        return new ActiGraphLinkDeviceState();
    }

    @Override
    protected DeviceTopics getTopics() {
        return ActiGraphLinkTopics.getInstance();
    }
}
