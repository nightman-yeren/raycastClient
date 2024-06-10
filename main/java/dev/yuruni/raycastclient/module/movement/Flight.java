package dev.yuruni.raycastclient.module.movement;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.AirStrafingSpeedEvent;
import dev.yuruni.raycastclient.event.events.TickEvent;
import dev.yuruni.raycastclient.event.listener.AirStrafingSpeedListener;
import dev.yuruni.raycastclient.event.listener.TickListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.setting.DoubleSetting;
import dev.yuruni.raycastclient.setting.IntegerSetting;
import dev.yuruni.raycastclient.setting.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class Flight extends Module implements TickListener, AirStrafingSpeedListener {

    private static final DoubleSetting horizontalSpeed = new DoubleSetting("Horizontal Speed", "horizontalspeed", "Horizontal Speed of flight",
            () -> true, 0.05, 10, 1);

    private static final DoubleSetting verticalSpeed = new DoubleSetting("Vertical Speed", "verticalspeed", "The vertical speed of flight",
            () -> true, 0.05, 10, 1);

    private static final BooleanSetting slowSneaking = new BooleanSetting("Slow Sneaking", "slowsneaking", "Reduces horizontal speed when sneaking to prevent glitches",
            () -> true, true);

    private static final BooleanSetting fasterStrafing = new BooleanSetting("Faster Strafing", "fasterstrafing", "WARNING: this might get detected more easily",
            () -> true, false);

    private static final BooleanSetting antiKick = new BooleanSetting("Anti Kick", "antikick", "Fall a little bit under a certain interval to avoid been kicked",
            () -> true, false);

    private static final IntegerSetting antiKickInterval = new IntegerSetting("Anti Kick Interval", "antikickinterval", "Interval of anti kick, with ticks",
            () -> true, 5, 80, 30);

    private static final DoubleSetting antiKickDistance = new DoubleSetting("Anti Kick Distance", "antikickdistance", "Distance of anti kick falling",
            () -> true, 0.01, 2, 0.07);

    private static final KeybindSetting keybind = new KeybindSetting("Keybind", "keybind", "The key to toggle this module", () -> true,
            GLFW.GLFW_KEY_G);

    public Flight() {
        super("Fly", "fly", "allows you to fly like in creative. No fall suggested", () -> true, true);
        settings.add(horizontalSpeed);
        settings.add(verticalSpeed);
        settings.add(slowSneaking);
        settings.add(fasterStrafing);
        settings.add(antiKick);
        settings.add(antiKickInterval);
        settings.add(antiKickDistance);
        settings.add(keybind);
    }

    private int tickCounter = 0;

    @Override
    public void OnUpdate(TickEvent event) {
        ClientPlayerEntity player = mc.player;

        if (player != null) {
            player.getAbilities().flying = false;

            player.setVelocity(0, 0, 0);
            Vec3d velocity = player.getVelocity();
            if (!fasterStrafing.isOn()) {

                if (mc.options.jumpKey.isPressed()) {
                    player.setVelocity(velocity.x, verticalSpeed.getValue(), velocity.z);
                }

                if (mc.options.sneakKey.isPressed()) {
                    player.setVelocity(velocity.x, -verticalSpeed.getValue(), velocity.z);
                }
            } else {
                double motionX = 0;
                double motionY = 0;
                double motionZ = 0;
                if (mc.options.jumpKey.isPressed()) {
                    motionY += verticalSpeed.getValue() * 0.4f;
                }
                if (mc.options.sneakKey.isPressed()) {
                    motionY += -verticalSpeed.getValue() * 0.4f;
                }
                float playerYaw = player.getYaw();
                if (mc.options.leftKey.isPressed()) {
                    motionX += ((horizontalSpeed.getValue() * 0.4f) * (Math.cos(playerYaw * 3.14 / 180)));
                    motionZ += ((horizontalSpeed.getValue() * 0.4f) * (Math.sin(playerYaw * 3.14 / 180)));
                }
                if (mc.options.rightKey.isPressed()) {
                    motionX += -((horizontalSpeed.getValue() * 0.4f) * (Math.cos(playerYaw * 3.14 / 180)));
                    motionZ += -((horizontalSpeed.getValue() * 0.4f) * (Math.sin(playerYaw * 3.14 / 180)));
                }
                if (mc.options.forwardKey.isPressed()) {
                    motionX += -((horizontalSpeed.getValue() * 0.4f) * (Math.cos((playerYaw - 90) * 3.14 / 180)));
                    motionZ += -((horizontalSpeed.getValue() * 0.4f) * (Math.sin((playerYaw - 90) * 3.14 / 180)));
                }
                if (mc.options.backKey.isPressed()) {
                    motionX += ((horizontalSpeed.getValue() * 0.4f) * (Math.cos((playerYaw - 90) * 3.14 / 180)));
                    motionZ += ((horizontalSpeed.getValue() * 0.4f) * (Math.sin((playerYaw - 90) * 3.14 / 180)));
                }
                Vec3d vec = new Vec3d(motionX, motionY, motionZ);
                player.setVelocity(vec);
                velocity = vec;
            }

            if (antiKick.isOn()) {
                doAntiKick(velocity);
            }
        }
    }


    private void doAntiKick(Vec3d velocity) {
        if (tickCounter > antiKickInterval.getValue() + 1) {
            tickCounter = 0;
        }

        switch (tickCounter) {
            case 0 -> {
                if (mc.options.sneakKey.isPressed()) {
                    tickCounter = 2;
                } else {
                    assert mc.player != null;
                    mc.player.setVelocity(velocity.x, -antiKickDistance.getValue(), velocity.z);
                }
            }
            case 1 -> {
                Objects.requireNonNull(mc.player).setVelocity(velocity.x, antiKickDistance.getValue(), velocity.z);
            }
        }

        tickCounter++;
    }

    @Override
    public void onGetAirStrafingSpeed(AirStrafingSpeedEvent event) {
        float speed = horizontalSpeed.getValue().floatValue();
        if (mc.options.sneakKey.isPressed() && slowSneaking.isOn()) {
            speed = Math.min(speed, 0.85F);
        }
        event.setSpeed(speed);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;

        RaycastClient.INSTANCE.eventManager.AddListener(TickListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
    }
}
