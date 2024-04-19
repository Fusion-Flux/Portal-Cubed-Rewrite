package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.phys.Vec3;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

/**
 * Teleport, don't lerp
 */
public class PlainTeleportPacket implements ClientboundPacket {
	private final int entityId;
	private final Vec3 pos;
	private final float pitch;
	private final float yaw;

	public PlainTeleportPacket(Entity entity) {
		this.entityId = entity.getId();
		this.pos = entity.position();
		this.pitch = entity.getXRot();
		this.yaw = entity.getYRot();
	}

	public PlainTeleportPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readVarInt();
		this.pos = PacketUtils.readVec3(buf);
		this.pitch = buf.readFloat();
		this.yaw = buf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		PacketUtils.writeVec3(buf, pos);
		buf.writeFloat(pitch);
		buf.writeFloat(yaw);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.PLAIN_TELEPORT;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientLevel level = player.clientLevel;
		Entity entity = level.getEntity(entityId);
		if (entity != null) {
			entity.moveTo(pos.x, pos.y, pos.z, yaw, pitch);
		}
	}
}
