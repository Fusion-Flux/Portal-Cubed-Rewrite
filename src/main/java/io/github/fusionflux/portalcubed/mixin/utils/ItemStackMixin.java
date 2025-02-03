package io.github.fusionflux.portalcubed.mixin.utils;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {
	@Shadow
	public abstract boolean is(TagKey<Item> tag);

	@Shadow
	public abstract void hurtAndBreak(int amount, LivingEntity entity, EquipmentSlot slot);

	@Shadow
	protected abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag);

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

	@Inject(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
					ordinal = 0
			)
	)
	private void addCustomTooltipProviders(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
		this.addToTooltip(PortalCubedDataComponents.CANNON_SETTINGS, tooltipContext, consumer, tooltipFlag);
		this.addToTooltip(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE, tooltipContext, consumer, tooltipFlag);
		this.addToTooltip(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE, tooltipContext, consumer, tooltipFlag);
	}
}
