package io.github.fusionflux.portalcubed.content.prop.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.PropSoundInstance;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Radio extends Prop implements AmbientSoundEmitter {
	public static final EntityDataAccessor<String> TRACK = SynchedEntityData.defineId(Radio.class, EntityDataSerializers.STRING);

	private static final String defaultSong = PortalCubedSounds.RADIO_SONG.location().toString();

	// This needs to be an object or else the server will crash while loading PropType
	@Environment(EnvType.CLIENT)
	private Object soundInstance;

	public Radio(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	protected void defineSynchedData(Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TRACK, defaultSong);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (this.level().isClientSide && TRACK.equals(data) && !this.isSilent())
			this.playAmbientSound();
	}

	public void playTrack(Holder<SoundEvent> holder) {
		String string = holder.value().location().toString();
		this.entityData.set(TRACK, string);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void playAmbientSound() {
		if (this.soundInstance != null)
			((FollowingSoundInstance) this.soundInstance).forceStop();
		String track = this.entityData.get(TRACK);
		ResourceLocation id = ResourceLocation.tryParse(track);
		if (id != null) {
			BuiltInRegistries.SOUND_EVENT.get(id).ifPresent(holder -> {
				PropSoundInstance soundInstance = new PropSoundInstance(holder.value(), this);
				soundInstance.setLooping(true);
				Minecraft.getInstance().getSoundManager().play(soundInstance);
				this.soundInstance = soundInstance;
			});
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("radio_track", this.entityData.get(TRACK));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(TRACK, tag.getString("radio_track"));
	}
}
