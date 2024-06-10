package dev.yuruni.raycastclient.module.hud;

import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.StringSetting;

public class SettingPrefixModule extends Module {
    private static final StringSetting prefix = new StringSetting("Settings Prefix", "prefix", "The prefix that goes before all settings", () -> true, "| ");
    public SettingPrefixModule() {
        super("Setting Prefix", "settingsprefix", "Customize the prefix that goes before all settings", () -> true, false);
        settings.add(prefix);
    }

    public static String getPrefix() {
        return prefix.getValue();
    }
}
