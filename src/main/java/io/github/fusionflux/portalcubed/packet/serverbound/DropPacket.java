package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.quiltmc.qsl.networking.api.PacketSender;

public record DropPacket() implements ServerboundPacket {
	public DropPacket(FriendlyByteBuf ignored) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.DROP;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		((PlayerExt) player).pc$getHeldProp().ifPresent(id -> {
			Entity entity = player.serverLevel().getEntity(id);
			if (entity instanceof HoldableEntity holdable)
				holdable.drop();
		});
	}
}
