package dev.yuruni.raycastclient.module.movement;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.TickEvent;
import dev.yuruni.raycastclient.event.listener.TickListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module implements TickListener {

    private static final EnumSetting<NoFallMode> mode = new EnumSetting<>("Mode","mode", "MLG doesn't work yet", () -> true, NoFallMode.Packet, NoFallMode.class);

    private enum NoFallMode {
        Packet, MLG
    }

    public NoFall() {
        super("No Fall", "nofall", "Removes fall damage", () -> true, true);
        settings.add(mode);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(TickListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void OnUpdate(TickEvent event) {
        if (mc.player != null) {
            if (mc.player.fallDistance > 2f) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
        }
    }
}
