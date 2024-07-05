package dev.yuruni.raycastclient.module.combat;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.SendPacketEvent;
import dev.yuruni.raycastclient.event.listener.SendPacketListener;
import dev.yuruni.raycastclient.mixin.interfaces.IPlayerInteractEntityC2SPacket;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module implements SendPacketListener {

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }

    private static final BooleanSetting legit = new BooleanSetting("Legit", "legit", "Makes this 'legit'", () -> true, false);

    public Criticals() {
        super("Criticals", "criticals", "Make all attacks crits", () -> true, true);
        settings.add(legit);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(SendPacketListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(SendPacketListener.class, this);
    }

    @Override
    public void OnSendPacket(SendPacketEvent event) {
        Packet<?> packet = event.GetPacket();
        if(packet instanceof PlayerInteractEntityC2SPacket playerInteractPacket) {
            IPlayerInteractEntityC2SPacket packetAccessor = (IPlayerInteractEntityC2SPacket)playerInteractPacket;

            PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
            packetAccessor.invokeWrite(packetBuf);
            packetBuf.readVarInt();
            InteractType type = packetBuf.readEnumConstant(InteractType.class);

            if(type == InteractType.ATTACK) {
                ClientPlayerEntity player = mc.player;
                if (player != null) {
                    if (player.isOnGround() && !player.isInLava() && !player.isSubmergedInWater()) {
                        if (legit.getValue()) {
                            //TODO: Make check for in air and falling then attack
                            player.jump();
                        } else {
                            ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
                            assert networkHandler != null;
                            networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.03125D, mc.player.getZ(), false));
                            networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
                            networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                        }
                    }
                }
            }
        }
    }

}
