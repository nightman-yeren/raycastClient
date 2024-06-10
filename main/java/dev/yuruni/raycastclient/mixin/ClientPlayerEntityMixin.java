package dev.yuruni.raycastclient.mixin;

import com.mojang.authlib.GameProfile;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.AirStrafingSpeedEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected float getOffGroundSpeed() {
        AirStrafingSpeedEvent event = new AirStrafingSpeedEvent(super.getOffGroundSpeed());
        RaycastClient.INSTANCE.eventManager.Fire(event);
        return event.getSpeed();
    }
}
