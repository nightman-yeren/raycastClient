package dev.yuruni.raycastclient.mixin;

import dev.yuruni.raycastclient.module.ModuleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin{

    @Shadow
    protected DataTracker dataTracker;

    @Shadow
    public abstract boolean isSubmergedIn(TagKey<Fluid> fluidTag);

    @Shadow
    public abstract boolean isOnGround();

    @Inject(at = { @At("HEAD") }, method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
    private void onIsInvisibleCheck(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if(ModuleManager.getModule("antiinvis").isenabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = {@At("HEAD")}, method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {

    }

    @Inject(at= {@At("HEAD")}, method = "getStepHeight()F", cancellable=true)
    public void onGetStepHeight(CallbackInfoReturnable<Float> cir) {

    }
}