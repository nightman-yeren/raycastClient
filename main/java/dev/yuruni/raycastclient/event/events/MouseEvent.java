package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.MouseListener;

import java.util.ArrayList;
import java.util.List;

public class MouseEvent extends AbstractEvent {

    private double deltaX;
    private double deltaY;
    private final double defaultDeltaX;
    private final double defaultDeltaY;

    public MouseEvent(double deltaX, double deltaY)
    {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        defaultDeltaX = deltaX;
        defaultDeltaY = deltaY;
    }

    public double getDeltaX()
    {
        return deltaX;
    }

    public void setDeltaX(double deltaX)
    {
        this.deltaX = deltaX;
    }

    public double getDeltaY()
    {
        return deltaY;
    }

    public void setDeltaY(double deltaY)
    {
        this.deltaY = deltaY;
    }

    public double getDefaultDeltaX()
    {
        return defaultDeltaX;
    }

    public double getDefaultDeltaY()
    {
        return defaultDeltaY;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            MouseListener mouseListener = (MouseListener) listener;
            mouseListener.OnMouseUpdate(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<MouseListener> GetListenerClassType() {
        return MouseListener.class;
    }
}
