package dev.yuruni.raycastclient.module.combat;

import com.lukflug.panelstudio.base.IBoolean;
import dev.yuruni.raycastclient.module.Module;

public class NoOverlay extends Module {

    public NoOverlay() {
        super("No Overlay", "nooverlay", "Prevents potion effects and overlays from rendering", () -> true, true);
    }
}
