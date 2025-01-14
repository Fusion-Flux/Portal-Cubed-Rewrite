package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class PortalCubedBannerPatternTags {
	public static final TagKey<BannerPattern> APERTURE = create("pattern_item/aperture");

	private static TagKey<BannerPattern> create(String name) {
		return TagKey.create(Registries.BANNER_PATTERN, PortalCubed.id(name));
	}
}
