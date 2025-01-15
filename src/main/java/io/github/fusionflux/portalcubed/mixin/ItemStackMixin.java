package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {
	@Shadow
	public abstract boolean is(TagKey<Item> tag);

	@Shadow
	public abstract void hurtAndBreak(int amount, LivingEntity entity, EquipmentSlot slot);

	@Unique
	private boolean hurtWithoutUnbreaking;

	@ModifyReturnValue(method = "isDamageableItem", at = @At("RETURN"))
	private boolean youTotallyDontHaveUnbreaking(boolean original) {
		return original || this.hurtWithoutUnbreaking;
	}

	@Override
	public void pc$hurtEquipmentNoUnbreaking(int amount, LivingEntity entity, EquipmentSlot slot) {
		this.hurtWithoutUnbreaking = true;
		this.hurtAndBreak(amount, entity, slot);
		this.hurtWithoutUnbreaking = false;
	}
}
