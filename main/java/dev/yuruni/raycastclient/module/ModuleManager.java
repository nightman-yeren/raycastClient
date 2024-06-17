package dev.yuruni.raycastclient.module;

import dev.yuruni.raycastclient.module.hud.*;
import dev.yuruni.raycastclient.module.movement.*;
import dev.yuruni.raycastclient.module.render.*;
import dev.yuruni.raycastclient.module.combat.*;

public class ModuleManager {

    public static void init() {
        Category.init();
        Category.HUD.modules.add(new ClickGUIModule());
        Category.HUD.modules.add(new HUDEditorModule());
        Category.HUD.modules.add(new TargetHUD());
        Category.HUD.modules.add(new ArrayListModule());
        Category.HUD.modules.add(new TabGUIModule());
        Category.HUD.modules.add(new WatermarkModule());
        Category.HUD.modules.add(new LogoModule());
        Category.HUD.modules.add(new SettingPrefixModule());
        Category.COMBAT.modules.add(new AntiInvis());
        Category.COMBAT.modules.add(new Criticals());
        Category.COMBAT.modules.add(new Nametags());
        Category.COMBAT.modules.add(new NoOverlay());
        Category.MOVEMENT.modules.add(new Flight());
        Category.RENDER.modules.add(new Fullbright());
        Category.RENDER.modules.add(new EntityESP());
        Category.RENDER.modules.add(new Tracers());
        //Category.RENDER.modules.add(new Trajectories());
        Category.RENDER.modules.add(new Breadcrumbs());
    }

    public static Module getModule(String configName) {
        if (configName == null) return null;
        if (configName.isBlank()) return null;
        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                if (module.configName.equals(configName)) {
                    return module;
                }
            }
        }
        return null;
    }

    public static Module getModule(Module module) {
        return getModule(module.getConfigName());
    }

}
