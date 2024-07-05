package dev.yuruni.raycastclient.util.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntityUtil {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Stream<Entity> getAttackableEntities()
    {
        assert mc.world != null;
        return StreamSupport.stream(mc.world.getEntities().spliterator(), true)
                .filter(IS_ATTACKABLE);
    }

    public static final Predicate<Entity> IS_ATTACKABLE = e -> e != null
            && !e.isRemoved()
            && (e instanceof LivingEntity && ((LivingEntity)e).getHealth() > 0
            || e instanceof EndCrystalEntity
            || e instanceof ShulkerBulletEntity)
            && e != mc.player; //&& if entity is not fake player(freecam)
            //&& if entity is not friend (make a optional option with this)

    public static Vec3d getLerpedPos(Entity e, float partialTicks)
    {
        // When an entity is removed, it stops moving and its lastRenderX/Y/Z
        // values are no longer updated.
        if(e.isRemoved())
            return e.getPos();

        double x = MathHelper.lerp(partialTicks, e.lastRenderX, e.getX());
        double y = MathHelper.lerp(partialTicks, e.lastRenderY, e.getY());
        double z = MathHelper.lerp(partialTicks, e.lastRenderZ, e.getZ());
        return new Vec3d(x, y, z);
    }
}
