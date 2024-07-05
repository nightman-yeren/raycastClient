package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.AirStrafingSpeedEvent;

public interface AirStrafingSpeedListener extends AbstractListener {
    public abstract void onGetAirStrafingSpeed(AirStrafingSpeedEvent event);
}
