package dev.yuruni.raycastclient.module.hud;

import com.lukflug.panelstudio.base.Context;
import com.lukflug.panelstudio.base.IInterface;
import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.component.IFixedComponent;
import com.lukflug.panelstudio.hud.HUDComponent;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.ColorSetting;
import dev.yuruni.raycastclient.setting.IntegerSetting;
import dev.yuruni.raycastclient.util.GSColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Interface;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class TargetHUD extends Module implements RenderListener {
    private static TargetHUD instance;

    private static final IntegerSetting range = new IntegerSetting("Range", "range", "Range of display", () -> true, 10, 260, 100);
    private static final ColorSetting background = new ColorSetting("Background", "background", "Background of box", () -> true, true, false, new Color(0, 0, 0, 255), false);
    private static final ColorSetting outline = new ColorSetting("Outline", "outline", "Outline of box", () -> true, true, false, new Color(0, 0, 0, 255), false);
    private static PlayerEntity targetPlayer;

    public TargetHUD() {
        super("Target HUD", "targethud", "Displays target info", () -> true, true);
        instance = this;
        settings.add(range);
        settings.add(background);
    }

    private static Color getNameColor(String playerName) {
        /*
        if (SocialManager.isFriend(playerName)) {
            return new GSColor(ModuleManager.getModule(ColorMain.class).getFriendGSColor(), 255);
        } else if (SocialManager.isEnemy(playerName)) {
            return new GSColor(ModuleManager.getModule(ColorMain.class).getEnemyGSColor(), 255);
        } else {
            return new GSColor(255, 255, 255, 255);
        }
         */
        //TODO: add friend system
        return new Color(255, 255, 255, 255);
    }

    private static Color getHealthColor(int health) {
        if (health > 36) {
            health = 36;
        }
        if (health < 0) {
            health = 0;
        }

        int red = (int) (255 - (health * 7.0833));
        int green = 255 - red;

        return new Color(red, green, 0, 255);
    }

    private static boolean isValidEntity(Entity e) {
        if (!(e instanceof PlayerEntity)) return false;
        else return e != mc.player;
    }

    public static float clamp(float val, float min, float max) {
        if (val <= min) {
            val = min;
        }
        if (val >= max) {
            val = max;
        }
        return val;
    }

    private static float getPing(PlayerEntity player) {
        float ping = 0;
        try {
            ping = clamp(Objects.requireNonNull(Objects.requireNonNull(mc.getNetworkHandler()).getPlayerListEntry(player.getUuid())).getLatency(), 1, 300.0f);
        } catch (NullPointerException ignored) {
        }
        return ping;
    }

    public static boolean isRenderingEntity(PlayerEntity entityPlayer) {
        return targetPlayer == entityPlayer;
    }

    public static IFixedComponent getComponent() {

        return new HUDComponent(() -> "Target HUD", new Point(0, 70), "targethud") {

            @Override
            public void render(Context context) {
                super.render(context);
                if (mc.world != null && Objects.requireNonNull(mc.player).age >= 10) {
                    ArrayList<PlayerEntity> availablePlayerEntities = new ArrayList<PlayerEntity>();
                    for (Entity entity : mc.world.getEntities()) {
                        if (isValidEntity(entity)) {
                            availablePlayerEntities.add((PlayerEntity) entity);
                        }
                    }
                    availablePlayerEntities.sort(Comparator.comparing(c -> mc.player.distanceTo(c)));
                    if (!availablePlayerEntities.isEmpty()) {
                        PlayerEntity playerEntity = availablePlayerEntities.get(0);
                        if (playerEntity.distanceTo(mc.player) <= range.getValue()) {
                            //background
                            Color backgroundColor = new GSColor(new GSColor(background.getValue()), 100);
                            context.getInterface().fillRect(context.getRect(), backgroundColor, backgroundColor, backgroundColor, backgroundColor);
                            //outline
                            Color color = outline.getValue();
                            context.getInterface().fillRect(new Rectangle(context.getPos(), new Dimension(context.getSize().width, 1)), color, color, color, color);
                            context.getInterface().fillRect(new Rectangle(context.getPos(), new Dimension(1, context.getSize().height)), color, color, color, color);
                            context.getInterface().fillRect(new Rectangle(new Point(context.getPos().x + context.getSize().width - 1, context.getPos().y), new Dimension(1, context.getSize().height)), color, color, color, color);
                            context.getInterface().fillRect(new Rectangle(new Point(context.getPos().x, context.getPos().y + context.getSize().height - 1), new Dimension(context.getSize().width, 1)), color, color, color, color);
                            //player - TODO: Fix player render
                            //renderEntity(playerEntity, ...)
                            //name
                            String playerName = playerEntity.getName().getString();
                            Color nameColor = getNameColor(playerName);
                            context.getInterface().drawString(new Point(context.getPos().x + 71, context.getPos().y + 11), mc.textRenderer.fontHeight, playerName, nameColor);
                            //health
                            int playerHealth = (int) (playerEntity.getHealth() + playerEntity.getAbsorptionAmount());
                            Color healthColor = getHealthColor(playerHealth);
                            context.getInterface().drawString(new Point(context.getPos().x + 71, context.getPos().y + 23), mc.textRenderer.fontHeight, "Health: ", healthColor);
                            //distance
                            context.getInterface().drawString(new Point(context.getPos().x + 71, context.getPos().y + 33), mc.textRenderer.fontHeight, "Distance: " + (int) playerEntity.distanceTo(mc.player), new Color(255, 255, 255));
                            //ping and info
                            String info;
                            if (playerEntity.getInventory().getArmorStack(2).getItem().equals(Items.ELYTRA)) {
                                info = "Bee";
                            } else if (playerEntity.getInventory().getArmorStack(2).getItem().equals(Items.DIAMOND_CHESTPLATE) || playerEntity.getInventory().getArmorStack(2).getItem().equals((Items.NETHERITE_CHESTPLATE))) {
                                info = "Stacked";
                            } else if (playerEntity.getInventory().getArmorStack(3).getItem().equals(Items.AIR)) {
                                info = "Noob";
                            } else {
                                info = "None";
                            }
                            context.getInterface().drawString(new Point(context.getPos().x + 71, context.getPos().y + 43), mc.textRenderer.fontHeight, info + " | " + getPing(playerEntity) + "ms", new Color(255, 255, 255));
                            String status = null;
                            Color statusColor = null;
                            for (StatusEffectInstance effect : playerEntity.getStatusEffects()) {
                                if (effect.getEffectType() == StatusEffects.WEAKNESS) {
                                    status = "Weakness ;)";
                                    statusColor = new Color(135, 0, 25);
                                } else if (effect.getEffectType() == StatusEffects.INVISIBILITY) {
                                    status = "Invisible w(ﾟДﾟ)w";
                                    statusColor = new Color(90, 90, 90);
                                } else if (effect.getEffectType() == StatusEffects.STRENGTH) {
                                    status = "Strength ┗|｀O′|┛";
                                    statusColor = new Color(185, 65, 185);
                                }
                            }
                            if (status != null) context.getInterface().drawString(new Point(context.getPos().x + 71, context.getPos().y + 55), mc.textRenderer.fontHeight, "Status: " + status, statusColor);
                            //TODO: Render armor items
                        }
                    }
                }
            }

            @Override
            public void getHeight(Context context) {
                context.setHeight(94);
            }

            @Override
            public Dimension getSize(IInterface inter) {
                return new Dimension(162, 94);
            }
        };
    }

    @Override
    public void OnRender(RenderEvent event) {

    }

    public static IToggleable getToggle() {
        return instance.isEnabled();
    }
}
