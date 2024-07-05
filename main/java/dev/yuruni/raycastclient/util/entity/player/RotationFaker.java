package dev.yuruni.raycastclient.util.entity.player;

import dev.yuruni.raycastclient.event.events.PostMotionEvent;
import dev.yuruni.raycastclient.event.events.PreMotionEvent;
import dev.yuruni.raycastclient.event.listener.PostMotionListener;
import dev.yuruni.raycastclient.event.listener.PreMotionListener;
import dev.yuruni.raycastclient.util.math.Rotation;
import dev.yuruni.raycastclient.util.math.RotationUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class RotationFaker
        implements PreMotionListener, PostMotionListener
{
    private static ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
    private boolean fakeRotation;
    private float serverYaw;
    private float serverPitch;
    private float realYaw;
    private float realPitch;

    @Override
    public void OnPreMotion(PreMotionEvent event)
    {
        if(!fakeRotation)
            return;

        ClientPlayerEntity player = clientPlayer;
        if (player == null) return;
        realYaw = player.getYaw();
        realPitch = player.getPitch();
        player.setYaw(serverYaw);
        player.setPitch(serverPitch);
    }

    @Override
    public void OnPostMotion(PostMotionEvent event)
    {
        if(!fakeRotation)
            return;

        ClientPlayerEntity player = clientPlayer;
        if (player == null) return;
        player.setYaw(realYaw);
        player.setPitch(realPitch);
        fakeRotation = false;
    }

    public void faceVectorPacket(Vec3d vec)
    {
        Rotation needed = RotationUtil.getNeededRotations(vec);
        ClientPlayerEntity player = clientPlayer;
        if (player == null) return;

        fakeRotation = true;
        serverYaw =
                RotationUtil.limitAngleChange(player.getYaw(), needed.yaw());
        serverPitch = needed.pitch();
    }

    public void faceVectorClient(Vec3d vec)
    {
        Rotation needed = RotationUtil.getNeededRotations(vec);

        ClientPlayerEntity player = clientPlayer;
        if (player == null) return;
        player.setYaw(
                RotationUtil.limitAngleChange(player.getYaw(), needed.yaw()));
        player.setPitch(needed.pitch());
    }

    public void faceVectorClientIgnorePitch(Vec3d vec)
    {
        Rotation needed = RotationUtil.getNeededRotations(vec);

        ClientPlayerEntity player = clientPlayer;
        if (player == null) return;
        player.setYaw(
                RotationUtil.limitAngleChange(player.getYaw(), needed.yaw()));
        player.setPitch(0);
    }

    public float getServerYaw()
    {
        if (fakeRotation) {
            return serverYaw;
        } else {
            clientPlayer = MinecraftClient.getInstance().player;
            assert clientPlayer != null;
            return clientPlayer.getYaw();
        }
    }

    public float getServerPitch()
    {
        if (fakeRotation) {
            return serverPitch;
        } else {
            clientPlayer = MinecraftClient.getInstance().player;
            assert clientPlayer != null;
            return clientPlayer.getYaw();
        }
    }
}