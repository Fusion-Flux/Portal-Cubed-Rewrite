package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class PortalCubedBlockTags {
	public static final TagKey<Block> MAGNESIUM_FIRE_BASE_BLOCKS = create("magnesium_fire_base_blocks");

	private static TagKey<Block> create(String name) {
		return TagKey.create(Registries.BLOCK, PortalCubed.id(name));
	}
}
