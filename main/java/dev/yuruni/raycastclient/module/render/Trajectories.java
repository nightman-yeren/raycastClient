package dev.yuruni.raycastclient.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.WorldRenderEvent;
import dev.yuruni.raycastclient.event.listener.WorldRenderListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.ColorSetting;
import dev.yuruni.raycastclient.util.block.BlockUtil;
import dev.yuruni.raycastclient.util.entity.EntityUtil;
import dev.yuruni.raycastclient.util.math.RotationUtil;
import dev.yuruni.raycastclient.util.render.AlternativeRenderUtil;
import dev.yuruni.raycastclient.util.render.RenderUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Predicate;

public class Trajectories extends Module implements WorldRenderListener {

    private static final ColorSetting missColor = new ColorSetting("Miss Color", "misscolor", "Color when hitting nothing", () -> true, false, false, new Color(89, 89, 89), false);
    private static final ColorSetting entityHitColor = new ColorSetting("EntityHit Color", "entityhitcolor", "Color when hitting entity", () -> true, false, false, new Color(176, 60, 255), false);
    private static final ColorSetting blockHitColor = new ColorSetting("BlockHit Color", "blockhitcolor", "Color when hitting block", () -> true, false, false, new Color(0, 229, 255), false);

    public Trajectories() {
        super("Trajectories", "trajectories", "Displays trajectory", () -> true, true);
        settings.add(missColor);
        settings.add(entityHitColor);
        settings.add(blockHitColor);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(WorldRenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(WorldRenderListener.class, this);
    }

    @Override
    public void OnRender(WorldRenderEvent event)
    {
        //AlternativeRenderUtil.applyRenderOffset(event.GetMatrixStack());
        MatrixStack matrixStack = event.GetMatrixStack();

        matrixStack.push();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        Trajectory trajectory = getTrajectory(event.GetPartialTicks());
        ArrayList<Vec3d> path = trajectory.path;

        ColorSetting color = switch(trajectory.type)
        {
            case MISS -> missColor;
            case ENTITY -> entityHitColor;
            case BLOCK -> blockHitColor;
        };

        drawLine(matrixStack, path, color);

        if(!path.isEmpty())
        {
            Vec3d end = path.get(path.size() - 1);
            drawEndOfLine(matrixStack, end, color);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        matrixStack.pop();
    }

    private void drawLine(MatrixStack matrixStack, ArrayList<Vec3d> path,
                          ColorSetting color)
    {
        Vec3d camPos = RenderUtil.getCameraPos();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP,
                VertexFormats.POSITION);
        float red = color.getColor().getRed() / 255F;
        float green = color.getColor().getGreen() / 255F;
        float blue = color.getColor().getBlue() / 255F;
        float[] colorF = new float[]{red, green, blue};
        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.75F);

        for(Vec3d point : path) {
            bufferBuilder
                    .vertex(matrix, (float) (point.x - camPos.x),
                            (float) (point.y - camPos.y), (float) (point.z - camPos.z))
                    .next();
        }

        tessellator.draw();
    }

