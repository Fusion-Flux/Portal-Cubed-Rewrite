package io.github.fusionflux.portalcubed.mixin.boots;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedAttributes;
import io.github.fusionflux.portalcubed.content.boots.LongFallBoots;
import io.github.fusionflux.portalcubed.content.boots.SourcePhysics;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedDamageTypeTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
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

	@ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
	private static AttributeSupplier.Builder addFallDamageAbsorptionAttribute(AttributeSupplier.Builder builder) {
		return builder.add(PortalCubedAttributes.FALL_DAMAGE_ABSORPTION);
	}

	@WrapOperation(
			method = "causeFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I"
			)
	)
	private int absorbFallDamageIntoBoots(LivingEntity self, double fallDistance, float damageModifier, Operation<Integer> original,
										  @Local(argsOnly = true, name = "damageSource") DamageSource damageSource) {
		int fallDamage = original.call(self, fallDistance, damageModifier);

		double absorption = this.getAttributeValue(PortalCubedAttributes.FALL_DAMAGE_ABSORPTION);
		ItemStack boots = this.getItemBySlot(EquipmentSlot.FEET);
		if (!damageSource.is(PortalCubedDamageTypeTags.BYPASSES_FALL_DAMAGE_ABSORPTION) && absorption > 0 && !boots.isEmpty()) {
			int bootDamage = LongFallBoots.calculateDamage(this.registryAccess(), boots, absorption, fallDamage);
			((ItemStackExt) (Object) boots).pc$hurtEquipmentNoUnbreaking(bootDamage, self, EquipmentSlot.FEET);

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
	@ModifyExpressionValue(
			method = "travelInAir",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;shouldDiscardFriction()Z"
			)
	)
	private boolean sourcePhysicsFriction(boolean original) {
		// source physics fully disables air drag
		if ((Object) this instanceof Player player && SourcePhysics.appliesTo(player) && !player.onGround()) {
			return true;
		}

		return original;
	}
}
