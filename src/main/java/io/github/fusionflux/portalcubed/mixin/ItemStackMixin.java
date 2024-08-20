package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {
	@Shadow
	public abstract <T extends LivingEntity> void hurtAndBreak(int amount, T entity, Consumer<T> breakCallback);

	@Shadow
	public abstract boolean is(TagKey<Item> tag);

	@Unique
	private boolean hurtWithoutUnbreaking;

	@WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I", ordinal = 0))
	private int youTotallyDontHaveUnbreaking(Enchantment enchantment, ItemStack stack, Operation<Integer> original) {
		if (hurtWithoutUnbreaking)
			return 0;
		return original.call(enchantment, stack);
	}

	@Override
	public <T extends LivingEntity> void pc$hurtAndBreakNoUnbreaking(int amount, T entity, Consumer<T> breakCallback) {
		hurtWithoutUnbreaking = true;
		this.hurtAndBreak(amount, entity, breakCallback);
		hurtWithoutUnbreaking = false;
	}

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;entries()Ljava/util/Collection;"))
	private void injectFallDamageImmunityTooltip(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> lines, @Local EquipmentSlot equipmentSlot) {
		if (equipmentSlot == EquipmentSlot.FEET && this.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE))
			lines.add(
					Component.translatable(
							"attribute.modifier.plus." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(),
							"100",
							Component.translatable("attribute.name.generic.fall_damage_absorption")
					)
					.withStyle(ChatFormatting.BLUE)
			);
	}
}
