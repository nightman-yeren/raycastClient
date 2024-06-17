package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.event.callback.GammaCallback;
import dev.yuruni.raycastclient.module.ModuleManager;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float fullbright$fireGammaEvent(Double d) {
        return GammaCallback.EVENT.invoker().onGammaChange(d.floatValue());
    }

    @Inject(at = { @At("HEAD") }, method = { "getDarknessFactor(F)F" }, cancellable = true)
    private void onGetDarknessFactor(float delta, CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.getModule("nooverlay").isenabled())
            cir.setReturnValue(0F);
    }
}