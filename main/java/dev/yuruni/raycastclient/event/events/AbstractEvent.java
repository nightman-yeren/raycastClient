package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;

import java.util.ArrayList;

public abstract class AbstractEvent {
    boolean isCancelled;

    public AbstractEvent() {
        isCancelled = false;
    }

    public boolean IsCancelled() {
        return isCancelled;
    }

    public void SetCancelled(boolean state) {
        this.isCancelled = state;
    }

    public abstract void Fire(ArrayList<? extends AbstractListener> listeners);
    public abstract <T extends AbstractListener> Class<T> GetListenerClassType();
}