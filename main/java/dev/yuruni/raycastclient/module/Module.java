package dev.yuruni.raycastclient.module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.setting.Setting;
import com.lukflug.panelstudio.base.IBoolean;
import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.setting.IModule;
import com.lukflug.panelstudio.setting.ISetting;
import dev.yuruni.raycastclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;

public class Module implements IModule, IToggleable {
    public String displayName;
    public final String configName;
    public final String description;
    public final IBoolean visible;
    public final List<Setting<?>> settings=new ArrayList<Setting<?>>();
    public final boolean toggleable;
    private boolean enabled=false;
    private final RenderUtil renderer = new RenderUtil();
    public final MinecraftClient mc = MinecraftClient.getInstance();

    public Module (String displayName, String configName, String description, IBoolean visible, boolean toggleable) {
        this.displayName=displayName;
        this.configName=configName;
        this.description=description;
        this.visible=visible;
        this.toggleable=toggleable;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getConfigName() {
        return configName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public RenderUtil getRenderer() {
        return renderer;
    }

    @Override
    public IBoolean isVisible() {
        return visible;
    }

    @Override
    public IToggleable isEnabled() {
        if (!toggleable) return null;
        return new IToggleable() {
            @Override
            public boolean isOn() {
                return enabled;
            }

            @Override
            public void toggle() {
                if (isenabled()) {
                    disable();
                } else if (!isenabled()) {
                    enable();
                }
            }
        };
    }

    public boolean isenabled() {
        return enabled;
    }

    @Override
    public Stream<ISetting<?>> getSettings() {
        return settings.stream().filter(setting->setting instanceof ISetting).sorted((a,b)->a.displayName.compareTo(b.displayName)).map(setting->(ISetting<?>)setting);
    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    public void onUpdate() {

    }

    public void onRender() {

    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public void enable() {
        setEnabled(true);
        onEnable();
    }

    public void disable() {
        setEnabled(false);
        onDisable();
    }

    @Override
    public boolean isOn() {
        return enabled;
    }

    @Override
    public void toggle() {
        if (isenabled()) {
            disable();
        } else if (!isenabled()) {
            enable();
        }
    }
}