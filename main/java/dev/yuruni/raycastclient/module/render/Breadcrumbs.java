package dev.yuruni.raycastclient.module.render;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.events.TickEvent;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.event.listener.TickListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.util.Color;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Breadcrumbs extends Module implements RenderListener, TickListener {

    //TODO: Colors and rainbow

    private static final BooleanSetting clearPositions = new BooleanSetting("Clear", "clearpositions", "Clears the current breadcrumb positions", () -> true, false);

    private static final BooleanSetting recordPositionInBackground = new BooleanSetting("Background", "recordinbackground", "Records player position even when module is disabled",
            () -> true, true);

    private static final BooleanSetting comment = new BooleanSetting("WARNING!", "comment", "ENABLE BREADCRUMBS ONCE BEFORE TURNING ON BACKGROUND. If this can not be toggled on, " +
            "you are good to go.", () -> true,
            false);

    private float timer = 10;
    private float currentTick = 0;
    private boolean listenerAdded = false;
    private List<Vec3d> positions = new ArrayList<Vec3d>();

    public Breadcrumbs() {
        super("Breadcrumbs", "breadcrumbs", "Shows where you stepped", () -> true, true);
        settings.add(recordPositionInBackground);
        settings.add(clearPositions);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(RenderListener.class, this);
        if (!listenerAdded) {
            RaycastClient.INSTANCE.eventManager.AddListener(TickListener.class, this);
            listenerAdded = true;
        }
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(RenderListener.class, this);
        if (!recordPositionInBackground.isOn()) {
            RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
            listenerAdded = false;
        }
    }

    @Override
    public void OnRender(RenderEvent event) {
        for(int i = 0; i < this.positions.size() - 1; i++) {
            getRenderer().drawLine3D(event.GetMatrixStack().peek().getPositionMatrix(), this.positions.get(i), this.positions.get(i + 1), new Color(255, 0, 255));
        }
    }

    @Override
    public void OnUpdate(TickEvent event) {
        if (mc.player != null) {
            currentTick++;
            if (timer == currentTick) {
                currentTick = 0;
                //if not in free cam {
                positions.add(mc.player.getPos());
                //}
            }
            //Make boolean option act like a button
            if (clearPositions.isOn()) {
                clearPositions.setValue(false);
                //Clear breadcrumbs
                positions = new ArrayList<Vec3d>();
            }
            if (comment.isOn()) {
                comment.setValue(false);
            }
        }
        if (!recordPositionInBackground.isOn()) {
            if (!this.isenabled()) {
                listenerAdded = false;
                RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
            }
        }
    }
}
