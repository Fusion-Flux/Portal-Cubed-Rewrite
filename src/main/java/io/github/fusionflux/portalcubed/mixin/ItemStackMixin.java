package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;entries()Ljava/util/Collection;"))
	private void injectFallDamageImmunityTooltip(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> lines, @Local EquipmentSlot equipmentSlot) {
		if (equipmentSlot == EquipmentSlot.FEET && ((ItemStack) (Object) this).is(PortalCubedItemTags.ABSORB_FALL_DAMAGE))
			lines.add(Component.translatable("attribute.name.generic.fall_damage_absorption").withStyle(ChatFormatting.BLUE));
	}
}
