package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.PreMotionEvent;

public interface PreMotionListener extends AbstractListener {
    public abstract void OnPreMotion(PreMotionEvent event);
}
