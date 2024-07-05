package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.MouseEvent;

public interface MouseListener extends AbstractListener {
    public abstract void OnMouseUpdate(MouseEvent event);
}
