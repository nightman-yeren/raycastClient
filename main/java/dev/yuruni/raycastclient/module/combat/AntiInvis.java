package dev.yuruni.raycastclient.module.combat;

import com.lukflug.panelstudio.base.IBoolean;
import dev.yuruni.raycastclient.module.Module;

public class AntiInvis extends Module {

    public AntiInvis() {
        super("Anti Invis", "antiinvis", "Makes invisible players still render", () -> true, true);
    }
}
