package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedArmorMaterials;
import io.github.fusionflux.portalcubed.content.PortalCubedAttributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

@Mixin(ArmorMaterial.class)
public class ArmorMaterialMixin {
	@Inject(
			method = "createAttributes",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers$Builder;add(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;Lnet/minecraft/world/entity/EquipmentSlotGroup;)Lnet/minecraft/world/item/component/ItemAttributeModifiers$Builder;",
					ordinal = 1
			)
	)
	private void addFallDamageAbsorptionAttribute(
			ArmorType type,
			CallbackInfoReturnable<ItemAttributeModifiers> cir,
			@Local ItemAttributeModifiers.Builder builder,
			@Local EquipmentSlotGroup equipmentSlotGroup
	) {
		ArmorMaterial self = (ArmorMaterial) (Object) this;
		if (self.equals(PortalCubedArmorMaterials.LONG_FALL_BOOTS) || self.equals(PortalCubedArmorMaterials.ADVANCED_KNEE_REPLACEMENTS)) {
			builder.add(
					PortalCubedAttributes.FALL_DAMAGE_ABSORPTION,
					new AttributeModifier(
							PortalCubed.id("armor." + type.getName()),
							1,
							AttributeModifier.Operation.ADD_VALUE
					),
					equipmentSlotGroup
			);
		}
	}
}
