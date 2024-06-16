package dev.yuruni.raycastclient.module.hud;

import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.component.IFixedComponent;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.module.Category;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.setting.ColorSetting;
import dev.yuruni.raycastclient.util.color.GSColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ArrayListModule extends Module implements RenderListener {

    private static ArrayListModule instance;

    public static ArrayList<Module> activeModules = new ArrayList<Module>();

    private static final BooleanSetting sortUp = new BooleanSetting("Sort Up", "sortup", "Sorting", () -> true, true);
    private static final BooleanSetting sortRight = new BooleanSetting("Sort Right", "sortright", "Sorting", () -> true, false);
    private static final ColorSetting color = new ColorSetting("color", "color", "The color", () -> true, true, true, new Color(255, 255 ,255), false);

    public ArrayListModule() {
        super("ArrayList Hud", "arraylisthud", "The side hud for enabled modules", () -> true, true);
        instance = this;
        settings.add(sortUp);
        settings.add(sortRight);
        settings.add(color);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(RenderListener.class, this);
    }

    @Override
    public void OnRender(RenderEvent event) {
        activeModules.clear();
        for (Category category : Category.values()) {
            for (Module module : category.modules) {
                if (module.isenabled()) {
                    activeModules.add(module);
                }
            }
            activeModules.sort(Comparator.comparing(module -> -mc.textRenderer.getWidth(module.getDisplayName())));
        }
    }

    public static IFixedComponent getComponent() {

        return new ListComponent(() -> "ArrayList Hud", new Point(300, 40), "arraylisthud", new HUDList() {
            @Override
            public int getSize() {
                return activeModules.size();
            }

            @Override
            public String getItem(int index) {
                Module module = activeModules.get(index);
                //return (!module.getHudInfo().equals("")) ? module.getName() + ChatFormatting.GRAY + " " + module.getHudInfo() : module.getName();
                return module.getDisplayName();
            }

            @Override
            public Color getItemColor(int index) {
                GSColor c = new GSColor(color.getColor().getRGB());
                return Color.getHSBColor(c.getHue() + (color.getRainbow() ? .02f * index : 0), c.getSaturation(), c.getBrightness());
            }

            @Override
            public boolean sortUp() {
                return sortUp.isOn();
            }

            @Override
            public boolean sortRight() {
                return sortRight.isOn();
            }
        }, 9, 2);
    }

    public static IToggleable getToggle() {
        return instance.isEnabled();
    }
}
