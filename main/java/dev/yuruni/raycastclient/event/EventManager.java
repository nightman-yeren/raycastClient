package dev.yuruni.raycastclient.event;

import dev.yuruni.raycastclient.event.events.AbstractEvent;
import dev.yuruni.raycastclient.event.listener.AbstractListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class EventManager {

    private final HashMap<Class<AbstractListener>, ArrayList<AbstractListener>> listeners;

    public EventManager() {
        listeners = new HashMap<Class<AbstractListener>, ArrayList<AbstractListener>>();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractListener> void AddListener(Class<T> object, AbstractListener listener) {
        try {
            ArrayList<AbstractListener> listOfListeners = listeners.get(object);
            if (listOfListeners == null) {
                listOfListeners = new ArrayList<>(Collections.singletonList(listener));
                listeners.put((Class<AbstractListener>) object, listOfListeners);
            } else {
                listOfListeners.add(listener);
            }
        } catch (Exception e) {
            System.out.println("Issue adding listener: " + object.getTypeName() + "...");
            e.printStackTrace();
        }
    }

    public <T extends AbstractListener> void RemoveListener(Class<T> object, AbstractListener listener) {
        try {
            ArrayList<AbstractListener> listOfListeners = listeners.get(object);
            if (listOfListeners != null) {
                listOfListeners.remove(listener);
            }
        } catch (Exception e) {
            System.out.println("Issue removing listener: " + object.getTypeName() + "...");
            e.printStackTrace();
        }
    }

    public void Fire(AbstractEvent event) {
        ArrayList<? extends AbstractListener> listOfListeners = listeners.get(event.GetListenerClassType());

        if (listOfListeners == null) {
            return;
        }

        event.Fire(listOfListeners);
    }
}