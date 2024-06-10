package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.TickEvent;

public interface TickListener extends AbstractListener {
    public abstract void OnUpdate(TickEvent event);
}