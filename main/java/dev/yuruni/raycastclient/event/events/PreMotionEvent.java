package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.PreMotionListener;

import java.util.ArrayList;
import java.util.List;

public class PreMotionEvent extends AbstractEvent {

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            PreMotionListener preMotionListener = (PreMotionListener) listener;
            preMotionListener.OnPreMotion(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PreMotionListener> GetListenerClassType() {
        return PreMotionListener.class;
    }

}
