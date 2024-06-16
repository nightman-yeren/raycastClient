package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.KeyDownEvent;

public interface KeyDownListener extends AbstractListener {
    public abstract void OnKeyDown(KeyDownEvent event);
}