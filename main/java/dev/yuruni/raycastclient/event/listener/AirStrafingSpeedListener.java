package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.AirStrafingSpeedEvent;

public interface AirStrafingSpeedListener {
    public abstract void onGetAirStrafingSpeed(AirStrafingSpeedEvent event);
}
