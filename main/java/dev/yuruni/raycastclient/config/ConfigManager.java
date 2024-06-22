package dev.yuruni.raycastclient.config;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.module.Category;
import dev.yuruni.raycastclient.module.Module;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Properties;

import com.google.gson.*;
import dev.yuruni.raycastclient.setting.*;

public class ConfigManager {

    public static final String fileName = "RayCast/";
    public static final String mainName = "main/";
    public static final String moduleName = "modules/";
    public static final String miscName = "misc/";
    public static Properties config;

    public static void saveAll() {
        try {
            makeConfigFolders(); //Make sure first
            saveClickGUIPositions();
            saveModules();
            saveEnabledModules();
            saveModuleKeybinds();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllExceptEnabledModules() {
        try {
            makeConfigFolders(); //Make sure first
            loadModules();
            loadModuleKeybinds();
            loadClickGUIPositions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadEnabledModulesConfig() {
        try {
            loadEnabledModules();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerFiles(String location, String name) throws IOException {
        if (Files.exists(Paths.get(fileName + location + name + ".xml"))) {
            File file = new File(fileName + location + name + ".xml");
            file.delete();
        }
        Path path = Paths.get(fileName + location + name + ".xml");
        Files.createFile(path);
    }

    public static void loadModules() throws IOException {
        String moduleLocation = fileName + moduleName;

        for (Category category : Category.values())
            for (Module module : category.modules) {
                try {
                    loadModuleDirect(moduleLocation, module);
                } catch (IOException e) {
                    System.out.println(module.getConfigName());
                    e.printStackTrace();
                }
            }
    }

    private static void loadModuleDirect(String moduleLocation, Module module) throws IOException {

        config = new Properties();
        config.loadFromXML(new FileInputStream(moduleLocation + module.getConfigName() + ".xml"));

        for (Setting setting : module.settings) {
            String value = config.getProperty(module.getConfigName() + "-" + setting.getConfigName());
            try {
                if (value != null) {
                    if (setting instanceof BooleanSetting) {
                        ((BooleanSetting) setting).setValue(Boolean.parseBoolean(value));
                    } else if (setting instanceof IntegerSetting) {
                        ((IntegerSetting) setting).setValue(Integer.parseInt(value));
                    } else if (setting instanceof DoubleSetting) {
                        ((DoubleSetting) setting).setValue(Double.parseDouble(value));
                    } else if (setting instanceof ColorSetting) {
                        ((ColorSetting) setting).fromInteger(Integer.parseInt(value));
                    /*
                    } else if (setting instanceof ModeSetting) {
                        setting.setValue(dataObject.getAsString());

                     */
                    } else if (setting instanceof StringSetting) {
                        ((StringSetting) setting).setValue(value);
                    } else if (setting instanceof EnumSetting) {
                        ((EnumSetting<?>) setting).setValueIndex(Integer.parseInt(value));
                    }
                } else {
                    System.out.println("Config is somehow null");
                }
            } catch (java.lang.NumberFormatException e) {
                System.out.println(setting.getConfigName() + " " + module.getConfigName());
                System.out.println(value);
            }
        }
    }

    private static void loadEnabledModules() throws IOException {

        config = new Properties();
        config.loadFromXML(new FileInputStream(fileName + mainName + "toggle" + ".xml"));
        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                boolean value = Boolean.parseBoolean(config.getProperty(module.getConfigName()));
                if (value) {
                    try {
                        module.enable();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void loadModuleKeybinds() throws IOException {
        String bindLocation = fileName + mainName;

        config = new Properties();

        config.loadFromXML(new FileInputStream(bindLocation + "bind.xml"));
        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                String value = config.getProperty(module.getConfigName());

                if (value != null) {
                    boolean found = false;
                    KeybindSetting targetSetting = null;
                    for (Setting setting : module.settings) {
                        if (setting instanceof KeybindSetting) {
                            found = true;
                            targetSetting = (KeybindSetting) setting;
                        }
                    }
                    if (found) {
                        targetSetting.setKey(Integer.parseInt(value));
                    }
                }
            }
        }
    }

    public static void loadClickGUIPositions() throws IOException {
        registerFiles(mainName, "ClickGUI");
        RaycastClient.gui.getGUI().loadConfig(new GUIConfig(fileName + mainName));
    }

    private static void makeConfigFolders() throws IOException {
        if (!Files.exists(Paths.get(fileName))) {
            Files.createDirectories(Paths.get(fileName));
        }
        if (!Files.exists(Paths.get(fileName + moduleName))) {
            Files.createDirectories(Paths.get(fileName + moduleName));
        }
        if (!Files.exists(Paths.get(fileName + mainName))) {
            Files.createDirectories(Paths.get(fileName + mainName));
        }
        if (!Files.exists(Paths.get(fileName + miscName))) {
            Files.createDirectories(Paths.get(fileName + miscName));
        }
        config = new Properties();
    }

    public static void saveModules() throws IOException {
        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                try {
                    saveModuleDirect(module);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void saveModuleDirect(Module module) throws IOException {
        config = new Properties();
        registerFiles(moduleName, module.getConfigName());

        for (Setting setting : module.settings) {
            String propertyName = module.getConfigName() + "-" + setting.getConfigName();
            if (setting instanceof BooleanSetting) {
                Boolean settingValue = ((BooleanSetting) setting).getValue();
                config.setProperty(propertyName, String.valueOf(settingValue));
            } else if (setting instanceof IntegerSetting) {
                int settingValue = ((IntegerSetting) setting).getValue();
                config.setProperty(propertyName, String.valueOf(settingValue));
            } else if (setting instanceof DoubleSetting) {
                double settingValue = ((DoubleSetting) setting).getValue();
                config.setProperty(propertyName, String.valueOf(settingValue));
            } else if (setting instanceof ColorSetting) {
                int settingValue = ((ColorSetting) setting).toInteger();
                config.setProperty(propertyName, String.valueOf(settingValue));
            /*
            } else if (setting instanceof ModeSetting) {
                settingObject.add(setting.getConfigName(), new JsonPrimitive(((ModeSetting) setting).getValue()));
            }
             */
            } else if (setting instanceof StringSetting) {
                String settingValue = ((StringSetting) setting).getValue();
                config.setProperty(propertyName, settingValue);
            } else if (setting instanceof EnumSetting) {
                String settingValue = String.valueOf(((EnumSetting<?>) setting).getValueIndex());
                config.setProperty(propertyName, settingValue);
            }
        }
        config.storeToXML(new FileOutputStream(fileName + moduleName + module.getConfigName() + ".xml"), null);
    }

    public static void saveClickGUIPositions() throws IOException {
        registerFiles(mainName, "ClickGUI");
        RaycastClient.INSTANCE.gui.getGUI().saveConfig(new GUIConfig(fileName + mainName));
    }

    private static void saveEnabledModules() throws IOException {
        config = new Properties();
        registerFiles(mainName, "toggle");

        for (Category category : Category.values()) {
            if (Category.valueOf("HUD") != category) {
                for (Module module : category.modules) {
                    config.setProperty(module.getConfigName(), String.valueOf(module.isenabled()));
                }
            }
        }
        config.storeToXML(new FileOutputStream(fileName + mainName + "toggle" + ".xml"), null);
    }

    private static void saveModuleKeybinds() throws IOException {
        config = new Properties();
        registerFiles(mainName, "bind");

        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                boolean found = false;
                KeybindSetting targetSetting = null;
                for (Setting setting : module.settings) {
                    if (setting instanceof KeybindSetting) {
                        found = true;
                        targetSetting = (KeybindSetting) setting;
                    }
                }
                if (found) {
                    config.setProperty(module.getConfigName(), String.valueOf(targetSetting.getKey()));
                }
            }
        }
        config.storeToXML(new FileOutputStream(fileName + mainName + "bind.xml"), null);
    }
}