    private void drawEndOfLine(MatrixStack matrixStack, Vec3d end,
                               ColorSetting color)
    {
        Vec3d camPos = RenderUtil.getCameraPos();
        double renderX = end.x - camPos.x;
        double renderY = end.y - camPos.y;
        double renderZ = end.z - camPos.z;
        float red = color.getColor().getRed() / 255F;
        float green = color.getColor().getGreen() / 255F;
        float blue = color.getColor().getBlue() / 255F;
        float[] colorF = new float[]{red, green, blue};

        matrixStack.push();
        matrixStack.translate(renderX - 0.5, renderY - 0.5, renderZ - 0.5);

        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.25F);
        AlternativeRenderUtil.drawSolidBox(matrixStack);

        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.75F);
        AlternativeRenderUtil.drawOutlinedBox(matrixStack);

        matrixStack.pop();
    }

    private record Trajectory(ArrayList<Vec3d> path, HitResult.Type type)
    {}

    private Trajectory getTrajectory(float partialTicks)
    {
        ClientPlayerEntity player = mc.player;
        ArrayList<Vec3d> path = new ArrayList<>();
        HitResult.Type type = HitResult.Type.MISS;

        // find the hand with a throwable item
        Hand hand = Hand.MAIN_HAND;
        ItemStack stack = player.getMainHandStack();
        if(!isThrowable(stack))
        {
            hand = Hand.OFF_HAND;
            stack = player.getOffHandStack();

            // if neither hand has a throwable item, return empty path
            if(!isThrowable(stack))
                return new Trajectory(path, type);
        }

        // calculate item-specific values
        Item item = stack.getItem();
        double throwPower = getThrowPower(item);
        double gravity = getProjectileGravity(item);
        FluidHandling fluidHandling = getFluidHandling(item);

        // prepare yaw and pitch
        double yaw = Math.toRadians(player.getYaw());
        double pitch = Math.toRadians(player.getPitch());

        // calculate starting position
        Vec3d arrowPos = EntityUtil.getLerpedPos(player, partialTicks)
                .add(getHandOffset(hand, yaw));

        // calculate starting motion
        Vec3d arrowMotion = getStartingMotion(yaw, pitch, throwPower);

        // build the path
        for(int i = 0; i < 1000; i++)
        {
            // add to path
            path.add(arrowPos);

            // apply motion
            arrowPos = arrowPos.add(arrowMotion.multiply(0.1));

            // apply air friction
            arrowMotion = arrowMotion.multiply(0.999);

            // apply gravity
            arrowMotion = arrowMotion.add(0, -gravity * 0.1, 0);

            Vec3d lastPos = path.size() > 1 ? path.get(path.size() - 2)
                    : RotationUtil.getEyesPos();

            // check for block collision
            BlockHitResult bResult =
                    BlockUtil.raycast(lastPos, arrowPos, fluidHandling);
            if(bResult.getType() != HitResult.Type.MISS)
            {
                // replace last pos with the collision point
                type = HitResult.Type.BLOCK;
                path.set(path.size() - 1, bResult.getPos());
                break;
            }

            // check for entity collision
            Box box = new Box(lastPos, arrowPos);
            Predicate<Entity> predicate = e -> !e.isSpectator() && e.canHit();
            double maxDistSq = 64 * 64;
            EntityHitResult eResult = ProjectileUtil.raycast(mc.player, lastPos,
                    arrowPos, box, predicate, maxDistSq);
            if(eResult != null && eResult.getType() != HitResult.Type.MISS)
            {
                // replace last pos with the collision point
                type = HitResult.Type.ENTITY;
                path.set(path.size() - 1, eResult.getPos());
                break;
            }
        }

        return new Trajectory(path, type);
    }

    private Vec3d getHandOffset(Hand hand, double yaw)
    {
        Arm mainArm = mc.options.getMainArm().getValue();

        boolean rightSide = mainArm == Arm.RIGHT && hand == Hand.MAIN_HAND
                || mainArm == Arm.LEFT && hand == Hand.OFF_HAND;

        double sideMultiplier = rightSide ? -1 : 1;
        double handOffsetX = Math.cos(yaw) * 0.16 * sideMultiplier;
        double handOffsetY = mc.player.getStandingEyeHeight() - 0.1;
        double handOffsetZ = Math.sin(yaw) * 0.16 * sideMultiplier;

        return new Vec3d(handOffsetX, handOffsetY, handOffsetZ);
    }

    private Vec3d getStartingMotion(double yaw, double pitch, double throwPower)
    {
        double cosOfPitch = Math.cos(pitch);

        double arrowMotionX = -Math.sin(yaw) * cosOfPitch;
        double arrowMotionY = -Math.sin(pitch);
        double arrowMotionZ = Math.cos(yaw) * cosOfPitch;

        return new Vec3d(arrowMotionX, arrowMotionY, arrowMotionZ).normalize()
                .multiply(throwPower);
    }

    private double getThrowPower(Item item)
    {
        // use a static 1.5x for snowballs and such
        if(!(item instanceof RangedWeaponItem))
            return 1.5;

        // calculate bow power
        float bowPower = (72000 - mc.player.getItemUseTimeLeft()) / 20F;
        bowPower = bowPower * bowPower + bowPower * 2F;

        // clamp value if fully charged or not charged at all
        if(bowPower > 3 || bowPower <= 0.3F)
            bowPower = 3;

        return bowPower;
    }

    private double getProjectileGravity(Item item)
    {
        if(item instanceof RangedWeaponItem)
            return 0.05;

        if(item instanceof ThrowablePotionItem)
            return 0.4;

        if(item instanceof FishingRodItem)
            return 0.15;

        if(item instanceof TridentItem)
            return 0.015;

        return 0.03;
    }

    private FluidHandling getFluidHandling(Item item)
    {
        if(item instanceof FishingRodItem)
            return FluidHandling.ANY;

        return FluidHandling.NONE;
    }

    public static boolean isThrowable(ItemStack stack)
    {
        if(stack.isEmpty())
            return false;

        Item item = stack.getItem();
        return item instanceof RangedWeaponItem || item instanceof SnowballItem
                || item instanceof EggItem || item instanceof EnderPearlItem
                || item instanceof ThrowablePotionItem
                || item instanceof FishingRodItem || item instanceof TridentItem;
    }
}
