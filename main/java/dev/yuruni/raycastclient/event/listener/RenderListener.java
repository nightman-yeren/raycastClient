package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.RenderEvent;

public interface RenderListener extends AbstractListener {
    public abstract void OnRender(RenderEvent event);
}