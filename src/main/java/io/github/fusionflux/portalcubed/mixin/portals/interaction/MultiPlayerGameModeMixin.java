package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalAwareUseItemOnPacket;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	// the generics here are safe I pinky promise
	@ModifyReturnValue(method = "method_41933", at = @At("RETURN"))
	private Packet<?> providePortalContext(Packet<ServerGamePacketListener> original) {
		if (!(original instanceof ServerboundUseItemOnPacket usePacket))
			return original;

		PortalPathHolder holder = usePacket.getHitResult().portalPath();
		if (!(holder instanceof PortalPathHolder.Present(PortalPath path)))
			return original;

		PortalAwareUseItemOnPacket payload = new PortalAwareUseItemOnPacket(usePacket, path.serialize());
		return ClientPlayNetworking.createC2SPacket(payload);
	}
}
