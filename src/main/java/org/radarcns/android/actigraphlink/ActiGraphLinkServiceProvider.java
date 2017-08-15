package org.radarcns.android.actigraphlink;

import android.os.Parcelable;
import org.radarcns.android.device.DeviceServiceProvider;

import java.util.List;

import static java.util.Collections.emptyList;

public class ActiGraphLinkServiceProvider extends DeviceServiceProvider<ActiGraphLinkDeviceState> {
    @Override
    public Class<?> getServiceClass() {
        return ActiGraphLinkService.class;
    }

    @Override
    public Parcelable.Creator<ActiGraphLinkDeviceState> getStateCreator() {
        return ActiGraphLinkDeviceState.CREATOR;
    }

    @Override
    public String getDisplayName() {
        return getActivity().getString(R.string.actiGraphLinkDisplayName);
    }

    @Override
    public List<String> needsPermissions() {
        return emptyList();
    }
}
