package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportInfo;
import io.github.fusionflux.portalcubed.content.portal.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public record PortalTeleportPacket(int entityId, PortalTeleportInfo info) implements ClientboundPacket {
	public PortalTeleportPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), PortalTeleportInfo.fromNetwork(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entityId);
		this.info.toNetwork(buf);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.PORTAL_TELEPORT;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		Entity entity = player.clientLevel.getEntity(this.entityId);
		if (entity == null) {
			PortalCubed.LOGGER.warn("Received portal teleport for unknown entity: {}", this.entityId);
			return;
		} else if (this.isInfoInvalid(player.clientLevel.portalManager())) {
			PortalCubed.LOGGER.warn("Received portal teleport containing unknown portals");
			return;
		}

		int timeoutAge = entity.tickCount + TeleportProgressTracker.TIMEOUT_TICKS;
		TeleportProgressTracker tracker = entity.getTeleportProgressTracker();
		if (tracker != null) {
			tracker.append(this.info, timeoutAge);
		} else {
			entity.setTeleportProgressTracker(new TeleportProgressTracker(timeoutAge, this.info));
		}
		System.out.println("tracking start");
	}

	private boolean isInfoInvalid(PortalManager manager) {
		PortalTeleportInfo info = this.info;
		while (info != null) {
			if (manager.getPair(info.pairId()) == null) {
				return true;
			}

			info = info.next();
		}

		return false;
	}
}
