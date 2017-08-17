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

import android.os.Parcelable;
import org.radarcns.android.device.DeviceServiceProvider;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static java.util.Arrays.asList;

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
        return asList(ACCESS_COARSE_LOCATION, BLUETOOTH, BLUETOOTH_ADMIN);
    }
}
