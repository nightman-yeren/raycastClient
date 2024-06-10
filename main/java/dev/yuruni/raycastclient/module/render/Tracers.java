package dev.yuruni.raycastclient.module.render;

import com.lukflug.panelstudio.base.IBoolean;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.util.Color;
import dev.yuruni.raycastclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module implements RenderListener {

    private static final BooleanSetting renderHostile = new BooleanSetting("Hostile", "hostile", "Do render hostile entity.", () -> true,
            true);

    private static final BooleanSetting renderAnimal = new BooleanSetting("Passive", "passive", "Do render passive entity.", () -> true,
            true);

    private static final BooleanSetting renderPlayer = new BooleanSetting("Player", "player", "Do render players.", () -> true,
            true);

    private static final BooleanSetting renderOther = new BooleanSetting("Other", "other", "Do render other entites.", () -> true,
            true);

    //TODO: Colors

    public Tracers() {
        super("Tracers", "tracers", "makes a line point at esp", () -> true, true);
        settings.add(renderHostile);
        settings.add(renderAnimal);
        settings.add(renderPlayer);
        settings.add(renderOther);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(RenderListener.class, this);
    }

    @Override
    public void OnRender(RenderEvent event) {
        Vec3d eyePosition = new Vec3d(0, 0, 1);
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d offset = getRenderer().getEntityPositionOffsetInterpolated(mc.cameraEntity, event.GetPartialTicks());
        eyePosition = eyePosition.rotateX((float) -Math.toRadians(camera.getPitch()));
        eyePosition = eyePosition.rotateY((float) -Math.toRadians(camera.getYaw()));
        eyePosition = eyePosition.add(mc.cameraEntity.getEyePos());
        eyePosition = eyePosition.subtract(offset);
        assert mc.world != null;
        for (Entity entity : mc.world.getEntities()) {
            if(entity instanceof LivingEntity && (entity != mc.player)) {
                Vec3d interpolated = getRenderer().getEntityPositionInterpolated(entity, MinecraftClient.getInstance().getTickDelta());
                if (entity instanceof AnimalEntity && renderAnimal.isOn()) {
                    getRenderer().drawLine3D(event.GetMatrixStack().peek().getPositionMatrix(), eyePosition, interpolated, new Color(0, 255, 0));
                } else if (entity instanceof Monster && renderHostile.isOn()) {
                    getRenderer().drawLine3D(event.GetMatrixStack().peek().getPositionMatrix(), eyePosition, interpolated, new Color(255, 0, 0));
                } else if (renderOther.isOn() && !(entity instanceof AnimalEntity) && !(entity instanceof Monster)){
                    getRenderer().drawLine3D(event.GetMatrixStack().peek().getPositionMatrix(), eyePosition, interpolated, new Color(0, 0, 255));
                }
            }
        }

        for(AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (renderPlayer.isOn() && player != mc.player) {
                Vec3d interpolated = getRenderer().getEntityPositionInterpolated(player, MinecraftClient.getInstance().getTickDelta());
                getRenderer().drawLine3D(event.GetMatrixStack().peek().getPositionMatrix(), eyePosition, interpolated, new Color(255, 255, 255));
            }
        }
    }
}
