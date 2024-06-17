package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.SendPacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", cancellable = true)
    private void onSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci)
    {
        SendPacketEvent event = new SendPacketEvent(packet);
        RaycastClient.INSTANCE.eventManager.Fire(event);

        if(event.IsCancelled()) {
            ci.cancel();
        }
    }

}
