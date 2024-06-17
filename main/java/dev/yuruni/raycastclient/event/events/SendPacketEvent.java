package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.SendPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class SendPacketEvent extends AbstractEvent {

    private Packet<?> packet;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> GetPacket(){
        return packet;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for(AbstractListener listener : List.copyOf(listeners)) {
            SendPacketListener sendPacketListener = (SendPacketListener) listener;
            sendPacketListener.OnSendPacket(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<SendPacketListener> GetListenerClassType() {
        return SendPacketListener.class;
    }
}
