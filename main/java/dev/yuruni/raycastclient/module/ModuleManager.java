package dev.yuruni.raycastclient.module;

import dev.yuruni.raycastclient.module.hud.*;
import dev.yuruni.raycastclient.module.movement.*;
import dev.yuruni.raycastclient.module.render.*;

public class ModuleManager {

    public static void init() {
        Category.init();
        Category.HUD.modules.add(new ClickGUIModule());
        Category.HUD.modules.add(new HUDEditorModule());
        Category.HUD.modules.add(new ArrayListModule());
        Category.HUD.modules.add(new TabGUIModule());
        Category.HUD.modules.add(new WatermarkModule());
        Category.HUD.modules.add(new LogoModule());
        Category.HUD.modules.add(new SettingPrefixModule());
        Category.MOVEMENT.modules.add(new Flight());
        Category.RENDER.modules.add(new Fullbright());
        Category.RENDER.modules.add(new EntityESP());
        Category.RENDER.modules.add(new Tracers());
        Category.RENDER.modules.add(new Breadcrumbs());
    }

    /*
    public static void addMod(Module module) {
        modulesClassMap.put(module.getClass(), module);
        modulesNameMap.put(module.getDisplayName().toLowerCase(Locale.CANADA), module);
    }

    public static Collection<Module> getModules() {
        return modulesClassMap.values();
    }

    /*
    public static ArrayList<Module> getModulesInCategory(Category category) {
        ArrayList<Module> list = new ArrayList<>();

        for (Module module : modulesClassMap.values()) {
            if (!module.getCategory().equals(category)) continue;
            list.add(module);
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> clazz) {
        return (T) modulesClassMap.get(clazz);
    }

    public static Module getModule(String name) {
        if (name == null) return null;
        return modulesNameMap.get(name.toLowerCase(Locale.ROOT));
    }

    public static boolean isModuleEnabled(Class<? extends Module> clazz) {
        Module module = getModule(clazz);
        return module != null && module.isenabled();
    }

    public static boolean isModuleEnabled(String name) {
        Module module = getModule(name);
        return module != null && module.isenabled();
    }
    */

}
