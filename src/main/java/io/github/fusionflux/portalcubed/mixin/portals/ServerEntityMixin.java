package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.fusionflux.portalcubed.packet.clientbound.LocalTeleportPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	@Mutable
	private Consumer<Packet<?>> broadcast;

	@Shadow
	@Final
	private Entity entity;

	// TODO: Look into this again, it requires changes because of the networking rewrite
//	@WrapMethod(method = "sendChanges")
//	private void bundlePackets(Operation<Void> original) {
//		Consumer<Packet<?>> originalBroadcast = this.broadcast;
//		try {
//			List<Packet<?>> packets = new ArrayList<>();
//			this.broadcast = packets::add;
//			original.call();
//			//noinspection unchecked
//			List<Packet<? super ClientGamePacketListener>> casted = (List<Packet<? super ClientGamePacketListener>>) packets;
//			originalBroadcast.accept(new ClientboundBundlePacket(casted));
//		} finally {
//			this.broadcast = originalBroadcast;
//		}
//	}

	@ModifyArg(
			method = "sendChanges",
			at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V")
	)
	private Object wrapTeleports(Object packet) {
		if (packet instanceof ClientboundTeleportEntityPacket tpPacket) {
			if (this.entity.pc$isNextTeleportNonLocal()) {
				// known non-local, leave it alone
				this.entity.pc$setNextTeleportNonLocal(false);
				return packet;
			}
			// otherwise wrap it
			return ServerPlayNetworking.createS2CPacket(new LocalTeleportPacket(tpPacket));
		}
		return packet;
	}
}
