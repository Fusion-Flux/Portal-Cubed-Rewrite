package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void syncDisintegrationStateOnJoin(CallbackInfo ci) {
		if (this.entity instanceof ServerPlayer player && Disintegration.isDisintegrating(player)) {
			PortalCubedPackets.sendToClient(player, new DisintegratePacket(player));
		}
	}

	@Inject(method = "addPairing", at = @At("TAIL"))
	private void syncDisintegrationOnTrackingStart(ServerPlayer player, CallbackInfo ci) {
		if (Disintegration.isDisintegrating(this.entity)) {
			PortalCubedPackets.sendToClient(player, new DisintegratePacket(this.entity));
		}
	}
}
