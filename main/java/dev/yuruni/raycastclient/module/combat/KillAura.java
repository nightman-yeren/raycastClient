package dev.yuruni.raycastclient.module.combat;

import dev.yuruni.raycastclient.RaycastClient;
import dev.yuruni.raycastclient.event.events.MouseEvent;
import dev.yuruni.raycastclient.event.events.RenderEvent;
import dev.yuruni.raycastclient.event.events.TickEvent;
import dev.yuruni.raycastclient.event.listener.MouseListener;
import dev.yuruni.raycastclient.event.listener.RenderListener;
import dev.yuruni.raycastclient.event.listener.TickListener;
import dev.yuruni.raycastclient.module.Module;
import dev.yuruni.raycastclient.setting.BooleanSetting;
import dev.yuruni.raycastclient.setting.DoubleSetting;
import dev.yuruni.raycastclient.setting.EnumSetting;
import dev.yuruni.raycastclient.setting.IntegerSetting;
import dev.yuruni.raycastclient.util.color.Color;
import dev.yuruni.raycastclient.util.entity.EntityUtil;
import dev.yuruni.raycastclient.util.math.Rotation;
import dev.yuruni.raycastclient.util.math.RotationUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class KillAura extends Module implements TickListener, RenderListener, MouseListener {

    private enum Weapon {
        SWORD,
        AXE
    }
    private enum AttackPriority {
        HEALTH("Health", e -> e instanceof LivingEntity ? ((LivingEntity) e).getHealth() : Integer.MAX_VALUE),
        CLOSEST("Closest", e -> {
            assert mc.player != null;
            return mc.player.squaredDistanceTo(e);
        }),
        ANGLE("Angle", e -> RotationUtil.getAngleToLookVec(e.getBoundingBox().getCenter()));

        private final String name;
        private final Comparator<Entity> comparator;

        private AttackPriority(String name, ToDoubleFunction<Entity> keyExtractor)
        {
            this.name = name;
            comparator = Comparator.comparingDouble(keyExtractor);
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
    private enum RotationType {
        CLIENT,
        PACKET,
        NONE
    }

    private static final DoubleSetting range = new DoubleSetting("Range", "range", "Range of attack", () -> true, 0, 15, 5);
    private static final IntegerSetting fov = new IntegerSetting("FOV", "fov", "Degree of attack", () -> true, 30, 360, 360);
    private static final BooleanSetting attackPlayers = new BooleanSetting("Players", "players", "Attack Players", () -> true, true);
    private static final BooleanSetting attackHostiles = new BooleanSetting("Hostiles", "hostiles", "Attack Monsters", () -> true, true);
    private static final BooleanSetting attackPassives = new BooleanSetting("Passives", "passives", "Attack Animals", () -> true, true);
    private static final EnumSetting<Weapon> weaponOfChoice = new EnumSetting<>("Weapon", "weapon", "What weapon  to use", () -> true, Weapon.SWORD, Weapon.class);
    private static final EnumSetting<AttackPriority> priority = new EnumSetting<>("Priority", "priority", "Priority of attack", () -> true, AttackPriority.CLOSEST, AttackPriority.class);
    private static final BooleanSetting doCriticals = new BooleanSetting("Criticals", "criticals", "Do Criticals", () -> true, false);
    private static final EnumSetting<RotationType> rotationType = new EnumSetting<>("Rot", "rotationtype", "Type of rotation to do", () -> true, RotationType.CLIENT, RotationType.class);
    private static final IntegerSetting rotationSpeed = new IntegerSetting("RotSpeed", "rotationspeed", "(rotations per second) Only applies to client rotation", () -> true, 10, 3600, 600);
    private static final IntegerSetting speedRandomization = new IntegerSetting("SpdRnd", "attackspeedrandomizer", "(In ticks) Helps to bypass anticheats", () -> true, 0, 20, 0);

    private Entity target;
    private int speedRandTick = 0;
    private int speedTickCap = 0;
    private float nextYaw;
    private float nextPitch;

    public KillAura() {
        super("Kill Aura", "killaura", "Attacks automatically", () -> true, true);
        settings.add(range);
        settings.add(fov);
        settings.add(attackPlayers);
        settings.add(attackHostiles);
        settings.add(attackPassives);
        settings.add(weaponOfChoice);
        settings.add(priority);
        settings.add(doCriticals);
        settings.add(rotationType);
        settings.add(rotationSpeed);
        settings.add(speedRandomization);
    }

    @Override
    protected void onEnable() {
        RaycastClient.INSTANCE.eventManager.AddListener(TickListener.class, this);
        RaycastClient.INSTANCE.eventManager.AddListener(RenderListener.class, this);
        RaycastClient.INSTANCE.eventManager.AddListener(MouseListener.class, this);
    }

    @Override
    protected void onDisable() {
        RaycastClient.INSTANCE.eventManager.RemoveListener(TickListener.class, this);
        RaycastClient.INSTANCE.eventManager.RemoveListener(RenderListener.class, this);
        RaycastClient.INSTANCE.eventManager.RemoveListener(MouseListener.class, this);
    }

    @Override
    public void OnUpdate(TickEvent event) { //TODO: Sync with target hud
        if (mc.player != null && mc.player.isAlive()) {
            if (mc.player.getAttackCooldownProgress(0) != 1) return;
            if (speedRandomization.getValue() != 0) {
                if (speedTickCap == 0) {
                    speedTickCap = new Random().nextInt(1, speedRandomization.getValue() + 1);
                }
                speedRandTick += 1;
                if (speedRandTick < speedTickCap) {
                    return;
                } else {
                    speedRandTick = 0;
                    speedTickCap = new Random().nextInt(1, speedRandomization.getValue() + 1);
                }
            }
            Stream<Entity> stream = getEntityStream();

            //Determine target
            target = stream.min(priority.getValue().comparator).orElse(null); //Found an much easier implementation with streams to avoid huge nested if statements, thanks Wurst!
            //TODO: target render
            if (target == null) {
                return;
            }

            //Weapon Switch
            //TODO: do other weapon outside of hotbar and switch em back
            ArrayList<Integer> weaponSlot = getMatchingWeapons(InventorySelectType.HOTBAR);
            if (weaponSlot == null) return;
            if (!weaponSlot.isEmpty()) {
                if (weaponSlot.get(0) != mc.player.getInventory().selectedSlot)
                    mc.player.getInventory().selectedSlot = weaponSlot.get(0); //Getting the first one, do rank comparison (Choose netherite sword if netherite sword and diamond sword are present) at free time
            }

            //Do crit
            if (doCriticals.isOn()) {
                ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
                assert networkHandler != null;
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.03125D, mc.player.getZ(), false));
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
            }
            //Rotation
            Vec3d hitVec = target.getBoundingBox().getCenter();
            if (rotationType.getValue() == RotationType.PACKET) {
                RaycastClient.INSTANCE.rotationFaker.faceVectorPacket(hitVec);
            } else if (rotationType.getValue() == RotationType.CLIENT) {
                faceEntityClient(target);
            }

            //Client Rotation Check
            if (rotationType.getValue() == RotationType.CLIENT) {
                if (!RotationUtil.isFacingBox(target.getBoundingBox(), range.getValue()))
                    return;
            }

            //Finally, swing the fucking hand
            if (mc.interactionManager == null) return; //HOLD!!! Prevent crashing on poor devices probably
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);

            target = null; //LAST LINE. Clearing the target
        }
    }

    private static Stream<Entity> getEntityStream() {
        if (mc.player == null) return null;
        Stream<Entity> stream = EntityUtil.getAttackableEntities();
        double attackRange = range.getValue();
        //Filter out entities out of range
        stream = stream.filter(e -> mc.player.squaredDistanceTo(e) <= attackRange);
        //Filter out entities not within attack type
        if (!attackHostiles.isOn()) {
            stream = stream.filter(e -> !(e instanceof Monster));
        }
        if (!attackPassives.isOn()) {
            stream = stream.filter(e -> !(e instanceof AnimalEntity));
        }
        if (!attackPlayers.isOn()) {
            stream = stream.filter(e -> !(e instanceof PlayerEntity));
        }

        if (fov.getValue() < 360) { //Ignores if it's 360 (It's all around anyway)
            //Filter out entities that is not with in FOV
            stream = stream.filter(e -> RotationUtil.getAngleToLookVec(e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
        }
        return stream;
    }

    private boolean faceEntityClient(Entity entity)
    {
        // get needed rotation
        Box box = entity.getBoundingBox();
        Rotation needed = RotationUtil.getNeededRotations(box.getCenter());

        // turn towards center of boundingBox
        Rotation next = RotationUtil.slowlyTurnTowards(needed,
                rotationSpeed.getValue() / 20F);
        nextYaw = next.yaw();
        nextPitch = next.pitch();

        // check if facing center
        if(RotationUtil.isAlreadyFacing(needed))
            return true;

        // if not facing center, check if facing anything in boundingBox
        return RotationUtil.isFacingBox(box, range.getValue());
    }

    @Override
    public void OnMouseUpdate(MouseEvent event) {
        if (target == null || mc.player == null) return;

        int yawDiff = (int)(nextYaw - mc.player.getYaw());
        int pitchDiff = (int)(nextPitch - mc.player.getPitch());
        if (MathHelper.abs(yawDiff) < 1 && MathHelper.abs(pitchDiff) < 1) {
            return;
        }

        event.setDeltaX(event.getDefaultDeltaX() + yawDiff);
        event.setDeltaY(event.getDefaultDeltaY() + pitchDiff);
    }

    private enum InventorySelectType {
        HOTBAR,
        INVENTORY,
        ANY
    }

    private ArrayList<Integer> getMatchingWeapons(InventorySelectType selectType) {
        if (mc.player != null) {
            ArrayList<Integer> weaponSlots = new ArrayList<>();
            ItemStack heldItem = mc.player.getMainHandStack();
            if (weaponOfChoice.getValue() == Weapon.SWORD) {
                if (!(heldItem.getItem() instanceof SwordItem)) {
                    PlayerInventory playerInventory = mc.player.getInventory();
                    for (int i = 0; i < playerInventory.size(); i++) {
                        ItemStack item = playerInventory.getStack(i);
                        if (!item.isEmpty() && item.getItem() instanceof SwordItem) {
                            if (selectType == InventorySelectType.HOTBAR) {
                                if (PlayerInventory.isValidHotbarIndex(i)) weaponSlots.add(i);
                            } else if (selectType == InventorySelectType.INVENTORY) {
                                if (!PlayerInventory.isValidHotbarIndex(i) && !playerInventory.offHand.get(0).equals(playerInventory.getStack(i))) weaponSlots.add(i);
                            } else if (selectType == InventorySelectType.ANY) {
                                weaponSlots.add(i);
                            }
                        }
                    }
                }
            } else if (weaponOfChoice.getValue() == Weapon.AXE) {
                if (!(heldItem.getItem() instanceof AxeItem)) {
                    PlayerInventory playerInventory = mc.player.getInventory();
                    for (int i = 0; i < playerInventory.size(); i++) {
                        ItemStack item = playerInventory.getStack(i);
                        if (!item.isEmpty() && item.getItem() instanceof AxeItem) {
                            if (selectType == InventorySelectType.HOTBAR) {
                                if (PlayerInventory.isValidHotbarIndex(i)) weaponSlots.add(i);
                            } else if (selectType == InventorySelectType.INVENTORY) {
                                if (!PlayerInventory.isValidHotbarIndex(i) && !playerInventory.offHand.get(0).equals(playerInventory.getStack(i))) weaponSlots.add(i);
                            } else if (selectType == InventorySelectType.ANY) {
                                weaponSlots.add(i);
                            }
                        }
                    }
                }
            }
            return weaponSlots;
        }
        return null;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public void OnRender(RenderEvent event) {
        if (target != null) {
            MatrixStack matrixStack = event.GetMatrixStack();
            float partialTicks = event.GetPartialTicks();

            matrixStack.push();

            Box boundingBox = target.getBoundingBox();

            Vec3d entityVelocity = target.getVelocity();
            Vec3d velocityPartial = new Vec3d(entityVelocity.x * partialTicks, 0, entityVelocity.z * partialTicks);

            boundingBox = boundingBox.offset(velocityPartial);

            getRenderer().drawTransparent3DBox(matrixStack, boundingBox, new Color(94, 52, 235), 0.8f);

            matrixStack.pop();
        }
    }
}
