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
