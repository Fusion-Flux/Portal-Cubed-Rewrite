package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
	@WrapOperation(
			method = {"appendItemLayers", "shouldPlaySwapAnimation"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
			)
	)
	private Object overrideModelWithPortalGunSkin(ItemStack stack, DataComponentType<ResourceLocation> dataComponentType, Operation<Object> original) {
		PortalGunSettings portalGun = PortalGunItem.getGunSettings(stack);
		if (portalGun != null) {
			PortalGunSkin skin = portalGun.skin();
			return skin != null ? skin.itemModel() : null;
		}
		return original.call(stack, dataComponentType);
	}
}
