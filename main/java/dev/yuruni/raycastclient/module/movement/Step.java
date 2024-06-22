package dev.yuruni.raycastclient.module.movement;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.TickEvent;
import dev.yuruni.raycastclient.event.listener.TickListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.EnumSetting;
import dev.yuruni.raycastclient.setting.IntegerSetting;
import dev.yuruni.raycastclient.util.block.BlockUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

public class Step extends Module implements TickListener {
    private static final EnumSetting<StepMode> mode = new EnumSetting<>("Mode", "mode", "How to execute step", () -> true, StepMode.Inject, StepMode.class);
    private static final IntegerSetting height = new IntegerSetting("Height", "height", "doesn't apply for legit mode", () -> true, 1, 10, 1);

    private enum StepMode {
        Inject, Attribute, Legit
    }

    public Step() {
        super("Step", "step", "Changes how many block you can step", () -> true, true);
        settings.add(mode);
        settings.add(height);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(TickListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
        if (mode.getValue() != StepMode.Attribute) return;
        assert mc.player != null;
        if (mc.player.getStepHeight() != .5f) {
            mc.player.setStepHeight(.5f);
        }
    }

    @Override
    public void OnUpdate(TickEvent event) {
        //Attributes
        if (mc.player != null) {
            if (mode.getValue() == StepMode.Attribute) {
                mc.player.setStepHeight(height.getValue().floatValue());
            } else {
                if (mc.player.getStepHeight() != .5f) {
                    mc.player.setStepHeight(.5f);
                }
            }
        }
        if (mode.getValue() == StepMode.Legit) {
            //Try to bypass anticheat
            ClientPlayerEntity player = mc.player;
            if (player != null && mc.world != null) {
                if (!player.horizontalCollision) return;

                if (!player.isOnGround() || player.isClimbing() || player.isTouchingWater() || player.isInLava()) {
                    return;
                }

                if (player.input.movementForward == 0 && player.input.movementSideways == 0) return;

                if (player.input.jumping) return;

                Box box = player.getBoundingBox().offset(0, 0.05, 0).expand(0.05);
                if (!mc.world.isSpaceEmpty(player, box.offset(0, 1, 0)))
                    return;

                double stepHeight = BlockUtil.getBlockCollisions(box)
                        .mapToDouble(bb -> bb.maxY).max().orElse(Double.NEGATIVE_INFINITY);

                stepHeight -= player.getY();

                if (stepHeight < 0 || stepHeight > 1) {
                    return;
                }

                ClientPlayNetworkHandler networkHandler = player.networkHandler;

                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        player.getX(), player.getY() + 0.42 * stepHeight, player.getZ(),
                        player.isOnGround()
                ));

                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        player.getX(), player.getY() + 0.753 * stepHeight, player.getZ(),
                        player.isOnGround()
                ));

                player.setPosition(player.getX(), player.getY() + stepHeight, player.getZ());
            }
        }
    }

    public float adjustStepHeight(float stepHeight) {
        if (isenabled() && mode.getValue() == StepMode.Inject) {
            return height.getValue().floatValue();
        }
        return stepHeight;
    }

    public boolean isAutoJumpAllowed() {
        return !isenabled();
    }
}
