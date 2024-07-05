package dev.yuruni.raycastclient.util.math;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.util.entity.player.RotationFaker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec3d getEyesPos()
    {
        ClientPlayerEntity player = mc.player;
        assert player != null;
        float eyeHeight = player.getEyeHeight(player.getPose());
        return player.getPos().add(0, eyeHeight, 0);
    }

    public static Vec3d getServerLookVec()
    {
        RotationFaker rf = RaycastClient.INSTANCE.rotationFaker;
        return new Rotation(rf.getServerYaw(), rf.getServerPitch()).toLookVec();
    }

    public static Rotation getNeededRotations(Vec3d vec)
    {
        Vec3d eyes = getEyesPos();

        double diffX = vec.x - eyes.x;
        double diffZ = vec.z - eyes.z;
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;

        double diffY = vec.y - eyes.y;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return Rotation.wrapped((float)yaw, (float)pitch);
    }

    public static double getAngleToLookVec(Vec3d vec)
    {
        ClientPlayerEntity player = mc.player;
        assert player != null;
        Rotation current = new Rotation(player.getYaw(), player.getPitch());
        Rotation needed = getNeededRotations(vec);
        return current.getAngleTo(needed);
    }

    public static float getHorizontalAngleToLookVec(Vec3d vec)
    {
        assert mc.player != null;
        float currentYaw = MathHelper.wrapDegrees(mc.player.getYaw());
        float neededYaw = getNeededRotations(vec).yaw();
        return MathHelper.wrapDegrees(currentYaw - neededYaw);
    }

    public static float limitAngleChange(float current, float intended,
                                         float maxChange)
    {
        float currentWrapped = MathHelper.wrapDegrees(current);
        float intendedWrapped = MathHelper.wrapDegrees(intended);

        float change = MathHelper.wrapDegrees(intendedWrapped - currentWrapped);
        change = MathHelper.clamp(change, -maxChange, maxChange);

        return current + change;
    }

    /**
     * Removes unnecessary changes in angle caused by wrapping. Useful for
     * making combat hacks harder to detect.
     *
     * <p>
     * For example, if the current angle is 179 degrees and the intended angle
     * is -179 degrees, you only need to turn 2 degrees to face the intended
     * angle, not 358 degrees.
     *
     * <p>
     * DO NOT wrap the current angle before calling this method! You will get
     * incorrect results if you do.
     */
    public static float limitAngleChange(float current, float intended)
    {
        float currentWrapped = MathHelper.wrapDegrees(current);
        float intendedWrapped = MathHelper.wrapDegrees(intended);

        float change = MathHelper.wrapDegrees(intendedWrapped - currentWrapped);

        return current + change;
    }

    public static boolean isAlreadyFacing(Rotation rotation)
    {
        return getAngleToLastReportedLookVec(rotation) <= 1.0;
    }

    public static double getAngleToLastReportedLookVec(Vec3d vec)
    {
        Rotation needed = getNeededRotations(vec);
        return getAngleToLastReportedLookVec(needed);
    }

    public static double getAngleToLastReportedLookVec(Rotation rotation)
    {
        ClientPlayerEntity player = mc.player;
        assert player != null;
        Rotation lastReported = new Rotation(player.prevYaw, player.prevPitch);
        return lastReported.getAngleTo(rotation);
    }

    /**
     * Returns true if the player is facing anywhere within the given box
     * and is no further away than the given range.
     */
    public static boolean isFacingBox(Box box, double range)
    {
        Vec3d start = getEyesPos();
        assert mc.player != null;
        Vec3d end = start.add(mc.player.getRotationVec(0).normalize().multiply(range));
        return box.raycast(start, end).isPresent();
    }

    /**
     * Returns the next rotation that the player should be facing in order to
     * slowly turn towards the specified end rotation, at a rate of roughly
     * <code>maxChange</code> degrees per tick.
     */
    public static Rotation slowlyTurnTowards(Rotation end, float maxChange)
    {
        assert mc.player != null;
        float startYaw = mc.player.prevYaw;
        float startPitch = mc.player.prevPitch;
        float endYaw = end.yaw();
        float endPitch = end.pitch();

        float yawChange = Math.abs(MathHelper.wrapDegrees(endYaw - startYaw));
        float pitchChange =
                Math.abs(MathHelper.wrapDegrees(endPitch - startPitch));

        float maxChangeYaw = pitchChange == 0 ? maxChange
                : Math.min(maxChange, maxChange * yawChange / pitchChange);
        float maxChangePitch = yawChange == 0 ? maxChange
                : Math.min(maxChange, maxChange * pitchChange / yawChange);

        float nextYaw = limitAngleChange(startYaw, endYaw, maxChangeYaw);
        float nextPitch =
                limitAngleChange(startPitch, endPitch, maxChangePitch);

        return new Rotation(nextYaw, nextPitch);
    }
}
