package dev.yuruni.raycastclient.util.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class BlockUtil {

    public static BlockHitResult raycast(Vec3d from, Vec3d to,
                                         RaycastContext.FluidHandling fluidHandling)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        RaycastContext context = new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, fluidHandling, mc.player);

        assert mc.world != null;
        return mc.world.raycast(context);
    }

    public static BlockHitResult raycast(Vec3d from, Vec3d to)
    {
        return raycast(from, to, RaycastContext.FluidHandling.NONE);
    }

}
