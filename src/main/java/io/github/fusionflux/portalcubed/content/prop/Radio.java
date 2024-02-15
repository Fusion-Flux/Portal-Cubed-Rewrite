package io.github.fusionflux.portalcubed.content.prop;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Radio extends Prop implements AmbientSoundEmitter {
	private static final EntityDataAccessor<SoundEvent> TRACK = SynchedEntityData.defineId(Radio.class, PortalCubedSerializers.SOUND_EVENT);

	@ClientOnly
	private PropSoundInstance soundInstance;

	public Radio(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(TRACK, PortalCubedSounds.RADIO_SONG);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (level().isClientSide && TRACK.equals(data))
			playAmbientSound();
	}

	@ClientOnly
	@Override
	public void playAmbientSound() {
		if (soundInstance != null) soundInstance.forceStop();
		soundInstance = new PropSoundInstance(entityData.get(TRACK), this);
		Minecraft.getInstance().getSoundManager().play(soundInstance);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("radio_track", entityData.get(TRACK).getLocation().toString());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		var trackId = ResourceLocation.tryParse(tag.getString("radio_track"));
		if (trackId == null) trackId = PortalCubedSounds.RADIO_SONG.getLocation();
		if (trackId.equals(entityData.get(TRACK).getLocation())) return;
		entityData.set(TRACK, SoundEvent.createVariableRangeEvent(trackId));
	}
}
