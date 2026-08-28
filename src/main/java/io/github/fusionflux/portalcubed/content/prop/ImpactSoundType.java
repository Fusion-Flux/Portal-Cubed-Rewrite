package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;
import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public enum ImpactSoundType {
	GENERIC,
	METAL,
	CUBE,
	OLD_AP_CUBE,
	PORTAL_1_CUBE;

	public TagKey<EntityType<?>> tag() {
		return PortalCubedEntityTags.IMPACT_SOUNDS.get(this);
	}

	public SoundEvent sound() {
		return PortalCubedSounds.IMPACTS.get(this);
	}

	@Override
	public String toString() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ImpactSoundType> forEntity(Entity entity) {
		for (ImpactSoundType soundType : values()) {
			if (entity.is(soundType.tag())) return Optional.of(soundType);
		}
		return Optional.empty();
	}
}
