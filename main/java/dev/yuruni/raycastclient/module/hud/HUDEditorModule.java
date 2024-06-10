package dev.yuruni.raycastclient.module.hud;

import dev.yuruni.raycastclient.module.Module;
import org.lwjgl.glfw.GLFW;

import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.setting.KeybindSetting;

public class HUDEditorModule extends Module {
    public static final BooleanSetting showHUD=new BooleanSetting("Show HUD Panels","showHUD","Whether to show the HUD panels in the ClickGUI.",()->true,true);
    public static final KeybindSetting keybind=new KeybindSetting("Keybind","keybind","The key to toggle the module.",()->true,GLFW.GLFW_KEY_P);

    public HUDEditorModule() {
        super("HUDEditor", "hudeditor","Module containing HUDEditor settings.",()->true,false);
        settings.add(showHUD);
        settings.add(keybind);
    }
}