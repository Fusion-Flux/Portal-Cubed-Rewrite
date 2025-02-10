package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	public LocalPlayer player;

	@WrapMethod(method = "continueAttack")
	private void portalGunCantBreakBlocks(boolean leftClick, Operation<Void> original) {
		ItemStack heldItem = this.player.getMainHandItem();
		if (!(heldItem.getItem() instanceof PortalGunItem))
			original.call(leftClick);
	}
}
