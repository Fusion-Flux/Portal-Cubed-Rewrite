package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@WrapOperation(
			method = "continueAttack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"
			)
	)
	private boolean portalGunCantBreakBlocks(LocalPlayer player, Operation<Boolean> original) {
		boolean isUsingItem = original.call(player);
		return isUsingItem || player.getMainHandItem().getItem() instanceof PortalGunItem;
	}
}
