package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.TeleportStep;
import io.github.fusionflux.portalcubed.framework.util.RangeSequence;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

/**
 * Sent when an entity teleports on the server.
 * A teleport occurs in a single tick. An entity may pass through multiple portals in 1 tick.
 * When a PortalTeleportPacket is sent, all entity syncing that is normally done by ServerEntity is replaced or replicated.
 */
public record PortalTeleportPacket(int entityId, RangeSequence<TeleportStep> steps) implements ClientboundPacket {
	public PortalTeleportPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), RangeSequence.fromNetwork(buf, TeleportStep::fromNetwork));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entityId);
		this.steps.toNetwork(buf, TeleportStep::toNetwork);
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
		}

		entity.setPortalTeleport(this.steps);

		TeleportStep first = this.steps.get(0);
		Vec3 centerToPos = PortalTeleportHandler.getCenterToPosOffset(entity);
		Vec3 from = first.from().add(centerToPos);
		entity.xOld = entity.xo = from.x;
		entity.yOld = entity.yo = from.y;
		entity.zOld = entity.zo = from.z;
		entity.xRotO = first.rotations().getX();
		entity.yRotO = first.rotations().getY();

		TeleportStep last = this.steps.get(1);
		Vec3 to = last.to().add(centerToPos);
		entity.setPos(to);
		entity.getPositionCodec().setBase(to);
		entity.setXRot(last.rotations().getX());
		entity.setYRot(last.rotations().getY());
		entity.setDeltaMovement(last.vel());
	}
}
