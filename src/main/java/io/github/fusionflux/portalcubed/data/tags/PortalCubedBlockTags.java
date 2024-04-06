package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PortalCubedBlockTags {
	public static final TagKey<Block> CANNON_REPLACEABLE = create("construction_cannon_replaceable");

	private static TagKey<Block> create(String name) {
		return TagKey.create(Registries.BLOCK, PortalCubed.id(name));
	}

	private static TagKey<Block> createCommon(String name) {
		return TagKey.create(Registries.BLOCK, new ResourceLocation("c", name));
	}
}
