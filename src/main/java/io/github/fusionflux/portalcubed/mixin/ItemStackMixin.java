package io.github.fusionflux.portalcubed.mixin;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
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

	@Inject(
			method = "addAttributeTooltips",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
					shift = At.Shift.AFTER
			)
	)
	private void injectFallDamageImmunityTooltip(Consumer<Component> tooltipAdder, @Nullable Player player, CallbackInfo ci, @Local EquipmentSlotGroup equipmentSlotGroup) {
		if (equipmentSlotGroup == EquipmentSlotGroup.FEET && this.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE))
			tooltipAdder.accept(
					Component.translatable(
							"attribute.modifier.plus." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(),
							"100",
							Component.translatable("attribute.name.generic.fall_damage_absorption")
					)
					.withStyle(ChatFormatting.BLUE)
			);
	}
}
