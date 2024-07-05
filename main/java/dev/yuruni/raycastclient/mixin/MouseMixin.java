package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.MouseEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(at = @At("HEAD"), method="updateMouse()V")
    private void onTick(CallbackInfo ci)
    {
        MouseEvent event = new MouseEvent(cursorDeltaX, cursorDeltaY);
        RaycastClient.INSTANCE.eventManager.Fire(event);
        cursorDeltaX = event.getDeltaX();
        cursorDeltaY = event.getDeltaY();
    }

}
