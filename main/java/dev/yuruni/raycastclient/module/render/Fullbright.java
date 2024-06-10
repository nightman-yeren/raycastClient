package dev.yuruni.raycastclient.module.render;

import dev.yuruni.raycastclient.event.callback.GammaCallback;
import dev.yuruni.raycastclient.module.Module;

public class Fullbright extends Module {

    public Fullbright() {
        super("Fullbright", "fullbright", "Fixes your eyes", () -> true, true);
    }

    @Override
    protected void onEnable() {
        GammaCallback.EVENT.register(gamma -> {
            gamma = 15f;
            return gamma;
        });
    }

    @Override
    protected void onDisable() {
        GammaCallback.EVENT.register(gamma -> {
            gamma = 0f;
            return gamma;
        });
    }
}
