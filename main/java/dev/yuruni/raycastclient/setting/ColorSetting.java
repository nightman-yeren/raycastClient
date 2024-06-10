package dev.yuruni.raycastclient.setting;

import java.awt.Color;

import dev.yuruni.raycastclient.module.hud.ClickGUIModule;
import dev.yuruni.raycastclient.module.hud.ClickGUIModule.ColorModel;
import com.lukflug.panelstudio.base.IBoolean;
import com.lukflug.panelstudio.setting.IColorSetting;
import com.lukflug.panelstudio.theme.ITheme;
import dev.yuruni.raycastclient.util.GSColor;

public class ColorSetting extends Setting<Color> implements IColorSetting {
    public final boolean hasAlpha,allowsRainbow;
    private boolean rainbow;

    public ColorSetting (String displayName, String configName, String description, IBoolean visible, boolean hasAlpha, boolean allowsRainbow, Color value, boolean rainbow) {
        super(displayName,configName,description,visible,value);
        this.hasAlpha=hasAlpha;
        this.allowsRainbow=allowsRainbow;
        this.rainbow=rainbow;
    }

    @Override
    public Color getValue() {
        if (rainbow) {
            int speed=ClickGUIModule.rainbowSpeed.getValue();
            return ITheme.combineColors(Color.getHSBColor((System.currentTimeMillis()%(360*speed))/(float)(360*speed),1,1),super.getValue());
        }
        else return super.getValue();
    }

    @Override
    public Color getColor() {
        return super.getValue();
    }

    @Override
    public boolean getRainbow() {
        return rainbow;
    }

    @Override
    public void setRainbow (boolean rainbow) {
        this.rainbow=rainbow;
    }

    @Override
    public boolean hasAlpha() {
        return hasAlpha;
    }

    @Override
    public boolean allowsRainbow() {
        return allowsRainbow;
    }

    @Override
    public boolean hasHSBModel() {
        return ClickGUIModule.colorModel.getValue()==ColorModel.HSB;
    }

    public int toInteger() {
        return getValue().getRGB() & 0xFFFFFF + (this.rainbow ? 1 : 0) * 0x1000000;
    }

    public void fromInteger(int number) {
        this.rainbow = ((number & 0x1000000) != 0);

        super.setValue(this.rainbow ? GSColor.fromHSB((System.currentTimeMillis() % (360 * 32)) / (360f * 32), 1, 1) : new GSColor(number & 0xFFFFFF));
    }
}