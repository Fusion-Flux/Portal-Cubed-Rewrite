package io.github.fusionflux.portalcubed.content.prop;

import java.util.List;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PlayerLookup;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.RadioSoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Radio extends Prop {
	private SoundEvent soundEvent;

	@ClientOnly
	private PropSoundInstance soundInstance;

	public Radio(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
		this.soundEvent = PortalCubedSounds.RADIO_SONG;
	}

	@ClientOnly
	public void clientUpdateSound(SoundEvent event) {
		if (soundInstance != null) soundInstance.forceStop();
		soundInstance = new PropSoundInstance(event, this);
		Minecraft.getInstance().getSoundManager().play(soundInstance);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("radio_track", soundEvent.getLocation().toString());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		var trackId = ResourceLocation.tryParse(tag.getString("radio_track"));
		if (trackId == null) trackId = PortalCubedSounds.RADIO_SONG.getLocation();
		if (trackId == soundEvent.getLocation()) return;
		soundEvent = SoundEvent.createVariableRangeEvent(trackId);
		for (var player : PlayerLookup.tracking(this))
			PortalCubedPackets.sendToClient(player, new RadioSoundPacket(getId(), Holder.direct(soundEvent)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundBundlePacket(List.of(
			super.getAddEntityPacket(),
			(Packet<ClientGamePacketListener>) PortalCubedPackets.createPayloadPacket(new RadioSoundPacket(getId(), Holder.direct(soundEvent)))
		));
	}
}
