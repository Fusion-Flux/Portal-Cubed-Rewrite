package io.github.fusionflux.portalcubed.mixin.utils;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {
	@Shadow
	public abstract void hurtAndBreak(int amount, LivingEntity entity, EquipmentSlot slot);

	@Shadow
	public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> type, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag);

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
			method = "addDetailsToTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
					shift = At.Shift.AFTER
			)
	)
	private void addCustomTooltipProviders(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag, Consumer<Component> output, CallbackInfo ci) {
		this.addToTooltip(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, context, display, output, flag);
		this.addToTooltip(PortalCubedDataComponents.CANNON_SETTINGS, context, display, output, flag);
		this.addToTooltip(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE, context, display, output, flag);
		this.addToTooltip(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE, context, display, output, flag);
	}
}
