package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.boots.LongFallBootsModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	@WrapMethod(method = "getArmorModel")
	private HumanoidModel<HumanoidRenderState> useLongFallBootsModel(HumanoidRenderState renderState, EquipmentSlot slot, Operation<HumanoidModel<HumanoidRenderState>> original) {
		if (slot == EquipmentSlot.FEET && renderState.feetEquipment.is(PortalCubedItems.LONG_FALL_BOOTS))
			return LongFallBootsModel.INSTANCE;
		return original.call(renderState, slot);
	}
}
