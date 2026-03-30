package io.github.fusionflux.portalcubed.mixin.holdable;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@WrapOperation(
			method = { "startUseItem", "startAttack" },
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z"
			)
	)
	private boolean dropHeldEntityOnUseOrAttack(LocalPlayer player, Operation<Boolean> original) {
		boolean handsBusy = original.call(player);
		if (handsBusy) {
			return true;
		}

		if (player.getHeldEntity() != null) {
			PortalCubedPackets.sendToServer(DropPacket.INSTANCE);
			return true;
		}

		return false;
	}
}
