package io.github.fusionflux.portalcubed.mixin.boots;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import io.github.fusionflux.portalcubed.content.PortalCubedAttributes;
import io.github.fusionflux.portalcubed.content.boots.LongFallBoots;
import io.github.fusionflux.portalcubed.content.boots.SourcePhysics;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedDamageTypeTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	protected LivingEntityMixin(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);

	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> attribute);

	@Shadow
	public abstract void remove(RemovalReason reason);

	@Inject(method = "dismountVehicle", at = @At("TAIL"))
	private void onDismount(CallbackInfo ci) {
		// calls teleportTo, which sets non-local to true
		this.pc$setNextTeleportNonLocal(false);
	}

	@ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
	private static AttributeSupplier.Builder addFallDamageAbsorptionAttribute(AttributeSupplier.Builder builder) {
		return builder.add(PortalCubedAttributes.FALL_DAMAGE_ABSORPTION);
	}

	@WrapOperation(
			method = "causeFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I"
			)
	)
	private int absorbFallDamageIntoBoots(LivingEntity instance, float fallDistance, float damageMultiplier, Operation<Integer> original, @Local(argsOnly = true) DamageSource source) {
		int fallDamage = original.call(instance, fallDistance, damageMultiplier);

		double absorption = this.getAttributeValue(PortalCubedAttributes.FALL_DAMAGE_ABSORPTION);
		ItemStack boots = this.getItemBySlot(EquipmentSlot.FEET);
		if (!source.is(PortalCubedDamageTypeTags.BYPASSES_FALL_DAMAGE_ABSORPTION) && absorption > 0 && !boots.isEmpty()) {
			int bootDamage = LongFallBoots.calculateDamage(this.registryAccess(), boots, absorption, fallDamage);
			((ItemStackExt) (Object) boots).pc$hurtEquipmentNoUnbreaking(bootDamage, instance, EquipmentSlot.FEET);

			if (!boots.isEmpty())
				return Mth.floor(fallDamage * (1 - absorption));
		}

		return fallDamage;
	}

	@SuppressWarnings("ConstantValue")
	@ModifyExpressionValue(
			method = "jumpFromGround",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"
			)
	)
	private boolean sourcePhysicsNoSprintBoost(boolean original) {
		if ((Object) this instanceof Player player && SourcePhysics.appliesTo(player)) {
			return false;
		} else {
			return original;
		}
	}

	@SuppressWarnings("ConstantValue")
	@WrapOperation(
			method = "travelInAir",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 sourcePhysicsFriction(LivingEntity self, Vec3 movementInput, float slipperiness, Operation<Vec3> original,
									   @Local(ordinal = 0) float blockFriction,
									   @Local(ordinal = 1) LocalFloatRef friction) {
		if ((Object) this instanceof Player player && SourcePhysics.appliesTo(player)) {
			boolean wasGrounded = self.onGround();
			Vec3 newVel = original.call(self, movementInput, slipperiness);
			boolean isGrounded = self.onGround();
			if (!isGrounded) {
				// when airborne, discard all friction to maintain speed.
				friction.set(1);
			}
			if (!wasGrounded && isGrounded) {
				// when landing, re-calculate friction.
				// Otherwise, air friction is used for an extra tick, building infinite speed.
				BlockPos movementEffectingPos = this.getBlockPosBelowThatAffectsMyMovement();
				float newBlockFriction = this.level().getBlockState(movementEffectingPos).getBlock().getFriction();
				float newFriction = newBlockFriction * 0.91f;
				friction.set(newFriction);
			}
			return newVel;
		}

		// no source physics, change nothing
		return original.call(self, movementInput, slipperiness);
	}
}
