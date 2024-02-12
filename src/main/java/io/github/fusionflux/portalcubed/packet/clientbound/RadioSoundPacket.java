package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.prop.Radio;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public record RadioSoundPacket(int entityId, Holder<SoundEvent> soundEvent) implements ClientboundPacket {
	public RadioSoundPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), buf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), soundEvent, (bufx, soundEvent) -> soundEvent.writeToNetwork(bufx));
	}

	@Override
	public ResourceLocation id() {
		return PortalCubedPackets.RADIO_SOUND;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		if (player.level().getEntity(entityId) instanceof Radio radio)
			radio.clientUpdateSound(soundEvent.value());
	}
}
