package dev.yuruni.raycastclient.event.events;

import dev.yuruni.raycastclient.event.listener.AbstractListener;
import dev.yuruni.raycastclient.event.listener.WorldRenderListener;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class WorldRenderEvent extends AbstractEvent {

    MatrixStack matrixStack;
    float partialTicks;

    public MatrixStack GetMatrixStack() {
        return matrixStack;
    }
    public float GetPartialTicks() {
        return partialTicks;
    }

    public WorldRenderEvent(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        for(AbstractListener listener : listeners) {
            WorldRenderListener worldRenderListener = (WorldRenderListener) listener;
            worldRenderListener.OnRender(this);
        }
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<WorldRenderListener> GetListenerClassType() {
        return WorldRenderListener.class;
    }

}
