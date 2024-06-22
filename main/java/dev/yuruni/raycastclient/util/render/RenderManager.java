package dev.yuruni.raycastclient.util.render;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.events.WorldRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderManager {

    public static void render(MatrixStack matrixStack) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);



        matrixStack.push();
        RenderUtil.applyRenderOffset(matrixStack);

        RenderEvent renderEvent = new RenderEvent(matrixStack, MinecraftClient.getInstance().getTickDelta());
        RaycastClient.INSTANCE.eventManager.Fire(renderEvent);

        matrixStack.pop();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void renderAlt(MatrixStack matrixStack) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);



        matrixStack.push();
        //Render offset is already done or not needed for some reason

        WorldRenderEvent worldRenderEvent = new WorldRenderEvent(matrixStack, MinecraftClient.getInstance().getTickDelta());
        RaycastClient.INSTANCE.eventManager.Fire(worldRenderEvent);

        matrixStack.pop();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

}
