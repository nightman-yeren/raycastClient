package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.PostMotionEvent;

public interface PostMotionListener extends AbstractListener {
    public abstract void OnPostMotion(PostMotionEvent event);
}
