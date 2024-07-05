package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.AirStrafingSpeedListener;
import dev.yuruni.raycastclient.event.listener.TickListener;

import java.util.ArrayList;
import java.util.List;

public class AirStrafingSpeedEvent extends AbstractEvent {

    private float airStrafingSpeed;
    private final float defaultSpeed;

    public AirStrafingSpeedEvent(float airStrafingSpeed) {
        this.airStrafingSpeed = airStrafingSpeed;
        defaultSpeed = airStrafingSpeed;
    }

    public float getSpeed() {
        return airStrafingSpeed;
    }

    public void setSpeed(float airStrafingSpeed)
    {
        this.airStrafingSpeed = airStrafingSpeed;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            AirStrafingSpeedListener airStrafingSpeedListener = (AirStrafingSpeedListener) listener;
            airStrafingSpeedListener.onGetAirStrafingSpeed(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<AirStrafingSpeedListener> GetListenerClassType() {
        return AirStrafingSpeedListener.class;
    }
}
