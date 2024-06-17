package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.config.ConfigManager;
import dev.yuruni.raycastclient.event.events.KeyDownEvent;
import dev.yuruni.raycastclient.module.Category;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.module.hud.ClickGUIModule;
import dev.yuruni.raycastclient.module.hud.HUDEditorModule;
import dev.yuruni.raycastclient.setting.KeybindSetting;
import dev.yuruni.raycastclient.setting.Setting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Objects;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(at = {@At("HEAD")}, method = {"onKey(JIIII)V" }, cancellable = true)
    private void OnKeyDown(long window, int key, int scancode,
                           int action, int modifiers, CallbackInfo ci) {
        RaycastClient raycast = RaycastClient.INSTANCE;
        if(raycast != null && raycast.eventManager != null) {
            if(action == GLFW.GLFW_PRESS) {
                if (key == ClickGUIModule.keybind.getKey()) {
                    if (!RaycastClient.INSTANCE.guiOpened) {
                        try {
                            ConfigManager.loadClickGUIPositions(); //TODO: fix
                        } catch (IOException ignore) {
                        }
                        RaycastClient.gui.enterGUI();
                        RaycastClient.INSTANCE.guiOpened = true;
                    } else {
                        try {
                            ConfigManager.saveClickGUIPositions(); //TODO: fix
                        } catch (IOException ignore) {
                        }
                        RaycastClient.gui.exitGUI();
                        RaycastClient.INSTANCE.guiOpened = false;
                    }
                }
                if (MinecraftClient.getInstance().currentScreen == null) {
                    //Modules
                    if (key == HUDEditorModule.keybind.getKey()) RaycastClient.gui.enterHUDEditor();
                    for (Category category : Category.values()) {
                        for (Module module : category.modules) {
                            for (Setting setting : module.settings) {
                                if (setting instanceof KeybindSetting) {
                                    if (key == ((KeybindSetting) setting).getKey()) module.toggle();
                                }
                            }
                        }
                    }
                    RaycastClient.gui.handleKeyEvent(key);
                }
            }
            KeyDownEvent event = new KeyDownEvent(window, key, scancode, action, modifiers);
            raycast.eventManager.Fire(event);
            if(event.IsCancelled()) {
                ci.cancel();
            }
        }
    }
}