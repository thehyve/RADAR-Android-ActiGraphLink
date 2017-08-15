package org.radarcns.android.actigraphlink;

import android.os.Parcel;
import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceStateCreator;

public class ActiGraphLinkDeviceState extends BaseDeviceState {
    public static final Creator<ActiGraphLinkDeviceState> CREATOR = new DeviceStateCreator<>(ActiGraphLinkDeviceState.class);
    private float[] acceleration = {0, 0, 0};

    @Override
    public boolean hasAcceleration() {
        return true;
    }


    @Override
    public float[] getAcceleration() {
        return acceleration;
    }

    @Override
    public synchronized void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.acceleration[0]);
        dest.writeFloat(this.acceleration[1]);
        dest.writeFloat(this.acceleration[2]);
    }

    public void updateFromParcel(Parcel in) {
        super.updateFromParcel(in);
        acceleration[0] = in.readFloat();
        acceleration[1] = in.readFloat();
        acceleration[2] = in.readFloat();
    }

    public void setAcceleration(float x, float y, float z) {
        acceleration = new float[] {x, y, z};
    }
}
