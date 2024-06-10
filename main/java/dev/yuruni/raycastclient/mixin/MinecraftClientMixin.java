package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.config.ConfigManager;
import dev.yuruni.raycastclient.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique
    int hudRenderCount = 0;
    @Unique
    boolean hudConfigLoaded = false;
    @Unique
    boolean doHudCount = false;

    @Inject(at = {@At(value="HEAD")}, method = {"close()V"})
    private void onClose(CallbackInfo ci) {
        try {
            RaycastClient.INSTANCE.onClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        if (doHudCount) {
            //Count Hud render
            if (MinecraftClient.getInstance().currentScreen == null) {
                if (hudRenderCount >= 40 && !hudConfigLoaded) { //2 seconds
                    ConfigManager.loadEnabledModulesConfig();
                    hudConfigLoaded = true;
                }
                if (!hudConfigLoaded) {
                    hudRenderCount += 1;
                }
            }
        }
        TickEvent updateEvent = new TickEvent();
        RaycastClient.INSTANCE.eventManager.Fire(updateEvent);
    }
}
