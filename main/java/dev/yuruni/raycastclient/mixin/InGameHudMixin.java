package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Unique
    private int amountOfRenderEventsBeforeClosing = 15;
    @Unique
    private int currentPassedRenderEvents = 0;
    @Unique
    private boolean fullyLoadedHud = false;

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        RaycastClient.globalContext = context;
        //Load Hud Modules
        if (!RaycastClient.hudInited && context != null && !fullyLoadedHud) {
            RaycastClient.gui.enterGUI();
            ConfigManager.loadHud();
            //TabGUIModule.instance.setEnabled(true);
            RaycastClient.hudInited = true;
        }
        if (RaycastClient.hudInited && context != null && !fullyLoadedHud) {
            if (currentPassedRenderEvents >= amountOfRenderEventsBeforeClosing) {
                RaycastClient.gui.exitGUI();
                fullyLoadedHud = true;
            }
            currentPassedRenderEvents += 1;
        }
    }
}
