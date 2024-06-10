package dev.yuruni.raycastclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.yuruni.raycastclient.util.RenderUtil;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void renderer_postWorldRender(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        //RenderProfiler.begin("World");

        RenderUtil.lastProjMat.set(RenderSystem.getProjectionMatrix());
        RenderUtil.lastModMat.set(RenderSystem.getModelViewMatrix());
        RenderUtil.lastWorldSpaceMatrix.set(matrices.peek().getPositionMatrix());
        //RenderEvents.WORLD.invoker().rendered(matrix);
        //Renderer3d.renderFadingBlocks(matrix);

        //RenderProfiler.pop();
    }
}
