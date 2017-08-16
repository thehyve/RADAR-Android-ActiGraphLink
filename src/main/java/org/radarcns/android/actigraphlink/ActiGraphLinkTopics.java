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

package org.radarcns.android.actigraphlink;

import org.radarcns.actigraphlink.ActiGraphLinkAcceleration;
import org.radarcns.android.device.DeviceTopics;
import org.radarcns.key.MeasurementKey;
import org.radarcns.topic.AvroTopic;

public class ActiGraphLinkTopics extends DeviceTopics {
    private static volatile ActiGraphLinkTopics instance;

    private final AvroTopic<MeasurementKey, ActiGraphLinkAcceleration> accelerationTopic =
            createTopic("android_actigraphlink_acceleration",
                    ActiGraphLinkAcceleration.getClassSchema(),
                    ActiGraphLinkAcceleration.class);

    public static ActiGraphLinkTopics getInstance() {
        if (instance == null) {
            synchronized (ActiGraphLinkTopics.class) {
                if (instance == null) {
                    instance = new ActiGraphLinkTopics();
                }
            }
        }
        return instance;
    }

    public AvroTopic<MeasurementKey, ActiGraphLinkAcceleration> getAccelerationTopic() {
        return accelerationTopic;
    }
}
