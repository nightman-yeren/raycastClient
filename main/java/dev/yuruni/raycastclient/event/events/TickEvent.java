package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.TickListener;

import java.util.ArrayList;
import java.util.List;

public class TickEvent extends AbstractEvent {
    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            TickListener tickListener = (TickListener) listener;
            tickListener.OnUpdate(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<TickListener> GetListenerClassType() {
        return TickListener.class;
    }
}