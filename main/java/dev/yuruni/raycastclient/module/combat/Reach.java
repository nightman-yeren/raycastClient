package dev.yuruni.raycastclient.module.combat;

import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.DoubleSetting;

public class Reach extends Module {

    private static final DoubleSetting distance = new DoubleSetting("Distance", "distance", "Reach Distance", () -> true, 1, 15, 5);

    public Reach() {
        super("Reach", "reach", "Makes you turn into Luffy", () -> true, true);
        settings.add(distance);
    } //TODO: Wait until a solution is posted for Reach then make this module

    public float getReach() {
        return distance.getValue().floatValue();
    }

    public void setReachLength(double reach) {
        distance.setValue(reach);
    }
}
