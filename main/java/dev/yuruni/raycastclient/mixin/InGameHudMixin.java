package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        RaycastClient.globalContext = context;
    }
}
