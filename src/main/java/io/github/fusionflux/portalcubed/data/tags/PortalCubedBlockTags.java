package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class PortalCubedBlockTags {
	public static final TagKey<Block> BULLET_HOLE_CONCRETE = create("bullet_hole_concrete");
	public static final TagKey<Block> BULLET_HOLE_GLASS = create("bullet_hole_glass");
	public static final TagKey<Block> BULLET_HOLE_METAL = create("bullet_hole_metal");
	public static final TagKey<Block> CROWBAR_MAKES_HOLES = create("crowbar_makes_holes");

	private static TagKey<Block> create(String name) {
		return TagKey.create(Registries.BLOCK, PortalCubed.id(name));
	}

	public static void init() {

	}
}
