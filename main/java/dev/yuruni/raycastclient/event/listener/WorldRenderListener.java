package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.WorldRenderEvent;

public interface WorldRenderListener extends AbstractListener {
    public abstract void OnRender(WorldRenderEvent event);
}
