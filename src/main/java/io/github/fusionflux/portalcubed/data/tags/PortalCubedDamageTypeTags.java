package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class PortalCubedDamageTypeTags {
	public static final TagKey<DamageType> BYPASSES_FALL_DAMAGE_ABSORPTION = create("bypasses_fall_damage_absorption");

	private static TagKey<DamageType> create(String name) {
		return TagKey.create(Registries.DAMAGE_TYPE, PortalCubed.id(name));
	}
}
