package dev.yuruni.raycastclient.mixin;

import com.mojang.authlib.GameProfile;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.AirStrafingSpeedEvent;
import dev.yuruni.raycastclient.event.events.PostMotionEvent;
import dev.yuruni.raycastclient.event.events.PreMotionEvent;
import dev.yuruni.raycastclient.module.ModuleManager;
import dev.yuruni.raycastclient.module.movement.Step;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Override
    public float getStepHeight() {
        return ((Step) ModuleManager.getModule("step")).adjustStepHeight(super.getStepHeight());
    }

    @Inject(at = @At("HEAD"),
            method = "isAutoJumpEnabled()Z",
            cancellable = true)
    private void onIsAutoJumpEnabled(CallbackInfoReturnable<Boolean> cir)
    {
        if(!((Step) ModuleManager.getModule("step")).isAutoJumpAllowed())
            cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method="sendMovementPackets()V")
    private void onSendMovementPacketsHEAD(CallbackInfo ci) {
        PreMotionEvent event = new PreMotionEvent();
        RaycastClient.INSTANCE.eventManager.Fire(event);
    }

    @Inject(at = @At("TAIL"), method="sendMovementPackets()V")
    private void onSendMovementPacketsTAIL(CallbackInfo ci)
    {
        PostMotionEvent event = new PostMotionEvent();
        RaycastClient.INSTANCE.eventManager.Fire(event);
    }
}
