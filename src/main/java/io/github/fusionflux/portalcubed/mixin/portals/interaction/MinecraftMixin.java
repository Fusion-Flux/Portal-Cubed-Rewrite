package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.interaction.UsableOnPortals;
import io.github.fusionflux.portalcubed.content.portal.interaction.UseItemOnPortalPacket;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.framework.extension.MinecraftExt;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

@Mixin(Minecraft.class)
public class MinecraftMixin implements MinecraftExt {
	@Unique
	private RaycastResult.@Nullable Portal selectedPortal;

	@Override
	public RaycastResult.@Nullable Portal selectedPortal() {
		return this.selectedPortal;
	}

	@Override
	public void setSelectedPortal(RaycastResult.@Nullable Portal result) {
		this.selectedPortal = result;
	}

	@WrapOperation(
			method = "startUseItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
			)
	)
	private InteractionResult useItemsOnPortals(MultiPlayerGameMode gameMode, Player player, InteractionHand hand, Operation<InteractionResult> original) {
		if (this.selectedPortal != null && gameMode.getPlayerMode() != GameType.SPECTATOR) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.getItem() instanceof UsableOnPortals item) {
				PortalReference portal = this.selectedPortal.portal;
				PortalCubedPackets.sendToServer(new UseItemOnPortalPacket(portal.id, hand));
				return item.useOnPortal(player, portal, stack, hand);
			}
		}

		return original.call(gameMode, player, hand);
	}
}
