package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.PostMotionListener;

import java.util.ArrayList;
import java.util.List;

public class PostMotionEvent extends AbstractEvent {

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            PostMotionListener postMotionListener = (PostMotionListener) listener;
            postMotionListener.OnPostMotion(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PostMotionListener> GetListenerClassType() {
        return PostMotionListener.class;
    }

}