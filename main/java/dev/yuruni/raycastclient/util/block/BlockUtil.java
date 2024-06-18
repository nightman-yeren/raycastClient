package dev.yuruni.raycastclient.util.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentMinecraftForge;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static Stream<Box> getBlockCollisions(Box box)
    {
        assert MinecraftClient.getInstance().world != null;
        Iterable<VoxelShape> blockCollisions =
                MinecraftClient.getInstance().world.getBlockCollisions(MinecraftClient.getInstance().player, box);

        return StreamSupport.stream(blockCollisions.spliterator(), false)
                .flatMap(shape -> shape.getBoundingBoxes().stream())
                .filter(shapeBox -> shapeBox.intersects(box));
    }

}
