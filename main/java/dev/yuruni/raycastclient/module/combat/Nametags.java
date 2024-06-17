package dev.yuruni.raycastclient.module.combat;

import com.lukflug.panelstudio.base.IBoolean;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.setting.DoubleSetting;
import it.unimi.dsi.fastutil.doubles.DoubleSet;

public class Nametags extends Module {

    private static final DoubleSetting scale = new DoubleSetting("Scale", "scale", "Scale of NameTags", () -> true, 0, 5, 0);
    private static final BooleanSetting onlyPlayers = new BooleanSetting("Only Players", "onlyplayers", "Wheather nametags are only modified for players", () -> true, false);
    private static final BooleanSetting alwaysVisible = new BooleanSetting("Always Visible", "alwaysvisible", "Weather NameTags will always be rendered", () -> true, false);

    public Nametags() {
        super("Nametags", "nametags", "Modified Nametags", () -> true, true);
        settings.add(scale);
        //settings.add(onlyPlayers);
        //settings.add(alwaysVisible);
    }

    public double getNametagScale() {
        return scale.getValue();
    }

    public boolean isPlayersOnly() {
        return onlyPlayers.getValue();
    }

    public boolean isAlwaysVisible() {
        return alwaysVisible.getValue();
    }
}
