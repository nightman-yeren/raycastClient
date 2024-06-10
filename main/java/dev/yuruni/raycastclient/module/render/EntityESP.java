package dev.yuruni.raycastclient.module.render;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.util.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EntityESP extends Module implements RenderListener {

    private static final BooleanSetting renderHostile = new BooleanSetting("Hostile", "hostile", "Do render hostile entity.", () -> true,
            true);

    private static final BooleanSetting renderAnimal = new BooleanSetting("Passive", "passive", "Do render passive entity.", () -> true,
            true);

    private static final BooleanSetting renderOther = new BooleanSetting("Other", "other", "Do render other entites.", () -> true,
            true);

    //TODO: colors and alpha

    public EntityESP() {
        super("EntityESP", "entityesp", "You should know what this does", () -> true, true);
        settings.add(renderHostile);
        settings.add(renderAnimal);
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
        MatrixStack matrixStack = event.GetMatrixStack();
        float partialTicks = event.GetPartialTicks();

        matrixStack.push();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {

                Box boundingBox = entity.getBoundingBox();

                Vec3d entityVelocity = entity.getVelocity();
                Vec3d velocityPartial = new Vec3d(entityVelocity.x * partialTicks, 0, entityVelocity.z * partialTicks);

                boundingBox = boundingBox.offset(velocityPartial);

                if (entity instanceof AnimalEntity && renderAnimal.isOn()) {
                    getRenderer().drawTransparent3DBox(matrixStack, boundingBox, new Color(0, 255, 0), 0.6f);
                } else if (entity instanceof Monster && renderHostile.isOn()) {
                    getRenderer().drawTransparent3DBox(matrixStack, boundingBox, new Color(255, 0, 0), 0.6f);
                } else if (!(entity instanceof AnimalEntity) && !(entity instanceof Monster) && renderOther.isOn()){
                    getRenderer().drawTransparent3DBox(matrixStack, boundingBox, new Color(0, 0, 255), 0.6f);
                }
            }
        }
        matrixStack.pop();
    }
}
