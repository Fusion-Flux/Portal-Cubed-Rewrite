package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public enum DisintegrationSoundType {
	GENERIC,
	RADIO,
	TURRET;

	public final String name = this.name().toLowerCase(Locale.ROOT);
	public final SoundEvent sound;
	public final TagKey<EntityType<?>> tag;

	DisintegrationSoundType() {
		ResourceLocation id = PortalCubed.id(name + "_disintegration");
		this.sound = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
		this.tag = TagKey.create(Registries.ENTITY_TYPE, id.withSuffix("_sound"));
	}

	public static Iterable<DisintegrationSoundType> allFor(EntityType<?> entityType) {
		Iterable<DisintegrationSoundType> iterable = Iterables.filter(List.of(values()), soundType -> entityType.is(soundType.tag));
		return Iterables.isEmpty(iterable) ? Collections.singleton(GENERIC) : iterable;
	}

	public static void init() {
	}
}
