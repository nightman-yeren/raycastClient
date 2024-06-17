package dev.yuruni.raycastclient.event.listener;

import dev.yuruni.raycastclient.event.events.SendPacketEvent;

public interface SendPacketListener extends AbstractListener {
    public abstract void OnSendPacket(SendPacketEvent event);
}