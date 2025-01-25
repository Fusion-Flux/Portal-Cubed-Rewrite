package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements Leashable {
	protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/ConversionType;convert(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/ConversionParams;)V"
			)
	)
	private void dontCopyDisintegrationState(ConversionType instance, Mob oldMob, Mob newMob, ConversionParams conversionParams, Operation<Void> original) {
		boolean wasDisintegrating = this.pc$disintegrating();
		this.pc$disintegrating(false);
		original.call(instance, oldMob, newMob, conversionParams);
		this.pc$disintegrating(wasDisintegrating);
	}

	@Override
	public boolean pc$disintegrate() {
		if (!this.level().isClientSide)
			this.dropLeash();
		return super.pc$disintegrate();
	}

	@Override
	public boolean canHaveALeashAttachedToIt() {
		return Leashable.super.canHaveALeashAttachedToIt() && !this.pc$disintegrating();
	}
}
