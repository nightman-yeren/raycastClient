package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.module.ModuleManager;
import dev.yuruni.raycastclient.util.render.RenderManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(at = { @At("RETURN") }, method = { "render" })
    private void onRenderWorld(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {

        // TODO: Per Module rendering.
        //RenderEvent event = new RenderEvent(matrixStack, tickDelta);
        //Client.getInstance().eventManager.Fire(event);

        RenderManager.render(matrices);
    }

    @Inject(at = @At("HEAD"), method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", cancellable = true)
    private void onHasBlindnessOrDarknessEffect(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getModule("nooverlay").isenabled())
            cir.setReturnValue(false);
    }
}
