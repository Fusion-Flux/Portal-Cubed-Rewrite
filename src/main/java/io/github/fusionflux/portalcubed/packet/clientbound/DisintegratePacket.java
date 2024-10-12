package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationSoundType;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record DisintegratePacket(int entity, int ticks) implements ClientboundPacket {
	public DisintegratePacket(Entity entity) {
		this(entity.getId(), entity.pc$disintegrateTicks());
	}

	public DisintegratePacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), buf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(entity);
		buf.writeVarInt(ticks);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.DISINTEGRATE;
	}

	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		Entity entity = player.clientLevel.getEntity(this.entity);
		if (entity != null) {
			if (!entity.isSilent() && ticks >= EntityExt.DISINTEGRATE_TICKS) {
				DisintegrationSoundType.allFor(entity.getType()).forEach(soundType ->
						entity.level().pc$playSoundInstance(new FollowingSoundInstance(soundType.sound, entity.getSoundSource(), entity, false)));
			}
			entity.pc$disintegrate(ticks);
		}
	}
}
