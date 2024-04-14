package io.github.fusionflux.portalcubed.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow @Final private Entity entity;

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	private void sendPropHoldPacket(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> sender, CallbackInfo ci) {
		if (entity instanceof PlayerExt ext)
			PortalCubedPackets.sendToClient(player, new HoldStatusPacket(entity.getId(), ext.pc$getHeldProp()));
	}
}
