package dev.yuruni.raycastclient;

import dev.yuruni.raycastclient.config.ConfigManager;
import dev.yuruni.raycastclient.event.EventManager;
import dev.yuruni.raycastclient.event.listener.PostMotionListener;
import dev.yuruni.raycastclient.event.listener.PreMotionListener;
import dev.yuruni.raycastclient.gui.ClickGUI;
import dev.yuruni.raycastclient.module.*;
import dev.yuruni.raycastclient.module.hud.*;
import dev.yuruni.raycastclient.util.entity.player.RotationFaker;
import dev.yuruni.raycastclient.util.render.RenderUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.TimerTask;

public class RaycastClient implements ModInitializer {

    public static final String MODNAME = "RaycastClient";
    public static final String MODID = "raycastclient";
    public static final String MODVERSION = "v1.0.0";

    public static RaycastClient INSTANCE;

    public RaycastClient() {
        INSTANCE = this;
    }

    public static ClickGUI gui;
    public static boolean inited = false, renderObjects = true, hudInited = false;
    public boolean guiOpened = false;
    private final boolean[] keys = new boolean[266];

    //Managers
    public EventManager eventManager;
    public RotationFaker rotationFaker;
    //Utilities
    public static RenderUtil globalRenderer;
    //Global variables
    public static DrawContext globalContext;

    @Override
    public void onInitialize() {
        ModuleManager.init();
        System.out.println("Modules Initialized");
        eventManager = new EventManager();
        globalRenderer = new RenderUtil();

        rotationFaker = new RotationFaker();
        eventManager.AddListener(PreMotionListener.class, rotationFaker);
        eventManager.AddListener(PostMotionListener.class, rotationFaker);
        System.out.println("Managers Assigned");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!inited) {
                for (int i = 32; i < keys.length; i++)
                    keys[i] = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), i) == GLFW.GLFW_PRESS;
                HudRenderCallback.EVENT.register((cli, tickDelta) -> gui.render());
                gui = new ClickGUI();
                System.out.println("First step initialization complete");
                inited = true;
            }
            for (int i=32;i<keys.length;i++) {
                if (keys[i]!=(GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(),i)==GLFW.GLFW_PRESS)) {
                    if (i != GLFW.GLFW_KEY_ESCAPE) {
                        /*
                        keys[i] = !keys[i];
                        if (keys[i]) {
                            if (i == HUDEditorModule.keybind.getKey()) gui.enterHUDEditor();
                            gui.handleKeyEvent(i);
                        }
                        ALL MOVED TO KEYBOARD MIXIN
                         */
                    } else {
                        guiOpened = false;
                    }
                }
            }
            /*
            //Special for right shift - MOVED TO KEYBOARD MIXIN
            if (guiKey[GLFW.GLFW_KEY_RIGHT_SHIFT - 1] == (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)==GLFW.GLFW_PRESS)) {
                guiKey[GLFW.GLFW_KEY_RIGHT_SHIFT - 1] =! guiKey[GLFW.GLFW_KEY_RIGHT_SHIFT - 1];
                if (guiKey[GLFW.GLFW_KEY_RIGHT_SHIFT - 1]) {
                    if (GLFW.GLFW_KEY_RIGHT_SHIFT == ClickGUIModule.keybind.getKey()) {
                        if (!guiOpened) {
                            try {
                                ConfigManager.loadClickGUIPositions(); //TODO: fix
                            } catch (IOException ignore) {
                            }
                            gui.enterGUI();
                            guiOpened = true;
                        } else {
                            try {
                                ConfigManager.saveClickGUIPositions(); //TODO: fix
                            } catch (IOException ignore) {
                            }
                            gui.exitGUI();
                            guiOpened = false;
                        }
                    }
                }
            }
             */
        });
        new java.util.Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        ConfigManager.loadAllExceptEnabledModules();
                        ConfigManager.loadEnabledModulesConfig();
                        System.out.println("Loaded config");
                        cancel();
                    }
                },
                5000
        );
        System.out.println("Raycast Client Initialized");
    }

    public void onClose() {
        ConfigManager.saveAll();
        System.out.println("Saved all!");
    }
}
